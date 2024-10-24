package co.example.kafkatraining.schemas;

public record InsufficientStock(String itemId, String saleId, String customerId, String description) {
}
