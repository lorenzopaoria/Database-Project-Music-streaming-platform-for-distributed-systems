package com.example.rbac;

import java.util.List;
import java.util.ArrayList;

public class Role {
    private String name;
    private List<Permission> permissions;

    public Role(String name) {
        this.name = name;
        this.permissions = new ArrayList<>();
    }

    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public String name() {
        return name;
    }
}