# Ideas for information to be monitored


## Goal

Primary goal of the presentation is to provide actually usable information and help for cluster admins in monitoring security related aspects of their clusters.

Secondary goals:

- Give an overview over the OpenSearch eco system
- Illustrate shortcomings of the security plugin and describe workarounds (for example: missing config validation)
- Discuss experiences with setting up the log ingestion/monitoring pipeline

## Privileges

### Missing privileges dashboard

Visualize missing privileges by action and index

Question: Use audit log or normal OpenSearch logs? I believe the normal OpenSearch logs provide a richer information than audit logs. However, they are also much more difficult to parse.

### Privilege evaluation exceptions

Track and visualize exceptions that happened during privilege evaluation due to invalid roles (either invalid regexes or invalid DLS queries).

Source ref: https://github.com/opensearch-project/security/blob/280d8e5fb80e7c0732a162ea9f682a75040593d3/src/main/java/org/opensearch/security/privileges/PrivilegesEvaluator.java#L588
Source ref: https://github.com/opensearch-project/security/blob/280d8e5fb80e7c0732a162ea9f682a75040593d3/src/main/java/org/opensearch/security/privileges/dlsfls/DocumentPrivileges.java#L131


### Indices withheld by DNFOF

Show per user the indices which were removed from the user's results due to DNFOF managing missing privileges

Problem: We do not have log output for such information at the moment



## Authentication

### Authentication error metrics

Show dashboard with recent authentication errors, by user name and by ip address.

Data source: Audit logs

Moving on: Potentially use alerting and/or anomaly detection for further insights


### Invalid configuration alerts

Scan logs for invalid configuration exceptions (in authentication config: config.yml) and send alerts. 


## Data access

Use audit logging to track access to documents by users, use dashboards to visualize access patterns, use alerting/anomaly detection to analyze it


# Tools/Features/Functionalities I'd like to use

- Alerting
- Data Prepper (maybe?)
- Event correlation
- Anomaly detection (maybe?)