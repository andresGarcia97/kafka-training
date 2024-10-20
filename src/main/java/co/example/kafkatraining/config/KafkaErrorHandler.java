package co.example.kafkatraining.config;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.RecordDeserializationException;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
public final class KafkaErrorHandler implements CommonErrorHandler {

	@Override
	public boolean handleOne(Exception exception, ConsumerRecord<?, ?> data, Consumer<?, ?> consumer, MessageListenerContainer container) {
		handle(exception, consumer);
		return true;
	}

	@Override
	public void handleOtherException(Exception exception, Consumer<?, ?> consumer, MessageListenerContainer container, boolean batchListener) {
		handle(exception, consumer);
	}

	private void handle(Exception exception, Consumer<?, ?> consumer) {
		log.error("KafkaErrorHandler :: Exception thrown", exception);
		if (exception instanceof RecordDeserializationException ex) {
			consumer.seek(ex.topicPartition(), ex.offset() + 1L);
			consumer.commitSync();
		} else {
			log.error("Exception not handled", exception);
		}

	}
}