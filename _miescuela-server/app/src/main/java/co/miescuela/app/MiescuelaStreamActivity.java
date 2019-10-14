package co.miescuela.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;

import static android.content.ContentValues.TAG;
import static android.widget.Toast.LENGTH_SHORT;

/** Basic embedded browser for viewing help pages. */
public final class MiescuelaStreamActivity extends Activity {

    private static final int READ_REQUEST_CODE = 42;
    public static final String BIN_PATH = "/data/data/co.miescuela/files/usr/bin/";
    public static final String HOME_PATH = "/data/data/co.miescuela/files/home/";
    private static final String ACTION_EXECUTE = "co.miescuela.service_execute";
    private static final String EXTRA_EXECUTE_IN_BACKGROUND = "co.miescuela.execute.background";
    private static final String TERMUX_SERVICE = "co.miescuela.app.TermuxService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/mp4");
            startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                System.out.println("*** Uri: " +  uri.toString());
                streamVideo(uri);
            }
        }
    }

    private void streamVideo(Uri uri) {
        //String path = getRealPathFromURI(getApplicationContext(),uri);
        String[] arrOfStr = uri.toString().split("%2F");
        String videoName = arrOfStr[arrOfStr.length - 1];
        System.out.println("*** Video Name: " +  videoName);
        //Me: Run method
        try {
            File streamfile = new File(HOME_PATH + "stream.sh");
            FileWriter writer = new FileWriter(streamfile);
            writer.append("#!/bin/sh");
            writer.append("\n");
            writer.append("cd ~/storage/shared/nms/Node-Media-Server; node app.js;");
            writer.append("ffmpeg -re -i ~/storage/shared/miescuela-koala/static/videos/" +  videoName + " -c:v libx264 -preset superfast -tune zerolatency -c:a aac -ar 44100 -f flv rtmp://0.0.0.0/live/stream.mp4;");
            writer.flush();
            writer.close();
            Toast.makeText(MiescuelaStreamActivity.this, "****** running streamming application...", LENGTH_SHORT).show();
        } catch (Exception e) { System.out.println("Error writting file..");}
        Uri uriScript = new Uri.Builder().scheme("co.miescuela.file").path(HOME_PATH + "stream.sh").build();
        Intent executeIntent = new Intent(ACTION_EXECUTE, uriScript);
        executeIntent.setClassName("co.miescuela", TERMUX_SERVICE);
        executeIntent.putExtra(EXTRA_EXECUTE_IN_BACKGROUND, true);
        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // https://developer.android.com/about/versions/oreo/background.html
            context.startForegroundService(executeIntent);
        } else {
            context.startService(executeIntent);
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


}
