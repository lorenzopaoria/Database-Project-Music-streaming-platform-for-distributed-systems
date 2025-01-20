package com.example.rbac;

public class Permission {
    private final String operation;
    private final String object;

    public Permission(String operation, String object) {
        this.operation = operation;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Permission)) return false;
        Permission other = (Permission) obj;
        return operation.equals(other.operation) && object.equals(other.object);
    }
}