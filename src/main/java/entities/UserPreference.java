package entities;

public class UserPreference {
    private long idPreference;
    private long idUtilisateur;
    private String typePolice = "OpenDyslexic";
    private int taillePolice = 18;
    private double interligne = 1.5;
    private double espacementLettres = 0.1;
    private double espacementMots = 1.0;
    private String couleurFond = "#FFFDE7";
    private String couleurTexte = "#333333";
    private boolean modeSyllabique = false;
    private boolean colorationSyllabes = false;
    private boolean focusLigne = false;
    private double vitesseAudio = 1.0;
    private boolean syntheseVocale = true;
    private boolean surlignageLecture = true;
    private boolean reduireAnimations = false;

    public UserPreference() {}

    // Getters and Setters
    public long getIdPreference() { return idPreference; }
    public void setIdPreference(long idPreference) { this.idPreference = idPreference; }

    public long getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(long idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getTypePolice() { return typePolice; }
    public void setTypePolice(String typePolice) { this.typePolice = typePolice; }

    public int getTaillePolice() { return taillePolice; }
    public void setTaillePolice(int taillePolice) { this.taillePolice = taillePolice; }

    public double getInterligne() { return interligne; }
    public void setInterligne(double interligne) { this.interligne = interligne; }

    public double getEspacementLettres() { return espacementLettres; }
    public void setEspacementLettres(double espacementLettres) { this.espacementLettres = espacementLettres; }

    public double getEspacementMots() { return espacementMots; }
    public void setEspacementMots(double espacementMots) { this.espacementMots = espacementMots; }

    public String getCouleurFond() { return couleurFond; }
    public void setCouleurFond(String couleurFond) { this.couleurFond = couleurFond; }

    public String getCouleurTexte() { return couleurTexte; }
    public void setCouleurTexte(String couleurTexte) { this.couleurTexte = couleurTexte; }

    public boolean isModeSyllabique() { return modeSyllabique; }
    public void setModeSyllabique(boolean modeSyllabique) { this.modeSyllabique = modeSyllabique; }

    public boolean isColorationSyllabes() { return colorationSyllabes; }
    public void setColorationSyllabes(boolean colorationSyllabes) { this.colorationSyllabes = colorationSyllabes; }

    public boolean isFocusLigne() { return focusLigne; }
    public void setFocusLigne(boolean focusLigne) { this.focusLigne = focusLigne; }

    public double getVitesseAudio() { return vitesseAudio; }
    public void setVitesseAudio(double vitesseAudio) { this.vitesseAudio = vitesseAudio; }

    public boolean isSyntheseVocale() { return syntheseVocale; }
    public void setSyntheseVocale(boolean syntheseVocale) { this.syntheseVocale = syntheseVocale; }

    public boolean isSurlignageLecture() { return surlignageLecture; }
    public void setSurlignageLecture(boolean surlignageLecture) { this.surlignageLecture = surlignageLecture; }

    public boolean isReduireAnimations() { return reduireAnimations; }
    public void setReduireAnimations(boolean reduireAnimations) { this.reduireAnimations = reduireAnimations; }
}
