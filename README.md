# Globally Scalable Food Delivery Super Platform

This is a production-grade, FAANG-level food delivery microservices platform designed using Domain Driven Design (DDD), Clean/Hexagonal Architecture, Event-Driven Sagas, and Kubernetes orchestration.

## System Bounded Contexts & Microservices

1. **API Gateway**: Spring Cloud Gateway proxy routing all incoming requests with rate-limiting and security validations.
2. **User Service**: Registration, credential management, profile, security roles, and user addresses.
3. **Order Service**: Coordinates order placement, processes tracking tokens, and handles state machine flow.
4. **Payment Service**: Integrates wallet systems, ledger balancing, and payment validation.

---

## Architecture Design Principles

- **Hexagonal Architecture**: Isolates the domain models (`Order.java`, `User.java`, `Payment.java`) from databases and messaging protocols using inputs ports (`ApplicationService`) and outputs ports (`Repository`, `MessagePublisher`).
- **CQRS**: Separates command writing transactions from reading operations (queries).
- **Outbox Pattern**: Assures Exactly-Once messaging by inserting events to `payment_outbox` table in the same transaction as state updates, polled asynchronously by schedulers.
- **Saga Orchestration**: Asynchronous choreography orchestrated by `Order Service` mapping transactions:
  - Create Order (PENDING) -> Process Payment (PAID) -> Approve Restaurant (APPROVED).
  - Compensation on failure: Refund Payment (REFUNDED) -> Cancel Order (CANCELLED).

---

## Local Development & Docker Setup

To spin up database clusters, Kafka brokers, Redis cache, and Elasticsearch locally, run:

```bash
docker-compose up -d
```

### Databases Mapped Local Ports:
- PostgreSQL DB Cluster: `5432`
- Redis Cache Instance: `6379`
- Kafka Message Broker: `9092`
- Elasticsearch Search Engine: `9200`

---

## Kubernetes Production Deployment

Production-grade manifests are located inside `infrastructure/k8s/`. Deploy the entire namespace stack:

```bash
kubectl apply -f infrastructure/k8s/food-delivery-manifests.yaml
```

The stack configures:
- Horizontal Pod Autoscalers (HPA) scaling deployments based on CPU load.
- Network policies blocking direct database access from untrusted pods.
- Replicas with zone-aware AntiAffinity configurations preventing cluster outages.

---

## Observability & Observability Monitoring

- **Prometheus** scrapes metric endpoints exposing JVM heap memory levels and HTTP request latency.
- **OpenTelemetry Collector** exports trace spans to Jaeger instances.
- **Grafana Dashboards** present KPIs and SLO charts.
