package com.tp.commandes.repository;

import com.tp.commandes.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring génère automatiquement toutes les méthodes CRUD
public interface CommandeRepository extends JpaRepository<Commande, String> {
}