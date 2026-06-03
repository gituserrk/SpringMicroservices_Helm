# Docker Compose vs Kubernetes — What's the Difference?

A quick reference for understanding when to use each and how they relate to this project.

---

## The Short Answer

**Docker Compose and Kubernetes are two separate ways to run the same application.**
Docker Compose does NOT involve Kubernetes. They are independent tools that both happen to run containers.

```
Docker Desktop
├── Docker Engine  ──►  docker compose up      (Compose stack — no K8s)
└── Kubernetes     ──►  kubectl apply -f k8s/  (K8s cluster  — no Compose)
```

---

## Side-by-Side Comparison

| | Docker Compose | Kubernetes |
|---|---|---|
| **What it is** | Runs containers on your local machine using a single YAML file | Orchestrates containers across a cluster of nodes |
| **Main command** | `docker compose up --build` | `kubectl apply -f k8s/` |
| **Config file** | `docker-compose.yml` | Multiple YAML manifests (Deployment, Service, ConfigMap, Secret, PVC…) |
| **Networking** | Container names act as DNS names automatically | Kubernetes Service objects + CoreDNS handle discovery |
| **Storage** | Named volumes managed by Docker | PersistentVolumeClaims (PVCs) bound to PersistentVolumes |
| **Secrets** | Plain env vars in the Compose file | `Secret` objects (base64-encoded, can be encrypted at rest) |
| **Health checks** | Optional `healthcheck:` block | Liveness and Readiness probes on every pod |
| **Scaling** | `docker compose up --scale service=3` (basic) | `replicas: N` in Deployment spec (production-grade) |
| **Self-healing** | No — crashed containers may not restart reliably | Yes — Kubernetes restarts failed pods automatically |
| **Rolling updates** | No — recreates containers | Yes — zero-downtime rolling deployments |
| **Complexity** | Low — one file, one command | Higher — many resource types to understand |
| **Best for** | Local development and quick demos | Production-grade deployment and orchestration |

---

## They Run the Same Docker Images

The key insight: **the application code and Docker images are identical in both environments.**
Only the runtime environment changes.

```
patient-service:latest  ─┬─►  Docker Compose  (docker-compose.yml)
                          └─►  Kubernetes      (k8s/patient-service/deployment.yaml)
                          └─►  Helm            (helm/patient-service/templates/deployment.yaml)
```

This means you can develop and test locally with Docker Compose, then deploy the exact same image to Kubernetes — no code changes required.

---

## Recommended Demo Flow for TT6

```bash
# ── Step 1: Prove the app works (Docker Compose) ──────────────────────────
docker compose up --build
# Patient:     http://localhost:8080/swagger-ui.html
# Appointment: http://localhost:8081/swagger-ui.html
# Test APIs, show Feign inter-service call, show error handling

# ── Step 2: Tear down Compose ─────────────────────────────────────────────
docker compose down

# ── Step 3: Show Kubernetes deployment (raw manifests) ────────────────────
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/patient-db/
kubectl apply -f k8s/appointment-db/
kubectl wait --for=condition=ready pod -l app=patient-db     -n healthcare --timeout=120s
kubectl wait --for=condition=ready pod -l app=appointment-db -n healthcare --timeout=120s
kubectl apply -f k8s/patient-service/
kubectl apply -f k8s/appointment-service/
kubectl get all -n healthcare

# ── Step 4: Show Helm (teardown K8s first) ────────────────────────────────
kubectl delete namespace healthcare
helm install patient-release     helm/patient-service     -n healthcare --create-namespace
helm install appointment-release helm/appointment-service -n healthcare
helm list -n healthcare
```

---

## Running on Docker Desktop (No Minikube Needed)

Docker Desktop ships with both Docker Engine and a built-in single-node Kubernetes cluster.

| | Minikube | Docker Desktop Kubernetes |
|---|---|---|
| Image access | Must run `eval $(minikube docker-env)` before building | Plain `docker build` — images are immediately available |
| NodePort access | `minikube service <name>` | Directly at `localhost:<nodePort>` |
| PVC storage | `standard` storage class | Default storage class works out of the box |
| Setup | Extra install required | Comes with Docker Desktop — just enable it in Settings |

**Enable Kubernetes in Docker Desktop:**
Settings → Kubernetes → ✅ Enable Kubernetes → Apply & Restart

---

## When to Use Each

| Scenario | Use |
|---|---|
| Quick local development or feature testing | Docker Compose |
| Demonstrating the app works end-to-end | Docker Compose |
| Learning Kubernetes concepts (pods, services, probes, PVCs) | Kubernetes manifests (`k8s/`) |
| Demonstrating Helm packaging and release management | Helm (`helm/`) |
| Production or staging deployment | Kubernetes + Helm |

---

## Key Takeaway

> Docker Compose is about **convenience**.
> Kubernetes is about **resilience, scalability, and production operations**.
>
> This project supports all three run modes — same code, same images, different runtime.
