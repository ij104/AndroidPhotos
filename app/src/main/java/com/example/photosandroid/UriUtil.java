package com.example.photosandroid;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * Helpers for loading content URIs safely when permission or files may be gone.
 */
public final class UriUtil {

    private UriUtil() {
    }

    public static boolean canOpenUri(Context context, Uri uri) {
        if (uri == null) {
            return false;
        }
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets the image from {@code uriString}, or a system placeholder if the URI cannot be opened.
     *
     * @param showToastIfMissing when true, shows a short toast when the image cannot be loaded
     */
    public static void loadPhotoIntoImageView(
            Context context,
            ImageView imageView,
            String uriString,
            boolean showToastIfMissing
    ) {
        if (uriString == null || uriString.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            return;
        }
        Uri uri = Uri.parse(uriString);
        if (canOpenUri(context, uri)) {
            imageView.setImageURI(uri);
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_report_image);
            if (showToastIfMissing) {
                Toast.makeText(
                        context,
                        "Image unavailable (permission revoked or file removed)",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}
