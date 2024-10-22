package co.example.kafkatraining.jpa.repository;

import co.example.kafkatraining.jpa.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<SaleEntity, String> {
}
