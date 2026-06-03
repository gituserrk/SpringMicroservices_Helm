# SpringMicroservices_Helm
### Healthcare Microservices Demo — Spring Boot · MySQL · Docker · Kubernetes · Helm

---

## WHO

This project is built for **Java/Spring Boot engineers** who want a hands-on, runnable reference for modern cloud-native microservice development. It targets developers who already understand basic REST and Spring Boot but want to understand how independent services are packaged, deployed, and wired together in a real Kubernetes environment — without the noise of unnecessary infrastructure.

---

## WHAT

A cloud-native healthcare application made up of **two independently deployable microservices**:

| Service | Responsibility | Port | Database |
|---|---|---|---|
| **PatientCore Service** | Create, read, update, delete patients | 8080 | MySQL (`patient_db`) |
| **Appointment Service** | Book, update, cancel appointments; validates patients before booking | 8081 | MySQL (`appointment_db`) |

Each service owns its own database, exposes a REST API with Swagger/OpenAPI documentation, and ships with Spring Boot Actuator health endpoints for liveness and readiness probes.

### Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Build | Maven (multi-module) |
| ORM | Spring Data JPA |
| Database | MySQL 8 |
| Inter-service call | OpenFeign |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Health | Spring Boot Actuator |
| Container | Docker (multi-stage build) |
| Orchestration | Kubernetes (Minikube) |
| Packaging | Helm 3 |

---

## WHEN

Use this architecture pattern when:

- You need to **scale services independently** — Patient and Appointment can be scaled to different replica counts based on load.
- You want **independent deployment cycles** — a bug fix in PatientCore Service does not require redeploying Appointment Service.
- Teams own separate services — one team owns patients, another owns appointments; they share only the API contract.
- You are running on **Kubernetes** and want to keep infrastructure lean — no service registry server to manage.

---

## WHERE

The project runs in three environments using the exact same application code:

```
┌──────────────────────────────────────────────────────────────────────────┐
│  LOCAL (Docker Compose)                                                  │
│  docker compose up --build                                               │
│  patient-core-service → localhost:8080  |  patient-appointment-service → localhost:8081│
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│  KUBERNETES (Minikube — raw manifests)                                   │
│  kubectl apply -f k8s/                                                   │
│  Namespace: healthcare                                                   │
│  Services discovered via Kubernetes CoreDNS                              │
└──────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────┐
│  KUBERNETES (Minikube — Helm charts)                                     │
│  helm install patient-release     helm/patient-core-service     -n healthcare │
│  helm install appointment-release helm/patient-appointment-service -n healthcare │
│  Each chart bundles the app + its own MySQL database                     │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## WHY

### Why Microservices?

| Concern | Monolith | Microservices |
|---|---|---|
| Deployment | Entire app redeploys for any change | Only the changed service redeploys |
| Scaling | Scale everything even if only one feature is busy | Scale only the bottleneck service |
| Fault isolation | One bug can crash the whole app | A failing Appointment Service does not take down PatientCore Service |
| Team autonomy | All teams touch the same codebase | Each team owns their service end-to-end |
| Technology choice | Locked to one stack | Each service could use a different language or DB |

### Why Kubernetes DNS instead of a Service Registry?

Traditional Spring microservice stacks often add **Eureka** (Netflix OSS) or **Consul** as a service registry — a dedicated server that services register with at startup, and that other services query to find each other's address.

This project deliberately skips that layer. Here is why:

| | Eureka / Consul | Kubernetes DNS (CoreDNS) |
|---|---|---|
| Extra infrastructure | Yes — a registry server must be deployed, scaled, and kept healthy | **No** — CoreDNS is built into every Kubernetes cluster |
| Service registration | Each service must include a client library and register itself | **Automatic** — creating a Kubernetes `Service` object registers its DNS entry instantly |
| Health-check routing | Registry tracks health and removes stale instances | **Kubernetes handles this** via readiness probes; unhealthy pods are removed from the Service endpoint list automatically |
| Failure mode | If Eureka goes down, services cannot discover each other | DNS failure = cluster failure; it is a cluster primitive, not an add-on |
| Learning overhead | Extra dependency, configuration, and ops knowledge | **Zero extra concepts** beyond what Kubernetes already provides |

**Bottom line:** Kubernetes DNS is a zero-cost, zero-config service discovery mechanism that is always present. Adding Eureka on top of Kubernetes is redundant infrastructure.

---

## HOW

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      Kubernetes Namespace: healthcare                   │
│                                                                         │
│  Client (Postman)                                                       │
│       │                                                                 │
│       │  POST /api/appointments                                         │
│       ▼                                                                 │
│  ┌──────────────────────┐   OpenFeign (HTTP)   ┌────────────────────┐  │
│  │  Appointment Service │ ──────────────────► │  PatientCore Service   │  │
│  │  Pod  :8081          │  patient-core-service:8080│  Pod  :8080        │  │
│  └──────────┬───────────┘                      └────────┬───────────┘  │
│             │                                           │              │
│             ▼                                           ▼              │
│  ┌──────────────────────┐                   ┌────────────────────────┐ │
│  │  appointment-db      │                   │  patient-db            │ │
│  │  MySQL  :3306        │                   │  MySQL  :3306          │ │
│  └──────────────────────┘                   └────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

### How the Two Services Communicate

The Appointment Service calls the PatientCore Service using **OpenFeign** — a declarative HTTP client built into Spring Cloud. The call is declared as a plain Java interface:

```java
// patient-appointment-service/src/main/.../feign/PatientClient.java
@FeignClient(name = "patient-core-service", url = "${patient.service.url:http://patient-core-service:8080}")
public interface PatientClient {
    @GetMapping("/api/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") Long id);
}
```

The URL `http://patient-core-service:8080` is **not** a hard-coded IP address or a registry lookup. It is a **Kubernetes DNS name**. Here is the full resolution path:

