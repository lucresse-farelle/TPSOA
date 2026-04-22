package com.tp.commandes.model;

import java.math.BigDecimal;

public class LigneCommandeRequest {
    private String produitId;
    private int quantite;
    private BigDecimal prixUnitaire;

    public String getProduitId() { return produitId; }
    public void setProduitId(String produitId) { this.produitId = produitId; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}