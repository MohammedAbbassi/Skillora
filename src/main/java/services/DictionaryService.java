package services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DictionaryService {

    private String lang = "fr";
    private final HttpClient client;
    private final Gson gson;

    public DictionaryService() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        this.gson = new Gson();
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    public CompletableFuture<WordDefinition> getDefinition(String word) {
        String cleanWord = word.replaceAll("[^a-zA-ZàâéèêëîïôûùÀÂÉÈÊËÎÏÔÛÙ-]", "").toLowerCase().trim();
        if (cleanWord.isEmpty()) return CompletableFuture.completedFuture(null);

        String apiUrl;
        if (lang.equals("fr")) {
            // Wikipedia Summary API is much more reliable for French
            apiUrl = "https://fr.wikipedia.org/api/rest_v1/page/summary/" + cleanWord;
        } else {
            apiUrl = "https://api.dictionaryapi.dev/api/v2/entries/" + lang + "/" + cleanWord;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("User-Agent", "Skillora-EduApp/1.0 (contact: support@skillora.com)")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 404) return null;
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erreur API : " + response.statusCode());
                    }

                    if (lang.equals("fr")) {
                        // Parse Wikipedia Response
                        JsonObject obj = gson.fromJson(response.body(), JsonObject.class);
                        WordDefinition def = new WordDefinition();
                        def.setWord(obj.get("title").getAsString());
                        
                        List<String> definitions = new ArrayList<>();
                        if (obj.has("extract")) {
                            definitions.add(obj.get("extract").getAsString());
                        }
                        def.setDefinitions(definitions);
                        return def;
                    } else {
                        // Parse Free Dictionary API Response
                        JsonArray array = gson.fromJson(response.body(), JsonArray.class);
                        if (array.size() == 0) return null;
                        // ... (keep previous parsing for EN)
                        JsonObject firstEntry = array.get(0).getAsJsonObject();
                        WordDefinition def = new WordDefinition();
                        def.setWord(firstEntry.get("word").getAsString());
                        // ... rest of the EN logic
                        if (firstEntry.has("phonetics") && firstEntry.getAsJsonArray("phonetics").size() > 0) {
                            JsonObject p = firstEntry.getAsJsonArray("phonetics").get(0).getAsJsonObject();
                            if (p.has("text")) def.setPhonetic(p.get("text").getAsString());
                        }

                        List<String> definitions = new ArrayList<>();
                        List<String> synonyms = new ArrayList<>();
                        JsonArray meanings = firstEntry.getAsJsonArray("meanings");
                        for (int i = 0; i < meanings.size(); i++) {
                            JsonObject meaning = meanings.get(i).getAsJsonObject();
                            JsonArray defs = meaning.getAsJsonArray("definitions");
                            for (int j = 0; j < defs.size(); j++) {
                                definitions.add(defs.get(j).getAsJsonObject().get("definition").getAsString());
                            }
                        }
                        def.setDefinitions(definitions);
                        return def;
                    }
                });
    }

    public static class WordDefinition {
        private String word;
        private String phonetic;
        private List<String> definitions = new ArrayList<>();
        private List<String> synonyms = new ArrayList<>();

        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getPhonetic() { return phonetic; }
        public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

        public List<String> getDefinitions() { return definitions; }
        public void setDefinitions(List<String> definitions) { this.definitions = definitions; }

        public List<String> getSynonyms() { return synonyms; }
        public void setSynonyms(List<String> synonyms) { this.synonyms = synonyms; }
    }
}
