package co.example.kafkatraining.mapper;

public interface MapperEventData<E, M> {

    E toEntity(M message);

}
