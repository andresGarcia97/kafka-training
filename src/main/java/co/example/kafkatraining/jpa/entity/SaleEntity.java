package co.example.kafkatraining.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "sales")
public class SaleEntity {

    @Id
    private String id;
    private String customerId;
    private double amount;
    private LocalDate sale;

}
