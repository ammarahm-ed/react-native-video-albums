package im.thecodrr.react.albums;

import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

public class RNAlbumsModule extends ReactContextBaseJavaModule {

    public RNAlbumsModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNAlbumsModule";
    }

    private void getVideos(ReadableMap options, Promise promise, @Nullable String albumName){
        ArrayList<String> projection = new ArrayList<>();
        ArrayList<ReadableMap> columns = new ArrayList<>();

        setColumn("path", MediaStore.Video.Media.DATA, projection, columns);

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("title", MediaStore.Video.Media.TITLE);
        fieldMap.put("name", MediaStore.Video.Media.DISPLAY_NAME);
        fieldMap.put("size", MediaStore.Video.Media.SIZE);
        fieldMap.put("description", MediaStore.Video.Media.DESCRIPTION);
        fieldMap.put("resolution", MediaStore.Video.Media.RESOLUTION);
        fieldMap.put("type", MediaStore.Video.Media.MIME_TYPE);
        fieldMap.put("album", MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
        fieldMap.put("duration", MediaStore.Video.Media.DURATION);

        Iterator<Map.Entry<String, String>> fieldIterator = fieldMap.entrySet().iterator();

        while (fieldIterator.hasNext()) {
            Map.Entry<String, String> pair = fieldIterator.next();

            if (shouldSetField(options, pair.getKey())) {
                setColumn(pair.getKey(), pair.getValue(), projection, columns);
            }

            fieldIterator.remove();
        }
        setColumn("timestamp", MediaStore.Video.Media.DATE_ADDED, projection, columns);
        if (shouldSetField(options, "location")) {
            setColumn("latitude", MediaStore.Video.Media.LATITUDE, projection, columns);
            setColumn("longitude", MediaStore.Video.Media.LONGITUDE, projection, columns);
        }

        if (shouldSetField(options, "date")) {
            setColumn("modified", MediaStore.Video.Media.DATE_MODIFIED, projection, columns);
            setColumn("taken", MediaStore.Video.Media.DATE_TAKEN, projection, columns);
        }

        if (shouldSetField(options, "dimensions")) {
            setColumn("width", MediaStore.Video.Media.WIDTH, projection, columns);
            setColumn("height", MediaStore.Video.Media.HEIGHT, projection, columns);
        }
        String selection = null;
        String orderBy = null;
        if(albumName != null && !TextUtils.isEmpty(albumName)) {
            selection = "bucket_display_name = \"" + albumName + "\"";
            orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";
            //String columnName = "count(" +  MediaStore.Video.VideoColumns.BUCKET_ID + ") as count";
            //setColumn("count", columnName, projection, columns);
        }
        Cursor cursor = getReactApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection.toArray(new String[projection.size()]),
                selection,
                null,
                orderBy
        );


        Map<String, Integer> columnIndexMap = new HashMap<>();
        WritableArray list = Arguments.createArray();

        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            for (int i = 0; i < projection.size(); i++) {
                String field = projection.get(i);
                columnIndexMap.put(field, cursor.getColumnIndex(field));
            }

            do {
                Iterator<ReadableMap> columnIterator = columns.iterator();

                WritableMap video = Arguments.createMap();

                while (columnIterator.hasNext()) {
                    ReadableMap column = columnIterator.next();
                    setWritableMap(video, column.getString("name"), cursor.getString(columnIndexMap.get(column.getString("columnName"))));
                }

                list.pushMap(video);
            } while (cursor.moveToNext());
            cursor.close();
        }

        promise.resolve(list);
    }

    @ReactMethod
    public void getVideoList(ReadableMap options, Promise promise) {
        getVideos(options, promise, null);
    }

    @ReactMethod
    public void getVideosByAlbum(ReadableMap options, String albumName, Promise promise){
        getVideos(options, promise, albumName);
    }

    @ReactMethod
    public void getAlbumList(Promise promise) {
        // which video properties are we querying
        String[] PROJECTION_BUCKET = {
                MediaStore.Video.VideoColumns.BUCKET_ID,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                "count(" +  MediaStore.Video.VideoColumns.BUCKET_ID + ") as count"
        };

        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        String BUCKET_ORDER_BY = "MAX(" + MediaStore.Video.VideoColumns.DATE_TAKEN + ") DESC";


        Cursor cursor = getReactApplicationContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_BUCKET,
                BUCKET_GROUP_BY,
                null,
                BUCKET_ORDER_BY
        );

        WritableArray list = Arguments.createArray();
        if (cursor != null && cursor.moveToFirst()) {
            String bucket;
            String date;
            String data;
            String count;
            String displayName;
            int bucketColumn = cursor.getColumnIndex(
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int dateColumn = cursor.getColumnIndex(
                    MediaStore.Video.Media.DATE_TAKEN);
            int dataColumn = cursor.getColumnIndex(
                    MediaStore.Video.Media.DATA);
            int displayNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            int countColumn = cursor.getColumnIndex("count");
            do {
                // Get the field values
                bucket = cursor.getString(bucketColumn);
                date = cursor.getString(dateColumn);
                data = cursor.getString(dataColumn);
                count = cursor.getString(countColumn);
                displayName = cursor.getString(displayNameColumn);

                WritableMap video = Arguments.createMap();
                setWritableMap(video, "count", count);
                setWritableMap(video, "date", date);
                setWritableMap(video, "cover", "file://" + data);
                setWritableMap(video, "name", bucket);
                setWritableMap(video, "displayName", displayName);

                list.pushMap(video);
            } while (cursor.moveToNext());

            cursor.close();
        }

        promise.resolve(list);
    }

    private boolean shouldSetField(ReadableMap options, String name) {
        return options.hasKey(name) && options.getBoolean(name);
    }

    private void setWritableMap(WritableMap map, String key, String value) {
        if (value == null) {
            map.putNull(key);
        } else {
            map.putString(key, value);
        }
    }

    private void setColumn(String name, String columnName, ArrayList<String> projection, ArrayList<ReadableMap> columns) {
        projection.add(columnName);
        WritableMap column = Arguments.createMap();
        column.putString("name", name);
        column.putString("columnName", columnName);
        columns.add(column);
    }
}
