package utils;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class SocialAuthService {
    private final Dotenv dotenv = Dotenv.load();
    private static SocialAuthService instance;

    private SocialAuthService() {}

    public static SocialAuthService getInstance() {
        if (instance == null) {
            instance = new SocialAuthService();
        }
        return instance;
    }

    public CompletableFuture<JSONObject> authenticate(String provider) {
        return CompletableFuture.supplyAsync(() -> {
            OAuth20Service service;
            String scope;
            String userInfoUrl;

            if ("google".equalsIgnoreCase(provider)) {
                service = new ServiceBuilder(dotenv.get("GOOGLE_CLIENT_ID"))
                        .apiSecret(dotenv.get("GOOGLE_CLIENT_SECRET"))
                        .defaultScope("openid email profile")
                        .callback(dotenv.get("OAUTH_REDIRECT_URI"))
                        .build(GoogleApi20.instance());
                userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            } else if ("github".equalsIgnoreCase(provider)) {
                service = new ServiceBuilder(dotenv.get("GITHUB_CLIENT_ID"))
                        .apiSecret(dotenv.get("GITHUB_CLIENT_SECRET"))
                        .defaultScope("user:email")
                        .callback(dotenv.get("OAUTH_REDIRECT_URI"))
                        .build(GitHubApi.instance());
                userInfoUrl = "https://api.github.com/user";
            } else {
                throw new IllegalArgumentException("Unknown provider: " + provider);
            }

            try {
                String authorizationUrl = service.getAuthorizationUrl();
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(authorizationUrl));
                } else {
                    System.out.println("Please open this URL in your browser: " + authorizationUrl);
                }

                String code = waitForCallbackCode();
                OAuth2AccessToken accessToken = service.getAccessToken(code);

                OAuthRequest request = new OAuthRequest(Verb.GET, userInfoUrl);
                service.signRequest(accessToken, request);
                Response response = service.execute(request);

                return new JSONObject(response.getBody());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private String waitForCallbackCode() throws Exception {
        String redirectUri = dotenv.get("OAUTH_REDIRECT_URI");
        int port = 8080; // Default
        try {
            URI uri = new URI(redirectUri);
            if (uri.getPort() != -1) {
                port = uri.getPort();
            }
        } catch (Exception e) {
            System.err.println("Error parsing OAUTH_REDIRECT_URI port, using 8080: " + e.getMessage());
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            try (Socket socket = serverSocket.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = reader.readLine();
                if (line == null) return null;

                String code = null;
                if (line.contains("code=")) {
                    code = line.split("code=")[1].split(" ")[0];
                }

                OutputStream output = socket.getOutputStream();
                String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" +
                        "<html><body><h1>Authentication Successful!</h1><p>You can close this window now.</p></body></html>";
                output.write(response.getBytes());
                output.flush();

                return code;
            }
        }
    }
}
