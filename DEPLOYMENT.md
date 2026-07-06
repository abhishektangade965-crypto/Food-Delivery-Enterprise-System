# Production Deployment Architecture Guide

This document outlines the deployment strategy for the Delivo Super Platform.

---

## 1. Dual Deployment Architecture

To balance development velocity and enterprise reliability, Delivo utilizes a dual deployment architecture:

```mermaid
graph TD
    subgraph Local Development Setup
        A[Browser] -->|Local HTTP Requests| B[Server.java Standalone Server]
        B -->|OpenAI Proxy| C[OpenAI API]
    end

    subgraph Production Cloud Architecture
        D[Client App] -->|HTTPS Requests| E[AWS Route53 / CloudFront CDN]
        E -->|Static Files| F[AWS S3 Bucket]
        E -->|API Endpoints| G[Nginx Ingress Gateway]
        G -->|Authentication Guards| H[Spring Boot User-Service]
        G -->|Payment Processing| I[Spring Boot Payment-Service]
        G -->|Order Lifecycle Saga| J[Spring Boot Order-Service]
        J -->|Event Messages| K[AWS MSK Managed Kafka Cluster]
        J -->|Cache Storage| L[AWS ElastiCache Redis Cluster]
        J -->|Persistent Storage| M[AWS RDS PostgreSQL Database]
    end
```

---

## 2. Local Prototype Server (`Server.java`)
The local server is optimized for standalone pair-programming testing without external system dependencies:
*   **Virtual Threads Executor**: Spawns light threads per request, ensuring high concurrency locally.
*   **Server-Side Includes (SSI)**: Automatically compiles frontend components (`/frontend/components/*.html`) dynamically into `index.html` on delivery.
*   **Secure API Proxying**: Routes chat queries to OpenAI, preventing key exposure. Key loaded via:
    `System.getenv("OPENAI_API_KEY")`

---

## 3. Production Pipeline (Kubernetes & AWS EKS)

Production services are fully containerized using Multi-stage Gradle Docker builds:
1.  **Registry Hosting**: Docker containers are compiled and pushed to AWS ECR.
2.  **Orchestrator Orchestrations**: Kubernetes manifest deployments deploy containers to AWS EKS cluster nodes.
3.  **State Logs CDC**: Change Data Capture processes (Debezium engine) capture commits from PostgreSQL master logs and publish event streams to Kafka topics.
