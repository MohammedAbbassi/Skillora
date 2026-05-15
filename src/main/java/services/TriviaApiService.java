package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entities.QuizQuestion;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.text.StringEscapeUtils;

public class TriviaApiService {

    private static final String API_URL = "https://opentdb.com/api.php?amount=10&category=19";
    private final HttpClient client;
    private final Gson gson;

    public TriviaApiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public CompletableFuture<List<QuizQuestion>> fetchQuizzes() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return new ArrayList<>();
                    
                    JsonObject root = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray results = root.getAsJsonArray("results");
                    
                    List<QuizQuestion> questions = new ArrayList<>();
                    for (int i = 0; i < results.size(); i++) {
                        JsonObject obj = results.get(i).getAsJsonObject();
                        
                        String qText = StringEscapeUtils.unescapeHtml4(obj.get("question").getAsString());
                        String correct = StringEscapeUtils.unescapeHtml4(obj.get("correct_answer").getAsString());
                        JsonArray incorrects = obj.getAsJsonArray("incorrect_answers");
                        
                        List<String> options = new ArrayList<>();
                        options.add(correct);
                        for (int j = 0; j < incorrects.size(); j++) {
                            options.add(StringEscapeUtils.unescapeHtml4(incorrects.get(j).getAsString()));
                        }
                        Collections.shuffle(options);
                        
                        questions.add(new QuizQuestion(qText, options, correct));
                    }
                    return questions;
                });
    }
}
