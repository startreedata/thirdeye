# Migration to MySQL 8
The ThirdEye MySql driver is already compatible with MySql8.
The database migration from MySql 5.7 to 8 can be performed *INPLACE*. 
This means there is no need to do a backup, create a new, db, etc. (*LOGICAL* upgrade).
We can just change the MySQL version of the DB.


## Migration process

### Test of the migration process on Azure Kube:
1. Create a deployment with the master branch (MySql 5.7) on Azure

    ```
    NAMESPACE=my-thirdeye-mysql-migration-test
    kt create namespace ${NAMESPACE}
    cd kubernetes/helm/startree-thirdeye
    ./helmw upgrade --install thirdeye . -n ${NAMESPACE}
    ```
2. Create some artefacts in the DB  
(manual - look at [thirdeye-server/test/resources/demo](thirdeye-server/test/resources/demo))

3. Get back on this branch. Upgrade the cluster. This branch has the mysql in version 8.0.28. 
   ```
   ./helmw upgrade --install thirdeye . -n ${NAMESPACE}
   ```

Look at the logs of the new mysql pod:
```
2022-02-04 16:51:14+00:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.28-1debian10 started.
2022-02-04 16:51:14+00:00 [Note] [Entrypoint]: Switching to dedicated user 'mysql'
2022-02-04 16:51:14+00:00 [Note] [Entrypoint]: Entrypoint script for MySQL Server 8.0.28-1debian10 started.
2022-02-04T16:51:14.664562Z 0 [System] [MY-010116] [Server] /usr/sbin/mysqld (mysqld 8.0.28) starting as process 1
2022-02-04T16:51:14.685427Z 1 [System] [MY-011012] [Server] Starting upgrade of data directory.
2022-02-04T16:51:14.685482Z 1 [System] [MY-013576] [InnoDB] InnoDB initialization has started.
2022-02-04T16:51:16.328853Z 1 [System] [MY-013577] [InnoDB] InnoDB initialization has ended.
2022-02-04T16:51:22.662527Z 2 [System] [MY-011003] [Server] Finished populating Data Dictionary tables with data.
2022-02-04T16:51:27.562415Z 5 [System] [MY-013381] [Server] Server upgrade from '50700' to '80028' started.
2022-02-04T16:51:56.933978Z 5 [System] [MY-013381] [Server] Server upgrade from '50700' to '80028' completed.
2022-02-04T16:51:57.107074Z 0 [Warning] [MY-010068] [Server] CA certificate ca.pem is self signed.
2022-02-04T16:51:57.107121Z 0 [System] [MY-013602] [Server] Channel mysql_main configured to support TLS. Encrypted connections are now supported for this channel.
2022-02-04T16:51:57.121204Z 0 [Warning] [MY-011810] [Server] Insecure configuration for --pid-file: Location '/var/run/mysqld' in the path is accessible to all OS users. Consider choosing a different directory.
2022-02-04T16:51:57.137864Z 0 [System] [MY-011323] [Server] X Plugin ready for connections. Bind-address: '::' port: 33060, socket: /var/run/mysqld/mysqlx.sock
2022-02-04T16:51:57.137925Z 0 [System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections. Version: '8.0.28'  socket: '/var/run/mysqld/mysqld.sock'  port: 3306  MySQL Community Server - GPL.
```

You should see 
```
Server upgrade from '50700' to '80028' started.
Server upgrade from '50700' to '80028' completed.
```

Workers \ scheduler \ coordinator pods throw connection errors during the migration because a new pod is relaunched and the update takes a few seconds.
After some time, they are able to connect. 

Notice the following logs about TLS.
```
2022-02-04T16:51:57.107074Z 0 [Warning] [MY-010068] [Server] CA certificate ca.pem is self signed.
2022-02-04T16:51:57.107121Z 0 [System] [MY-013602] [Server] Channel mysql_main configured to support TLS. Encrypted connections are now supported for this channel.
```
See [about tls connection](#about-tls-connection)

## Backup
In theory there is no need for a backup, but we should do one for clients, just in case.
I suggest doing so manually:
- perform a dump on a local machine 
- replicate on an object storage
- run the migration process

### Perform the backup
- Connect to the relevant cluster
- Run the following
```
NAMESPACE=[YOUR_NAMESPACE]
# get the password - you can also get it manually
MYSQL_ROOT_PASSWORD=$(kubectl get secret -n ${NAMESPACE} thirdeye-mysql -o jsonpath="{.data.mysql-root-password}" | base64 --decode; echo)
MYSQL_POD_NAME=$(kt get pods -n ${NAMESPACE} --no-headers -o custom-columns=":metadata.name" | grep mysql)
kt exec ${MYSQL_POD_NAME} -n ${NAMESPACE} -- mysqldump -u root --password=${MYSQL_ROOT_PASSWORD} --add-drop-table --routines --events --all-databases --force > db-dump.sql 
```


## Starting from a new MySQL instance
With MySql 8, new accounts are created with a [new authentication mechanism](https://dev.mysql.com/doc/refman/8.0/en/upgrading-from-previous-series.html#upgrade-caching-sha2-password). This connection mechanism is more secured. 
With this new mechanism, the password must be protected during transmission; TLS is the preferred mechanism for this,
but if it is not available then RSA public key encryption will be used. 
AllowPublicKeyRetrieval=True allows the client to automatically request the public key from the server. 
Note: `AllowPublicKeyRetrieval=True` could allow a malicious proxy to perform a MITM attack to get the plaintext password, 
so it is False by default and must be explicitly enabled.

In a new namespace, run the k8S helm upgrade script above directly from this branch.

    ```
    NAMESPACE=thirdeye-mysql8-start-test
    kt create namespace ${NAMESPACE}
    cd kubernetes/helm/startree-thirdeye
    ./helmw upgrade --install thirdeye . -n ${NAMESPACE}
    ```

We can upgrade the existing accounts with:
```
ALTER USER user
  IDENTIFIED WITH caching_sha2_password
  BY 'password';
```

## About TLS connection
The migration keeps the same behavior: a connection without SSL.
Relevant documentation for SSL connection:  
https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-connp-props-security.html
https://dev.mysql.com/doc/refman/8.0/en/using-encrypted-connections.html

It should be fairly easy to change to TLS without CA check and host check by changing
`allowPublicKeyRetrieval=true&sslMode=DISABLED` to `sslMode=REQUIRED`.


### JDBC Security configuration recommendation
JDBC connection parameters for the migration:

|                 | local/dev | prod             |
|-----------------|-----------|------------------|
| new MySql8      | allowPublicKeyRetrieval=true&sslMode=DISABLED | allowPublicKeyRetrieval=true&sslMode=DISABLED |
| migrated MySql8 | allowPublicKeyRetrieval=true&sslMode=DISABLED | allowPublicKeyRetrieval=true&sslMode=DISABLED |


Strictest possible jdbc connection parameters matrix (without any other change):

|                 | local/dev | prod             |
|-----------------|-----------|------------------|
| new MySql8      | allowPublicKeyRetrieval=true&sslMode=DISABLED | sslMode=REQUIRED |
| migrated MySql8 | sslMode=DISABLED | sslMode=REQUIRED |

sslMode=REQUIRED corresponds to SSL with self-signed certificate.  
It will be added after the migration.
