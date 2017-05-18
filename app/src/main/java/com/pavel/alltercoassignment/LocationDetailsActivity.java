package com.pavel.alltercoassignment;


import android.Manifest.permission;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pavel.alltercoassignment.model.LocationsManager;
import com.pavel.alltercoassignment.model.MarkerLocation;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.pavel.alltercoassignment.Constants.ADDRESS;
import static com.pavel.alltercoassignment.Constants.COUNTRY_NAME;
import static com.pavel.alltercoassignment.Constants.KEY_LOCATION_ID;
import static com.pavel.alltercoassignment.Constants.LATITUDE;
import static com.pavel.alltercoassignment.Constants.LONGITUDE;
import static com.pavel.alltercoassignment.R.id.location_details_tv;

public class LocationDetailsActivity extends AppCompatActivity {

    private static final int TAKE_PICTURE = 1;
    public static final String ALLTERCO = "Allterco";
    private Uri imageUri;
    protected TextView detailsTv;
    protected Button pictureBtn, galleryBtn;
    protected MarkerLocation markerLocation;
    protected ImageView locationImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        detailsTv = (TextView) findViewById(location_details_tv);
        pictureBtn = (Button) findViewById(R.id.picture_btn);
        galleryBtn = (Button) findViewById(R.id.gallery_btn);
        locationImage = (ImageView) findViewById(R.id.location_photo);


        markerLocation = LocationsManager.getInstance().getLocation(getIntent().getExtras().getLong(KEY_LOCATION_ID));
        if (markerLocation == null) finish();

        detailsTv.setText(ADDRESS.concat(markerLocation.getAddress()).concat("\n").
                concat(COUNTRY_NAME.concat(markerLocation.getCountry()).concat("\n").
                        concat(LATITUDE.concat(String.valueOf(markerLocation.getLat())).concat("\n").
                                concat(LONGITUDE.concat(String.valueOf(markerLocation.getLon()))))));

        pictureBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        galleryBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhotoFromGallery();
            }
        });

        File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALLTERCO);
        photoDir.mkdir();
        String[] filenames = photoDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                int indexOfIdSeparatpr = name.indexOf("_");
                String idPrefix = name.substring(0, indexOfIdSeparatpr);
                return idPrefix.equals(String.valueOf(markerLocation.getId()));
            }
        });

        if(filenames != null) {
            for (String photoName : filenames) {
                Log.e("detaili", "snimka: " + photoName);
                Bitmap bitmap = BitmapFactory.decodeFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALLTERCO.concat("/").concat(photoName)).getAbsolutePath());
                locationImage.setImageBitmap(bitmap);
            }
        }
    }

    private void pickPhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1234);
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE}, 123123);
            }
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALLTERCO);
        photoDir.mkdir();
        StringBuilder photoName = new StringBuilder();
        photoName.append(markerLocation.getId()).append("_").append(new SimpleDateFormat("yyyy.MM.dd HH-mm- ss").format(new Date())).append(".jpg");
        File photo = new File(photoDir, photoName.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123123) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            takePhoto();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();
                    Bitmap bitmap;
                    try {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, selectedImage);
                        locationImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Log.e("camera", "error: ", e);
                    }
                }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
