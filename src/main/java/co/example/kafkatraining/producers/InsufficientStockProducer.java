package co.example.kafkatraining.producers;

import co.example.kafkatraining.schemas.InsufficientStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static co.example.kafkatraining.config.KafkaConfig.TOPIC_INSUFFICIENT_STOCK;

@Component
@RequiredArgsConstructor
@Slf4j
public class InsufficientStockProducer {

    private final KafkaTemplate<String, InsufficientStock> kafkaTemplate;

    public void send(final InsufficientStock message) {

        final CompletableFuture<SendResult<String, InsufficientStock>> result = kafkaTemplate.send(TOPIC_INSUFFICIENT_STOCK, message.id(),message);

        result.thenAccept(insufficientStockSendResult -> log.info("Sent message: {} to: {}", message, TOPIC_INSUFFICIENT_STOCK));

        result.exceptionally(ex -> {
            log.error("Error al enviar el mensaje: {}", ex.getMessage());
            return null;
        });

    }

}
