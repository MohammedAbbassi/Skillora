package com.example.services;

import com.example.entities.Cours;
import com.example.interfaces.ICoursChapitreService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier des Cours.
 * Implémente {@link ICoursChapitreService} pour les opérations CRUD.
 *
 * Note : La persistance est ici simulée en mémoire.
 *        Remplacez la liste par des appels JDBC / JPA selon votre couche DAO.
 */
public class CoursService implements ICoursChapitreService<Cours, Integer> {

    /* ── Stockage en mémoire (à remplacer par une couche DAO) ── */
    private final List<Cours> stockage = new ArrayList<>();
    private int nextId = 1;

    /* ── Singleton ──────────────────────────────────────────── */
    private static CoursService instance;

    private CoursService() {
        initialiserDonneesDemoAn();
    }

    public static CoursService getInstance() {
        if (instance == null) instance = new CoursService();
        return instance;
    }

    /* ── CRUD ────────────────────────────────────────────────── */

    @Override
    public Cours ajouter(Cours cours) {
        if (cours == null) throw new IllegalArgumentException("Le cours ne peut pas être null.");
        cours.setId(nextId++);
        stockage.add(cours);
        return cours;
    }

    @Override
    public Cours modifier(Cours cours) {
        if (cours == null) throw new IllegalArgumentException("Le cours ne peut pas être null.");
        for (int i = 0; i < stockage.size(); i++) {
            if (stockage.get(i).getId() == cours.getId()) {
                stockage.set(i, cours);
                return cours;
            }
        }
        throw new IllegalArgumentException("Cours introuvable avec l'id : " + cours.getId());
    }

    @Override
    public void supprimer(Integer id) {
        boolean supprime = stockage.removeIf(c -> c.getId() == id);
        if (!supprime) throw new IllegalArgumentException("Cours introuvable avec l'id : " + id);
    }

    @Override
    public Cours trouverParId(Integer id) {
        return stockage.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Cours> trouverTous() {
        return new ArrayList<>(stockage);
    }

    /* ── Méthodes métier spécifiques ────────────────────────── */

    /**
     * Filtre les cours par catégorie.
     */
    public List<Cours> trouverParCategorie(String categorie) {
        return stockage.stream()
                .filter(c -> c.getCategorie().equalsIgnoreCase(categorie))
                .collect(Collectors.toList());
    }

    /**
     * Filtre les cours par niveau.
     */
    public List<Cours> trouverParNiveau(String niveau) {
        return stockage.stream()
                .filter(c -> c.getNiveau().equalsIgnoreCase(niveau))
                .collect(Collectors.toList());
    }

    /**
     * Recherche par mot-clé dans le titre ou la description.
     */
    public List<Cours> rechercher(String motCle) {
        String mc = motCle.toLowerCase();
        return stockage.stream()
                .filter(c -> c.getTitre().toLowerCase().contains(mc)
                          || c.getDescription().toLowerCase().contains(mc))
                .collect(Collectors.toList());
    }

    /**
     * Met à jour la progression d'un cours (recalcul depuis ses chapitres).
     */
    public void mettreAJourProgression(int coursId) {
        Cours cours = trouverParId(coursId);
        if (cours != null) cours.recalculerProgression();
    }

    /* ── Données de démo ────────────────────────────────────── */

    private void initialiserDonneesDemoAn() {
        Cours c1 = new Cours(nextId++,
                "Advanced Machine Learning & Neural Networks",
                "Maîtrisez l'architecture des systèmes deep learning.",
                "Machine Learning", "Intermédiaire");
        c1.setProgression(0.67);

        Cours c2 = new Cours(nextId++,
                "Développement Web avec JavaFX",
                "Créez des interfaces graphiques modernes en Java.",
                "JavaFX", "Débutant");
        c2.setProgression(0.0);

        stockage.add(c1);
        stockage.add(c2);
    }
}