```
patient-appointment-service Pod
  └─► resolves "patient-core-service" via CoreDNS
        └─► patient-core-service.healthcare.svc.cluster.local
              └─► ClusterIP of the patient-core-service Kubernetes Service
                    └─► one of the healthy patient-core-service Pods
```

When you create a Kubernetes `Service` named `patient-core-service`, CoreDNS automatically creates a DNS `A` record for it. No registration code, no registry server, no health-check polling — it just works.

In Docker Compose the same env variable (`PATIENT_SERVICE_URL`) is overridden to `http://patient-core-service:8080`, where Docker Compose's internal DNS resolves container names the same way.

### Synchronous Communication — The Deliberate Choice

This project uses **synchronous** (blocking) HTTP communication between services.

```
Appointment Service                     PatientCore Service
       │                                       │
       │── GET /api/patients/{id} ────────────►│
       │                                       │ (processes)
       │◄── 200 PatientDTO ───────────────────│
       │  (or 404 if not found)               │
       │                                       │
   [saves appointment OR throws exception]
```

When a new appointment is requested:
1. Appointment Service **blocks** and waits for PatientCore Service to confirm the patient exists.
2. If PatientCore Service returns **200** → appointment is saved.
3. If PatientCore Service returns **404** → `PatientNotFoundException` is thrown and a `404` is returned to the caller immediately.
4. If PatientCore Service is **unreachable** → `FeignException` is caught, wrapped as a `503 Service Unavailable`, and returned to the caller.

#### Why Synchronous, Not Asynchronous (e.g. Kafka/RabbitMQ)?

| | Synchronous (Feign / HTTP) | Asynchronous (Kafka / RabbitMQ) |
|---|---|---|
| **Consistency** | **Immediate** — patient is validated before the appointment is ever written | **Eventual** — appointment may be written and later invalidated by a consumer |
| **Simplicity** | No broker to deploy, configure, or monitor | Requires a message broker cluster (Kafka, RabbitMQ) as extra infrastructure |
| **Error handling** | Caller gets an immediate error response they can act on | Errors are published to a dead-letter queue; the original caller has already received a 202 |
| **Debugging** | A single distributed trace — one request, one response | Events fan out; tracing requires correlation IDs across queues |
| **When it fails** | PatientCore Service down → appointment creation fails fast | PatientCore Service down → messages queue up and are processed when it recovers |
| **Best for** | **Queries and validations** where you need an answer before proceeding | **Commands** where the caller does not need to wait (e.g. "send email", "generate report") |

**The main advantage of synchronous communication here** is **strong consistency at the boundary**: it is impossible to create an appointment for a patient that does not exist, because the validation and the write happen in the same request-response cycle. This is the right trade-off for a booking system where data integrity matters more than throughput.

Asynchronous messaging (Kafka/RabbitMQ) would be the right choice for operations that are fire-and-forget or that need to survive partial failures — for example, notifying a patient via email after an appointment is booked, or publishing audit events to a data warehouse.

### Helm Chart Design

Each Helm chart is **self-contained**: it deploys both the Spring Boot application and its dedicated MySQL database in a single `helm install` command. The `_helpers.tpl` file uses `fullnameOverride` to pin the Kubernetes Service name, ensuring that the Feign URL `http://patient-core-service:8080` always resolves correctly regardless of the Helm release name used.

```
helm install patient-release helm/patient-core-service -n healthcare
# Creates: patient-core-service (app), patient-core-service-mysql (DB)

helm install appointment-release helm/patient-appointment-service -n healthcare
# Creates: patient-appointment-service (app), patient-appointment-service-mysql (DB)
# PATIENT_SERVICE_URL=http://patient-core-service:8080  ← resolves to the chart above
```

---

## Project Structure

```
SpringMicroservices_Helm/
├── pom.xml                        ← Maven parent (Java 21, Spring Boot 3.4.1)
├── patient-core-service/               ← Patient CRUD service + MySQL
├── patient-appointment-service/           ← Appointment service + OpenFeign client
├── docker-compose.yml             ← 4-container local stack
├── k8s/                           ← Raw Kubernetes manifests (17 files)
├── helm/                          ← Helm charts (22 templates, 2 charts)
├── api-testing-guide.md           ← curl commands for all demo scenarios
└── postman-collection.json        ← Importable Postman collection (22 requests)
```

---

## Quick Start

```bash
# Option 1 — Docker Compose (fastest)
docker compose up --build
# Patient:     http://localhost:8080/swagger-ui.html
# Appointment: http://localhost:8081/swagger-ui.html

# Option 2 — Kubernetes (raw manifests)
minikube start
eval $(minikube docker-env)
docker build -t patient-core-service:latest     -f patient-core-service/Dockerfile .
docker build -t patient-appointment-service:latest -f patient-appointment-service/Dockerfile .
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/patient-db/ && kubectl apply -f k8s/appointment-db/
kubectl apply -f k8s/patient-core-service/ && kubectl apply -f k8s/patient-appointment-service/

# Option 3 — Helm
kubectl create namespace healthcare
helm install patient-release     helm/patient-core-service     -n healthcare
helm install appointment-release helm/patient-appointment-service -n healthcare
helm list -n healthcare
```
