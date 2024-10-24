package co.example.kafkatraining.handler;

import co.example.kafkatraining.jpa.entity.ItemEntity;
import co.example.kafkatraining.jpa.repository.ItemRepository;
import co.example.kafkatraining.mapper.ItemMapper;
import co.example.kafkatraining.schemas.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemHandler {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public void process(final Item item){

        if(item == null || item.id() == null || item.id().isBlank()){
            log.error("Item not valid(ID attributes is required): {}", item);
            return;
        }

        if(item.quantity() == null || item.quantity().compareTo(0) == 0 || item.value() == null || item.value().compareTo(0.0) == 0){
            log.error("Item not valid(quantity and value can not be 0): {}", item);
            return;
        }

        if(item.name() == null || item.name().isBlank()){
            log.error("Item not valid(the name is required): {}", item);
            return;
        }

        itemRepository.findById(item.id())
                .ifPresentOrElse(existingItem -> {

                    final ItemEntity itemToUpdate = itemMapper.toEntity(item);

                    if(item.quantity().compareTo(existingItem.getQuantity()) < 0){
                        log.warn("change of quantity of {} to {}, don't permitted on item {}", item.quantity(), existingItem.getQuantity(), item.id());
                        itemToUpdate.setQuantity(existingItem.getQuantity());
                    }

                    else if(item.quantity().compareTo(existingItem.getQuantity()) > 0){
                        log.info("adding new stock passing of {} to {} on item {}", existingItem.getQuantity(), item.quantity(), item.id());
                    }

                    final ItemEntity itemUpdate =  itemRepository.save(itemToUpdate);
                    log.info("itemUpdate: {}", itemUpdate);

                }, () -> {

                    final ItemEntity itemSaved =  itemRepository.save(itemMapper.toEntity(item));
                    log.info("itemSaved: {}", itemSaved);

                });

    }

    public void delete(final Item item) {

        if(item == null || item.id() == null || item.id().isBlank()){
            log.error("Item to delete no valid(ID attribute is required): {}", item);
            return;
        }

        itemRepository.findById(item.id())
                .ifPresent(itemToDelete -> {

                    if(itemToDelete.getQuantity() > 0){
                        log.warn("Item can not be deleted, until have {} elements on stock", itemToDelete.getQuantity());
                        return;
                    }

                    itemRepository.deleteById(itemToDelete.getId());
                    log.warn("Item deleted: {}", itemToDelete);

                });

    }
}
