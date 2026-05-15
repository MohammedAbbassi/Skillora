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
<<<<<<< Updated upstream
    private boolean estActif = true;
=======
    private String pays;
    private boolean estActif = true;
    private int xpPoints;
    private int rankedPoints;
    private int streakDays;
    private int certificatesCount;
    private boolean estEnLigne;
    private String resetToken;
    private Timestamp resetTokenExpiry;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    public User(int idUtilisateur, String nomUtilisateur, String email, String motDePasse, Role role, String photoProfil) {
        this.idUtilisateur = idUtilisateur;
=======
    public User(String nomUtilisateur, String email, String motDePasse, Role role) {
>>>>>>> Stashed changes
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
<<<<<<< Updated upstream
        this.photoProfil = photoProfil;
    }

    // ── Getters / Setters ──────────────────────────

=======
    }

    public User(int idUtilisateur, String nomUtilisateur, String email, String motDePasse, Role role, String photoProfil) {
        this.idUtilisateur = idUtilisateur;
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.photoProfil = photoProfil;
    }

    public int getId() { return idUtilisateur; }
    public void setId(int id) { this.idUtilisateur = id; }

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
    public void setRole(String role) { 
        if (role == null || role.isBlank()) {
            this.role = Role.ETUDIANT;
        } else {
            try {
                this.role = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.role = Role.ETUDIANT;
            }
        }
    }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
>>>>>>> Stashed changes

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

<<<<<<< Updated upstream
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
=======
    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    /** Convenience: badge display string e.g. "🔵 Actif" */
    public String getBadgeDisplay() {
        return badge != null ? badge.display() : Badge.DEBUTANT.display();
    }
=======
    public boolean isEstEnLigne() { return estEnLigne; }
    public void setEstEnLigne(boolean estEnLigne) { this.estEnLigne = estEnLigne; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Timestamp getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Timestamp resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
>>>>>>> Stashed changes

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    public String getDisplayName() {
        if (prenom != null && nom != null) return prenom + " " + nom;
        if (prenom != null) return prenom;
        return nomUtilisateur != null ? nomUtilisateur : "";
    }

    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isEtudiant() { return role == Role.ETUDIANT; }

    public int getScore() { return score; }
    public void setScore(int score) {
        this.score = score;
        this.badge = Badge.fromScore(score);
    }

    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }

    public String getBadgeDisplay() {
        return badge != null ? badge.display() : Badge.DEBUTANT.display();
    }

    @Override
    public String toString() {
        return "User{id=" + idUtilisateur + ", username='" + nomUtilisateur + "', role=" + role + '}';
    }
}
