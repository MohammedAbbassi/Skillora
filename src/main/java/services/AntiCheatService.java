package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AntiCheatService {

    public enum Severity {
        WARNING,
        CRITICAL
    }

    public static class Incident {
        private final LocalDateTime occurredAt;
        private final String type;
        private final String message;
        private final Severity severity;
        private final int questionNumber;

        private Incident(String type, String message, Severity severity, int questionNumber) {
            this.occurredAt = LocalDateTime.now();
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.questionNumber = questionNumber;
        }

        public LocalDateTime getOccurredAt() {
            return occurredAt;
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public Severity getSeverity() {
            return severity;
        }

        public int getQuestionNumber() {
            return questionNumber;
        }
    }

    private static final int MAX_CRITICAL_INCIDENTS = 3;
    private static final int MAX_TOTAL_INCIDENTS = 8;
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final List<Incident> incidents = new ArrayList<>();
    private LocalDateTime startedAt;
    private String userName = "Utilisateur";
    private String quizTitle = "Quiz";
    private String reportPath;

    public void startSession(String userName, String quizTitle) {
        this.startedAt = LocalDateTime.now();
        this.userName = isBlank(userName) ? "Utilisateur" : userName;
        this.quizTitle = isBlank(quizTitle) ? "Quiz" : quizTitle;
        this.reportPath = null;
        incidents.clear();
    }

    public Incident record(String type, String message, Severity severity, int questionNumber) {
        Incident incident = new Incident(type, message, severity, Math.max(1, questionNumber));
        incidents.add(incident);
        return incident;
    }

    public boolean shouldFinishQuiz() {
        return getCriticalIncidentCount() >= MAX_CRITICAL_INCIDENTS
                || getIncidentCount() >= MAX_TOTAL_INCIDENTS;
    }

    public int getIncidentCount() {
        return incidents.size();
    }

    public int getCriticalIncidentCount() {
        int count = 0;
        for (Incident incident : incidents) {
            if (incident.getSeverity() == Severity.CRITICAL) {
                count++;
            }
        }
        return count;
    }

    public String getStatusText() {
        int count = getIncidentCount();
        if (count == 0) {
            return "Anti-triche : 0 incident";
        }
        return "Anti-triche : " + count + " incident(s)";
    }

    public String getSummary() {
        if (incidents.isEmpty()) {
            return "Aucun incident anti-triche detecte.";
        }
        return getIncidentCount() + " incident(s), dont " + getCriticalIncidentCount() + " critique(s).";
    }

    public String getReportPath() {
        return reportPath;
    }

    public List<Incident> getIncidents() {
        return new ArrayList<>(incidents);
    }

    public String writeReport() throws IOException {
        Path reportDir = Path.of("exports", "anti-cheat");
        Files.createDirectories(reportDir);

        String fileName = "anti_cheat_" + sanitize(quizTitle) + "_" + LocalDateTime.now().format(FILE_DATE) + ".txt";
        Path reportFile = reportDir.resolve(fileName);

        List<String> lines = new ArrayList<>();
        lines.add("Rapport anti-triche");
        lines.add("Utilisateur: " + userName);
        lines.add("Quiz: " + quizTitle);
        lines.add("Debut: " + (startedAt == null ? "Non disponible" : DISPLAY_DATE.format(startedAt)));
        lines.add("Fin: " + DISPLAY_DATE.format(LocalDateTime.now()));
        lines.add("Resume: " + getSummary());
        lines.add("");
        if (incidents.isEmpty()) {
            lines.add("Aucun incident.");
        } else {
            for (Incident incident : incidents) {
                lines.add(DISPLAY_DATE.format(incident.getOccurredAt())
                        + " | question " + incident.getQuestionNumber()
                        + " | " + incident.getSeverity()
                        + " | " + incident.getType()
                        + " | " + incident.getMessage());
            }
        }

        Files.write(reportFile, lines);
        reportPath = reportFile.toAbsolutePath().toString();
        return reportPath;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sanitize(String value) {
        String safe = isBlank(value) ? "quiz" : value.trim().toLowerCase();
        safe = safe.replaceAll("[^a-z0-9]+", "_");
        safe = safe.replaceAll("_+", "_");
        safe = safe.replaceAll("^_|_$", "");
        return safe.isBlank() ? "quiz" : safe;
    }
}
