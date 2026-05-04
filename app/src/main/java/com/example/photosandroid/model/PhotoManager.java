package com.example.photosandroid.model;

import java.io.Serializable;
import java.util.ArrayList;

public class PhotoManager implements Serializable {
    private ArrayList<Album> albums;

    public PhotoManager() {
        albums = new ArrayList<>();
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }

    public boolean addAlbum(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (getAlbumByName(name) != null) {
            return false;
        }

        albums.add(new Album(name.trim()));
        return true;
    }

    public void deleteAlbum(Album album) {
        albums.remove(album);
    }

    public Album getAlbumByName(String name) {
        if (name == null) {
            return null;
        }

        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(name.trim())) {
                return album;
            }
        }

        return null;
    }
}
