package com.example.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un Cours.
 */
public class Cours {

    private int id;
    private String titre;
    private String description;
    private String categorie;
    private String niveau;       // Débutant / Intermédiaire / Avancé
    private double progression;  // 0.0 → 1.0
    private List<Chapitre> chapitres;

    /* ── Constructeurs ─────────────────────────────────── */

    public Cours() {
        this.chapitres = new ArrayList<>();
    }

    public Cours(int id, String titre, String description,
                 String categorie, String niveau) {
        this.id          = id;
        this.titre       = titre;
        this.description = description;
        this.categorie   = categorie;
        this.niveau      = niveau;
        this.progression = 0.0;
        this.chapitres   = new ArrayList<>();
    }

    /* ── Getters / Setters ─────────────────────────────── */

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getTitre()                  { return titre; }
    public void setTitre(String titre)        { this.titre = titre; }

    public String getDescription()            { return description; }
    public void setDescription(String d)      { this.description = d; }

    public String getCategorie()              { return categorie; }
    public void setCategorie(String c)        { this.categorie = c; }

    public String getNiveau()                 { return niveau; }
    public void setNiveau(String n)           { this.niveau = n; }

    public double getProgression()            { return progression; }
    public void setProgression(double p)      { this.progression = p; }

    public List<Chapitre> getChapitres()      { return chapitres; }
    public void setChapitres(List<Chapitre> l){ this.chapitres = l; }

    /* ── Méthodes utilitaires ──────────────────────────── */

    public void ajouterChapitre(Chapitre c) {
        chapitres.add(c);
    }

    /** Recalcule la progression globale d'après l'état des chapitres. */
    public void recalculerProgression() {
        if (chapitres.isEmpty()) { progression = 0.0; return; }
        long termines = chapitres.stream()
                .filter(c -> c.getStatut() == Chapitre.Statut.TERMINE)
                .count();
        progression = (double) termines / chapitres.size();
    }

    @Override
    public String toString() {
        return "Cours{id=" + id + ", titre='" + titre + "', niveau='" + niveau + "'}";
    }
}
