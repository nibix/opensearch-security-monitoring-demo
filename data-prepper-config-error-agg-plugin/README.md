# Data Prepper Plugin

This directory contains a custom plugin implementation of data prepper. At the moment, it adds one further processor for data prepper.

*This code is experimental and subject to change.*

## Installation

The plugin can be installed in Data Prepper by placing the `.jar` file in the `lib` directory of
the Data Prepper installation.

## Processors

### log_event_time_correlation

The processor `log_event_time_correlation` looks for log events which match certain specified criteria. If such an event is
found, all subsequent events that occur in a specific time frame will marked with a back reference to the initial log event.

This allows you to correlate log events by proximity in time.

#### Example usage

```yaml
  processor:
  - log_event_time_correlation:
      group_ref_key_prefix: config_change
      start_key: audit_category
      start_value: COMPLIANCE_INTERNAL_CONFIG_WRITE
      timestamp_key: "@timestamp"
      group_duration: 10s
```

In the example usage, the processor logs for log events where the attribute specified by `start_key` is equal to the value specified by `start_value`.

In the subsequent time frame of the length specified by `group_duration`, all further processed log events get a back reference to the start log event.
The back reference is manifested by attributes that are prefixed by the value specified in `group_ref_key_prefix`. 

