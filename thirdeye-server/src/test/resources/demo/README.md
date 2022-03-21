# Demo resources
This folder contains the configs used for the demo environment.

## Alerts
When the alert is created, the alert is replayed on past data.
If it is not replayed on the timeframe you required for the demo environment, 
you can run the alert on a custom time period to get anomalies. 
Use [runTask endpoint](http://13.64.199.160:8080/swagger#/Alert/runTask).

Eg for: 
```
curl -X POST "http://13.64.199.160:8080/api/alerts/[MY_ALERT_ID]/run" -H "accept: application/json" -H "Content-Type: application/x-www-form-urlencoded" -d "start=1581292800000&end=1591747200000"
```

## Subscriptions
Subscriptions are not in this folder yet.
