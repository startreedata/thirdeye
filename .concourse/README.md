# Building Thirdeye with Concourse

Thirdeye is being built continuously by the StarTree Concourse CI server available [here](https://ci.startreedata.io).

# Pre requisite

* Download Concourse Fly CLI from https://concourse-ci.org/ and install it in your PATH.
* Login to the Concourse thirdeye team:
  * `fly login -t thirdeye -n thirdeye -c https://ci.startreedata.io`

# Secrets administration for the Thirdeye pipelines

Every variable used by concourse is stored inside Startree's Vault instance. Everyone from StarTree is allowed to access this Vault.

## Accessing Vault

Our internal Vault is available publicly [here](https://vault.distrib.startreedata.io/ui/vault/auth?with=oidc).

> <strong>Note:</strong> Login will only work with the `oidc` mode. 

## Secrets organisation

Variables that are common to all Thirdeye pipelines are located within the `/concourse/thirdeye/` secret path.

See [/concourse/thirdeye/ in Vault](https://vault.distrib.startreedata.io/ui/vault/secrets/concourse/list/thirdeye/)

As for pipeline specific secrets, they are located in subfolders named with the pipeline named (e.g.: [/concourse/thirdeye/publish/thirdeye-master](https://vault.distrib.startreedata.io/ui/vault/secrets/concourse/list/thirdeye/publish-thirdeye-master/))

# Updating the publish-thirdeye pipelines

All branches are using the same pipeline definition from [publish-thirdeye.yml](./publish-thirdeye.yml). Modifying this pipeline will affect every pipeline AFTER they are updated using the `fly set-pipeline` commands.

To update the master pipeline, simply run the [publish-thirdeye-master.sh](./publish-thirdeye-master.sh) script after having modified the [publish-thirdeye.yml](./publish-thirdeye.yml). If tasks files where not altered, it is not a pre-requisite to commit the changes immediately.

Pipeline definition is held in Concourse's database. Pipeline dependencies and tasks are however versioned outside of concourse. 

# Creating a new branch

1. Duplicates all secrets from [/thirdeye/publish-thirdeye-master/](https://vault.distrib.startreedata.io/ui/vault/secrets/concourse/list/thirdeye/publish-thirdeye-master/) to `/thirdeye/publish-thirdeye-<branch>`.
2. Fork a new copy of [publish-thirdeye-master.sh](./publish-thirdeye-master.sh) to `publish-thirdeye-<branch>.sh`.
  1. Update the `-p` argument of the `set-pipeline` command to `publish-thirdeye-<branch>`.
3. Execute the newly created `set-pipeline` script (e.g.: `./publish-thirdeye-my-branch.sh`.