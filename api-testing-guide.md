# API Testing Guide — Healthcare Microservices Demo

## Base URLs

| Environment     | PatientCore Service           | Appointment Service       |
|-----------------|---------------------------|---------------------------|
| Docker Compose  | http://localhost:8080     | http://localhost:8081     |
| Kubernetes (pf) | http://localhost:8080     | http://localhost:8081     |
| Minikube NodePort | http://localhost:8080   | `minikube service patient-appointment-service -n healthcare` |

**Swagger UI**
- Patient:     http://localhost:8080/swagger-ui.html
- Appointment: http://localhost:8081/swagger-ui.html

---

## Scenario 1 — Create a Patient

```bash
curl -s -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "age": 35,
    "mobile": "9876543210",
    "email": "john@example.com"
  }' | jq .
```

**Expected response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "mobile": "9876543210",
  "email": "john@example.com",
  "createdAt": "2026-06-03T10:00:00",
  "updatedAt": "2026-06-03T10:00:00"
}
```

Create a second patient for list testing:
```bash
curl -s -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "age": 28,
    "mobile": "9123456780",
    "email": "jane@example.com"
  }' | jq .
```

---

## Scenario 2 — Retrieve Patient

**Get by ID:**
```bash
curl -s http://localhost:8080/api/patients/1 | jq .
```

**List all patients:**
```bash
curl -s http://localhost:8080/api/patients | jq .
```

**Update a patient:**
```bash
curl -s -X PUT http://localhost:8080/api/patients/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "age": 36,
    "mobile": "9876543210",
    "email": "john.doe@example.com"
  }' | jq .
```

**Patient not found (demonstrates 404 error response):**
```bash
curl -s http://localhost:8080/api/patients/999 | jq .
```

Expected:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id: 999",
  "timestamp": "2026-06-03T10:01:00",
  "validationErrors": null
}
```

**Validation error (demonstrates 400 response):**
```bash
curl -s -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "",
    "lastName": "Doe",
    "age": -5,
    "mobile": "abc",
    "email": "not-an-email"
  }' | jq .
```

---

## Scenario 3 — Create an Appointment

Uses patient ID 1 created in Scenario 1.

```bash
curl -s -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorName": "Dr. Smith",
    "appointmentDate": "2026-12-15"
  }' | jq .
```

**Expected response (201 Created):**
```json
{
  "id": 1,
  "patientId": 1,
  "doctorName": "Dr. Smith",
  "appointmentDate": "2026-12-15",
  "status": "BOOKED",
  "createdAt": "2026-06-03T10:02:00",
  "updatedAt": "2026-06-03T10:02:00"
}
```

**List all appointments:**
```bash
curl -s http://localhost:8081/api/appointments | jq .
```

**Get appointment by ID:**
```bash
curl -s http://localhost:8081/api/appointments/1 | jq .
```

**Update appointment (change doctor and status):**
```bash
curl -s -X PUT http://localhost:8081/api/appointments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorName": "Dr. Jones",
    "appointmentDate": "2026-12-20",
    "status": "COMPLETED"
  }' | jq .
```

**Cancel an appointment:**
```bash
curl -s -X DELETE http://localhost:8081/api/appointments/1 | jq .
```

Expected — status changes to CANCELLED, data is retained:
```json
{
  "id": 1,
  "patientId": 1,
  "doctorName": "Dr. Smith",
  "appointmentDate": "2026-12-15",
  "status": "CANCELLED",
  ...
}
```

---

## Scenario 4 — Appointment Service Calling PatientCore Service

This demonstrates the inter-service Feign call and patient validation.

**Case A — Patient exists (happy path):**
```bash
# First verify patient 1 exists
curl -s http://localhost:8080/api/patients/1 | jq .

# Create appointment — Appointment Service calls PatientCore Service internally
curl -s -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorName": "Dr. Brown",
    "appointmentDate": "2026-12-22"
  }' | jq .
```

