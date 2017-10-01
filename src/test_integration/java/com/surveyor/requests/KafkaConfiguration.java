package com.surveyor.requests;

import com.surveyor.requests.data.dto.QuestionDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@Configuration
public class KafkaConfiguration {

    public DefaultKafkaConsumerFactory<String, QuestionDTO> consumerFactory(KafkaEmbedded embeddedKafka) {
        Map<String, Object> props =
                KafkaTestUtils.consumerProps("sender", "false", embeddedKafka);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 15000);
        JsonDeserializer<QuestionDTO> jsonDeserializer = new JsonDeserializer<>(QuestionDTO.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }
}
