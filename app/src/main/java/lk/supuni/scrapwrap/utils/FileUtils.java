package lk.supuni.scrapwrap.utils;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static Bitmap getBitmapFromFile(File file) {
        Bitmap userImage = null;
        if (file != null) {
            String imagePath = file.getPath();
            userImage = BitmapFactory.decodeFile(imagePath);
        }
        return userImage;
    }

//    public static File getFileFromBitmap(Bitmap finalBitmap) {
//        File localFile = null;
//        try {
//            localFile = File.createTempFile("images", "jpeg");
////            String timeStamp = String.valueOf(System.currentTimeMillis());
//
//            FileOutputStream out = new FileOutputStream(localFile);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            out.flush();
//            out.close();
//            return localFile;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return localFile;
//    }

    public static File getFileFromImageUri(Activity activity, Uri selectedImageURI){
        File file = new File(getRealPathFromURI(activity,selectedImageURI));
        return file;
    }
    public static String getRealPathFromURI(Activity activity,Uri contentURI) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(contentURI);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }
}
