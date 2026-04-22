package com.tp.commandes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CommandeRequest {

    @NotBlank(message = "L'ID client est obligatoire")
    private String clientId;

    @NotBlank
    private String adresseLivraison;

    @NotEmpty
    private List<LigneCommandeRequest> lignes;

    // Getters et Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public List<LigneCommandeRequest> getLignes() { return lignes; }
    public void setLignes(List<LigneCommandeRequest> lignes) { this.lignes = lignes; }
}
