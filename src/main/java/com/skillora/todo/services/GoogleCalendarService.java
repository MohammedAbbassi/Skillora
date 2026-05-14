package com.skillora.todo.services;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.skillora.todo.entities.Task;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.MyDatabase;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleCalendarService {
    private static GoogleCalendarService instance;
    private final Dotenv dotenv = Dotenv.load();
    private final Connection cnx = MyDatabase.getInstance().getCnx();
    private String cachedAccessToken;
    private boolean connected;

    private GoogleCalendarService() {
        ensureTokenTable();
        checkExistingToken();
    }

    public static GoogleCalendarService getInstance() {
        if (instance == null) instance = new GoogleCalendarService();
        return instance;
    }

    public boolean isConnected() { return connected; }

    private void ensureTokenTable() {
        String sql = "CREATE TABLE IF NOT EXISTS todo_calendar_tokens (id INT PRIMARY KEY DEFAULT 1, access_token TEXT, refresh_token TEXT, token_expiry BIGINT, calendar_id VARCHAR(255))";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { System.err.println("GCal.ensureTokenTable: " + e.getMessage()); }
    }

    private void checkExistingToken() {
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery("SELECT access_token, refresh_token, token_expiry FROM todo_calendar_tokens WHERE id=1")) {
            if (rs.next() && rs.getString("refresh_token") != null) {
                cachedAccessToken = rs.getString("access_token");
                connected = true;
            }
        } catch (SQLException e) { /* no token yet */ }
    }

    private OAuth20Service buildService(String scope) {
        return new ServiceBuilder(dotenv.get("GOOGLE_CLIENT_ID"))
                .apiSecret(dotenv.get("GOOGLE_CLIENT_SECRET"))
                .defaultScope(scope)
                .callback(dotenv.get("OAUTH_REDIRECT_URI"))
                .build(GoogleApi20.instance());
    }

    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                OAuth20Service service = buildService("https://www.googleapis.com/auth/calendar email profile");
                String authUrl = service.getAuthorizationUrl();
                if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(authUrl));

                String code = waitForCallback();
                if (code == null) return false;

                OAuth2AccessToken token = service.getAccessToken(code);
                cachedAccessToken = token.getAccessToken();

                try (PreparedStatement ps = cnx.prepareStatement(
                        "REPLACE INTO todo_calendar_tokens (id, access_token, refresh_token, token_expiry) VALUES (1,?,?,?)")) {
                    ps.setString(1, token.getAccessToken());
                    ps.setString(2, token.getRefreshToken() != null ? token.getRefreshToken() : "");
                    ps.setLong(3, token.getExpiresIn() != null ? System.currentTimeMillis() + token.getExpiresIn() * 1000 : 0);
                    ps.executeUpdate();
                }
                connected = true;
                return true;
            } catch (Exception e) {
                System.err.println("GCal.connect: " + e.getMessage());
                return false;
            }
        });
    }

    public void disconnect() {
        try (Statement stmt = cnx.createStatement()) {
            stmt.execute("DELETE FROM todo_calendar_tokens WHERE id=1");
        } catch (SQLException e) { /* ignore */ }
        cachedAccessToken = null;
        connected = false;
    }

    private JSONObject callApi(String endpoint, String method, String body) throws Exception {
        if (cachedAccessToken == null) throw new Exception("Not connected");
        URL url = new URL("https://www.googleapis.com/calendar/v3/" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Authorization", "Bearer " + cachedAccessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = conn.getResponseCode();
        String respBody;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                code >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = br.readLine()) != null) sb.append(l);
            respBody = sb.toString();
        }
        if (code >= 200 && code < 300) {
            return respBody.isEmpty() ? new JSONObject() : new JSONObject(respBody);
        }
        if (code == 401 && refreshToken()) {
            return callApi(endpoint, method, body);
        }
        throw new Exception("API error " + code + ": " + respBody);
    }

    private boolean refreshToken() {
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery("SELECT refresh_token FROM todo_calendar_tokens WHERE id=1")) {
            if (!rs.next()) return false;
            String refreshToken = rs.getString("refresh_token");
            if (refreshToken == null || refreshToken.isEmpty()) return false;

            URL url = new URL("https://oauth2.googleapis.com/token");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            String params = "client_id=" + dotenv.get("GOOGLE_CLIENT_ID")
                    + "&client_secret=" + dotenv.get("GOOGLE_CLIENT_SECRET")
                    + "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                    + "&grant_type=refresh_token";
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JSONObject json = new JSONObject(br.readLine());
            cachedAccessToken = json.getString("access_token");
            try (PreparedStatement ps = cnx.prepareStatement("UPDATE todo_calendar_tokens SET access_token=?, token_expiry=? WHERE id=1")) {
                ps.setString(1, cachedAccessToken);
                ps.setLong(2, System.currentTimeMillis() + (json.has("expires_in") ? json.getLong("expires_in") * 1000 : 3600000));
                ps.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            System.err.println("GCal.refreshToken: " + e.getMessage());
            return false;
        }
    }

    public void syncTask(Task t, int userId) throws Exception {
        if (!connected) throw new Exception("Google Calendar not connected");
        String eventId = getEventIdForTask(t.getId());
        if (eventId != null) {
            deleteEvent(eventId);
            clearTaskEventId(t.getId());
        }
        if (t.getDeadline() == null || t.isCompleted()) return;

        JSONObject event = buildTaskEvent(t);
        JSONObject created = callApi("calendars/primary/events", "POST", event.toString());
        String newEventId = created.getString("id");
        saveEventMapping(t.getId(), newEventId);
    }

    public void deleteTaskEvent(int taskId) throws Exception {
        String eventId = getEventIdForTask(taskId);
        if (eventId == null) return;
        deleteEvent(eventId);
        clearTaskEventId(taskId);
    }

    public List<Map<String, String>> fetchEvents(LocalDate start, LocalDate end) throws Exception {
        List<Map<String, String>> events = new ArrayList<>();
        if (!connected) return events;

        String timeMin = start.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);
        String timeMax = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_INSTANT);
        String params = "calendars/primary/events?timeMin=" + URLEncoder.encode(timeMin, StandardCharsets.UTF_8)
                + "&timeMax=" + URLEncoder.encode(timeMax, StandardCharsets.UTF_8)
                + "&orderBy=startTime&singleEvents=true";

        JSONObject resp = callApi(params, "GET", null);
        JSONArray items = resp.optJSONArray("items");
        if (items == null) return events;

        for (int i = 0; i < items.length(); i++) {
            JSONObject ev = items.getJSONObject(i);
            Map<String, String> map = new HashMap<>();
            map.put("id", ev.optString("id"));
            map.put("summary", ev.optString("summary"));
            map.put("description", ev.optString("description"));
            map.put("start", ev.optJSONObject("start") != null ? ev.optJSONObject("start").optString("dateTime", ev.optJSONObject("start").optString("date")) : "");
            map.put("source", "google");
            events.add(map);
        }
        return events;
    }

    private JSONObject buildTaskEvent(Task t) {
        JSONObject event = new JSONObject();
        event.put("summary", "[Skillora] " + t.getTitle());
        event.put("description", (t.getDescription() != null ? t.getDescription() + "\n\n" : "")
                + "Category: " + t.getCategory() + "\nPriority: " + t.getPriority());

        JSONObject start = new JSONObject();
        start.put("date", t.getDeadline().toString());
        start.put("timeZone", "UTC");
        event.put("start", start);
        event.put("end", start);

        JSONArray reminders = new JSONArray();
        JSONObject reminder = new JSONObject();
        reminder.put("method", "popup");
        reminder.put("minutes", 30);
        reminders.put(reminder);
        event.put("reminders", new JSONObject().put("useDefault", false).put("overrides", reminders));
        return event;
    }

    private void deleteEvent(String eventId) throws Exception {
        callApi("calendars/primary/events/" + eventId, "DELETE", null);
    }

    private String getEventIdForTask(int taskId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT event_id FROM todo_calendar_events WHERE task_id=?")) {
            ps.setInt(1, taskId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("event_id");
        } catch (SQLException e) { /* ignore */ }
        return null;
    }

    private void saveEventMapping(int taskId, String eventId) {
        try (PreparedStatement ps = cnx.prepareStatement("CREATE TABLE IF NOT EXISTS todo_calendar_events (task_id INT PRIMARY KEY, event_id VARCHAR(255))")) {
            ps.execute();
        } catch (SQLException e) { /* ignore */ }
        try (PreparedStatement ps = cnx.prepareStatement("REPLACE INTO todo_calendar_events (task_id, event_id) VALUES (?,?)")) {
            ps.setInt(1, taskId);
            ps.setString(2, eventId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("GCal.saveEventMapping: " + e.getMessage()); }
    }

    private void clearTaskEventId(int taskId) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM todo_calendar_events WHERE task_id=?")) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        } catch (SQLException e) { /* ignore */ }
    }

    public void syncAllTasks(int userId) {
        TaskService taskService = new TaskService();
        List<Task> tasks = taskService.getByUser(userId);
        for (Task t : tasks) {
            try { syncTask(t, userId); }
            catch (Exception e) { System.err.println("GCal.syncAll: " + e.getMessage() + " for task " + t.getId()); }
        }
    }

    private String waitForCallback() throws Exception {
        int port = 8080;
        try { port = new URI(dotenv.get("OAUTH_REDIRECT_URI")).getPort(); } catch (Exception e) { /* use default */ }
        if (port == -1) port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setSoTimeout(120000);
            try (Socket socket = serverSocket.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = reader.readLine();
                if (line == null) return null;
                String code = null;
                if (line.contains("code=")) {
                    int idx = line.indexOf("code=") + 5;
                    int end = line.indexOf(" ", idx);
                    int amp = line.indexOf("&", idx);
                    if (amp != -1 && (end == -1 || amp < end)) end = amp;
                    code = end != -1 ? line.substring(idx, end) : line.substring(idx);
                    code = URLDecoder.decode(code, StandardCharsets.UTF_8);
                }
                try (OutputStream os = socket.getOutputStream()) {
                    os.write(("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                            "<html><body style='text-align:center;padding-top:50px;font-family:sans-serif;'>" +
                            "<h2 style='color:#4F46E5;'>Skillora Calendar Connected!</h2>" +
                            "<p>You can close this window.</p></body></html>").getBytes());
                    os.flush();
                }
                return code;
            }
        }
    }
}
