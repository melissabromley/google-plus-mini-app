package com.example.googleplusmini;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

public class LoadImageAsyncTask extends AsyncTask<String, String, Bitmap> {
    private Context c;
    private Bitmap photo;
    private ImageView imageView;

    public LoadImageAsyncTask(Context c, ImageView imageView) {
        this.c = c;
        this.imageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        try {
            photo = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photo;
    }

    @Override
    protected void onPostExecute(Bitmap image) {
        if (image != null) {
            imageView.setImageBitmap(image);
        } else {
            Toast.makeText(c, "Image Does Not exist or Network Error",
                    Toast.LENGTH_SHORT).show();
        }
    }
}