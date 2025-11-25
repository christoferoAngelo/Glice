package br.edu.fatecgru.glice.network;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "de4j4ibb6"); // Substitua pelo seu Cloud Name
            config.put("api_key", "792215385188622");       // Substitua pela sua API Key
            config.put("api_secret", "LB10k0h9NhImokGxXyp9GLwxJpk"); // Substitua pelo seu API Secret
            config.put("secure", true);
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
}
