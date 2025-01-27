package com.example.session;

import com.example.rbac.Role;
import java.util.*;
import java.time.LocalDateTime;

public class Session {
    private final String sessionId;
    private final String userId;
    private final Set<Role> activeRoles;
    private LocalDateTime lastAccessTime;
    private static final int SESSION_TIMEOUT_MINUTES = 5;

    public Session(String userId) {
        this(userId, UUID.randomUUID().toString());
    }

    public Session(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.activeRoles = new HashSet<>();
        updateLastAccessTime();
    }

    public void activate(Role role) {
        activeRoles.add(role);
        updateLastAccessTime();
    }

    /*public boolean isActive(Role role) {
        updateLastAccessTime();
        return activeRoles.contains(role);
    }*/

    public Set<Role> getActiveRoles() {
        updateLastAccessTime();
        return Collections.unmodifiableSet(activeRoles);
    }

    public boolean isExpired() {
        return LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES)
                .isAfter(lastAccessTime);
    }

    private void updateLastAccessTime() {
        lastAccessTime = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionId;
    }
}