package com.example.SonicCanopy.service.infrastructure.firebase.concretes;

import com.example.SonicCanopy.domain.exception.firebase.*;
import com.example.SonicCanopy.service.infrastructure.firebase.abstracts.StorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageException;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FirebaseStorageService implements StorageService {

	//TODO: move this to app.props
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final Bucket storageBucket;
    private final String bucketUrlPrefix;
    
    @Value("${firebase.club.image.max-size}")
    private DataSize MAX_FILE_SIZE;

    public FirebaseStorageService(Bucket storageBucket) {
        this.storageBucket = storageBucket;
        this.bucketUrlPrefix = "https://storage.googleapis.com/" + storageBucket.getName() + "/";
    }

    public String uploadClubImage(Long clubId, MultipartFile file) {
        //Client should validate
        if (file == null || file.isEmpty()) {
        	throw new InvalidFileException("File not found. Invalid file.");
        }

        if (file.getSize() > MAX_FILE_SIZE.toBytes()) {
            throw new ImageSizeLimitExceededException(
                "File size exceeds {}MB limit.", MAX_FILE_SIZE.toMegabytes()
            );
        }

        String detectedType = getFileTypeValidated(file);

        try {
            String uuid = UUID.randomUUID().toString();
            String safeFileName = uuid + getFileExtension(detectedType);
            String objectName = "club_images/" + clubId + "/" + safeFileName;

            storageBucket.create(objectName, file.getBytes(), detectedType);

            return bucketUrlPrefix + objectName;

        } catch (IOException e) {
            log.error("Failed to upload image to Firebase Storage. {}", e.getMessage());
            throw new ImageUploadException("Image upload failed. Try again later");
        }
    }

    @Override
    public void deleteClubImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new InvalidImageUrlException("Invalid image url");
        }

        if (!imageUrl.startsWith(bucketUrlPrefix)) {
            log.error("Attempted to delete an image with an invalid URL prefix: {}", imageUrl);
            throw new InvalidImageUrlException("Invalid image url");
        }

        String objectPath = imageUrl.substring(bucketUrlPrefix.length());

        try {
        	Blob blob = storageBucket.get(objectPath);
            
            if(blob == null || !blob.exists()) {
            	log.warn("Blob doesn't exist. Attempted to delete a non-existent image: {}", objectPath);
            	throw new ImageDeletionException("Failed to delete image. Image url can be invalid.");
            }
            
            if (!blob.delete()) {
                log.error("Failed to delete image. The storage service returned false.");
                throw new ImageDeletionException("Storage service failed to delete the image");
            }
            
            log.info("Successfully deleted image: {}", objectPath);
        } catch (StorageException e) {
        	log.error("Failed to delete image. Storage service error: {}",e.getMessage());
            throw new ImageDeletionException("Failed to delete image");
        }
    }

    private String getFileTypeValidated(MultipartFile file) {
        String detectedType;
        try (InputStream inputStream = file.getInputStream()) {
            Tika tika = new Tika();
            detectedType = tika.detect(inputStream);
        } catch (IOException e) {
            log.error("Error while reading file type with Tika: {}", e.getMessage());
            throw new ImageUploadException("Image upload failed", e);
        }
        //Client should validate
        if (!ALLOWED_CONTENT_TYPES.contains(detectedType.toLowerCase())) {
            log.error("Image type {} is not supported.", detectedType);
            throw new UnsupportedFileTypeException("Unsupported file type: " + detectedType);
        }
        return detectedType;
    }

    private String getFileExtension(String type) {
        return switch (type) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        };
    }
}
