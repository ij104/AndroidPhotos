package com.example.photosandroid.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Photo implements Serializable {
    private String uriString;
    private ArrayList<Tag> tags;

    public Photo(String uriString) {
        this.uriString = uriString;
        this.tags = new ArrayList<>();
    }

    public String getUriString() {
        return uriString;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    /** @return whether this photo already has a tag with the same type and value (case-insensitive). */
    public boolean hasTagMatching(String type, String value) {
        if (type == null || value == null) {
            return false;
        }
        for (Tag t : tags) {
            if (t.getType().equalsIgnoreCase(type) && t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
