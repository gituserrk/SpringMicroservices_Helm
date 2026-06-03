{{/*
Expand the name of the chart.
*/}}
{{- define "patient-core-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a fully qualified app name.
Respects fullnameOverride so the Kubernetes Service name is predictable
(patient-appointment-service uses "patient-core-service:8080" via K8s DNS).
*/}}
{{- define "patient-core-service.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
MySQL service name — this is injected as DB_HOST into the application config.
*/}}
{{- define "patient-core-service.mysqlServiceName" -}}
{{- printf "%s-mysql" (include "patient-core-service.fullname" .) }}
{{- end }}

{{/*
Common labels applied to every resource in this chart.
*/}}
{{- define "patient-core-service.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{ include "patient-core-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels — used in Deployment.spec.selector and Service.spec.selector.
*/}}
{{- define "patient-core-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "patient-core-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
