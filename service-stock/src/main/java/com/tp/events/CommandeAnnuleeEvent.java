package com.tp.events;

import java.util.List;

public class CommandeAnnuleeEvent extends CommandeEvent {
    private List<LigneCommande> lignes;
    private String motifAnnulation;

    public CommandeAnnuleeEvent() { super(); }

    public CommandeAnnuleeEvent(String commandeId, List<LigneCommande> lignes, String motif) {
        super(commandeId, "COMMANDE_ANNULEE");
        this.lignes = lignes;
        this.motifAnnulation = motif;
    }

    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }
    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String motif) { this.motifAnnulation = motif; }
}
