# PhotosAndroid

## Team Members

- Ivan Jiang
- Eric Loebs

## Project Description

PhotosAndroid is an Android photo album application written in **Java**. The app lets users create and manage photo albums, add photos from the device using the system document picker, tag photos with **person** and **location** values, move photos between albums, and search photos by those tags (including **AND** / **OR**). Data is saved **locally** so albums, photo URIs, and tags stay available after closing and reopening the app.

This project follows the Android Photos assignment style: **RecyclerView** on the home screen, **`GridView`** for album and search thumbnails, dialogs, Java model classes, and **Java serialization** for persistence (no cloud backend).

## Features Implemented

### Album management

- View all albums on the home screen (`MainActivity`) in a **RecyclerView**.
- Create, rename, and delete albums.
- Duplicate album names are blocked when creating or renaming.
- Long-press an album for rename/delete options; tap to open.
- Hints on the home screen explain tap vs. long-press.

### Photo management

- Open an album (`AlbumActivity`) to see its name and a thumbnail grid.
- Add photos with **ACTION_OPEN_DOCUMENT** (`image/*`) and persist read URI permission where supported.
- Remove a photo (long-press thumbnail → **Remove Photo**).
- Move a photo to another album (long-press → **Move Photo**); the same `Photo` object (URI + tags) is moved.
- **Back to albums** returns to the home list.

### Photo display

- Tap a thumbnail to open `PhotoDisplayActivity` with a larger image.
- **Previous** / **Next** move within the current album; title shows **Photo X of Y**.
- Edge insets and **Back to album** help navigation on gesture devices.
- Tags for the current photo are listed under the image.

### Tags

- Add **person** or **location** tags from the photo screen; remove tags via **Remove tag…**
- Empty tag values show a toast and are not added.
- **Duplicate tags** (same type and value, case-insensitive) are rejected with a toast.
- Tags are stored on each `Photo` and saved with serialization.

### Search

- **Search Photos** from the home screen opens `SearchActivity`.
- First and second conditions use **Person** / **Location** radio buttons and value fields (`AutoCompleteTextView`).
- One tag only: fill the first value; leave the second blank (second condition ignored).
- **AND** / **OR** applies when the second value is non-empty.
- Matching is **case-insensitive** for both tag type and value.
- Results are thumbnails; tap opens `PhotoDisplayActivity` with the correct album and index (**index is re-resolved by URI** after a fresh load so it stays correct if the album changed).
- Autocomplete suggests existing values for the selected tag type (all albums); dropdown filtering is case-insensitive for prefixes.

### Data

- Changes are written through `DataStorage.saveData` after the main user actions (albums, photos, tags, moves, search does not need to save).

## How to run the app

1. Clone or open the **PhotosAndroid** project in **Android Studio**.
2. Let **Gradle** finish syncing (internet required on first sync).
3. Choose a **virtual device** (AVD) or a **physical device** with USB debugging enabled.
4. Click **Run** (green play button) for the `app` configuration.

**Suggested environment**

- Android Studio (recent stable release)
- JDK compatible with the project toolchain (see `app/build.gradle.kts`)
- Emulator: **Pixel 6** or **Medium Phone** system image
- **API level 36** (`targetSdk` / compile SDK 36; see `app/build.gradle.kts`)

## Emulator / device used

Testing was done on an **Android emulator** only (no physical hardware).

- **Device profile:** Pixel 6 or Medium Phone AVD  
- **Approx. resolution:** 1080 × 2400 (varies by skin)  
- **API level:** **36** (matches project `targetSdk` / compile configuration)  
- **Physical device:** **None** — all runs were on the emulator in Android Studio.

## Data persistence

The app stores one serialized object graph in **app-private internal storage**:

| Item | Detail |
|------|--------|
| **File** | `photos_data.ser` (see `FILE_NAME` in `DataStorage`) |
| **Root object** | `PhotoManager` → list of `Album` → each has `Photo` list |
| **Photo** | `uriString` (content URI as string) + `Tag` list |
| **Tag** | `type` (`person` / `location`) + `value` |

Helper API:

```java
DataStorage.saveData(Context context, PhotoManager manager);
PhotoManager DataStorage.loadData(Context context);
```

Implementation: `ObjectOutputStream` / `ObjectInputStream` wrapped with **buffered** streams over `Context.openFileOutput` / `openFileInput` for more efficient I/O. If load fails (no file yet, corrupt file), a new empty `PhotoManager` is returned.

## Testing checklist

Use this as a manual regression list before submission.

**Albums**

- [ ] Create album; duplicate name rejected.
- [ ] Rename album; duplicate rename rejected.
- [ ] Delete album; list updates immediately; album gone after app restart.
- [ ] Long-press shows rename/delete; tap opens album.

**Photos**

- [ ] Add image(s) from picker; thumbnails appear.
- [ ] Kill and reopen app; thumbnails still load (persisted URI permission path).
- [ ] Long-press → remove; grid and persistence OK.
- [ ] Long-press → move to second album; photo leaves source, appears in target with tags intact.

**Display & tags**

- [ ] Open photo; Previous/Next and “Photo X of Y” correct.
- [ ] Add person and location tags; try adding the same tag again → duplicate rejected.
- [ ] Remove one tag; persistence after restart.
- [ ] Back navigation: photo → album → home.

**Search**

- [ ] Single condition (e.g. person = John) returns expected thumbnails.
- [ ] AND with two values; OR with two values; case-insensitive match.
- [ ] No matches → “No matching photos found”.
- [ ] Autocomplete updates when switching Person vs. Location; typing prefix suggests values.
- [ ] Tap result → correct photo and tags.

## Known issues / limitations

- **Single archive:** all data is still one serialized `photos_data.ser` file (no database or incremental sync). Very large libraries may mean slower saves/loads on low-end devices.
- **Broken URIs:** if a URI cannot be opened, the UI shows a **placeholder icon** and (on the full photo screen) a short **toast**. The stored URI string is kept so the entry still exists in the album.
- **UI framework:** the app uses classic Android Views such as ListView and GridView instead of Jetpack Compose, which keeps the project aligned with the Java-only assignment requirement.
- **Search grid:** thumbnails use the same URI checks as albums; if a result’s file is gone, the tile shows the placeholder until you remove or replace that photo.

## GenAI usage

**ChatGPT** was used to help with **mapping and structuring** this project (for example: clarifying assignment checkpoints, outlining features and file responsibilities, and drafting README sections). Other implementation and debugging were done in **Android Studio** with team review.

All submitted behavior was run and checked on the **emulator** described above. The team takes responsibility for the final code and any remaining issues.
