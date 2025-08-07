package com.example.SonicCanopy.entities;

import java.util.EnumSet;
import java.util.Set;

public enum ClubRole {
    OWNER,
    ADMIN,
    NERD,
    MEMBER;

    // Roles allowed to manage events
    private static final Set<ClubRole> MEMBER_MANAGEMENT_PRIVILEGED_ROLES =
            EnumSet.of(ADMIN, OWNER);

    private static final Set<ClubRole> EVENT_MANAGEMENT_PRIVILEGED_ROLES =
            EnumSet.of(ADMIN, OWNER, NERD);

    public boolean canManageMembers(){
        return MEMBER_MANAGEMENT_PRIVILEGED_ROLES.contains(this);
    }

    public boolean canManageEvents() {
        return EVENT_MANAGEMENT_PRIVILEGED_ROLES.contains(this);
    }
}