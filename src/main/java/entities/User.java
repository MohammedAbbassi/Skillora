package entities;

import java.sql.Timestamp;

public class User {
    private int id;
    private String nomUtilisateur;
    private String email;
    private String motDePasse;
    private String prenom;
    private String nom;
    private String photoProfil;
    private String role;
    private String pays;
    private boolean estActif;
    private Timestamp dateCreation;
    private Timestamp dateModification;
    
    // Progress fields
    private int xpPoints;
    private int rankedPoints;
    private int streakDays;
    private int certificatesCount;
    private boolean estEnLigne;
    private String resetToken;
    private Timestamp resetTokenExpiry;

    public User() {
    }

    public User(String nomUtilisateur, String email, String motDePasse, String prenom, String nom) {
        this.nomUtilisateur = nomUtilisateur;
        this.email = email;
        this.motDePasse = motDePasse;
        this.prenom = prenom;
        this.nom = nom;
        this.xpPoints = 0;
        this.rankedPoints = 0;
        this.streakDays = 0;
        this.certificatesCount = 0;
        this.pays = "Tunisia";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }

    public boolean isEstActif() { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }

    public boolean isEstEnLigne() { return estEnLigne; }
    public void setEstEnLigne(boolean estEnLigne) { this.estEnLigne = estEnLigne; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }

    public int getRankedPoints() { return rankedPoints; }
    public void setRankedPoints(int rankedPoints) { this.rankedPoints = rankedPoints; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getCertificatesCount() { return certificatesCount; }
    public void setCertificatesCount(int certificatesCount) { this.certificatesCount = certificatesCount; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public Timestamp getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(Timestamp resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', xp=" + xpPoints + ", streak=" + streakDays + "}";
    }
}