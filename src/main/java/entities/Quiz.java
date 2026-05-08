package entities;

public class Quiz {

    public enum Matiere {
        JAVA, HTML, CSS, JAVASCRIPT, PHP, PYTHON, SQL
    }

    private int id;
    private String titre;
    private String description;
    private String niveau;
    private Matiere matiere;
    private int idCreateur;
    private String createurNom;

    public Quiz() {}

    public Quiz(String titre, String description, String niveau, Matiere matiere) {
        this.titre = titre;
        this.description = description;
        this.niveau = niveau;
        this.matiere = matiere;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public Matiere getMatiere() { return matiere; }
    public void setMatiere(Matiere matiere) { this.matiere = matiere; }
    public String getMatiereValue() { return matiere == null ? "" : matiere.name(); }
    public int getIdCreateur() { return idCreateur; }
    public void setIdCreateur(int idCreateur) { this.idCreateur = idCreateur; }
    public String getCreateurNom() { return createurNom; }
    public void setCreateurNom(String createurNom) { this.createurNom = createurNom; }

    @Override
    public String toString() { return titre; }
}
