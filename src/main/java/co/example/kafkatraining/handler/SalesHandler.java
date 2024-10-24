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
import java.util.UUID;

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

    // TODO buscar forma de devolver todas las transacciones en caso de error, tal vez usar @Transactional
    public void process(Sale sale) {

        if(sale == null || sale.items().isEmpty()){
            log.error("A sale required at least one item: {}", sale);
            return;
        }

        if(sale.id() == null || sale.id().isBlank() || saleRepository.findById(sale.id()).isPresent()){
            log.error("A sale can not be exist previously: {}", sale);
            return;
        }

        final List<ItemEntity> stockUpdates = updateStockAndSendAlert(sale);

        final SaleEntity saleSaved = saleRepository.save(saleMapper.toEntity(sale));

        final List<SaleItemEntity> saleItems = saveItemsSale(saleSaved, stockUpdates, sale);
        log.info("saleItems: {}", saleItems);

        final double calculatedAmount = calculateCorrectValueSale(saleItems);

        if(calculatedAmount != sale.amount()){
            saleSaved.setAmount(calculatedAmount);
            final SaleEntity saleCorrectionAmount = saleRepository.save(saleSaved);
            log.warn("the amounts are not identical, amount: {}, calculated: {}, saleCorrectionId: {}", sale.amount(), calculatedAmount, saleCorrectionAmount.getId());
            log.warn("the amounts are not identical, saleItems: {}, itemsReceived: {}, customerId: {}", saleItems, sale.items().size(), saleCorrectionAmount.getCustomerId());
        }

        sendAlertsLowStock(stockUpdates, sale);

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

    private List<ItemEntity> updateStockAndSendAlert(final Sale sale){

        final List<ItemEntity> stockUpdates = new ArrayList<>(sale.items().size());

        for (final Item saleItem: sale.items()) {

            final Optional<ItemEntity> entityOpt = repository.findById(saleItem.id());

            if (entityOpt.isPresent()) {

                final ItemEntity itemEntity = entityOpt.get();

                try {

                    itemEntity.decreaseQuantity(saleItem.quantity());

                    final ItemEntity quantityUpdated = repository.save(itemEntity);
                    stockUpdates.add(quantityUpdated);

                } catch (Exception e) {
                    log.warn("Item {} has {} and the sale require {}", saleItem.id(), itemEntity.getQuantity(), saleItem.quantity());
                    final InsufficientStock insufficientStock = new InsufficientStock(
                            saleItem.id(),
                            sale.id(),
                            sale.customerId(),
                            "Inventory insufficient stock for sale with %s quantity, by a sale of %s".formatted(itemEntity.getQuantity(), saleItem.quantity())
                    );

                    insufficientStockProducer.send(insufficientStock);
                }
            }
        }

        return stockUpdates;
    }

    private List<SaleItemEntity> saveItemsSale(final SaleEntity saleSaved, final List<ItemEntity> stockUpdates, final Sale originalSale){

        final List<SaleItemEntity> saleItemEntities = new ArrayList<>(stockUpdates.size());

        stockUpdates.forEach(itemStock -> {

            final SaleItemEntity saleItem = new SaleItemEntity();
            saleItem.setValue(itemStock.getValue());
            saleItem.setItem(itemStock);
            saleItem.setSale(saleSaved);

            originalSale.items().stream()
                    .filter(item -> itemStock.getId().equals(item.id()))
                    .findFirst()
                    .ifPresent(itemQuantity -> saleItem.setQuantity(itemQuantity.quantity()));

            saleItemEntities.add(saleItem);
        });

        return saleItemRepository.saveAll(saleItemEntities);
    }

    private double calculateCorrectValueSale(final List<SaleItemEntity> stockUpdates){
        return stockUpdates.stream()
                .map(item -> item.getQuantity() * item.getValue())
                .reduce(0.0, Double::sum);
    }

}
