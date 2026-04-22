package com.tp.events;


import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type") // Le champ "type" dans le JSON dira quel objet créer
@JsonSubTypes({
        @JsonSubTypes.Type(value = CommandeCreeEvent.class, name = "COMMANDE_CREEE")
})

public abstract class CommandeEvent {
    private String commandeId;
    private String type;
    private Instant timestamp;

    // Constructeur sans argument OBLIGATOIRE pour la désérialisation JSON
    public CommandeEvent() {}

    public CommandeEvent(String commandeId, String type) {
        this.commandeId = commandeId;
        this.type = type;
        this.timestamp = Instant.now();
    }

    // Getters et Setters
    public String getCommandeId() { return commandeId; }
    public void setCommandeId(String commandeId) { this.commandeId = commandeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
