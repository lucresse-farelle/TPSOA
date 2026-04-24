package com.tp.commandes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.commandes.model.*;
import com.tp.commandes.repository.CommandeRepository;
import com.tp.commandes.repository.OutboxEventRepository;
import com.tp.events.CommandeAnnuleeEvent;
import com.tp.events.CommandeCreeEvent;
import com.tp.events.LigneCommande;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // Déclare cette classe comme un service Spring (géré automatiquement)
public class CommandeService {

    private static final Logger log = LoggerFactory.getLogger(CommandeService.class);

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxRepo;
    private final CommandeRepository repo;

    // Injection de dépendances : Spring fournit automatiquement ces objets
    public CommandeService(ObjectMapper objectMapper,
                           OutboxEventRepository outboxRepo,
                           CommandeRepository repo) {
        this.objectMapper = objectMapper;
        this.outboxRepo = outboxRepo;
        this.repo = repo;
    }

    @Transactional // Assure que la sauvegarde DB et l'insertion en outbox sont atomiques
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

        // 4. Créer l'événement et le sauvegarder dans l'outbox
        CommandeCreeEvent event = new CommandeCreeEvent(
                sauvegardee.getId(),
                req.getClientId(),
                montant,
                req.getAdresseLivraison(),
                lignesEvent
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(sauvegardee.getId(), "CommandeCreeEvent", payload);
            outboxRepo.save(outboxEvent);
            log.info("[COMMANDES] Événement CommandeCreeEvent sauvegardé en outbox pour commande {}", sauvegardee.getId());
        } catch (Exception e) {
            log.error("[COMMANDES] Erreur sérialisation événement pour commande {}", sauvegardee.getId(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde de l'événement", e);
        }

        return sauvegardee;
    }

    @Transactional
    public Commande annulerCommande(String commandeId, String motif) {
        Commande cmd = repo.findById(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande " + commandeId + " introuvable"));

        cmd.setStatut(StatutCommande.ANNULEE);
        repo.save(cmd);

        // Créer l'événement et le sauvegarder dans l'outbox
        CommandeAnnuleeEvent event = new CommandeAnnuleeEvent(commandeId, List.of(), motif);
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = new OutboxEvent(commandeId, "CommandeAnnuleeEvent", payload);
            outboxRepo.save(outboxEvent);
            log.info("[COMMANDES] Événement CommandeAnnuleeEvent sauvegardé en outbox pour commande {}", commandeId);
        } catch (Exception e) {
            log.error("[COMMANDES] Erreur sérialisation événement pour commande {}", commandeId, e);
            throw new RuntimeException("Erreur lors de la sauvegarde de l'événement", e);
        }

        return cmd;
    }
}