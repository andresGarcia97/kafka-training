package co.example.kafkatraining.mapper;

import co.example.kafkatraining.jpa.entity.SaleEntity;
import co.example.kafkatraining.schemas.Sale;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SaleMapper extends MapperEventData<SaleEntity, Sale>{

    SaleEntity toEntity(Sale sale);

}
