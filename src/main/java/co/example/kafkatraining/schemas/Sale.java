package co.example.kafkatraining.schemas;

import java.time.LocalDate;
import java.util.List;

public record Sale(String id,
                   String customerId,
                   double amount,
                   LocalDate sale,
                   List<Item> items) {
}
