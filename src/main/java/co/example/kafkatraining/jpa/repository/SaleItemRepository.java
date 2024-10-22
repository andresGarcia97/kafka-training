package co.example.kafkatraining.jpa.repository;

import co.example.kafkatraining.jpa.entity.SaleItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItemEntity, String> {
}
