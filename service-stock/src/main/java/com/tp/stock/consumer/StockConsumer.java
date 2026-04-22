package com.tp.stock.consumer;

import com.tp.events.CommandeAnnuleeEvent;
import com.tp.events.CommandeCreeEvent;
import com.tp.events.CommandeEvent;
import com.tp.stock.model.Stock;
import com.tp.stock.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockConsumer.class);
    private final StockRepository stockRepo;

    public StockConsumer(StockRepository stockRepo) { this.stockRepo = stockRepo; }

    @KafkaListener(topics = "commandes", groupId = "service-stock")
    @Transactional
    public void onMessage(CommandeEvent event) {
        switch (event.getType()) {
            case "COMMANDE_CREEE" -> reserverStock((CommandeCreeEvent) event);
            case "COMMANDE_ANNULEE" -> libererStock((CommandeAnnuleeEvent) event);
            default -> log.debug("[STOCK] Événement ignoré : {}", event.getType());
        }
    }

    private void reserverStock(CommandeCreeEvent event) {
        log.info("╔══ [STOCK] ═══════════════════════════════════");
        log.info("║ Réservation stock pour commande : {}", event.getCommandeId());
        if (event.getLignes() == null) { log.warn("║ Aucune ligne dans l'événement"); return; }

        event.getLignes().forEach(ligne -> {
            stockRepo.findByProduitId(ligne.getProduitId()).ifPresentOrElse(
                    stock -> {
                        stock.setQuantiteReservee(stock.getQuantiteReservee() + ligne.getQuantite());
                        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - ligne.getQuantite());
                        stockRepo.save(stock);
                        log.info("║ {} : -{} unités réservées (restant: {})",
                                ligne.getProduitId(), ligne.getQuantite(), stock.getQuantiteDisponible());
                    },
                    () -> log.warn("║ Produit {} non trouvé en stock", ligne.getProduitId())
            );
        });
        log.info("╚══════════════════════════════════════════════");
    }

    private void libererStock(CommandeAnnuleeEvent event) {
        log.info("╔══ [STOCK] ═══════════════════════════════════");
        log.info("║ Libération stock — commande annulée : {}", event.getCommandeId());
        if (event.getLignes() != null) {
            event.getLignes().forEach(ligne ->
                    stockRepo.findByProduitId(ligne.getProduitId()).ifPresent(stock -> {
                        stock.setQuantiteReservee(stock.getQuantiteReservee() - ligne.getQuantite());
                        stock.setQuantiteDisponible(stock.getQuantiteDisponible() + ligne.getQuantite());
                        stockRepo.save(stock);
                    })
            );
        }
        log.info("╚══════════════════════════════════════════════");
    }
}
