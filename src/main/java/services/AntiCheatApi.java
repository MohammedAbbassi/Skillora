package services;

import services.AntiCheatService.Incident;
import services.AntiCheatService.Severity;

import java.io.IOException;

public class AntiCheatApi {

    private final AntiCheatService service = new AntiCheatService();

    public void startSession(String userName, String quizTitle) {
        service.startSession(userName, quizTitle);
    }

    public Incident recordTimerSuspension(long seconds, int questionNumber) {
        return service.record(
                "PAUSE_SYSTEME",
                "Pause ou suspension detectee pendant " + seconds + " secondes.",
                Severity.WARNING,
                questionNumber
        );
    }

    public Incident recordContextMenu(int questionNumber) {
        return service.record(
                "CLIC_DROIT",
                "Menu contextuel demande pendant le quiz.",
                Severity.WARNING,
                questionNumber
        );
    }

    public Incident recordRightClick(int questionNumber) {
        return service.record(
                "CLIC_DROIT",
                "Clic droit detecte pendant le quiz.",
                Severity.WARNING,
                questionNumber
        );
    }

    public Incident recordMouseExited(int questionNumber) {
        return service.record(
                "SOURIS_HORS_FENETRE",
                "La souris est sortie de la fenetre du quiz.",
                Severity.WARNING,
                questionNumber
        );
    }

    public Incident recordFocusLost(int questionNumber) {
        return service.record(
                "PERTE_FOCUS",
                "La fenetre du quiz a perdu le focus: changement de fenetre, Alt-Tab ou clic externe.",
                Severity.CRITICAL,
                questionNumber
        );
    }

    public Incident recordWindowMinimized(int questionNumber) {
        return service.record(
                "MINIMISATION",
                "La fenetre du quiz a ete minimisee.",
                Severity.CRITICAL,
                questionNumber
        );
    }

    public Incident recordScreenshotAttempt(int questionNumber) {
        return service.record(
                "CAPTURE_ECRAN",
                "Touche impression ecran detectee.",
                Severity.CRITICAL,
                questionNumber
        );
    }

    public Incident recordAltTabAttempt(int questionNumber) {
        return service.record(
                "ALT_TAB",
                "Tentative de changement de fenetre avec Alt-Tab.",
                Severity.CRITICAL,
                questionNumber
        );
    }

    public Incident recordForbiddenShortcut(String keyName, int questionNumber) {
        return service.record(
                "RACCOURCI_INTERDIT",
                "Raccourci clavier interdit detecte: " + keyName,
                Severity.WARNING,
                questionNumber
        );
    }

    public Incident recordDevToolsAttempt(int questionNumber) {
        return service.record(
                "OUTILS_DEV",
                "Tentative d'ouverture d'outils de developpement.",
                Severity.CRITICAL,
                questionNumber
        );
    }

    public boolean shouldFinishQuiz() {
        return service.shouldFinishQuiz();
    }

    public int getIncidentCount() {
        return service.getIncidentCount();
    }

    public int getCriticalIncidentCount() {
        return service.getCriticalIncidentCount();
    }

    public String getStatusText() {
        return service.getStatusText();
    }

    public String getSummary() {
        return service.getSummary();
    }

    public String writeReport() throws IOException {
        return service.writeReport();
    }
}
