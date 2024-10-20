package co.example.kafkatraining.consumer;

import co.example.kafkatraining.handler.ItemHandler;
import co.example.kafkatraining.schemas.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static co.example.kafkatraining.config.KafkaConfig.KAFKA_BEAN_NAME_ITEM_CONSUMER_FACTORY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemConsumer {

    private final ItemHandler itemHandler;

    @KafkaListener(id = "ITEMS", topics = "ITEMS", containerFactory = KAFKA_BEAN_NAME_ITEM_CONSUMER_FACTORY)
    public void consume(final ConsumerRecord<String, Item> itemRecord) {

        final Header deleteHeader = itemRecord.headers().lastHeader("delete");
        final Boolean deleteItem = deleteHeader != null && Boolean.parseBoolean(new String(deleteHeader.value()));

        final Item item = itemRecord.value();

        if(Boolean.TRUE.equals(deleteItem)){
            log.info("itemToDelete: {}", item);
            itemHandler.delete(item);
            return;
        }

        log.info("item: {}", item);
        itemHandler.process(item);

    }

}
