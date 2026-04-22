package com.tp.facturation.consumer;

import com.tp.events.CommandeCreeEvent;
import com.tp.events.CommandeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class FacturationConsumer {

    private static final Logger log = LoggerFactory.getLogger(FacturationConsumer.class);

    @KafkaListener(topics = "commandes", groupId = "service-facturation")
    public void onMessage(CommandeEvent event) {
        if ("COMMANDE_CREEE".equals(event.getType())) {
            CommandeCreeEvent creee = (CommandeCreeEvent) event;
            String numeroFacture = "FACT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            log.info("╔══ [FACTURATION] ════════════════════════════");
            log.info("║ Facture générée : {}", numeroFacture);
            log.info("║ Commande        : {}", creee.getCommandeId());
            log.info("║ Client          : {}", creee.getClientId());
            log.info("║ Montant HT      : {} €", creee.getMontantTotal());
            log.info("║ Montant TTC     : {} €", creee.getMontantTotal().multiply(new java.math.BigDecimal("1.20")));
            log.info("║ Date facture    : {}", LocalDate.now());
            log.info("╚═════════════════════════════════════════════");
        }
    }
}
