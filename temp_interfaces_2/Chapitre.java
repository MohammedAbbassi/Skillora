package com.example.entities;

/**
 * Entité représentant un Chapitre appartenant à un Cours.
 */
public class Chapitre {

    /** Statut possible d'un chapitre. */
    public enum Statut {
        VERROUILLE,
        EN_COURS,
        TERMINE
    }

    private int    id;
    private int    numero;          // Ordre d'affichage (1, 2, 3…)
    private String titre;
    private String description;
    private int    dureeMinutes;    // Durée estimée en minutes
    private int    nombreLecons;
    private boolean avecQuiz;
    private Statut statut;
    private int    coursId;         // Clé étrangère vers Cours

    /* ── Constructeurs ─────────────────────────────────── */

    public Chapitre() {
        this.statut = Statut.VERROUILLE;
    }

    public Chapitre(int id, int numero, String titre, String description,
                    int dureeMinutes, int nombreLecons, boolean avecQuiz, int coursId) {
        this.id            = id;
        this.numero        = numero;
        this.titre         = titre;
        this.description   = description;
        this.dureeMinutes  = dureeMinutes;
        this.nombreLecons  = nombreLecons;
        this.avecQuiz      = avecQuiz;
        this.statut        = Statut.VERROUILLE;
        this.coursId       = coursId;
    }

    /* ── Getters / Setters ─────────────────────────────── */

    public int getId()                         { return id; }
    public void setId(int id)                  { this.id = id; }

    public int getNumero()                     { return numero; }
    public void setNumero(int numero)          { this.numero = numero; }

    public String getTitre()                   { return titre; }
    public void setTitre(String titre)         { this.titre = titre; }

    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }

    public int getDureeMinutes()               { return dureeMinutes; }
    public void setDureeMinutes(int d)         { this.dureeMinutes = d; }

    public int getNombreLecons()               { return nombreLecons; }
    public void setNombreLecons(int n)         { this.nombreLecons = n; }

    public boolean isAvecQuiz()                { return avecQuiz; }
    public void setAvecQuiz(boolean q)         { this.avecQuiz = q; }

    public Statut getStatut()                  { return statut; }
    public void setStatut(Statut statut)       { this.statut = statut; }

    public int getCoursId()                    { return coursId; }
    public void setCoursId(int coursId)        { this.coursId = coursId; }

    /* ── Méthodes utilitaires ──────────────────────────── */

    /** Retourne la durée formatée, ex : "1h 45m". */
    public String getDureeFormatee() {
        int heures  = dureeMinutes / 60;
        int minutes = dureeMinutes % 60;
        if (heures == 0) return minutes + "m";
        return heures + "h " + (minutes > 0 ? minutes + "m" : "");
    }

    @Override
    public String toString() {
        return "Chapitre{id=" + id + ", numero=" + numero +
               ", titre='" + titre + "', statut=" + statut + "}";
    }
}
