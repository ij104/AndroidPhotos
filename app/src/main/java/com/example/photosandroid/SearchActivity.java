package com.example.photosandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.PhotoManager;
import com.example.photosandroid.model.Tag;
import com.example.photosandroid.storage.DataStorage;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private PhotoManager photoManager;

    private RadioGroup firstTagTypeGroup;
    private RadioGroup operatorGroup;
    private RadioGroup secondTagTypeGroup;
    private EditText firstValueEditText;
    private EditText secondValueEditText;
    private Button backToAlbumsButton;
    private Button runSearchButton;
    private GridView searchResultsGridView;

    private ArrayList<SearchResult> searchResults;
    private SearchResultsAdapter searchResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        int contentPad = (int) (16 * getResources().getDisplayMetrics().density + 0.5f);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.searchMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left + contentPad,
                    systemBars.top + contentPad,
                    systemBars.right + contentPad,
                    systemBars.bottom + contentPad
            );
            return insets;
        });

        photoManager = DataStorage.loadData(this);

        firstTagTypeGroup = findViewById(R.id.firstTagTypeGroup);
        operatorGroup = findViewById(R.id.operatorGroup);
        secondTagTypeGroup = findViewById(R.id.secondTagTypeGroup);
        firstValueEditText = findViewById(R.id.firstValueEditText);
        secondValueEditText = findViewById(R.id.secondValueEditText);
        backToAlbumsButton = findViewById(R.id.backToAlbumsButton);
        runSearchButton = findViewById(R.id.runSearchButton);
        searchResultsGridView = findViewById(R.id.searchResultsGridView);

        backToAlbumsButton.setOnClickListener(v -> finish());

        searchResults = new ArrayList<>();
        searchResultsAdapter = new SearchResultsAdapter();
        searchResultsGridView.setAdapter(searchResultsAdapter);

        runSearchButton.setOnClickListener(v -> runSearch());

        searchResultsGridView.setOnItemClickListener((parent, view, position, id) -> {
            SearchResult result = searchResults.get(position);

            Intent intent = new Intent(SearchActivity.this, PhotoDisplayActivity.class);
            intent.putExtra("albumName", result.album.getName());
            intent.putExtra("photoIndex", result.photoIndex);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        photoManager = DataStorage.loadData(this);
    }

    private String getFirstTagType() {
        return firstTagTypeGroup.getCheckedRadioButtonId() == R.id.firstLocationRadio
                ? "location"
                : "person";
    }

    private String getSecondTagType() {
        return secondTagTypeGroup.getCheckedRadioButtonId() == R.id.secondLocationRadio
                ? "location"
                : "person";
    }

    private String getOperator() {
        return operatorGroup.getCheckedRadioButtonId() == R.id.operatorOrRadio ? "OR" : "AND";
    }

    private void runSearch() {
        photoManager = DataStorage.loadData(this);

        String firstType = getFirstTagType();
        String firstValue = firstValueEditText.getText().toString().trim();

        String operator = getOperator();

        String secondType = getSecondTagType();
        String secondValue = secondValueEditText.getText().toString().trim();

        if (firstValue.isEmpty()) {
            Toast.makeText(this, "Enter at least one tag value", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean useSecondCondition = !secondValue.isEmpty();

        searchResults.clear();

        for (Album album : photoManager.getAlbums()) {
            ArrayList<Photo> photos = album.getPhotos();

            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);

                boolean firstMatches = photoMatchesTag(photo, firstType, firstValue);
                boolean secondMatches = false;

                if (useSecondCondition) {
                    secondMatches = photoMatchesTag(photo, secondType, secondValue);
                }

                boolean shouldAdd;

                if (!useSecondCondition) {
                    shouldAdd = firstMatches;
                } else if ("AND".equals(operator)) {
                    shouldAdd = firstMatches && secondMatches;
                } else {
                    shouldAdd = firstMatches || secondMatches;
                }

                if (shouldAdd && !resultsContain(album, i)) {
                    searchResults.add(new SearchResult(album, photo, i));
                }
            }
        }

        searchResultsAdapter.notifyDataSetChanged();

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No matching photos found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, searchResults.size() + " matching photo(s) found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean resultsContain(Album album, int photoIndex) {
        for (SearchResult r : searchResults) {
            if (r.album.getName().equalsIgnoreCase(album.getName()) && r.photoIndex == photoIndex) {
                return true;
            }
        }
        return false;
    }

    private boolean photoMatchesTag(Photo photo, String type, String value) {
        for (Tag tag : photo.getTags()) {
            if (tag.getType().equalsIgnoreCase(type)
                    && tag.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    private static class SearchResult {
        Album album;
        Photo photo;
        int photoIndex;

        SearchResult(Album album, Photo photo, int photoIndex) {
            this.album = album;
            this.photo = photo;
            this.photoIndex = photoIndex;
        }
    }

    private class SearchResultsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchResults.size();
        }

        @Override
        public Object getItem(int position) {
            return searchResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(SearchActivity.this);
                int gridWidth = parent.getWidth();
                if (gridWidth <= 0) {
                    gridWidth = getResources().getDisplayMetrics().widthPixels;
                }
                int size = gridWidth / 3;
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            Photo photo = searchResults.get(position).photo;
            imageView.setImageURI(Uri.parse(photo.getUriString()));

            return imageView;
        }
    }
}
