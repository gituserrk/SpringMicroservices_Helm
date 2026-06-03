# CREATE Prompt – Healthcare Microservices with Spring Boot, Kubernetes, and Helm

## C – Context

You are building a cloud-native healthcare application to demonstrate modern microservice development using Spring Boot and Kubernetes.

The purpose of this application is educational and intended for a technical presentation (Tech Tuesday).

The solution should be simple enough to understand in a single session while still demonstrating real-world microservice concepts.

The application consists of two independently deployable microservices:

1. PatientCore Service
2. PatientAppointment Service

The PatientAppointment Service must communicate with the PatientCore Service before creating appointments.

The application will be deployed locally on Kubernetes (Minikube or Kind) and packaged using Helm.

Do not use AWS, EKS, Eureka, Config Server, Kafka, RabbitMQ, Service Mesh, or any unnecessary infrastructure.

---

## R – Role

Act as:

* Principal Software Architect
* Senior Spring Boot Developer
* Kubernetes Engineer
* Helm Expert

Design and generate a production-quality reference implementation suitable for learning microservices, Docker, Kubernetes, and Helm.

---

## E – Expectations

### PatientCore Service

Responsibilities:

* Create Patient
* Update Patient
* Delete Patient
* Get Patient by ID
* List Patients

Patient Entity:

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "age": 35,
  "mobile": "9876543210",
  "email": "john@example.com"
}
```

Database:

PostgreSQL

---

### PatientAppointment Service

Responsibilities:

* Create Appointment
* Update Appointment
* Cancel Appointment
* Get Appointment by ID
* List Appointments

Appointment Entity:

```json
{
  "id": 1,
  "patientId": 1,
  "doctorName": "Dr. Smith",
  "appointmentDate": "2026-06-15",
  "status": "BOOKED"
}
```

Before creating an appointment:

* Validate the patient exists by calling PatientCore Service.

Use OpenFeign for inter-service communication.

---

### API Documentation

Provide:

* Swagger/OpenAPI
* Example requests
* Example responses

---

### Health Monitoring

Provide:

* Spring Boot Actuator
* Health endpoint
* Readiness endpoint
* Liveness endpoint

---

## A – Architecture

### Technology Stack

* Java 21
* Spring Boot 3.x
* Maven
* Spring Data JPA
* PostgreSQL
* OpenFeign
* Docker
* Kubernetes
* Helm

### Communication

PatientAppointment Service → PatientCore Service

Use Kubernetes DNS-based service discovery.

Example:

```text
http://patient-core-service:8080
```

Do not use Eureka.

Do not use Config Server.

---

### Architecture Diagram

Generate an architecture diagram showing:

Client
→ PatientAppointment Service
→ PatientCore Service
→ MySQL Databases

Also show Kubernetes Services and Pods.

---

## T – Tasks

Generate the solution in the following phases.

### Phase 1

Produce:

* High-level architecture
* Component diagram
* Sequence diagram
* Database design
* Folder structure

Wait for approval.

---

### Phase 2

Generate complete PatientCore Service.

Include:

* Entity
* DTO
* Repository
* Service Layer
* Controller
* Exception Handling
* Validation
* Unit Tests

Wait for approval.

---

### Phase 3

Generate complete PatientAppointment Service.

Include:

* Entity
* DTO
* Repository
* Service Layer
* Controller
* OpenFeign Client
* Validation
* Unit Tests

Wait for approval.

---

### Phase 4

Generate Docker configuration.

Include:

* Dockerfile for PatientCore Service
* Dockerfile for PatientAppointment Service
* Docker Compose for local execution

Wait for approval.

---

### Phase 5

Generate Kubernetes manifests.

Include:

* Namespace
* Deployments
* Services
* ConfigMaps
* Secrets
* MySQL Deployments
* MySQL Services

Provide kubectl commands for deployment.

Wait for approval.

---

### Phase 6

Generate Helm charts.

For:

* PatientCore Service
* PatientAppointment Service

Include:

* Chart.yaml
* values.yaml
* deployment.yaml
* service.yaml
* configmap.yaml
* secret.yaml

Provide Helm installation commands.

Wait for approval.

---

### Phase 7

Generate API testing instructions.

Provide:

* curl commands
* Postman collection examples

Demonstrate:

1. Create Patient
2. Retrieve Patient
3. Create Appointment
4. PatientAppointment Service calling PatientCore Service
5. Kubernetes deployment verification
6. Helm deployment verification

---

## E – Execution Rules

1. Generate complete files, not snippets.
2. Follow Spring Boot best practices.
3. Use constructor injection only.
4. Use proper package structure.
5. Follow SOLID principles.
6. Add meaningful comments where necessary.
7. Use validation annotations.
8. Use global exception handling.
9. Explain architectural decisions.
10. Do not skip implementation details.
11. Wait for approval after each phase before continuing.
12. Assume Kubernetes service names are used for service discovery.
13. Ensure the code is runnable without additional modifications.
