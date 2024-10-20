package co.example.kafkatraining.jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "items")
public class ItemEntity {

    @Id
    private String id;
    private int quantity;
    private double value;

    public void decreaseQuantity(int quantity) throws Exception {

        this.quantity -= quantity;

        if (this.quantity < 0) {
            throw new Exception("insufficient stock");
        }
    }

    public void incrementQuantity(int quantity) {
        this.quantity += quantity;
    }
}
