package co.example.kafkatraining.config;

import co.example.kafkatraining.schemas.InsufficientStock;
import co.example.kafkatraining.schemas.LowStock;
import co.example.kafkatraining.schemas.Sale;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    public static final String KAFKA_BEAN_NAME_SALE_CONSUMER_FACTORY = "listenerContainerFactory";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    private Map<String, Object> defaultConfigProducerFactory() {
        final Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return config;
    }

    private Map<String, Object> defaultConfigDeserialization() {
        final Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return config;
    }

    @Bean
    protected CommonErrorHandler commonErrorHandler() {
        return new KafkaErrorHandler();
    }

    @Bean
    public ConsumerFactory<String, Sale> consumerSaleFactory() {
        return new DefaultKafkaConsumerFactory<>(defaultConfigDeserialization(), new StringDeserializer(), new JsonDeserializer<>(Sale.class));
    }

    @Bean(name = KAFKA_BEAN_NAME_SALE_CONSUMER_FACTORY)
    public ConcurrentKafkaListenerContainerFactory<String, Sale> kafkaListenerFactory() {
        final ConcurrentKafkaListenerContainerFactory<String, Sale> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerSaleFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(commonErrorHandler());
        factory.setConcurrency(3);
        return factory;
    }

    @Bean
    public ProducerFactory<String, LowStock> producerLowStockFactory() {
        return new DefaultKafkaProducerFactory<>(defaultConfigProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, LowStock> kafkaSaleTemplate(ProducerFactory<String, LowStock> producerFactory) {
        return new KafkaTemplate<>(producerLowStockFactory());
    }

    @Bean
    public ProducerFactory<String, InsufficientStock> producerInsufficientStockFactory() {
        return new DefaultKafkaProducerFactory<>(defaultConfigProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, InsufficientStock> kafkaInsufficientTemplate(ProducerFactory<String, InsufficientStock> producerFactory) {
        return new KafkaTemplate<>(producerInsufficientStockFactory());
    }


}
