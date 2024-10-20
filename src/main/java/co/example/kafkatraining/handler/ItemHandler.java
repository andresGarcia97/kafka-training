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

        if(item == null || item.id() == null || item.id().isBlank() || item.quantity() == null || item.value() == null){
            log.error("Item not valid(all attributes are required): {}", item);
            return;
        }

        itemRepository.findById(item.id())
                .ifPresentOrElse(itemEntity -> {

                    final ItemEntity itemUpdate =  itemRepository.save(itemMapper.toEntity(item));
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
