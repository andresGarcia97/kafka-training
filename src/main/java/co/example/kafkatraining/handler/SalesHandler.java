package co.example.kafkatraining.handler;

import co.example.kafkatraining.jpa.entity.ItemEntity;
import co.example.kafkatraining.jpa.entity.SaleEntity;
import co.example.kafkatraining.jpa.entity.SaleItemEntity;
import co.example.kafkatraining.jpa.repository.ItemRepository;
import co.example.kafkatraining.jpa.repository.SaleItemRepository;
import co.example.kafkatraining.jpa.repository.SaleRepository;
import co.example.kafkatraining.mapper.SaleMapper;
import co.example.kafkatraining.schemas.InsufficientStock;
import co.example.kafkatraining.schemas.LowStock;
import co.example.kafkatraining.schemas.Item;
import co.example.kafkatraining.schemas.Sale;
import co.example.kafkatraining.producers.InsufficientStockProducer;
import co.example.kafkatraining.producers.LowStockProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesHandler {

    private final ItemRepository repository;
    private final LowStockProducer lowStockProducer;
    private final InsufficientStockProducer insufficientStockProducer;
    private final SaleMapper saleMapper;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;

    public void process(Sale sale) {

        if(sale == null || sale.items().isEmpty()){
            return;
        }

        // TODO validar que no exista la venta, si existe no hacer nada

        final List<ItemEntity> stockUpdates = new ArrayList<>(sale.items().size());

        for (Item item: sale.items()){

            Optional<ItemEntity> entityOpt = repository.findById(item.id());

            if (entityOpt.isPresent()) {

                ItemEntity itemEntity = entityOpt.get();

                try{

                    itemEntity.decreaseQuantity(item.quantity());

                } catch (Exception e) {
                    InsufficientStock message2 = InsufficientStock.builder()
                            .id(item.id())
                            .saleId(sale.id())
                            .customerId(sale.customerId())
                            .descripcion("Inventory insufficient stock for sale with %s quantity".formatted(itemEntity.getQuantity()))
                            .build();

                    insufficientStockProducer.send(message2);
                }

                final ItemEntity quantityUpdated = repository.save(itemEntity);
                stockUpdates.add(quantityUpdated);

                final SaleEntity entity = saleMapper.toEntity(sale);
                log.info("saleEn0: {}", entity);
                final SaleEntity saleSaved = saleRepository.save(entity);
                log.info("saleEn1: {}", saleSaved);

                final List<SaleItemEntity> saleItemEntities = new ArrayList<>(sale.items().size());

                stockUpdates.forEach(itemStock -> {
                    final SaleItemEntity saleItemEntity = new SaleItemEntity();
                    saleItemEntity.setValue(itemStock.getValue());
                    saleItemEntity.setItem(itemStock);
                    saleItemEntity.setSale(saleSaved);

                    sale.items().stream()
                            .filter(validSale -> itemStock.getId().equals(validSale.id()))
                            .findFirst()
                            .ifPresent(itemQuantity -> saleItemEntity.setQuantity(itemQuantity.quantity()));

                    saleItemEntities.add(saleItemEntity);
                });

                final List<SaleItemEntity> saleItemSaved = saleItemRepository.saveAll(saleItemEntities);
                log.info("saleEn2: {}", saleItemSaved);

                sendAlertsLowStock(stockUpdates, sale);
            }
        }
    }

    private void sendAlertsLowStock(final List<ItemEntity> stockUpdates, final Sale sale) {

        stockUpdates.forEach(itemEntity -> {

            if (itemEntity.getQuantity() < 100 ){

                final LowStock messageLowStock = new LowStock(
                        itemEntity.getId(),
                        sale.id(),
                        sale.customerId(),
                        "Inventory near out of stock %s".formatted(itemEntity.getQuantity()));

                lowStockProducer.send(messageLowStock);
            }
        });

    }

}
