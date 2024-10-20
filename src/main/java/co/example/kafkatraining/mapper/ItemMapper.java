package co.example.kafkatraining.mapper;

import co.example.kafkatraining.jpa.entity.ItemEntity;
import co.example.kafkatraining.schemas.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper extends MapperEventData<ItemEntity, Item>{

}
