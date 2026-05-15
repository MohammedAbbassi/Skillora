package services;

import entities.Chapitre;
import entities.Cours;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class NavigationService {

    private final ChapitreService chapitreService;

    public NavigationService() {
        this.chapitreService = new ChapitreService();
    }

    /**
     * Determines the next best chapter for the student within a specific course.
     */
    public Optional<Chapitre> getRecommendedNext(Cours course) {
        try {
            List<Chapitre> chapters = chapitreService.getByCours(course.getIdCours());
            if (chapters == null || chapters.isEmpty()) return Optional.empty();

            return chapters.stream()
                    .filter(c -> !c.isEstComplete())
                    .findFirst();
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Provides a "Smart Guidance" message based on the student's status.
     */
    public String getSmartGuidance(Cours course) {
        try {
            List<Chapitre> chapters = chapitreService.getByCours(course.getIdCours());
            long completed = chapters.stream().filter(Chapitre::isEstComplete).count();
            int total = chapters.size();

            if (total == 0) return "Commencez par ajouter des chapitres à ce cours !";
            if (completed == total) return "Félicitations ! Vous avez maîtrisé ce cours. Pourquoi ne pas explorer un nouveau sujet ?";
            
            double progress = (double) completed / total;
            if (progress < 0.3) return "Vous débutez bien. Concentrez-vous sur les bases pour construire des fondations solides.";
            if (progress < 0.7) return "Vous progressez bien ! Continuez sur cette lancée pour atteindre vos objectifs.";
            
            return "Presque fini ! Terminez les derniers chapitres pour valider vos compétences.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Analyse du parcours indisponible.";
        }
    }
}
