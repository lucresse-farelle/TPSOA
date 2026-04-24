package com.tp.commandes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.commandes.model.OutboxEvent;
import com.tp.commandes.repository.OutboxEventRepository;
import com.tp.events.CommandeAnnuleeEvent;
import com.tp.events.CommandeCreeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxPublisherService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisherService.class);
    private static final String TOPIC = "commandes";

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxPublisherService(OutboxEventRepository outboxRepo,
                                  KafkaTemplate<String, Object> kafkaTemplate,
                                  ObjectMapper objectMapper) {
        this.outboxRepo = outboxRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000) // Every 5 seconds ,lit les événements non publiés, les désérialise et les envoie à Kafka, puis les marque comme publiés.
    public void publishEvents() {
        List<OutboxEvent> events = outboxRepo.findUnpublishedEvents();

        for (OutboxEvent event : events) {
            try {
                Object kafkaEvent = deserializeEvent(event);
                kafkaTemplate.send(TOPIC, event.getAggregateId(), kafkaEvent)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Erreur publication Kafka pour event {}", event.getId(), ex);
                            } else {
                                log.info("Événement {} publié dans partition {}",
                                        event.getId(), result.getRecordMetadata().partition());
                                // Mark as published
                                event.setPublished(true);
                                outboxRepo.save(event);
                            }
                        });
            } catch (Exception e) {
                log.error("Erreur désérialisation event {}", event.getId(), e);
            }
        }
    }

    private Object deserializeEvent(OutboxEvent event) throws Exception {
        switch (event.getEventType()) {
            case "CommandeCreeEvent":
                return objectMapper.readValue(event.getPayload(), CommandeCreeEvent.class);
            case "CommandeAnnuleeEvent":
                return objectMapper.readValue(event.getPayload(), CommandeAnnuleeEvent.class);
            default:
                throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }
}