package com.pavel.alltercoassignment.model;

import android.graphics.Bitmap;

/**
 * Created by Pavel Pavlov on 5/19/2017.
 */

public class BitmapWrapper {
    private Bitmap bitmap;
    private String fileName;

    public BitmapWrapper(Bitmap bitmap, String fileName) {
        this.bitmap = bitmap;
        this.fileName = fileName;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
