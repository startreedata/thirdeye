

{{- define "ui.validate.serviceLabels" -}}
{{- $invalidKeys := list "app" "chart" "component" "release" "heritage" -}}
{{- $invalidKeysString := join ", " $invalidKeys -}}  {{/* Join the list elements into a string */}}
{{- range $key, $_ := .Values.ui.serviceLabels -}}
  {{- if has $key $invalidKeys -}}
    {{- fail (printf "Invalid key '%s' found in ui.serviceLabels. The keys %s are reserved and cannot be used." $key $invalidKeysString) -}}
  {{- end -}}
{{- end -}}
{{- end -}}
