package co.example.kafkatraining.schemas;

public record InsufficientStock(String id, String saleId, String customerId, String description) {
}
