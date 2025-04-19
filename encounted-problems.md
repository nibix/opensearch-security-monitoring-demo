# Encountered problems

## OpenSearch Dashboards

- Visualizations on nested columns are not available https://github.com/opensearch-project/OpenSearch-Dashboards/issues/657
- Computing diffs to the security index has a bug fetching existing config: https://github.com/opensearch-project/security/issues/5280
- Alerting dashboards plugin not extract all keyword subfields to make them selectable in the group by dropdown for Bucket monitors: https://github.com/opensearch-project/alerting-dashboards-plugin/pull/1234
- Exporting saved objects was able to export dashboards, visualizations and index patterns but alerting monitors and notification channels are not exported as easily
- k8s operator does not support `external_opensearch` as an audit log sink