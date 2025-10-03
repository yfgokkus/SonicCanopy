package com.example.SonicCanopy.controller;

import com.example.SonicCanopy.domain.dto.event.EventDto;
import com.example.SonicCanopy.domain.dto.global.ApiResponse;
import com.example.SonicCanopy.domain.dto.global.PagedResponse;
import com.example.SonicCanopy.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
@Slf4j
public class FeedController {

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<PagedResponse<EventDto>>> feed(
            @AuthenticationPrincipal User user
    ){
        return null
                ;
    }
}
