package com.tp.notification.consumer;

import com.tp.events.CommandeAnnuleeEvent;
import com.tp.events.CommandeCreeEvent;
import com.tp.events.CommandeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    // @KafkaListener = "écoute ce topic, avec ce groupe, appelle cette méthode à chaque message"
    @KafkaListener(topics = "commandes", groupId = "service-notification")
    public void onMessage(CommandeCreeEvent event) {
        // 1. Vérification de l'objet global
        if (event == null) {
            System.err.println("Événement reçu totalement null");
            return;
        }

        // 2. Log pour voir ce qu'il y a dedans (avant le crash)
        System.out.println("Événement reçu : " + event.toString());

        // Dans NotificationConsumer.java
        if (event instanceof CommandeCreeEvent creeEvent) {
            // On utilise getMontantTotal() au lieu de getStatus()
            System.out.println("Nouvelle commande reçue ! Montant : " + creeEvent.getMontantTotal());
            System.out.println("Client ID : " + creeEvent.getClientId());
        }

        // Ton code actuel...
    }
}

