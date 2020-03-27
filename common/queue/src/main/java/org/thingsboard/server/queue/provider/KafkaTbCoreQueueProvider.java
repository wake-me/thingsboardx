/**
 * Copyright © 2016-2020 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.queue.provider;

import org.springframework.stereotype.Component;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.gen.transport.TransportProtos.ToCoreMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToCoreNotificationMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToRuleEngineMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToRuleEngineNotificationMsg;
import org.thingsboard.server.gen.transport.TransportProtos.ToTransportMsg;
import org.thingsboard.server.gen.transport.TransportProtos.TransportApiRequestMsg;
import org.thingsboard.server.gen.transport.TransportProtos.TransportApiResponseMsg;
import org.thingsboard.server.queue.TbQueueConsumer;
import org.thingsboard.server.queue.TbQueueCoreSettings;
import org.thingsboard.server.queue.TbQueueProducer;
import org.thingsboard.server.queue.TbQueueRuleEngineSettings;
import org.thingsboard.server.queue.TbQueueTransportApiSettings;
import org.thingsboard.server.queue.TbQueueTransportNotificationSettings;
import org.thingsboard.server.queue.common.TbProtoQueueMsg;
import org.thingsboard.server.queue.discovery.PartitionService;
import org.thingsboard.server.queue.discovery.TbServiceInfoProvider;
import org.thingsboard.server.queue.kafka.TBKafkaConsumerTemplate;
import org.thingsboard.server.queue.kafka.TBKafkaProducerTemplate;
import org.thingsboard.server.queue.kafka.TbKafkaSettings;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.queue.util.TbKafkaQueue;

@Component
@TbCoreComponent
@TbKafkaQueue
public class KafkaTbCoreQueueProvider implements TbCoreQueueProvider {

    private final PartitionService partitionService;
    private final TbKafkaSettings kafkaSettings;
    private final TbServiceInfoProvider serviceInfoProvider;
    private final TbQueueCoreSettings coreSettings;
    private final TbQueueRuleEngineSettings ruleEngineSettings;
    private final TbQueueTransportApiSettings transportApiSettings;
    private final TbQueueTransportNotificationSettings transportNotificationSettings;

    public KafkaTbCoreQueueProvider(PartitionService partitionService, TbKafkaSettings kafkaSettings,
                                    TbServiceInfoProvider serviceInfoProvider,
                                    TbQueueCoreSettings coreSettings,
                                    TbQueueRuleEngineSettings ruleEngineSettings,
                                    TbQueueTransportApiSettings transportApiSettings,
                                    TbQueueTransportNotificationSettings transportNotificationSettings) {
        this.partitionService = partitionService;
        this.kafkaSettings = kafkaSettings;
        this.serviceInfoProvider = serviceInfoProvider;
        this.coreSettings = coreSettings;
        this.ruleEngineSettings = ruleEngineSettings;
        this.transportApiSettings = transportApiSettings;
        this.transportNotificationSettings = transportNotificationSettings;
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToTransportMsg>> getTransportNotificationsMsgProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToTransportMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-transport-notifications-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(coreSettings.getTopic());
        return requestBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineMsg>> getRuleEngineMsgProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToRuleEngineMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-rule-engine-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(coreSettings.getTopic());
        return requestBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToRuleEngineNotificationMsg>> getRuleEngineNotificationsMsgProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToRuleEngineNotificationMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-rule-engine-notifications-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(ruleEngineSettings.getTopic());
        return requestBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreMsg>> getTbCoreMsgProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToCoreMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-to-core-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(coreSettings.getTopic());
        return requestBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<ToCoreNotificationMsg>> getTbCoreNotificationsMsgProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<ToCoreNotificationMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-to-core-notifications-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(coreSettings.getTopic());
        return requestBuilder.build();
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToCoreMsg>> getToCoreMsgConsumer() {
        TBKafkaConsumerTemplate.TBKafkaConsumerTemplateBuilder<TbProtoQueueMsg<ToCoreMsg>> consumerBuilder = TBKafkaConsumerTemplate.builder();
        consumerBuilder.settings(kafkaSettings);
        consumerBuilder.topic(coreSettings.getTopic());
        consumerBuilder.clientId("tb-core-consumer-" + serviceInfoProvider.getServiceId());
        consumerBuilder.groupId("tb-core-node-" + serviceInfoProvider.getServiceId());
        consumerBuilder.decoder(msg -> new TbProtoQueueMsg<>(msg.getKey(), ToCoreMsg.parseFrom(msg.getData()), msg.getHeaders()));
        return consumerBuilder.build();
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<ToCoreNotificationMsg>> getToCoreNotificationsMsgConsumer() {
        TBKafkaConsumerTemplate.TBKafkaConsumerTemplateBuilder<TbProtoQueueMsg<ToCoreNotificationMsg>> consumerBuilder = TBKafkaConsumerTemplate.builder();
        consumerBuilder.settings(kafkaSettings);
        consumerBuilder.topic(partitionService.getNotificationsTopic(ServiceType.TB_CORE, serviceInfoProvider.getServiceId()).getFullTopicName());
        consumerBuilder.clientId("tb-core-notifications-consumer-" + serviceInfoProvider.getServiceId());
        consumerBuilder.groupId("tb-core-notifications-node-" + serviceInfoProvider.getServiceId());
        consumerBuilder.decoder(msg -> new TbProtoQueueMsg<>(msg.getKey(), ToCoreNotificationMsg.parseFrom(msg.getData()), msg.getHeaders()));
        return consumerBuilder.build();
    }

    @Override
    public TbQueueConsumer<TbProtoQueueMsg<TransportApiRequestMsg>> getTransportApiRequestConsumer() {
        TBKafkaConsumerTemplate.TBKafkaConsumerTemplateBuilder<TbProtoQueueMsg<TransportApiRequestMsg>> consumerBuilder = TBKafkaConsumerTemplate.builder();
        consumerBuilder.settings(kafkaSettings);
        consumerBuilder.topic(transportApiSettings.getRequestsTopic());
        consumerBuilder.clientId("tb-core-transport-api-consumer-" + serviceInfoProvider.getServiceId());
        consumerBuilder.groupId("tb-core-transport-api-consumer-" + serviceInfoProvider.getServiceId());
        consumerBuilder.decoder(msg -> new TbProtoQueueMsg<>(msg.getKey(), TransportApiRequestMsg.parseFrom(msg.getData()), msg.getHeaders()));
        return consumerBuilder.build();
    }

    @Override
    public TbQueueProducer<TbProtoQueueMsg<TransportApiResponseMsg>> getTransportApiResponseProducer() {
        TBKafkaProducerTemplate.TBKafkaProducerTemplateBuilder<TbProtoQueueMsg<TransportApiResponseMsg>> requestBuilder = TBKafkaProducerTemplate.builder();
        requestBuilder.settings(kafkaSettings);
        requestBuilder.clientId("tb-core-transport-api-producer-" + serviceInfoProvider.getServiceId());
        requestBuilder.defaultTopic(coreSettings.getTopic());
        return requestBuilder.build();
    }

}
