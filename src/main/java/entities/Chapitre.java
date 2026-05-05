package entities;

public class Chapitre {

    private int idChapitre;
    private String titre;
    private String contenu;
    private int ordre;
    private int duree;
    private String pdfUrl;
    private int idCours;
    private String resume;
    private String typeExplication;
    private String explication;
    private String quizJson;

    // Nouveaux champs pour Skillora Premium
    private String niveau; // FACILE, MOYEN, DIFFICILE
    private String videoUrl;
    private String imageUrl;
    private boolean estComplete;

    // Constructeur vide
    public Chapitre() {}

    // Constructeur complet
    public Chapitre(int idChapitre, String titre, String contenu, int ordre,
                    int duree, String pdfUrl, int idCours) {
        this.idChapitre = idChapitre;
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
        this.duree = duree;
        this.pdfUrl = pdfUrl;
        this.idCours = idCours;
    }

    // Constructeur sans id (pour insertion)
    public Chapitre(String titre, String contenu, int ordre,
                    int duree, String pdfUrl, int idCours) {
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
        this.duree = duree;
        this.pdfUrl = pdfUrl;
        this.idCours = idCours;
    }

    // Getters & Setters

    public int getIdChapitre() {
        return idChapitre;
    }

    public void setIdChapitre(int idChapitre) {
        this.idChapitre = idChapitre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public int getIdCours() {
        return idCours;
    }

    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getTypeExplication() {
        return typeExplication;
    }

    public void setTypeExplication(String typeExplication) {
        this.typeExplication = typeExplication;
    }

    public String getExplication() {
        return explication;
    }

    public void setExplication(String explication) {
        this.explication = explication;
    }

    public String getQuizJson() {
        return quizJson;
    }

    public void setQuizJson(String quizJson) {
        this.quizJson = quizJson;
    }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isEstComplete() { return estComplete; }
    public void setEstComplete(boolean estComplete) { this.estComplete = estComplete; }

    public int getId_chapitre() {
        return getIdChapitre();
    }

    public void setId_chapitre(int idChapitre) {
        setIdChapitre(idChapitre);
    }

    public String getPdf_url() {
        return getPdfUrl();
    }

    public void setPdf_url(String pdfUrl) {
        setPdfUrl(pdfUrl);
    }

    public int getCours_id() {
        return getIdCours();
    }

    public void setCours_id(int idCours) {
        setIdCours(idCours);
    }

    @Override
    public String toString() {
        return "Chapitre{" +
                "idChapitre=" + idChapitre +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", ordre=" + ordre +
                ", duree=" + duree +
                ", pdfUrl='" + pdfUrl + '\'' +
                ", idCours=" + idCours +
                ", resume='" + resume + '\'' +
                ", typeExplication='" + typeExplication + '\'' +
                ", explication='" + explication + '\'' +
                ", quizJson='" + quizJson + '\'' +
                '}';
    }
}
