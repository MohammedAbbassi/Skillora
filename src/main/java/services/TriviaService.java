package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour interagir avec l'API Open Trivia Database.
 */
public class TriviaService {

    private static final String API_URL = "https://opentdb.com/api.php?amount=10&type=multiple";
    private final HttpClient client;
    private final Gson gson;

    public TriviaService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Récupère 10 questions de culture générale depuis l'API.
     */
    public CompletableFuture<List<TriviaQuestion>> fetchQuestions() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erreur API Trivia : " + response.statusCode());
                    }

                    JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                    int responseCode = jsonResponse.get("response_code").getAsInt();
                    
                    if (responseCode != 0) {
                        throw new RuntimeException("L'API Trivia a retourné un code d'erreur : " + responseCode);
                    }

                    JsonArray results = jsonResponse.getAsJsonArray("results");
                    List<TriviaQuestion> questions = new ArrayList<>();

                    for (int i = 0; i < results.size(); i++) {
                        JsonObject obj = results.get(i).getAsJsonObject();
                        String question = decodeHtml(obj.get("question").getAsString());
                        String correctAnswer = decodeHtml(obj.get("correct_answer").getAsString());
                        
                        List<String> options = new ArrayList<>();
                        options.add(correctAnswer);
                        JsonArray incorrects = obj.getAsJsonArray("incorrect_answers");
                        for (int j = 0; j < incorrects.size(); j++) {
                            options.add(decodeHtml(incorrects.get(j).getAsString()));
                        }
                        
                        // Mélanger les options pour que la bonne réponse ne soit pas toujours la première
                        Collections.shuffle(options);
                        
                        questions.add(new TriviaQuestion(question, options, correctAnswer));
                    }
                    return questions;
                });
    }

    // Décodage simple des entités HTML (ex: &quot; -> ")
    private String decodeHtml(String input) {
        return input.replace("&quot;", "\"")
                    .replace("&#039;", "'")
                    .replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">");
    }

    /**
     * Modèle de données pour une question Trivia.
     */
    public static class TriviaQuestion {
        private final String question;
        private final List<String> options;
        private final String correctAnswer;

        public TriviaQuestion(String question, List<String> options, String correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public String getCorrectAnswer() { return correctAnswer; }
    }
}
