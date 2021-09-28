
{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "thirdeye.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "thirdeye.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "thirdeye.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified thirdeye ui name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "thirdeye.ui.fullname" -}}
{{ template "thirdeye.fullname" . }}-{{ .Values.ui.name }}
{{- end -}}

{{/*
Create a default fully qualified thirdeye coordinator name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "thirdeye.coordinator.fullname" -}}
{{ template "thirdeye.fullname" . }}-{{ .Values.coordinator.name }}
{{- end -}}

{{/*
Create a default fully qualified thirdeye worker name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "thirdeye.worker.fullname" -}}
{{ template "thirdeye.fullname" . }}-{{ .Values.worker.name }}
{{- end -}}

{{/*
Create a default fully qualified thirdeye scheduler (worker with special detector.yml) name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "thirdeye.scheduler.fullname" -}}
{{ template "thirdeye.fullname" . }}-{{ .Values.scheduler.name }}
{{- end -}}

{{/*
The name of the thirdeye config.
*/}}
{{- define "thirdeye.config" -}}
{{- printf "%s-config" (include "thirdeye.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye scheduler (worker with special detector.yml) config.
*/}}
{{- define "thirdeye.scheduler.config" -}}
{{- printf "%s-scheduler-config" (include "thirdeye.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye coordinator external service.
*/}}
{{- define "thirdeye.coordinator.external" -}}
{{- printf "%s-external" (include "thirdeye.coordinator.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye coordinator headless service.
*/}}
{{- define "thirdeye.ui.headless" -}}
{{- printf "%s-headless" (include "thirdeye.ui.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye coordinator headless service.
*/}}
{{- define "thirdeye.coordinator.headless" -}}
{{- printf "%s-headless" (include "thirdeye.coordinator.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye worker headless service.
*/}}
{{- define "thirdeye.worker.headless" -}}
{{- printf "%s-headless" (include "thirdeye.worker.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
The name of the thirdeye scheduler (worker with special detector.yml) headless service.
*/}}
{{- define "thirdeye.scheduler.headless" -}}
{{- printf "%s-headless" (include "thirdeye.scheduler.fullname" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
  Create a default fully qualified traefik name.
  We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "thirdeye.traefik.fullname" -}}
{{-   if .Values.traefik.fullnameOverride -}}
{{-     .Values.traefik.fullnameOverride | trunc -63 | trimSuffix "-" -}}
{{-   else -}}
{{-     $name := default "traefik" .Values.traefik.nameOverride -}}
{{-     printf "%s-%s" .Release.Name $name | trunc -63 | trimSuffix "-" -}}
{{-    end -}}
{{- end -}}

