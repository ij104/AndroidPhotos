package com.example.photosandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.storage.DataStorage;

public class MainActivity extends AppCompatActivity {

    private PhotoManager photoManager;
    private ArrayAdapter<Album> albumAdapter;
    private ListView albumListView;
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

        albumListView = findViewById(R.id.albumListView);
        Button createAlbumButton = findViewById(R.id.createAlbumButton);
        searchPhotosButton = findViewById(R.id.searchPhotosButton);

        attachAlbumAdapter();

        createAlbumButton.setOnClickListener(v -> showCreateAlbumDialog());

        searchPhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            Album selectedAlbum = photoManager.getAlbums().get(position);
            Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
            intent.putExtra("albumName", selectedAlbum.getName());
            startActivity(intent);
        });

        albumListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Album selectedAlbum = photoManager.getAlbums().get(position);
            showAlbumOptionsDialog(selectedAlbum);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoManager = DataStorage.loadData(this);
        attachAlbumAdapter();
    }

    /**
     * Always bind the list to the current {@link PhotoManager#getAlbums()} instance so deletes
     * and reloads from disk update the ListView immediately.
     */
    private void attachAlbumAdapter() {
        albumAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                photoManager.getAlbums()
        );
        albumListView.setAdapter(albumAdapter);
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
                        albumAdapter.notifyDataSetChanged();
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
                    albumAdapter.notifyDataSetChanged();

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
                    albumAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
