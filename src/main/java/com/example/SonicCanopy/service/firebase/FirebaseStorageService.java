package com.example.SonicCanopy.service.firebase;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@Service
public class FirebaseStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    public String uploadClubProfileImage(Long clubId, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }


        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }


        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }


        String objectName = "club_images/" + clubId + "/profile.jpg";


        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.create(objectName, file.getBytes(), contentType);

        // publicly accessible (optional)
        //blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        return "https://storage.googleapis.com/" + bucket.getName() + "/" + objectName;
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String bucketName = StorageClient.getInstance().bucket().getName();
        String objectPath = URI.create(imageUrl).getPath().replaceFirst("/" + bucketName + "/", "");

        Blob blob = StorageClient.getInstance().bucket().get(objectPath);
        if (blob != null) {
            blob.delete();
        }
    }
}
