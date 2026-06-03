# TT6 — Post-Session Summary
### Healthcare Microservices with Spring Boot, Kubernetes & Helm

---

## WHO

**Audience:** Java/Spring Boot engineers looking to move from monolithic applications to cloud-native microservice architectures.

**Skills assumed going in:**
- Basic Java and Spring Boot familiarity
- Understanding of REST APIs
- Awareness of Docker (but not necessarily Kubernetes)

**Skills gained by the end:**
- Building independently deployable Spring Boot microservices
- Wiring inter-service HTTP calls with OpenFeign
- Packaging applications as Docker images (multi-stage builds)
- Deploying to Kubernetes without a service registry
- Managing releases with Helm charts

---

## WHAT

We built a **cloud-native healthcare application** from scratch consisting of two microservices:

### PatientCore Service (port 8080)
| Endpoint | Method | Description |
|---|---|---|
| `/api/patients` | POST | Create a new patient |
| `/api/patients` | GET | List all patients |
| `/api/patients/{id}` | GET | Get patient by ID |
| `/api/patients/{id}` | PUT | Update patient |
| `/api/patients/{id}` | DELETE | Delete patient |

### Appointment Service (port 8081)
| Endpoint | Method | Description |
|---|---|---|
| `/api/appointments` | POST | Book appointment (validates patient first) |
| `/api/appointments` | GET | List all appointments |
| `/api/appointments/{id}` | GET | Get appointment by ID |
| `/api/appointments/{id}` | PUT | Update appointment |
| `/api/appointments/{id}` | DELETE | Cancel appointment (soft cancel → CANCELLED) |

### What was generated across all 7 phases:

| Phase | Deliverable | Files |
|---|---|---|
| 1 | Architecture, diagrams, DB design, folder structure | — |
| 2 | Complete PatientCore Service | 18 files |
| 3 | Complete Appointment Service + OpenFeign | 21 files |
| 4 | Dockerfiles + Docker Compose | 4 files |
| 5 | Kubernetes manifests | 17 files |
| 6 | Helm charts (2 self-contained charts) | 22 files |
| 7 | API testing guide + Postman collection | 2 files |
| — | README (5W+1H) + this document | 2 files |
| **Total** | | **86+ files / 4,000+ lines** |

---

## WHEN

### When to apply these patterns in your own projects

| Situation | Pattern to reach for |
|---|---|
| Teams need to deploy independently | Split into microservices — each owns its schema and deployment pipeline |
| Running on Kubernetes | Use K8s DNS for service discovery — drop Eureka/Consul |
| Need immediate data consistency across services | Synchronous HTTP (OpenFeign) — caller blocks until response |
| Fire-and-forget operations (email, audit, notifications) | Asynchronous messaging (Kafka/RabbitMQ) — caller doesn't wait |
| Repeatable, versioned deployments across environments | Helm charts — one chart, multiple `values.yaml` overrides |
| Local development or quick demos | Docker Compose — one command, zero cluster setup |
| Production or staging with resilience requirements | Kubernetes — self-healing, rolling updates, probe-based traffic management |

---

## WHERE

### Three run environments — same code, same images

```
┌─────────────────────────────────────────────────────────────┐
│  1. Docker Compose (local)                                  │
│     docker compose up --build                               │
│     → patient-core-service:  http://localhost:8080               │
│     → patient-appointment-service: http://localhost:8081            │
│     Best for: quick local testing, demos without a cluster  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  2. Kubernetes — Raw Manifests                              │
│     kubectl apply -f k8s/                                   │
│     Namespace: healthcare                                    │
│     → Demonstrates: Pods, Services, ConfigMaps,             │
│       Secrets, PVCs, liveness/readiness probes              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│  3. Kubernetes — Helm Charts                                │
│     helm install patient-release helm/patient-core-service       │
│     helm install appointment-release helm/patient-appointment-service│
│     → Demonstrates: templating, release management,         │
│       values overrides, rollback, upgrade                   │
└─────────────────────────────────────────────────────────────┘
```

---

## WHY

### Why microservices over a monolith?

| Concern | Monolith | Microservices |
|---|---|---|
| Deployment | Entire app redeploys for any change | Only the changed service redeploys |
| Scaling | Scale everything even if one feature is busy | Scale only the bottleneck service |
| Fault isolation | One bug can crash the whole app | Services fail independently |
| Team ownership | All teams share one codebase | Each team owns their service end-to-end |

### Why Kubernetes DNS instead of Eureka?

Traditional Spring stacks often add **Eureka** as a service registry — a dedicated server that services register with at startup. This project deliberately skips it.

