export interface NavItem {
  title: string
  path: string
}

export interface NavSection {
  title: string
  items: NavItem[]
}

export const navigation: NavSection[] = [
  {
    title: 'Início',
    items: [
      { title: 'Home', path: '/' },
    ],
  },
  {
    title: 'Claude Code',
    items: [
      { title: 'Básico', path: '/claude-code/basics' },
    ],
  },
  {
    title: 'LocalStack & Terraform',
    items: [
      { title: 'Conceitos', path: '/localstack/concepts' },
      { title: 'Terraform + LocalStack', path: '/localstack/terraform' },
    ],
  },
  {
    title: 'POCs',
    items: [
      { title: 'Visão Geral', path: '/#pocs' },
      { title: 'Roadmap', path: '/pocs/roadmap' },
      { title: 'S3 Storage', path: '/pocs/s3-storage' },
      { title: 'SQS Messaging', path: '/pocs/sqs-messaging' },
      { title: 'DynamoDB CRUD', path: '/pocs/dynamodb-crud' },
      { title: 'SNS Notifications', path: '/pocs/sns-notifications' },
      { title: 'Lambda Java', path: '/pocs/lambda-java' },
      { title: 'Event-Driven', path: '/pocs/event-driven' },
      { title: 'Secrets Manager', path: '/pocs/secrets-manager' },
      { title: 'KMS Encryption', path: '/pocs/kms-encryption' },
      { title: 'CloudWatch Logs', path: '/pocs/cloudwatch-logs' },
      { title: 'Step Functions', path: '/pocs/step-functions' },
      { title: 'API Gateway', path: '/pocs/api-gateway' },
      { title: 'ElastiCache Redis', path: '/pocs/elasticache-redis' },
      { title: 'Kinesis Streaming', path: '/pocs/kinesis-streaming' },
      { title: 'SES Email', path: '/pocs/ses-email' },
      { title: 'CloudFront CDN', path: '/pocs/cloudfront-cdn' },
      { title: 'IAM Policies', path: '/pocs/iam-policies' },
    ],
  },
]

export const flatNav: NavItem[] = navigation.flatMap((s) => s.items)

export function getAdjacentPages(currentPath: string) {
  const index = flatNav.findIndex((item) => item.path === currentPath)
  return {
    prev: index > 0 ? flatNav[index - 1] : null,
    next: index < flatNav.length - 1 ? flatNav[index + 1] : null,
  }
}
