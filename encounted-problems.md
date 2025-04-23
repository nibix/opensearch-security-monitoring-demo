# Encountered problems

## OpenSearch Dashboards

- Visualizations on nested columns are not available https://github.com/opensearch-project/OpenSearch-Dashboards/issues/657

## Privileges Evaluator Logs

- Three just slightly differing formats for "No x-level perm match" log messages
- Correlating exceptions with missing privileges log messages is not possible (except using time wise proximity)
- The logs for DLS errors are not really well findable

## Logging configuration

- Could not configure log4j2 JSONLayout due to

```
{"log":"ERROR StatusConsoleListener Could not create plugin of type class org.apache.logging.log4j.core.layout.JsonLayout for element JSONLayout: java.lang.NoClassDefFoundError: com/fasterxml/jackson/databind/ser/FilterProvider\n","stream":"stdout","time":"2025-04-23T04:49:41.825231051Z"}
```


## Audit logging

- Properties for audit.yml described in https://docs.opensearch.org/docs/latest/security/audit-logs/index/#audit-log-settings are wrong
