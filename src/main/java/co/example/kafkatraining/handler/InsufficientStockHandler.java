package co.example.kafkatraining.handler;

import co.example.kafkatraining.schemas.InsufficientStock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsufficientStockHandler {

    public void process(final InsufficientStock insufficientStock){

        log.info("insufficientStock process: {}", insufficientStock);

    }

}
