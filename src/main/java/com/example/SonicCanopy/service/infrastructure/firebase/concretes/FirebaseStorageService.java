package com.example.SonicCanopy.service.infrastructure.firebase.concretes;

import com.example.SonicCanopy.domain.exception.club.ImageDeletionException;
import com.example.SonicCanopy.domain.exception.club.ImageUploadException;
import com.example.SonicCanopy.service.infrastructure.firebase.abstracts.StorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageException;
import com.google.firebase.cloud.StorageClient;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FirebaseStorageService implements StorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    @Override
    public String uploadClubImage(Long clubId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File size exceeds 5MB limit");
            }

            Tika tika = new Tika();
            String detectedType = tika.detect(file.getInputStream());

            if (!ALLOWED_CONTENT_TYPES.contains(detectedType.toLowerCase())) {
                throw new IllegalArgumentException("Unsupported file type: " + detectedType);
            }

            //Filename Sanitization (Potential Risk)
            String uuid = UUID.randomUUID().toString();
            String safeFileName = uuid + getFileExtension(detectedType);
            String objectName = "club_images/" + clubId + "/" + safeFileName;

            Bucket bucket = StorageClient.getInstance().bucket();
            Blob blob = bucket.create(objectName, file.getBytes(), detectedType); // add image to the bucket

            return "https://storage.googleapis.com/" + bucket.getName() + "/" + objectName;

        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image to Firebase Storage", e);
        }
    }

    @Override
    public void deleteClubImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String bucketName = StorageClient.getInstance().bucket().getName();
            String prefix = "https://storage.googleapis.com/" + bucketName + "/";
            if (!imageUrl.startsWith(prefix)) {
                throw new IllegalArgumentException("Invalid image URL format");
            }

            String objectPath = imageUrl.substring(prefix.length());
            Blob blob = StorageClient.getInstance().bucket().get(objectPath);

            if (blob != null && blob.exists()) {
                boolean success = blob.delete();
                if (!success) {
                    throw new ImageDeletionException("Failed to delete image: blob.delete() returned false");
                }
            }
        } catch (StorageException e) {
            throw new ImageDeletionException("Failed to delete image from Firebase Storage", e);
        }
    }

    private String getFileExtension(String type) {
        // Mapping MIME type to proper file extension
        return switch (type) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        };
    }
}
