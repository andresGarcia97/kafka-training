package co.example.kafkatraining.schemas;

public record Item (String id,
                    Integer quantity,
                    Double value,
                    String name) {
}
