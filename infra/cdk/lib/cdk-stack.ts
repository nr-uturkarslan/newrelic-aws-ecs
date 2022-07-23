import { Stack, StackProps } from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import { SubnetType } from 'aws-cdk-lib/aws-ec2';
import * as elbv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import { Construct } from 'constructs';

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
      defaultTargetGroups: [targetGroupDefault]
    });
  }
}
