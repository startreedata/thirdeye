## Slack notifications

We use the slack github app.
To subscribe to events in a channel use:
```
/github subscribe startreedata/thirdeye workflows:{name:"Release ThirdEye","Publish ThirdEye Backend" branch:"master"}
/github unsubscribe startreedata/thirdeye pulls commits deployments
```
