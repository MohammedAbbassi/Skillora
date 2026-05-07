Skillora — extrait « produit + commande » uniquement
=====================================================

Emplacement : dossier Skillora-Shop-Orders (copie autonome du projet Skillora).

Contenu
-------
- Connexion (utilisateur MySQL)
- Onglet Boutique : statistiques catalogue, cartes produits, panier, validation commande
- Onglet Commandes : liste, stats, détail lignes
- Administrateur (ADMIN) :
  - Onglet « Catalogue (admin) » : ajouter / modifier / supprimer des produits
  - Sur Commandes : modifier statut + total, supprimer une commande, créer une commande (client + lignes)

Base de données
---------------
1. Importer sql/shop_minimal.sql dans MySQL/MariaDB (crée la base skillora_shop + tables + comptes démo).
2. Vérifier src/main/resources/application.properties (db.url, db.user, db.password).
   Pour utiliser l’ancienne base skillora : remplacer l’URL par jdbc:mysql://localhost:3306/skillora?…

Comptes démo (après import shop_minimal.sql)
--------------------------------------------
- demo@shop.local / demo123
- admin@shop.local / admin123 (voit toutes les commandes + changement de statut)

Lancer
------
  mvn javafx:run

Main class : com.skillora.shop.ShopOrdersApp

Dépendances Maven : JavaFX (controls+FXML), MySQL connector, jbcrypt (auth).

Note : ce module n’inclut pas les cours, la navigation complète de Skillora, ni l’ouverture d’accès
cours après achat (OrderService ici n’enregistre que la commande et les lignes).
