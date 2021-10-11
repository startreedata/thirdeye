# Scripts

This directory holds utility scripts that aid in the dev/testing of ThirdEye. 

Note: Please use bash instead of sh. The scripts may contain bash functions.  

## Launching a Pinot Cluster

You can use the instuctions below to load up a local pinot cluster.

### Starting pinot
```
# Download and Start Pinot
./start-pinot.sh
```
After some time, you should be able to access pinot controller at http://localhost:9000

### Loading a dataset

We internally use pageviews for testing.
```bash
# Run this once you are able to access pinot controller on the browser.
./load-pageviews.sh
```
After this, you should be able to view pageviews data on pinot
