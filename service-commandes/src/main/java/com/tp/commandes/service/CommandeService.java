package com.tp.commandes.service;

import com.tp.commandes.model.*;
import com.tp.commandes.repository.CommandeRepository;
import com.tp.events.CommandeAnnuleeEvent;
import com.tp.events.CommandeCreeEvent;
import com.tp.events.LigneCommande;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // Déclare cette classe comme un service Spring (géré automatiquement)
public class CommandeService {

    private static final Logger log = LoggerFactory.getLogger(CommandeService.class);
    private static final String TOPIC = "commandes"; // Nom du topic Kafka

    // KafkaTemplate = l'outil pour envoyer des messages dans Kafka
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CommandeRepository repo;

    // Injection de dépendances : Spring fournit automatiquement ces objets
    public CommandeService(KafkaTemplate<String, Object> kafkaTemplate,
                           CommandeRepository repo) {
        this.kafkaTemplate = kafkaTemplate;
        this.repo = repo;
    }

    @Transactional // Assure que la sauvegarde DB et la publication Kafka sont groupées
    public Commande creerCommande(CommandeRequest req) {
        // 1. Calculer le montant total
        BigDecimal montant = req.getLignes().stream()
                .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Créer et sauvegarder la commande en base
        Commande cmd = new Commande();
        cmd.setId(UUID.randomUUID().toString());
        cmd.setClientId(req.getClientId());
        cmd.setMontantTotal(montant);
        cmd.setAdresseLivraison(req.getAdresseLivraison());
        cmd.setStatut(StatutCommande.CREEE);
        Commande sauvegardee = repo.save(cmd);

        // 3. Transformer les lignes du DTO en objets d'événement
        List<LigneCommande> lignesEvent = req.getLignes().stream()
                .map(l -> {
                    LigneCommande lc = new LigneCommande();
                    lc.setProduitId(l.getProduitId());
                    lc.setQuantite(l.getQuantite());
                    lc.setPrixUnitaire(l.getPrixUnitaire());
                    return lc;
                })
                .collect(Collectors.toList());

        // 4. Publier l'événement dans Kafka (fire-and-forget)
        // La clé = commandeId garantit que tous les événements d'une même commande
        // arrivent dans la même partition, dans l'ordre
        CommandeCreeEvent event = new CommandeCreeEvent(
                sauvegardee.getId(),
                req.getClientId(),
                montant,
                req.getAdresseLivraison(),
                lignesEvent
        );

        kafkaTemplate.send(TOPIC, sauvegardee.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[COMMANDES] Erreur publication Kafka pour commande {}", sauvegardee.getId(), ex);
                    } else {
                        log.info("[COMMANDES] Événement publié — commande {} dans partition {}",
                                sauvegardee.getId(),
                                result.getRecordMetadata().partition());
                    }
                });

        return sauvegardee;
    }

    @Transactional
    public Commande annulerCommande(String commandeId, String motif) {
        Commande cmd = repo.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande " + commandeId + " introuvable"));

        cmd.setStatut(StatutCommande.ANNULEE);
        repo.save(cmd);

        // Reconstituer les lignes (simplification TP : on publie sans lignes détaillées)
        CommandeAnnuleeEvent event = new CommandeAnnuleeEvent(commandeId, List.of(), motif);
        kafkaTemplate.send(TOPIC, commandeId, event);

        log.info("[COMMANDES] Commande {} annulée, événement publié", commandeId);
        return cmd;
    }
}