```
❌ With Eureka:
   Service A starts → registers with Eureka Server → Service B queries Eureka → gets IP
   (extra server to deploy, configure, scale, and keep healthy)

✅ With Kubernetes DNS:
   Create a Kubernetes Service → CoreDNS auto-registers it
   Service B calls "patient-core-service:8080" → CoreDNS resolves → done
   (zero extra infrastructure — built into every K8s cluster)
```

**Kubernetes DNS resolution chain:**
```
patient-appointment-service Pod
  └─► "patient-core-service" resolves via CoreDNS
        └─► patient-core-service.healthcare.svc.cluster.local
              └─► ClusterIP of the patient-core-service K8s Service
                    └─► one of the healthy patient-core-service Pods
```

### Why synchronous (OpenFeign) instead of async (Kafka)?

```
Appointment Service                     PatientCore Service
       │                                       │
       │── GET /api/patients/{id} ────────────►│
       │◄── 200 OK / 404 Not Found ────────────│
       │                                       │
  [saves appointment OR throws 404 immediately]
```

**Synchronous is correct here because:**
- Patient validation and appointment creation must happen in the **same request-response cycle**
- It is **impossible to book for a non-existent patient** — the write never happens if validation fails
- The caller gets an **immediate, actionable error** (404) rather than a queued failure

**Async (Kafka/RabbitMQ) would be right for:**
- Sending confirmation emails after booking
- Writing audit logs to a data warehouse
- Triggering downstream workflows that don't affect the booking outcome

---

## HOW

### How the two services communicate (the crucial mechanism)

The Appointment Service calls the PatientCore Service using **OpenFeign** — a declarative HTTP client. The entire integration is one interface:

```java
@FeignClient(name = "patient-core-service", url = "${patient.service.url:http://patient-core-service:8080}")
public interface PatientClient {
    @GetMapping("/api/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") Long id);
}
```

The URL `http://patient-core-service:8080` is a **Kubernetes DNS name**, not a hardcoded IP. When Kubernetes creates a `Service` named `patient-core-service`, CoreDNS automatically creates a DNS record for it. OpenFeign resolves that name at call time — no registry, no client-side load balancer configuration required.

The same env variable (`PATIENT_SERVICE_URL`) is overridden to `http://patient-core-service:8080` in Docker Compose, where Docker Compose's internal DNS resolves container names identically.

### How Helm makes deployment repeatable

Each Helm chart bundles the Spring Boot app **and** its MySQL database:

```bash
# One command installs: app deployment + app service + MySQL deployment +
#                       MySQL service + MySQL PVC + ConfigMap + Secrets
helm install patient-release helm/patient-core-service -n healthcare

# Override any value without touching chart files
helm install patient-release helm/patient-core-service -n healthcare \
  --set mysql.rootPassword=securepassword \
  --set replicaCount=3

# Upgrade after a change
helm upgrade patient-release helm/patient-core-service -n healthcare

# Roll back if something goes wrong
helm rollback patient-release 1 -n healthcare
```

### How health probes protect production traffic

Both services expose three Actuator endpoints that Kubernetes polls automatically:

```
/actuator/health           → overall health (DB connectivity, disk space)
/actuator/health/liveness  → "am I alive?" — K8s restarts the pod if this fails
/actuator/health/readiness → "am I ready for traffic?" — K8s removes the pod from
                             the Service endpoint list if this fails (zero-downtime deploys)
```

The difference matters: a pod that is alive but not ready (e.g. still connecting to MySQL on startup) does not receive traffic — it just waits until it is ready.

---

## Key Takeaways

> **1. Microservices = independent deploy + independent scale + independent failure.**
> The price is distributed complexity. Use them when that trade-off is worth it.

> **2. Kubernetes DNS replaces Eureka.**
> CoreDNS is built into every K8s cluster. A Service name IS a DNS name. No registry server needed.

> **3. Synchronous = consistency. Asynchronous = resilience.**
> Choose based on whether the caller needs an answer before proceeding.

> **4. Docker Compose ≠ Kubernetes.**
> Compose is for local convenience. Kubernetes is for production operations. Same images, different runtimes.

> **5. Helm = versioned, repeatable Kubernetes deployments.**
> One chart, unlimited environments. Override `values.yaml` per environment, never touch templates.

---

## Reference Links

| Resource | Location |
|---|---|
| Source code | `patient-core-service/` · `patient-appointment-service/` |
| K8s manifests | `k8s/` |
| Helm charts | `helm/` |
| Docker Compose | `docker-compose.yml` |
| API testing guide | `api-testing-guide.md` |
| Postman collection | `postman-collection.json` |
| Compose vs K8s reference | `docs/docker-compose-vs-kubernetes.md` |
| Swagger — Patient | http://localhost:8080/swagger-ui.html |
| Swagger — Appointment | http://localhost:8081/swagger-ui.html |
