package com.example.security;

import com.example.AccessControl;

public class RoleBasedAccessControl {
    public boolean isAllowed(String role, String query) {
        return AccessControl.isAllowed(role, query);
    }
}