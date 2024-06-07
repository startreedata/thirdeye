
## [V1_293_0__remove_name_unique_constraints_datasource.sql](V1_293_0__remove_name_unique_constraints_datasource.sql)
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

## [V1_293_1__remove_name_unique_constraints_dataset.sql](V1_293_1__remove_name_unique_constraints_dataset.sql)
TODO CYRIL authz best would be to have a constraint on (name, namespace) but namespace is not added to the index tables yet
for the moment we just remove the name unique constraints
indexes should be revisited once the namespace migration is done

