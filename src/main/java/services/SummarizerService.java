package services;

import java.util.concurrent.CompletableFuture;

/**
 * Service Intelligent de Résumée Pédagogique (NLP Simulation)
 */
public class SummarizerService {
    
    private static SummarizerService instance;
    
    private SummarizerService() {}
    
    public static SummarizerService getInstance() {
        if (instance == null) {
            instance = new SummarizerService();
        }
        return instance;
    }

    /**
     * Génère un résumé automatique du contenu d'un chapitre.
     * @param content Le texte intégral du chapitre
     * @return Un CompletableFuture contenant le résumé
     */
    public CompletableFuture<String> summarize(String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulation d'un temps de traitement IA
                Thread.sleep(1500);
                
                if (content == null || content.trim().isEmpty()) {
                    return "Aucun contenu à résumer.";
                }

                // Logique extractive simplifiée pour le MVP :
                // On prend les phrases les plus significatives (simulées ici)
                String[] sentences = content.split("[.!?]");
                if (sentences.length <= 2) {
                    return content; // Trop court pour être résumé
                }

                StringBuilder summary = new StringBuilder("✨ RÉSUMÉ GÉNÉRÉ PAR IA :\n\n");
                
                // On prend la première phrase (contexte)
                summary.append("📌 ").append(sentences[0].trim()).append(".\n\n");
                
                // On simule une extraction des points clés
                summary.append("🔹 Points clés identifiés :\n");
                int count = 0;
                for (int i = 1; i < sentences.length && count < 3; i++) {
                    String s = sentences[i].trim();
                    if (s.length() > 20) {
                        summary.append("  • ").append(s).append(".\n");
                        count++;
                    }
                }
                
                // On prend la dernière phrase (conclusion)
                summary.append("\n💡 Conclusion : ").append(sentences[sentences.length - 1].trim()).append(".");
                
                return summary.toString();
                
            } catch (InterruptedException e) {
                return "Erreur lors de la génération du résumé.";
            }
        });
    }
}
