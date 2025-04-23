package com.eliatra.dataprepper.plugin;

import org.opensearch.dataprepper.expression.ExpressionEvaluator;
import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.plugin.PluginFactory;
import org.opensearch.dataprepper.model.processor.AbstractProcessor;
import org.opensearch.dataprepper.model.processor.Processor;

import org.opensearch.dataprepper.model.record.Record;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@DataPrepperPlugin(name = "log_event_time_correlation", pluginType = Processor.class, pluginConfigurationType = LogEventTimeCorrelatorConfig.class)
public class LogEventTimeCorrelator extends AbstractProcessor<Record<Event>, Record<Event>> {
    private final LogEventTimeCorrelatorConfig config;
    private volatile long groupEndsAt = -1;
    private volatile Event groupStartEvent;

    @DataPrepperPluginConstructor
    public LogEventTimeCorrelator(LogEventTimeCorrelatorConfig aggregateProcessorConfig, PluginMetrics pluginMetrics, PluginFactory pluginFactory, ExpressionEvaluator expressionEvaluator) {
        super(pluginMetrics);
        this.config = aggregateProcessorConfig;
    }


    @Override
    public Collection<Record<Event>> doExecute(Collection<Record<Event>> records) {
        List<Record<Event>> result = new ArrayList<>(records.size());

        for (Record<Event> record : records) {
            Event event = record.getData();
            long eventTime = getTimeFromEvent(event);

            if (isStartEvent(event)) {
                groupEndsAt = eventTime + config.groupDuration.toMillis();
                groupStartEvent = event;
                event.put(config.groupRefKeyPrefix + "_group_head", UUID.randomUUID().toString());
                result.add(record);
            } else if (eventTime <= groupEndsAt) {
                Event groupStartEvent = this.groupStartEvent;

                if (groupStartEvent != null) {
                    record.getData().put(config.groupRefKeyPrefix + "_start_timestamp", groupStartEvent.get(config.timestampKey, String.class));
                    record.getData().put(config.groupRefKeyPrefix + "_group_ref", groupStartEvent.get(config.groupRefKeyPrefix + "_group_head", String.class));
                }

                result.add(record);
            } else {
                result.add(record);
            }
        }

        return result;
    }

    @Override
    public void prepareForShutdown() {

    }

    @Override
    public boolean isReadyForShutdown() {
        return false;
    }

    @Override
    public void shutdown() {

    }

    private boolean isStartEvent(Event event) {
        return config.startValue.equals(event.get(config.startKey, String.class));
    }

    private long getTimeFromEvent(Event event) {
        String timestampString = event.get(config.timestampKey, String.class);
        if (timestampString == null) {
            return -1;
        }

        try {
            return Instant.parse(timestampString).toEpochMilli();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
