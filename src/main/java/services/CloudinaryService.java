package services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class CloudinaryService {
    private static CloudinaryService instance;
    private final Cloudinary cloudinary;

    private CloudinaryService() {
        Dotenv dotenv = Dotenv.load();
        String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
        String apiKey = dotenv.get("CLOUDINARY_API_KEY");
        String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");

        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public static synchronized CloudinaryService getInstance() {
        if (instance == null) {
            instance = new CloudinaryService();
        }
        return instance;
    }

    public String uploadImage(File file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
        ));
        return (String) uploadResult.get("secure_url");
    }

    public String uploadImage(byte[] imageBytes, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image"
        ));
        return (String) uploadResult.get("secure_url");
    }
}
