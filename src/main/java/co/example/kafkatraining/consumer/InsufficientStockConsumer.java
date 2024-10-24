package co.example.kafkatraining.consumer;

import co.example.kafkatraining.handler.InsufficientStockHandler;
import co.example.kafkatraining.schemas.InsufficientStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static co.example.kafkatraining.config.KafkaConfig.KAFKA_BEAN_NAME_INSUFFICIENT_STOCK_CONSUMER_FACTORY;
import static co.example.kafkatraining.config.KafkaConfig.TOPIC_INSUFFICIENT_STOCK;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsufficientStockConsumer {

    private final InsufficientStockHandler insufficientStockHandler;

    @KafkaListener(id = TOPIC_INSUFFICIENT_STOCK, topics = TOPIC_INSUFFICIENT_STOCK, containerFactory = KAFKA_BEAN_NAME_INSUFFICIENT_STOCK_CONSUMER_FACTORY)
    public void consume(final InsufficientStock insufficientStock) {

        log.info("insufficientStock: {}", insufficientStock);
        insufficientStockHandler.process(insufficientStock);

    }

}
