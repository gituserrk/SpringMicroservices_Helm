{{/*
Expand the name of the chart.
*/}}
{{- define "patient-appointment-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a fully qualified app name.
*/}}
{{- define "patient-appointment-service.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
MySQL service name — injected as DB_HOST into the application config.
*/}}
{{- define "patient-appointment-service.mysqlServiceName" -}}
{{- printf "%s-mysql" (include "patient-appointment-service.fullname" .) }}
{{- end }}

{{/*
Common labels applied to every resource in this chart.
*/}}
{{- define "patient-appointment-service.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
{{ include "patient-appointment-service.selectorLabels" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels.
*/}}
{{- define "patient-appointment-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "patient-appointment-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
