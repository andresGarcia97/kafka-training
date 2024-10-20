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

        itemRepository.findById(item.id())
                .ifPresentOrElse(itemEntity -> {

                    final ItemEntity itemUpdate =  itemRepository.save(itemMapper.toEntity(item));
                    log.info("itemUpdate: {}", itemUpdate);

                }, () -> {

                    final ItemEntity itemSaved =  itemRepository.save(itemMapper.toEntity(item));
                    log.info("itemSaved: {}", itemSaved);

                });

    }
}
