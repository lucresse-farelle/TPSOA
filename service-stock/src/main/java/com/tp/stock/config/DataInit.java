package com.tp.stock.config;

import com.tp.stock.model.Stock;
import com.tp.stock.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInit implements CommandLineRunner {

    private final StockRepository repo;

    public DataInit(StockRepository repo) { this.repo = repo; }

    @Override
    public void run(String... args) {
        // Initialiser quelques produits en stock au démarrage
        repo.save(new Stock("PROD-A", 100));
        repo.save(new Stock("PROD-B", 50));
        repo.save(new Stock("PROD-C", 200));
        System.out.println("[STOCK] Stocks initialisés : PROD-A(100), PROD-B(50), PROD-C(200)");
    }
}