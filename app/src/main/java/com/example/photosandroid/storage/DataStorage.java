package com.example.photosandroid.storage;

import android.content.Context;

import com.example.photosandroid.model.PhotoManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class DataStorage {
    private static final String FILE_NAME = "photos_data.ser";

    public static void saveData(Context context, PhotoManager manager) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(manager);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PhotoManager loadData(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            PhotoManager manager = (PhotoManager) ois.readObject();
            ois.close();
            return manager;
        } catch (Exception e) {
            return new PhotoManager();
        }
    }
}
