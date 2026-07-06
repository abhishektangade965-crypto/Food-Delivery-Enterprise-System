output "eks_cluster_endpoint" {
  value = aws_eks_cluster.eks.endpoint
}

output "rds_cluster_endpoint" {
  value = aws_rds_cluster.postgresql.endpoint
}

output "redis_primary_endpoint" {
  value = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "msk_bootstrap_brokers" {
  value = aws_msk_cluster.kafka.bootstrap_brokers_sasl_iam
}
