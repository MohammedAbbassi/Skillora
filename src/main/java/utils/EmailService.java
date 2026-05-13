package utils;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import io.github.cdimascio.dotenv.Dotenv;

public class EmailService {
    private final Resend resend;
    private static EmailService instance;

    private EmailService() {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("RESEND_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("your_resend_api_key_here")) {
            System.err.println("CRITICAL: RESEND_API_KEY is not set in .env file!");
            this.resend = null;
        } else {
            this.resend = new Resend(apiKey);
        }
    }

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    public boolean sendPasswordResetEmail(String toEmail, String token) {
        if (resend == null) {
            System.err.println("Cannot send email: Resend client not initialized (check API key).");
            return false;
        }
        
        CreateEmailOptions sendEmailRequest = CreateEmailOptions.builder()
                .from("Skillora <onboarding@resend.dev>")
                .to(toEmail)
                .subject("Reset your Skillora password")
                .html("<strong>Reset your password</strong><br>Use the following token to reset your password: <b>" + token + "</b><br>This token will expire in 1 hour.")
                .build();

        try {
            CreateEmailResponse data = resend.emails().send(sendEmailRequest);
            return data.getId() != null;
        } catch (ResendException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