**Case B — Patient does NOT exist (validation failure):**
```bash
# Attempt to book for a non-existent patient
curl -s -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 9999,
    "doctorName": "Dr. Brown",
    "appointmentDate": "2026-12-22"
  }' | jq .
```

Expected (404):
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with id: 9999",
  "timestamp": "2026-06-03T10:05:00",
  "validationErrors": null
}
```

This confirms Appointment Service → PatientCore Service → 404 → mapped to PatientNotFoundException.

---

## Scenario 5 — Kubernetes Deployment Verification

Run these after `kubectl apply -f k8s/` (Phase 5) or `helm install` (Phase 6).

```bash
# All pods running
kubectl get pods -n healthcare

# Expected output:
# NAME                                    READY   STATUS    RESTARTS
# patient-db-xxx                          1/1     Running   0
# appointment-db-xxx                      1/1     Running   0
# patient-core-service-xxx                     1/1     Running   0
# patient-appointment-service-xxx                 1/1     Running   0

# All services exist
kubectl get services -n healthcare

# PVCs bound
kubectl get pvc -n healthcare

# Check readiness/liveness probes
kubectl describe pod -l app=patient-core-service     -n healthcare | grep -A5 "Liveness\|Readiness"
kubectl describe pod -l app=patient-appointment-service -n healthcare | grep -A5 "Liveness\|Readiness"

# View application logs
kubectl logs -l app=patient-core-service     -n healthcare --tail=50
kubectl logs -l app=patient-appointment-service -n healthcare --tail=50

# Health endpoints via port-forward
kubectl port-forward svc/patient-core-service 8080:8080 -n healthcare &
curl -s http://localhost:8080/actuator/health | jq .

# Liveness / Readiness probes
curl -s http://localhost:8080/actuator/health/liveness  | jq .
curl -s http://localhost:8080/actuator/health/readiness | jq .
```

**Expected health response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

---

## Scenario 6 — Helm Deployment Verification

Run these after `helm install` (Phase 6).

```bash
# List installed releases
helm list -n healthcare

# Expected:
# NAME                   NAMESPACE    STATUS    CHART
# patient-release        healthcare   deployed  patient-core-service-1.0.0
# appointment-release    healthcare   deployed  patient-appointment-service-1.0.0

# Inspect computed values
helm get values patient-release     -n healthcare
helm get values appointment-release -n healthcare

# Inspect all rendered manifests for a release
helm get manifest patient-release -n healthcare

# Dry-run to verify templates before upgrade
helm upgrade patient-release helm/patient-core-service -n healthcare --dry-run --debug

# Verify release history
helm history patient-release -n healthcare

# Test the release (runs chart tests if defined)
helm test patient-release -n healthcare

# Check pods deployed by Helm
kubectl get pods -n healthcare -l app.kubernetes.io/instance=patient-release
kubectl get pods -n healthcare -l app.kubernetes.io/instance=appointment-release
```

---

## Complete Demo Flow (all scenarios in sequence)

```bash
# 1. Create two patients
curl -s -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","age":35,"mobile":"9876543210","email":"john@example.com"}' | jq .

curl -s -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Smith","age":28,"mobile":"9123456780","email":"jane@example.com"}' | jq .

# 2. List patients
curl -s http://localhost:8080/api/patients | jq .

# 3. Book appointment for patient 1 (Feign call: APPT -> PATIENT)
curl -s -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"doctorName":"Dr. Smith","appointmentDate":"2026-12-15"}' | jq .

# 4. Try booking for invalid patient (shows validation)
curl -s -X POST http://localhost:8081/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"patientId":9999,"doctorName":"Dr. Smith","appointmentDate":"2026-12-15"}' | jq .

# 5. List all appointments
curl -s http://localhost:8081/api/appointments | jq .

# 6. Cancel the appointment
curl -s -X DELETE http://localhost:8081/api/appointments/1 | jq .

# 7. Delete patient 2
curl -s -X DELETE http://localhost:8080/api/patients/2

# 8. Health check both services
curl -s http://localhost:8080/actuator/health | jq .status
curl -s http://localhost:8081/actuator/health | jq .status
```
