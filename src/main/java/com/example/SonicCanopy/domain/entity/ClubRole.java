package com.example.SonicCanopy.domain.entity;


import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
public enum ClubRole {
    OWNER(EnumSet.of(
            Privilege.MANAGE_EVENTS,
            Privilege.MANAGE_MEMBERS,
            Privilege.EDIT_CLUB_SETTINGS,
            Privilege.DELETE_CLUB,
            Privilege.COMMENT_ON_EVENTS,
            Privilege.DELETE_COMMENTS
    )),
    ADMIN(EnumSet.of(
            Privilege.MANAGE_EVENTS,
            Privilege.MANAGE_MEMBERS,
            Privilege.EDIT_CLUB_SETTINGS,
            Privilege.COMMENT_ON_EVENTS,
            Privilege.DELETE_COMMENTS
    )),
    NERD(EnumSet.of(
            Privilege.MANAGE_EVENTS,
            Privilege.COMMENT_ON_EVENTS
    )),
    MEMBER(EnumSet.of(
            Privilege.COMMENT_ON_EVENTS
    ));

    private final Set<Privilege> privileges;
    ClubRole(Set<Privilege> privileges) { this.privileges = privileges; }

    public boolean allowedTo(Privilege privilege) { return privileges.contains(privilege);

    }
}
