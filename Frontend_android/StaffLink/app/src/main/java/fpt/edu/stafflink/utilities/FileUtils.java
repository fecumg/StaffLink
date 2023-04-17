package fpt.edu.stafflink.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileUtils {
    public static String getContentType(Context context, Uri uri) {
        String type = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE);
                type = cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }

    public static String getFilename(Context context, Uri uri) {
        String filename = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                filename = cursor.getString(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filename;
    }
}
