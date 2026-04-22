package com.tp.commandes.controller;

import com.tp.commandes.model.Commande;
import com.tp.commandes.model.CommandeRequest;
import com.tp.commandes.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    // POST /api/commandes → crée une commande et publie dans Kafka
    @PostMapping
    public ResponseEntity<Commande> creer(
            @Valid @RequestBody CommandeRequest req,
            UriComponentsBuilder ucb) {
        Commande creee = commandeService.creerCommande(req);
        URI location = ucb.path("/api/commandes/{id}")
                .buildAndExpand(creee.getId()).toUri();
        return ResponseEntity.created(location).body(creee); // HTTP 201
    }

    // DELETE /api/commandes/{id} → annule une commande
    @DeleteMapping("/{id}")
    public ResponseEntity<Commande> annuler(
            @PathVariable String id,
            @RequestParam(defaultValue = "Annulation client") String motif) {
        return ResponseEntity.ok(commandeService.annulerCommande(id, motif));
    }
}