package com.example.photosandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.storage.DataStorage;

public class MainActivity extends AppCompatActivity implements AlbumRecyclerAdapter.Listener {

    private PhotoManager photoManager;
    private AlbumRecyclerAdapter albumRecyclerAdapter;
    private RecyclerView albumRecyclerView;
    private Button searchPhotosButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 24, systemBars.top + 24,
                    systemBars.right + 24, systemBars.bottom + 24);
            return insets;
        });

        photoManager = DataStorage.loadData(this);

        albumRecyclerView = findViewById(R.id.albumRecyclerView);
        Button createAlbumButton = findViewById(R.id.createAlbumButton);
        searchPhotosButton = findViewById(R.id.searchPhotosButton);

        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        albumRecyclerAdapter = new AlbumRecyclerAdapter(this);
        albumRecyclerView.setAdapter(albumRecyclerAdapter);
        albumRecyclerAdapter.setPhotoManager(photoManager);

        createAlbumButton.setOnClickListener(v -> showCreateAlbumDialog());

        searchPhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoManager = DataStorage.loadData(this);
        albumRecyclerAdapter.setPhotoManager(photoManager);
    }

    @Override
    public void onAlbumClick(Album album) {
        Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
        intent.putExtra("albumName", album.getName());
        startActivity(intent);
    }

    @Override
    public void onAlbumLongClick(Album album) {
        showAlbumOptionsDialog(album);
    }

    private void showCreateAlbumDialog() {
        EditText input = new EditText(this);
        input.setHint("Album name");

        new AlertDialog.Builder(this)
                .setTitle("Create Album")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String albumName = input.getText().toString();

                    boolean added = photoManager.addAlbum(albumName);

                    if (added) {
                        DataStorage.saveData(this, photoManager);
                        albumRecyclerAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Album created", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Invalid or duplicate album name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAlbumOptionsDialog(Album album) {
        String[] options = {"Rename Album", "Delete Album"};

        new AlertDialog.Builder(this)
                .setTitle(album.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showRenameAlbumDialog(album);
                    } else if (which == 1) {
                        showDeleteAlbumDialog(album);
                    }
                })
                .show();
    }

    private void showRenameAlbumDialog(Album album) {
        EditText input = new EditText(this);
        input.setText(album.getName());

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Album existingAlbum = photoManager.getAlbumByName(newName);
                    if (existingAlbum != null && existingAlbum != album) {
                        Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    album.setName(newName);
                    DataStorage.saveData(this, photoManager);
                    albumRecyclerAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteAlbumDialog(Album album) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete " + album.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    photoManager.deleteAlbum(album);
                    DataStorage.saveData(this, photoManager);
                    albumRecyclerAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
