# OpenSearch Alerts for Monitoring Cluster

In this demo we will configure 2 alerting monitors for the following use-cases:

1. Monitor changes to the security index
2. Monitor for brute-force attacks and get alerted after going above certain threshold

Before creating the monitors we must add an index template for the auditlog index as OpenSearch deduces the mappings from the first audit messages ingested otherwise.

### Apply audit log index template

```
PUT _index_template/audit_logs
{
  "index_patterns": [
    "security-auditlog*"
  ],
  "template": {
    "settings": {
      "number_of_shards": 2,
      "number_of_replicas": 1
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date"
        },
        "audit_category": {
          "type": "keyword"
        },
        "audit_cluster_name": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_compliance_diff_content": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_compliance_diff_is_noop": {
          "type": "boolean"
        },
        "audit_compliance_doc_version": {
          "type": "long"
        },
        "audit_compliance_operation": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_format_version": {
          "type": "long"
        },
        "audit_node_host_address": {
          "type": "ip"
        },
        "audit_node_host_name": {
          "type": "keyword"
        },
        "audit_node_id": {
          "type": "keyword"
        },
        "audit_node_name": {
          "type": "keyword"
        },
        "audit_request_body": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_request_effective_user": {
          "type": "keyword"
        },
        "audit_request_layer": {
          "type": "keyword"
        },
        "audit_request_origin": {
          "type": "keyword"
        },
        "audit_request_privilege": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_request_remote_address": {
          "type": "ip"
        },
        "audit_trace_doc_id": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_trace_indices": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_trace_resolved_indices": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "audit_trace_shard_id": {
          "type": "long"
        },
        "audit_trace_task_id": {
          "type": "keyword"
        },
        "audit_transport_headers": {
          "properties": {
            "X-Opaque-Id": {
              "type": "text",
              "fields": {
                "keyword": {
                  "type": "keyword",
                  "ignore_above": 256
                }
              }
            }
          }
        },
        "audit_transport_request_type": {
          "type": "keyword"
        }
      }
    }
  }
}
```

After this template is added, any existing auditlog indices must be removed. The audit log index will be re-created on the subsequent auditlog event.

```
DELETE security-auditlog*
```

### Create notification channel

In this directory you will find a file called `test_webhook.py` which creates a simple webserver that has a single endpoint. Run the webserver in a separate terminal with `python3 test_webhook.py`

Once the webhook is up you can run a simple test:

```
curl -XPOST http://localhost:5005/webhook -H "Content-Type: application/json" -d '{"test": "data"}'

{"status":"received"}
```

After spinning up the simple webhook we then need to create a notification channel for alerts either within OpenSearch Dashboards or with the Notifications API.

If using the API we can run:

```
POST /_plugins/_notifications/configs 
{
    "config_id" : "sample-id",
    "name" : "Webhook",
    "config" : {
        "name" : "Webhook",
        "description" : "",
        "config_type" : "webhook",
        "is_enabled" : true,
        "webhook" : {
            "url" : "http://host.docker.internal:5005/webhook",
            "header_params" : {
            "Content-Type" : "application/json"
            },
            "method" : "POST"
        }
    }
}
```

### Create alerting monitors

1. For the first monitor, we will create a monitor that detects changes to the security index and will notify the channel created above

```
POST _plugins/_alerting/monitors
{
    "type" : "monitor",
    "name" : "Changes to Security",
    "monitor_type" : "doc_level_monitor",
    "enabled" : true,
    "schedule" : {
    "period" : {
        "interval" : 1,
        "unit" : "MINUTES"
    }
    },
    "inputs" : [
    {
        "doc_level_input" : {
        "description" : "",
        "indices" : [
            "security-auditlog*"
        ],
        "queries" : [
            {
            "id" : "1d733113-d4df-45a5-a1f7-ed8fcd3d077f",
            "name" : "COMPLIANCE_INTERNAL_CONFIG_WRITE",
            "fields" : [ ],
            "query" : "audit_category:\"COMPLIANCE_INTERNAL_CONFIG_WRITE\"",
            "tags" : [ ],
            "query_field_names" : [ ]
            }
        ],
        "fan_out_enabled" : true
        }
    }
    ],
    "triggers" : [
    {
        "document_level_trigger" : {
        "id" : "NiFBaZYBNzeaS266S4y1",
        "name" : "Change occurred",
        "severity" : "3",
        "condition" : {
            "script" : {
            "source" : "query[name=COMPLIANCE_INTERNAL_CONFIG_WRITE]",
            "lang" : "painless"
            }
        },
        "actions" : [
            {
            "id" : "notification729563",
            "name" : "Webhook",
            "destination_id" : "3jYHaZYB9hZUErZbYknX",
            "message_template" : {
                "source" : "Monitor {{ctx.monitor.name}} just entered alert status. Please investigate the issue.\n  - Trigger: {{ctx.trigger.name}}\n  - Severity: {{ctx.trigger.severity}}\n  - Period start: {{ctx.periodStart}} UTC\n  - Period end: {{ctx.periodEnd}} UTC",
                "lang" : "mustache"
            },
            "throttle_enabled" : false,
            "subject_template" : {
                "source" : "Alerting Notification action",
                "lang" : "mustache"
            },
            "action_execution_policy" : {
                "action_execution_scope" : {
                "per_alert" : {
                    "actionable_alerts" : [ ]
                }
                }
            }
            }
        ]
        }
    }
    ]
}
```

