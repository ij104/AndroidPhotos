package com.example.photosandroid.model;

import java.io.Serializable;

public class Tag implements Serializable {
    private String type;
    private String value;

    public Tag(String type, String value) {
        this.type = type.toLowerCase();
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
