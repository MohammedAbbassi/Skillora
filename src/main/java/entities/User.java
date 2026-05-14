package entities;

import java.sql.Timestamp;

public class User {
    private int idUtilisateur;
    private String nomUtilisateur;
    private String email;
    private String motDePasse;
    private String prenom;
    private String nom;
    private String photoProfil;
    private Role role;
    private boolean estActif = true;
    private Timestamp dateCreation;
    private Timestamp dateModification;
    // ── Badge system ───────────────────────────────────────────
    private int   score = 0;
    private Badge badge = Badge.DEBUTANT;

    public User() {
    }

    /** Minimal constructor used by FeedController mock */
    public User(String nomUtilisateur, String email, String motDePasse, Role role) {
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
    }

    /** Full constructor used by UserService */
    public User(String nomUtilisateur, String email, String motDePasse, String prenom, String nom) {
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.prenom = prenom;
        this.nom = nom;
    }

    public User(int idUtilisateur, String nomUtilisateur, String email, String motDePasse, Role role, String photoProfil) {
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.photoProfil = photoProfil;
    }

    // ── Getters / Setters ──────────────────────────

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    /** Returns full name if available, otherwise username */
    public String getDisplayName() {
        if (prenom != null && nom != null) return prenom + " " + nom;
        if (prenom != null) return prenom;
        return nomUtilisateur != null ? nomUtilisateur : "";
    }

    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isEtudiant() { return role == Role.ETUDIANT; }

    // ── Badge / Score getters & setters ───────────────────────
    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
        this.badge = Badge.fromScore(score); // always keep badge in sync
    }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }

    /** Convenience: badge display string e.g. "🔵 Actif" */
    public String getBadgeDisplay() {
        return badge != null ? badge.display() : Badge.DEBUTANT.display();
    }

    @Override
    public String toString() {
        return "User{id=" + idUtilisateur + ", username='" + nomUtilisateur + "', role=" + role + '}';
    }
}
