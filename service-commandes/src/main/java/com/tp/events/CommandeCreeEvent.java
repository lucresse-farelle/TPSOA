package com.tp.events;

import java.math.BigDecimal;
import java.util.List;

public class CommandeCreeEvent extends CommandeEvent {
    private String clientId;
    private BigDecimal montantTotal;
    private String adresseLivraison;
    private List<LigneCommande> lignes;

    public CommandeCreeEvent() { super(); }

    public CommandeCreeEvent(String commandeId, String clientId,
                             BigDecimal montantTotal, String adresseLivraison,
                             List<LigneCommande> lignes) {
        super(commandeId, "COMMANDE_CREEE");
        this.clientId = clientId;
        this.montantTotal = montantTotal;
        this.adresseLivraison = adresseLivraison;
        this.lignes = lignes;
    }

    // Getters et Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }
}