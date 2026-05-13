package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResult {

    private int id;
    private int quizId;
    private String userName;
    private String quizTitle;
    private int finalPoints;
    private int totalPoints;
    private int correctAnswers;
    private int totalQuestions;
    private int bonusPoints;
    private int antiCheatIncidentCount;
    private int antiCheatCriticalCount;
    private boolean terminatedByAntiCheat;
    private String antiCheatSummary;
    private String antiCheatReportPath;
    private LocalDateTime completedAt;
    private List<QuizResultDetail> details = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public int getFinalPoints() {
        return finalPoints;
    }

    public void setFinalPoints(int finalPoints) {
        this.finalPoints = finalPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public int getAntiCheatIncidentCount() {
        return antiCheatIncidentCount;
    }

    public void setAntiCheatIncidentCount(int antiCheatIncidentCount) {
        this.antiCheatIncidentCount = antiCheatIncidentCount;
    }

    public int getAntiCheatCriticalCount() {
        return antiCheatCriticalCount;
    }

    public void setAntiCheatCriticalCount(int antiCheatCriticalCount) {
        this.antiCheatCriticalCount = antiCheatCriticalCount;
    }

    public boolean isTerminatedByAntiCheat() {
        return terminatedByAntiCheat;
    }

    public void setTerminatedByAntiCheat(boolean terminatedByAntiCheat) {
        this.terminatedByAntiCheat = terminatedByAntiCheat;
    }

    public String getAntiCheatSummary() {
        return antiCheatSummary;
    }

    public void setAntiCheatSummary(String antiCheatSummary) {
        this.antiCheatSummary = antiCheatSummary;
    }

    public String getAntiCheatReportPath() {
        return antiCheatReportPath;
    }

    public void setAntiCheatReportPath(String antiCheatReportPath) {
        this.antiCheatReportPath = antiCheatReportPath;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<QuizResultDetail> getDetails() {
        return details;
    }

    public void setDetails(List<QuizResultDetail> details) {
        this.details = details == null ? new ArrayList<>() : details;
    }

    public int getIncorrectAnswers() {
        return Math.max(0, totalQuestions - correctAnswers);
    }

    public double getSuccessPercentage() {
        if (totalPoints <= 0) {
            return 0;
        }
        return finalPoints * 100.0 / totalPoints;
    }
}
