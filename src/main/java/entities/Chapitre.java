package entities;

public class Chapitre {

    private int idChapitre;
    private String titre;
    private String contenu;
    private int duree;
    private String pdfUrl;
    private int idCours;
    private String typeExplication;
    private String explication;
    private String quizJson;
    private String niveau; // FACILE, MOYEN, DIFFICILE
    private String youtubeLink;
    private boolean estComplete;
    private String remarques;
    private String fichiersTp;
    private int idChapitreRevision;
    private String imageUrl;

    // Constructeur vide
    public Chapitre() {}

    // Constructeur complet
    public Chapitre(int idChapitre, String titre, String contenu,
                    int duree, String pdfUrl, int idCours) {
        this.idChapitre = idChapitre;
        this.titre = titre;
        this.contenu = contenu;
        this.duree = duree;
        this.pdfUrl = pdfUrl;
        this.idCours = idCours;
    }

    // Getters & Setters

    public int getIdChapitre() { return idChapitre; }
    public void setIdChapitre(int idChapitre) { this.idChapitre = idChapitre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public int getIdCours() { return idCours; }
    public void setIdCours(int idCours) { this.idCours = idCours; }

    public String getTypeExplication() { return typeExplication; }
    public void setTypeExplication(String typeExplication) { this.typeExplication = typeExplication; }

    public String getExplication() { return explication; }
    public void setExplication(String explication) { this.explication = explication; }

    public String getQuizJson() { return quizJson; }
    public void setQuizJson(String quizJson) { this.quizJson = quizJson; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getYoutubeLink() { return youtubeLink; }
    public void setYoutubeLink(String youtubeLink) { this.youtubeLink = youtubeLink; }

    public boolean isEstComplete() { return estComplete; }
    public void setEstComplete(boolean estComplete) { this.estComplete = estComplete; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }

    public String getFichiersTp() { return fichiersTp; }
    public void setFichiersTp(String fichiersTp) { this.fichiersTp = fichiersTp; }

    public int getIdChapitreRevision() { return idChapitreRevision; }
    public void setIdChapitreRevision(int idChapitreRevision) { this.idChapitreRevision = idChapitreRevision; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Compatibilité database aliases
    public int getId_chapitre() { return getIdChapitre(); }
    public void setId_chapitre(int idChapitre) { setIdChapitre(idChapitre); }

    public String getPdf_url() { return getPdfUrl(); }
    public void setPdf_url(String pdfUrl) { setPdfUrl(pdfUrl); }

    public int getCours_id() { return getIdCours(); }
    public void setCours_id(int idCours) { setIdCours(idCours); }

    @Override
    public String toString() {
        return "Chapitre{" +
                "idChapitre=" + idChapitre +
                ", titre='" + titre + '\'' +
                '}';
    }
}
