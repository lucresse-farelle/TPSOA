package com.tp.stock.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String produitId;

    private int quantiteDisponible;
    private int quantiteReservee;

    public Stock() {}

    public Stock(String produitId, int quantiteDisponible) {
        this.produitId = produitId;
        this.quantiteDisponible = quantiteDisponible;
        this.quantiteReservee = 0;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public String getProduitId() { return produitId; }
    public void setProduitId(String produitId) { this.produitId = produitId; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(int q) { this.quantiteDisponible = q; }
    public int getQuantiteReservee() { return quantiteReservee; }
    public void setQuantiteReservee(int q) { this.quantiteReservee = q; }
}
