package com.example.SonicCanopy.service.firebase.abstracts;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadClubImage(Long clubId, MultipartFile file);
    void deleteClubImage(String imageUrl);
}
