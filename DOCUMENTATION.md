# Documentation Complète du Projet TPSOA - Architecture Microservices

## Vue d'Ensemble

Ce projet est une implémentation d'une architecture microservices pour un système de gestion de commandes e-commerce. Il démontre les concepts clés des architectures distribuées, de la communication asynchrone via Kafka, et des patterns comme l'Outbox pour garantir la cohérence des données.

Le système comprend quatre services principaux :
- **Service-Commandes** : Gestion des commandes clients
- **Service-Facturation** : Génération des factures
- **Service-Notification** : Envoi des notifications
- **Service-Stock** : Gestion des stocks

## Architecture Générale

### Pattern Microservices
Chaque service est une application Spring Boot indépendante avec sa propre base de données et responsabilités métier. Les services communiquent via des événements Kafka plutôt que des appels synchrones, assurant le découplage et la résilience.

### Communication Asynchrone avec Kafka
- Les événements métier sont publiés sur des topics Kafka
- Les services consommateurs traitent ces événements de manière asynchrone
- Cela permet une scalabilité horizontale et une tolérance aux pannes

### Pattern Outbox
Implémenté dans le service-commandes pour garantir l'atomicité entre les opérations de base de données et la publication d'événements. Sans ce pattern, un échec de publication Kafka après une sauvegarde DB pourrait entraîner une incohérence.

## Technologies Utilisées

- **Spring Boot 3.5.13** : Framework principal pour les microservices
- **Spring Data JPA** : Accès aux données
- **Spring Kafka** : Intégration avec Apache Kafka
- **H2 Database** : Base de données en mémoire pour les tests/démo
- **Docker & Docker Compose** : Conteneurisation et orchestration
- **Maven** : Gestion des dépendances

## Description Détaillée des Services

### 1. Service-Commandes (Port 8081)

**Responsabilités :**
- Création et annulation de commandes
- Validation des données d'entrée
- Publication d'événements métier

**Entités principales :**
- `Commande` : Représente une commande avec ses lignes
- `OutboxEvent` : Stockage des événements en attente de publication

**Endpoints REST :**
- `POST /commandes` : Créer une commande
- `PUT /commandes/{id}/annuler` : Annuler une commande

**Événements publiés :**
- `CommandeCreeEvent` : Quand une commande est créée
- `CommandeAnnuleeEvent` : Quand une commande est annulée

**Implémentation de l'Outbox :**
- Les événements sont d'abord sauvegardés dans la table `outbox_events`
- Un service planifié (`OutboxPublisherService`) les publie vers Kafka toutes les 5 secondes
- Garantit que DB save et event publication sont atomiques

### 2. Service-Facturation (Port 8082)

**Responsabilités :**
- Consommation des événements `CommandeCreeEvent`
- Génération automatique des factures
- Calcul des montants TTC

**Logique métier :**
- Écoute le topic "commandes"
- Pour chaque nouvelle commande, crée une facture
- Applique une TVA de 20%

### 3. Service-Notification (Port 8083)

**Responsabilités :**
- Envoi de notifications par email/SMS
- Consommation des événements de facturation et commandes

**Fonctionnalités :**
- Notifications de confirmation de commande
- Alertes de facturation

### 4. Service-Stock (Port 8084)

**Responsabilités :**
- Gestion des niveaux de stock
- Mise à jour automatique lors des commandes

**Logique :**
- Réduction du stock lors de `CommandeCreeEvent`
- Réapprovisionnement lors d'annulation

## Concepts Clés Implémentés

### 1. Architecture Microservices
- **Découplage** : Chaque service a ses propres responsabilités
- **Indépendance** : Déploiement et scaling séparés
- **Résilience** : Un service peut tomber sans affecter les autres

### 2. Communication Asynchrone
- **Avantages** : Non-bloquant, scalable, tolérant aux pannes
- **Inconvénients** : Complexité de debugging, éventuelle incohérence
- **Solution** : Pattern Outbox pour atomicité

### 3. Pattern Outbox
**Problème résolu :** Dans une transaction DB + publication Kafka, si Kafka échoue après DB commit, l'événement est perdu.

**Solution :**
1. Sauvegarde DB + insertion en outbox dans une seule transaction
2. Processus séparé publie depuis l'outbox vers Kafka
3. Marque comme publié ou supprime après succès

**Avantages :**
- Atomicité garantie
- Retry automatique
- Idempotence

### 4. Event-Driven Architecture
- **Événements vs Commandes** : Les services réagissent aux faits passés
- **Choreography vs Orchestration** : Ici, choreography (pas de coordinateur central)

### 5. Persistence et Transactions
- Utilisation de `@Transactional` pour grouper les opérations
- JPA/Hibernate pour mapping objet-relationnel
- H2 pour simplicité en développement

### 6. Configuration et Déploiement
- **Docker Compose** : Orchestration des services + Kafka + UI Kafka
- **Profiles Spring** : Différentes configs par environnement
- **Health Checks** : Monitoring de l'état des services

## Comment Lancer le Projet

### Prérequis
- Docker et Docker Compose installés
- Java 21
- Maven

### Démarrage
```bash
# Depuis la racine du projet
docker-compose up -d

# Construire et lancer chaque service
cd service-commandes
mvn clean install
mvn spring-boot:run

# Répéter pour chaque service
```

### Test du Système
1. Créer une commande via POST /commandes
2. Vérifier les logs pour voir les événements Kafka
3. Vérifier les bases H2 via console web
4. Observer les notifications et mises à jour stock

## Points d'Attention pour la Présentation

### Questions Potentielles du Professeur
1. **Pourquoi microservices ?** Scalabilité, maintenabilité, technologies différentes par service
2. **Avantages/inconvénients de l'asynchrone ?** Performance vs complexité
3. **Pourquoi Outbox ?** Montrer le problème d'atomicité sans pattern
4. **Comment gérer les erreurs ?** Retry, dead letter queues, compensation
5. **Monitoring et observabilité ?** Logs, métriques, tracing distribué

### Démonstrations Pratiques
- Montrer la création d'une commande et propagation des événements
- Simuler une panne Kafka et voir l'Outbox en action
- Scaler un service avec Docker
- Montrer les bases de données séparées

### Évolutions Possibles
- Saga pattern pour transactions distribuées
- API Gateway pour routing
- Service Registry (Eureka)
- Monitoring avec Prometheus/Grafana
- Sécurité avec OAuth2/JWT

## Lien vers le Dépôt Git
https://github.com/lucresse-farelle/TPSOA.git

Cette documentation couvre tous les aspects du TP. Assurez-vous de comprendre chaque section avant la présentation !