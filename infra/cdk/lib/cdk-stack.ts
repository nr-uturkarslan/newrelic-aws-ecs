import { Stack, StackProps, RemovalPolicy, CfnOutput } from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as dynamodb from "aws-cdk-lib/aws-dynamodb";

const dockerImagePersistence = "uturkarslan/aws-ecs-persistence:1658608497"
const dockerImageProxy = "uturkarslan/aws-ecs-proxy:1658609581"

export class CdkStack extends Stack {
  constructor(scope: Construct, id: string, props?: StackProps) {
    super(scope, id, props);

    // VPC
    const vpc = new ec2.Vpc(this, "Vpc", {
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

    // Application target group - default
    const targetGroupDefault = new elbv2.ApplicationTargetGroup(this, "TargetGroupDefault", {
      targetGroupName: "TargetGroupDefault",
      targetType: elbv2.TargetType.INSTANCE,
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      vpc: vpc,
    });

    // Security group - Load balancer
    const securityGroupLoadBalancer = new ec2.SecurityGroup(this, "SecurityGroupLoadBalancer", {
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
    const alb = new elbv2.ApplicationLoadBalancer(this, "LoadBalancer", {
      loadBalancerName: "LoadBalancer",
      internetFacing: true,
      vpc: vpc,
      vpcSubnets: {subnetType: ec2.SubnetType.PUBLIC},
      deletionProtection: false,
      securityGroup: securityGroupLoadBalancer,
    });

    const listener = alb.addListener("LoadBalancerListener", {
      port: 80,
      open: true,
      defaultTargetGroups: [targetGroupDefault],
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
    const ecsCluster = new ecs.Cluster(this, "EcsCluster", {
      clusterName: "EcsCluster",
      containerInsights: true,
      vpc: vpc,
    });

    // ------------------- //
    // --- PERSISTENCE --- //
    // ------------------- //

    // Task definition - persistence
    const fargateTaskDefinitionPersistence = new ecs.FargateTaskDefinition(this, 'FargateTaskDefinitionPersistence', {
      memoryLimitMiB: 4096,
      cpu: 2048,
    });

    fargateTaskDefinitionPersistence.addToExecutionRolePolicy(executionRolePolicy);

    const containerPersistence = fargateTaskDefinitionPersistence.addContainer("persistence", {
      image: ecs.ContainerImage.fromRegistry(dockerImagePersistence),
      logging: ecs.LogDrivers.awsLogs({streamPrefix: 'aws-ecs-persistence'}),
    });
    
    containerPersistence.addPortMappings({
      containerPort: 8080,
    });

    // Security group - Fargate persistence
    const securityGroupFargatePersistence = new ec2.SecurityGroup(this, "SecurityGroupFargatePersistence", {
      securityGroupName: "SecurityGroupFargatePersistence",
      vpc: vpc,
      allowAllOutbound: true,
      description: "Security group for persistence application.",
    });

    securityGroupFargatePersistence.addIngressRule(
      ec2.Peer.securityGroupId(securityGroupLoadBalancer.securityGroupId),
      // ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      'Allow HTTP access only from ALB.',
    );

    // ECS Fargate Service - Persistence
    const fargateServicePersistence = new ecs.FargateService(this, "FargateServicePersistence", {
      serviceName: "FargateServicePersistence",
      assignPublicIp: false,
      desiredCount: 1,
      cluster: ecsCluster,
      taskDefinition: fargateTaskDefinitionPersistence,
      vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_WITH_NAT},
      securityGroups: [securityGroupFargatePersistence],
    });

    // fargateServicePersistence.registerLoadBalancerTargets({
    //   containerName: containerPersistence.containerName,
    //   containerPort: 8080,
    //   newTargetGroupId: "TargetGroupFargatePersistence",
    //   listener: ecs.ListenerConfig.applicationListener(listener, {
    //     targetGroupName: "TargetGroupFargatePersistence",
    //     port: 80,
    //     protocol: elbv2.ApplicationProtocol.HTTP,
    //     healthCheck: {
    //       enabled: false
    //     }
    //   }),
    // });

    // Application target group - persistence
    const targetGroupFargatePersistence = new elbv2.ApplicationTargetGroup(this, "TargetGroupFargatePersistence", {
      targetGroupName: "TargetGroupFargatePersistence",
      targetType: elbv2.TargetType.IP,
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      vpc: vpc,
      healthCheck: {
        enabled: true,
        path: "/persistence/health",
        port: "8080",
        healthyHttpCodes: "200"
      }
    });

    fargateServicePersistence.attachToApplicationTargetGroup(targetGroupFargatePersistence);

    new elbv2.ApplicationListenerRule(this, "PersistenceRoute", {
      listener: listener,
      priority: 1,
      action: elbv2.ListenerAction.forward([targetGroupFargatePersistence]),
      conditions: [elbv2.ListenerCondition.pathPatterns(["/persistence/*"])]
    });

    // Dynamodb - CustomItem
    const dynamoDbTableCustomItem = new dynamodb.Table(this, "DynamoDbCustomItem", {
      tableName: "DynamoDbCustomItem",
      billingMode: dynamodb.BillingMode.PROVISIONED,
      readCapacity: 1,
      writeCapacity: 1,
      removalPolicy: RemovalPolicy.DESTROY,
      partitionKey: {name: "id", type: dynamodb.AttributeType.STRING},
      // sortKey: {name: 'createdAt', type: dynamodb.AttributeType.NUMBER},
      pointInTimeRecovery: true,
    });

    dynamoDbTableCustomItem.grantReadWriteData(fargateServicePersistence.taskDefinition.taskRole);

    // ------------- //
    // --- PROXY --- //
    // ------------- //

    // Task definition - proxy
    const fargateTaskDefinitionProxy = new ecs.FargateTaskDefinition(this, 'FargateTaskDefinitionProxy', {
      memoryLimitMiB: 4096,
      cpu: 2048,
    });

    fargateTaskDefinitionProxy.addToExecutionRolePolicy(executionRolePolicy);

    const containerProxy = fargateTaskDefinitionProxy.addContainer("proxy", {
      image: ecs.ContainerImage.fromRegistry(dockerImageProxy),
      logging: ecs.LogDrivers.awsLogs({streamPrefix: 'aws-ecs-proxy'}),
      environment: { 
        'LOAD_BALANCER_URL': alb.loadBalancerDnsName,
      }
    });
    
    containerProxy.addPortMappings({
      containerPort: 8080,
    });

    // Security group - Fargate proxy
    const securityGroupFargateProxy = new ec2.SecurityGroup(this, "SecurityGroupFargateProxy", {
      securityGroupName: "SecurityGroupFargateProxy",
      vpc: vpc,
      allowAllOutbound: true,
      description: "Security group for proxy application.",
    });

    securityGroupFargateProxy.addIngressRule(
      ec2.Peer.securityGroupId(securityGroupLoadBalancer.securityGroupId),
      // ec2.Peer.anyIpv4(),
      ec2.Port.tcp(80),
      'Allow HTTP access only from ALB.',
    );

    // ECS Fargate Service - Proxy
    const fargateServiceProxy = new ecs.FargateService(this, "FargateServiceProxy", {
      serviceName: "FargateServiceProxy",
      assignPublicIp: false,
      desiredCount: 1,
      cluster: ecsCluster,
      taskDefinition: fargateTaskDefinitionProxy,
      vpcSubnets: {subnetType: ec2.SubnetType.PRIVATE_WITH_NAT},
      securityGroups: [securityGroupFargateProxy],
    });

    // Application target group - proxy
    const targetGroupFargateProxy = new elbv2.ApplicationTargetGroup(this, "TargetGroupFargateProxy", {
      targetGroupName: "TargetGroupFargateProxy",
      targetType: elbv2.TargetType.IP,
      port: 80,
      protocol: elbv2.ApplicationProtocol.HTTP,
      vpc: vpc,
      healthCheck: {
        enabled: true,
        path: "/proxy/health",
        port: "8080",
        healthyHttpCodes: "200"
      }
    });

    fargateServiceProxy.attachToApplicationTargetGroup(targetGroupFargateProxy);

    new elbv2.ApplicationListenerRule(this, "ProxyRoute", {
      listener: listener,
      priority: 2,
      action: elbv2.ListenerAction.forward([targetGroupFargateProxy]),
      conditions: [elbv2.ListenerCondition.pathPatterns(["/proxy/*"])]
    });
  }
}