2. For the second monitor, we will create a monitor that detects brute-force attacks by using a bucket monitor and looking for a set number of FAILED_LOGIN attempts within a window of time.

```
POST _plugins/_alerting/monitors
{
    "type" : "monitor",
    "name" : "Failed Login",
    "monitor_type" : "bucket_level_monitor",
    "enabled" : true,
    "schedule" : {
    "period" : {
        "interval" : 1,
        "unit" : "MINUTES"
    }
    },
    "inputs" : [
    {
        "search" : {
        "indices" : [
            "security-auditlog*"
        ],
        "query" : {
            "size" : 0,
            "query" : {
            "bool" : {
                "filter" : [
                {
                    "range" : {
                    "@timestamp" : {
                        "from" : "{{period_end}}||-1m",
                        "to" : "{{period_end}}",
                        "include_lower" : true,
                        "include_upper" : true,
                        "format" : "epoch_millis",
                        "boost" : 1.0
                    }
                    }
                },
                {
                    "term" : {
                    "audit_category" : {
                        "value" : "FAILED_LOGIN",
                        "boost" : 1.0
                    }
                    }
                }
                ],
                "adjust_pure_negative" : true,
                "boost" : 1.0
            }
            },
            "aggregations" : {
            "composite_agg" : {
                "composite" : {
                "size" : 10,
                "sources" : [
                    {
                    "audit_category" : {
                        "terms" : {
                        "field" : "audit_category",
                        "missing_bucket" : false,
                        "order" : "asc"
                        }
                    }
                    }
                ]
                }
            }
            }
        }
        }
    }
    ],
    "triggers" : [
    {
        "bucket_level_trigger" : {
        "id" : "jzYRaZYB9hZUErZb-EvD",
        "name" : "Failed Login Attempts",
        "severity" : "1",
        "condition" : {
            "buckets_path" : {
            "_count" : "_count"
            },
            "parent_bucket_path" : "composite_agg",
            "script" : {
            "source" : "params._count > 5",
            "lang" : "painless"
            },
            "gap_policy" : "skip"
        },
        "actions" : [
            {
            "id" : "notification696612",
            "name" : "Webhook",
            "destination_id" : "3jYHaZYB9hZUErZbYknX",
            "message_template" : {
                "source" : "Monitor {{ctx.monitor.name}} just entered alert status. Please investigate the issue.\n  - Trigger: {{ctx.trigger.name}}\n  - Severity: {{ctx.trigger.severity}}\n  - Period start: {{ctx.periodStart}} UTC\n  - Period end: {{ctx.periodEnd}} UTC\n\n  - Deduped Alerts:\n  {{#ctx.dedupedAlerts}}\n    * {{id}} : {{bucket_keys}}\n  {{/ctx.dedupedAlerts}}\n\n  - New Alerts:\n  {{#ctx.newAlerts}}\n    * {{id}} : {{bucket_keys}}\n  {{/ctx.newAlerts}}\n\n  - Completed Alerts:\n  {{#ctx.completedAlerts}}\n    * {{id}} : {{bucket_keys}}\n  {{/ctx.completedAlerts}}",
                "lang" : "mustache"
            },
            "throttle_enabled" : false,
            "subject_template" : {
                "source" : "Alerting Notification action",
                "lang" : "mustache"
            },
            "action_execution_policy" : {
                "action_execution_scope" : {
                "per_alert" : {
                    "actionable_alerts" : [
                    "DEDUPED",
                    "NEW"
                    ]
                }
                }
            }
            }
        ]
        }
    }
    ]
}
```

### Make changes to security

Next navigate to one of the pages in the security plugin and make a simple change.

Wait one minute and then navigate to the Alerting app to view the alert.

### Make failed login attempts

In an incognito window, try putting in the wrong credentials enough times to trigger the monitor we previously created. Wait for the window of time the alert is configured for and then reload the alerting page to view the alert that was triggered.