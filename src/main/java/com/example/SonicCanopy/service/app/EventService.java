package com.example.SonicCanopy.service.app;

import com.example.SonicCanopy.entities.Event;
import com.example.SonicCanopy.entities.User;
import com.example.SonicCanopy.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private EventRepository eventRepository;
    private ClubAuthorizationService clubAuthorizationService;

    public EventService(EventRepository eventRepository, ClubAuthorizationService clubAuthorizationService) {
        this.eventRepository = eventRepository;
        this.clubAuthorizationService = clubAuthorizationService;
    }

    public Event createEvent(Event event, User user) {
        clubAuthorizationService.authorizeEventManagement(event.getClub().getId(), user);
        return eventRepository.save(event);
    }

}
