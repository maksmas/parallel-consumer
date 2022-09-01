package io.confluent.parallelconsumer.internal;

/*-
 * Copyright (C) 2020-2022 Confluent, Inc.
 */

import io.confluent.parallelconsumer.ParallelConsumerOptions;
import io.confluent.parallelconsumer.state.ModelUtils;
import lombok.NonNull;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * Version of the {@link PCModule} in test contexts.
 *
 * @author Antony Stubbs
 */
public class PCModuleTestEnv extends PCModule<String, String> {

    ModelUtils mu = new ModelUtils(this);

    public PCModuleTestEnv(ParallelConsumerOptions<String, String> optionsInstance) {
        super(optionsInstance);
        var copy = options().toBuilder();

        if (optionsInstance.getConsumer() == null) {
            Consumer<String, String> mockConsumer = Mockito.mock(Consumer.class);
            Mockito.when(mockConsumer.groupMetadata()).thenReturn(mu.consumerGroupMeta());
            copy.consumer(mockConsumer);
        }

        var override = copy
                .producer(Mockito.mock(Producer.class))
                .build();

        // overwrite super's with new instance
        super.optionsInstance = override;
    }

    public PCModuleTestEnv() {
        this(ParallelConsumerOptions.<String, String>builder()
                .producer(mock(Producer.class))
                .consumer(mock(Consumer.class))
                .build());
    }

    @Override
    protected ProducerWrap<String, String> producerWrap() {
        return mockProducerWrapTransactional();
    }

    ProducerWrap<String, String> mockProduceWrap = Mockito.spy(new ProducerWrap<>(options(), true, producer()));

    @NonNull
    private ProducerWrap mockProducerWrapTransactional() {
        return mockProduceWrap;
    }

    @Override
    protected ConsumerManager<String, String> consumerManager() {
        ConsumerManager<String, String> consumerManager = super.consumerManager();

        // force update to set cache, otherwise maybe never called (fake consuemr)
        consumerManager.updateMetadataCache();

        return consumerManager;
    }
}