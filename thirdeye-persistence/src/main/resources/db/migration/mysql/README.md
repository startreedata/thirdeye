
## [V1_293_0__remove_name_unique_constraints_datasource.sql](V1_293_0__remove_name_unique_constraints_datasource.sql)
Datasource:
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

## [V1_293_1__remove_name_unique_constraints_dataset.sql](V1_293_1__remove_name_unique_constraints_dataset.sql)
Dataset:
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

## [V1_293_2__remove_name_dataset_unique_constraints_metrics.sql](V1_293_2__remove_name_dataset_unique_constraints_metrics.sql)
Metrics:
TODO CYRIL authz best would be to have a constraint on (name, dataset, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

## [V1_293_4__remove_name_unique_constraints_alert.sql](V1_293_4__remove_name_unique_constraints_alert.sql)
Alert
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

## [V1_293_5__remove_name_unique_constraints_subscription_group.sql](V1_293_5__remove_name_unique_constraints_subscription_group.sql)
Subscription Group
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

