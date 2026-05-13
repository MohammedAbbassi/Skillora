package entities;

import java.sql.Timestamp;

public class UserPreferences {
    private long idPreference;
    private long idUtilisateur;
    private String typePolice;
    private int taillePolice;
    private double interligne;
    private double espacementLettres;
    private String couleurFond;
    private String couleurTexte;
    private boolean syntheseVocale;
    private boolean surlignageLecture;
    private boolean reduireAnimations;

    private String userName;
    private String userRole;
    private String userEmail;
    private int streakDays;
    private int xpPoints;
    private int rankedPoints;
    private String rank;
    private String fontFamily;

    public UserPreferences() {
    }

    public UserPreferences(long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
        this.typePolice = "OPENDYSLEXIC";
        this.taillePolice = 18;
        this.interligne = 1.5;
        this.espacementLettres = 0.1;
        this.couleurFond = "#FFFDE7";
        this.couleurTexte = "#333333";
        this.syntheseVocale = true;
        this.surlignageLecture = true;
        this.reduireAnimations = false;
        this.fontFamily = "Georgia";
        this.streakDays = 0;
        this.xpPoints = 0;
        this.rankedPoints = 0;
        this.rank = "Bronze";
    }

    public static UserPreferences defaults() {
        UserPreferences p = new UserPreferences();
        p.typePolice = "SEGOEUI";
        p.fontFamily = "Georgia";
        p.taillePolice = 15;
        p.interligne = 1.6;
        p.espacementLettres = 0.0;
        p.couleurFond = "#FFFFFF";
        p.couleurTexte = "#1A1A2E";
        p.reduireAnimations = false;
        p.syntheseVocale = false;
        p.surlignageLecture = false;
        p.userName = "Guest";
        p.userRole = "Learner";
        p.userEmail = "";
        p.streakDays = 0;
        p.xpPoints = 0;
        p.rankedPoints = 0;
        p.rank = "Bronze";
        return p;
    }

    public static UserPreferences dyslexiaFriendly() {
        UserPreferences p = new UserPreferences();
        p.fontFamily = "OpenDyslexic";
        p.taillePolice = 18;
        p.interligne = 1.8;
        p.espacementLettres = 1.2;
        p.couleurFond = "#FDF6E3";
        p.couleurTexte = "#333333";
        p.reduireAnimations = true;
        p.syntheseVocale = false;
        p.surlignageLecture = false;
        p.userName = "Guest";
        p.userRole = "Learner";
        p.userEmail = "";
        p.streakDays = 0;
        p.xpPoints = 0;
        p.rankedPoints = 0;
        p.rank = "Bronze";
        return p;
    }

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

    public String getCouleurFond() { return couleurFond; }
    public void setCouleurFond(String couleurFond) { this.couleurFond = couleurFond; }

    public String getCouleurTexte() { return couleurTexte; }
    public void setCouleurTexte(String couleurTexte) { this.couleurTexte = couleurTexte; }

    public boolean isSyntheseVocale() { return syntheseVocale; }
    public void setSyntheseVocale(boolean syntheseVocale) { this.syntheseVocale = syntheseVocale; }

    public boolean isSurlignageLecture() { return surlignageLecture; }
    public void setSurlignageLecture(boolean surlignageLecture) { this.surlignageLecture = surlignageLecture; }

    public boolean isReduireAnimations() { return reduireAnimations; }
    public void setReduireAnimations(boolean reduireAnimations) { this.reduireAnimations = reduireAnimations; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }

    public int getRankedPoints() { return rankedPoints; }
    public void setRankedPoints(int rankedPoints) { this.rankedPoints = rankedPoints; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }

    public int getFontSize() { return taillePolice; }
    public void setFontSize(int fontSize) { this.taillePolice = fontSize; }

    public String getBackgroundColor() { return couleurFond; }
    public void setBackgroundColor(String backgroundColor) { this.couleurFond = backgroundColor; }

    public String getTextColor() { return couleurTexte; }
    public void setTextColor(String textColor) { this.couleurTexte = textColor; }

    public boolean isReduceAnimations() { return reduireAnimations; }
    public void setReduceAnimations(boolean reduceAnimations) { this.reduireAnimations = reduceAnimations; }

    public boolean isSpeechSynthesis() { return syntheseVocale; }
    public void setSpeechSynthesis(boolean speechSynthesis) { this.syntheseVocale = speechSynthesis; }

    public double getLineSpacing() { return interligne; }
    public void setLineSpacing(double lineSpacing) { this.interligne = lineSpacing; }

    public double getLetterSpacing() { return espacementLettres; }
    public void setLetterSpacing(double letterSpacing) { this.espacementLettres = letterSpacing; }
}