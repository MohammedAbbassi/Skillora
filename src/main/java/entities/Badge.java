package entities;

/**
 * Badge — represents a user's achievement level based on their activity score.
 *
 * Score rules:
 *   +1  creating a post
 *   +2  writing a comment
 *   +3  receiving a like on a post
 *
 * Badge thresholds:
 *   DEBUTANT  (Beginner) : score < 10
 *   ACTIF     (Active)   : 10 <= score <= 30
 *   PRO                  : score > 30
 */
public enum Badge {

    DEBUTANT("Debutant",  "🟢", 0,  9),
    ACTIF   ("Actif",     "🔵", 10, 30),
    PRO     ("Pro",       "🔴", 31, Integer.MAX_VALUE);

    public final String label;   // display name
    public final String emoji;   // colored circle emoji
    public final int    minScore;
    public final int    maxScore;

    Badge(String label, String emoji, int minScore, int maxScore) {
        this.label    = label;
        this.emoji    = emoji;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * Returns the badge that matches the given score.
     * Always returns a valid badge (defaults to DEBUTANT).
     */
    public static Badge fromScore(int score) {
        if (score >= PRO.minScore)    return PRO;
        if (score >= ACTIF.minScore)  return ACTIF;
        return DEBUTANT;
    }

    /** Full display string, e.g. "🔵 Actif" */
    public String display() {
        return emoji + " " + label;
    }

    @Override
    public String toString() {
        return display();
    }
}
