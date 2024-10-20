package co.example.kafkatraining.consumer;

import co.example.kafkatraining.handler.SalesHandler;
import co.example.kafkatraining.schemas.Sale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static co.example.kafkatraining.config.KafkaConfig.KAFKA_BEAN_NAME_SALE_CONSUMER_FACTORY;

@Component
@Slf4j
@RequiredArgsConstructor
public class SaleConsumer {

    private final SalesHandler handler;


    @KafkaListener(id = "SALES", topics = "SALES", containerFactory = KAFKA_BEAN_NAME_SALE_CONSUMER_FACTORY)
    public void consume(final Sale sale) {

        log.info("sale: {}", sale);

        handler.process(sale);

    }






}
