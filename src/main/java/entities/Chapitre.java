package entities;

public class Chapitre {

    private int id_chapitre;
    private String titre;
    private String contenu;
    private int ordre;
    private int duree;
    private String pdf_url;
    private int cours_id;

    // Constructeur vide
    public Chapitre() {}

    // Constructeur complet
    public Chapitre(int id_chapitre, String titre, String contenu, int ordre,
                    int duree, String pdf_url, int cours_id) {
        this.id_chapitre = id_chapitre;
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
        this.duree = duree;
        this.pdf_url = pdf_url;
        this.cours_id = cours_id;
    }

    // Constructeur sans id (pour insertion)
    public Chapitre(String titre, String contenu, int ordre,
                    int duree, String pdf_url, int cours_id) {
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
        this.duree = duree;
        this.pdf_url = pdf_url;
        this.cours_id = cours_id;
    }

    // Getters & Setters

    public int getId_chapitre() {
        return id_chapitre;
    }

    public void setId_chapitre(int id_chapitre) {
        this.id_chapitre = id_chapitre;
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

    public String getPdf_url() {
        return pdf_url;
    }

    public void setPdf_url(String pdf_url) {
        this.pdf_url = pdf_url;
    }

    public int getCours_id() {
        return cours_id;
    }

    public void setCours_id(int cours_id) {
        this.cours_id = cours_id;
    }

    @Override
    public String toString() {
        return "Chapitre{" +
                "id_chapitre=" + id_chapitre +
                ", titre='" + titre + '\'' +
                ", contenu='" + contenu + '\'' +
                ", ordre=" + ordre +
                ", duree=" + duree +
                ", pdf_url='" + pdf_url + '\'' +
                ", cours_id=" + cours_id +
                '}';
    }
}