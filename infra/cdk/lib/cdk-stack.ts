import { Stack, StackProps } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as iam from "aws-cdk-lib/aws-iam";

export class CdkStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    // VPC
    const vpc = new ec2.Vpc(this, "VpcTest", {
      cidr: "10.0.0.0/16",
      maxAzs: 2,
      subnetConfiguration: [
        {
            cidrMask: 24,
            name: "SnetPublic",
            subnetType: ec2.SubnetType.PUBLIC
        },
        {
            cidrMask: 24,
            name: "SnetPrivate",
            subnetType: ec2.SubnetType.PRIVATE_WITH_NAT,
        },
      ]
    });

    // Application target group
    const targetGroupDefault = new elbv2.ApplicationTargetGroup(this, "TgTestDefault", {
      targetType: elbv2.TargetType.INSTANCE,
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      vpc: vpc,
    });

    // Security group - Load balancer
    const securityGroupLoadBalancer = new ec2.SecurityGroup(this, "SgTestAlb", {
      securityGroupName: "SgTestAlb",
      vpc: vpc,
      allowAllOutbound: true,
      description: 'Security group for application load balancer.',
    });

    securityGroupLoadBalancer.addIngressRule(
      ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      'Allow HTTP access from anywhere.',
    );

    // Load balancer
    const alb = new elbv2.ApplicationLoadBalancer(this, "AlbTest", {
      loadBalancerName: "AlbTest",
      internetFacing: true,
      vpc: vpc,
      vpcSubnets: {subnetType: ec2.SubnetType.PUBLIC},
      deletionProtection: false,
      securityGroup: securityGroupLoadBalancer,
    });

    const listener = alb.addListener("AlbListenerTest", {
      port: 80,
      open: true,
      defaultTargetGroups: [targetGroupDefault],
    });

    new elbv2.ApplicationListenerRule(this, "app-route", {
      listener: listener,
      priority: 1,
      action: elbv2.ListenerAction.forward([targetGroupDefault]),
      conditions: [elbv2.ListenerCondition.pathPatterns(["/app/*"])]
    });

    // ECS task execution role
    const executionRolePolicy =  new iam.PolicyStatement({
      effect: iam.Effect.ALLOW,
      resources: ['*'],
      actions: [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ]
    });

    // ECS cluster
    const ecsCluster = new ecs.Cluster(this, "EcsTest", {
      clusterName: "EcsTest",
      containerInsights: true,
      vpc: vpc,
    });

    // Task definition
    const fargateTaskDefinition = new ecs.FargateTaskDefinition(this, 'TdTest', {
      memoryLimitMiB: 512,
      cpu: 256,
    });

    fargateTaskDefinition.addToExecutionRolePolicy(executionRolePolicy);

    const container = fargateTaskDefinition.addContainer("backend", {
      image: ecs.ContainerImage.fromRegistry("nginx:latest"),
      logging: ecs.LogDrivers.awsLogs({streamPrefix: 'nginx-test'}),
      // environment: { 
      //   'DYNAMODB_MESSAGES_TABLE': table.tableName,
      //   'APP_ID' : 'my-app'
      // }
    });
    
    container.addPortMappings({
      containerPort: 80,
      hostPort: 80
    });

    // Security group - Fargate
    const securityGroupFargate = new ec2.SecurityGroup(this, "SgTestFargate", {
      securityGroupName: "SgTestFargate",
      vpc: vpc,
      allowAllOutbound: true,
      description: 'Security group for application load balancer.',
    });

    securityGroupFargate.addIngressRule(
      ec2.Peer.securityGroupId(securityGroupLoadBalancer.securityGroupId),
      ec2.Port.tcp(80),
      'Allow HTTP access only from ALB.',
    );

    // ECS Fargate Service
    const fargateService = new ecs.FargateService(this, 'EcsServiceTest', {
      serviceName: "EcsServiceName",
      assignPublicIp: false,
      desiredCount: 1,
      cluster: ecsCluster,
      taskDefinition: fargateTaskDefinition,
      vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_WITH_NAT},
      securityGroups: [securityGroupFargate],
    });

    fargateService.registerLoadBalancerTargets({
      containerName: container.containerName,
      containerPort: 80,
      newTargetGroupId: "EcsFargate",
      listener: ecs.ListenerConfig.applicationListener(listener, {
        targetGroupName: "TgEcsFargate",
        port: 80,
        protocol: elbv2.ApplicationProtocol.HTTP
      }),
    });

    // listener.addTargetGroups("default", {
    //   targetGroups: [targetGroupDefault]
    // });

    // listener.addTargets("ECS", {
    //   port: 80,
    //   targets: [fargateService]
    // });
  }
}
