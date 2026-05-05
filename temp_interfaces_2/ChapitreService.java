package com.example.services;

import com.example.entities.Chapitre;
import com.example.entities.Chapitre.Statut;
import com.example.interfaces.ICoursChapitreService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service gérant la logique métier des Chapitres.
 * Implémente {@link ICoursChapitreService} pour les opérations CRUD.
 */
public class ChapitreService implements ICoursChapitreService<Chapitre, Integer> {

    /* ── Stockage en mémoire ─────────────────────────────── */
    private final List<Chapitre> stockage = new ArrayList<>();
    private int nextId = 1;

    /* ── Singleton ───────────────────────────────────────── */
    private static ChapitreService instance;

    private ChapitreService() {
        initialiserDonneesDemoAn();
    }

    public static ChapitreService getInstance() {
        if (instance == null) instance = new ChapitreService();
        return instance;
    }

    /* ── CRUD ─────────────────────────────────────────────── */

    @Override
    public Chapitre ajouter(Chapitre chapitre) {
        if (chapitre == null) throw new IllegalArgumentException("Le chapitre ne peut pas être null.");
        chapitre.setId(nextId++);
        stockage.add(chapitre);
        return chapitre;
    }

    @Override
    public Chapitre modifier(Chapitre chapitre) {
        if (chapitre == null) throw new IllegalArgumentException("Le chapitre ne peut pas être null.");
        for (int i = 0; i < stockage.size(); i++) {
            if (stockage.get(i).getId() == chapitre.getId()) {
                stockage.set(i, chapitre);
                return chapitre;
            }
        }
        throw new IllegalArgumentException("Chapitre introuvable avec l'id : " + chapitre.getId());
    }

    @Override
    public void supprimer(Integer id) {
        boolean supprime = stockage.removeIf(c -> c.getId() == id);
        if (!supprime) throw new IllegalArgumentException("Chapitre introuvable avec l'id : " + id);
    }

    @Override
    public Chapitre trouverParId(Integer id) {
        return stockage.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Chapitre> trouverTous() {
        return new ArrayList<>(stockage);
    }

    /* ── Méthodes métier spécifiques ─────────────────────── */

    /**
     * Retourne tous les chapitres d'un cours, triés par numéro.
     */
    public List<Chapitre> trouverParCours(int coursId) {
        return stockage.stream()
                .filter(c -> c.getCoursId() == coursId)
                .sorted(Comparator.comparingInt(Chapitre::getNumero))
                .collect(Collectors.toList());
    }

    /**
     * Retourne les chapitres d'un cours ayant un statut donné.
     */
    public List<Chapitre> trouverParStatut(int coursId, Statut statut) {
        return trouverParCours(coursId).stream()
                .filter(c -> c.getStatut() == statut)
                .collect(Collectors.toList());
    }

    /**
     * Marque un chapitre comme TERMINE et déverrouille le suivant.
     *
     * @param chapitreId  Id du chapitre terminé
     */
    public void terminerChapitre(int chapitreId) {
        Chapitre courant = trouverParId(chapitreId);
        if (courant == null) return;

        courant.setStatut(Statut.TERMINE);

        // Déverrouiller le chapitre suivant du même cours
        trouverParCours(courant.getCoursId()).stream()
                .filter(c -> c.getNumero() == courant.getNumero() + 1)
                .findFirst()
                .ifPresent(suivant -> {
                    if (suivant.getStatut() == Statut.VERROUILLE) {
                        suivant.setStatut(Statut.EN_COURS);
                    }
                });

        // Recalcule la progression du cours parent
        CoursService.getInstance().mettreAJourProgression(courant.getCoursId());
    }

    /**
     * Démarre un chapitre (passe de VERROUILLE à EN_COURS).
     */
    public void demarrerChapitre(int chapitreId) {
        Chapitre c = trouverParId(chapitreId);
        if (c != null && c.getStatut() == Statut.VERROUILLE) {
            c.setStatut(Statut.EN_COURS);
        }
    }

    /**
     * Calcule la durée totale (en minutes) de tous les chapitres d'un cours.
     */
    public int dureeTotale(int coursId) {
        return trouverParCours(coursId).stream()
                .mapToInt(Chapitre::getDureeMinutes)
                .sum();
    }

    /* ── Données de démo ──────────────────────────────────── */

    private void initialiserDonneesDemoAn() {
        // Cours 1 — Machine Learning (coursId = 1)
        Object[][] data = {
            {1, "Introduction aux Réseaux de Neurones",  105, 12, true,  Statut.TERMINE},
            {2, "Réseaux Convolutifs (CNN)",             150, 18, true,  Statut.EN_COURS},
            {3, "Réseaux Récurrents & LSTMs",            195, 20, false, Statut.VERROUILLE},
            {4, "Mécanismes d'Attention & Transformers", 240, 24, true,  Statut.VERROUILLE},
            {5, "Réseaux Antagonistes Génératifs (GAN)", 170, 16, false, Statut.VERROUILLE},
        };

        for (int i = 0; i < data.length; i++) {
            Chapitre c = new Chapitre(
                nextId++,
                i + 1,
                (String)  data[i][1],
                "",
                (int)     data[i][2],
                (int)     data[i][3],
                (boolean) data[i][4],
                1   // coursId
            );
            c.setStatut((Statut) data[i][5]);
            stockage.add(c);
        }
    }
}
