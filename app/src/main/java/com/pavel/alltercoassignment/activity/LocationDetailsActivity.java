package com.pavel.alltercoassignment.activity;

import android.Manifest.permission;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pavel.alltercoassignment.R;
import com.pavel.alltercoassignment.adapter.PhotoAdapter;
import com.pavel.alltercoassignment.adapter.PhotoAdapter.PhotoDeleteListener;
import com.pavel.alltercoassignment.data_base.DBManager;
import com.pavel.alltercoassignment.model.BitmapWrapper;
import com.pavel.alltercoassignment.model.LocationsManager;
import com.pavel.alltercoassignment.model.MarkerLocation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.pavel.alltercoassignment.Constants.KEY_LOCATION_ID;

public class LocationDetailsActivity extends AppCompatActivity {

    public static final String ALLTERCO = "Allterco";
    public static final int REQUEST_CODE = 123123;
    private Uri imageUri;
    protected MarkerLocation markerLocation;
    private MenuItem camera;
    protected RecyclerView photoGrid;
    private ArrayList<BitmapWrapper> photos = new ArrayList<>();

    private EditText tvAddress, tvCountry, tvLon, tvLat;
    private Button btnSave;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_details);
        tvAddress = (EditText) findViewById(R.id.addressValue);
        tvCountry = (EditText) findViewById(R.id.countryValue);
        tvLat = (EditText) findViewById(R.id.latValue);
        tvLon = (EditText) findViewById(R.id.lonValue);
        btnSave = (Button) findViewById(R.id.saveButton);
        photoGrid = (RecyclerView) findViewById(R.id.photo_grid);
        tvNoData = (TextView) findViewById(R.id.tvNoPhotos);

        markerLocation = LocationsManager.getInstance().getLocation(getIntent().getExtras().getLong(KEY_LOCATION_ID));
        if (markerLocation == null) finish();

        tvAddress.setText(markerLocation.getAddress());
        tvCountry.setText(markerLocation.getCountry());
        tvLat.setText(String.valueOf(markerLocation.getLat()));
        tvLon.setText(String.valueOf(markerLocation.getLon()));

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                markerLocation.setAddress(tvAddress.getText().toString());
                markerLocation.setCountry(tvCountry.getText().toString());
                markerLocation.setLat(Double.valueOf(tvLat.getText().toString()));
                markerLocation.setLon(Double.valueOf(tvLon.getText().toString()));
                DBManager.getInstance(getApplicationContext()).updateLocation(markerLocation);
            }
        });

        MarkerEditTextWatcher textWatcher = new MarkerEditTextWatcher();

        tvAddress.addTextChangedListener(textWatcher);
        tvCountry.addTextChangedListener(textWatcher);
        tvLat.addTextChangedListener(textWatcher);
        tvLon.addTextChangedListener(textWatcher);


    }

    @Override
    protected void onStart() {
        super.onStart();
        photos.clear();
        File photoDir = new File(Environment.getExternalStorageDirectory(), ALLTERCO);
        photoDir.mkdir();
        String[] filenames = photoDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                int indexOfIdSeparator = name.indexOf("_");
                String idPrefix = name.substring(0, indexOfIdSeparator);
                return idPrefix.equals(String.valueOf(markerLocation.getId()));
            }
        });

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (filenames != null) {
            for (String photoName : filenames) {
                File photoFile = new File(Environment.getExternalStorageDirectory(), ALLTERCO.concat("/").concat(photoName));
                BitmapFactory.Options bitmapOptions = new Options();
                bitmapOptions.inSampleSize = 4;
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bitmapOptions);
                photos.add(new BitmapWrapper(bitmap, photoName));
                out.reset();
            }
        }

        photoGrid.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        final PhotoAdapter adapter = new PhotoAdapter(photos);
        adapter.setListener(new PhotoDeleteListener() {
            @Override
            public void onDelete(final String fileName, final int position) {
                AlertDialog dialog = new AlertDialog.Builder(LocationDetailsActivity.this)
                        .setTitle("Do you want to remove this image?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File photoFile = new File(Environment.getExternalStorageDirectory(), ALLTERCO.concat("/").concat(fileName));

                                if (photoFile.delete()) {
                                    photos.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    tvNoData.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);
                                }
                            }
                        }).setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

                dialog.show();

            }
        });
        photoGrid.setAdapter(adapter);

        tvNoData.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void pickPhotoFromGallery() { //TODO implement
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 1);
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{permission.CAMERA, permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoDir = new File(Environment.getExternalStorageDirectory(), ALLTERCO);
        photoDir.mkdir();
        StringBuilder photoName = new StringBuilder();
        photoName.append(markerLocation.getId()).append("_").append(new SimpleDateFormat("yyyy.MM.dd HH-mm- ss").format(new Date())).append(".jpg");
        File photo = new File(photoDir, photoName.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivity(intent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        camera = menu.add("Camera");
        camera.setIcon(R.drawable.ic_camera);
        camera.getIcon().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_ATOP);
        camera.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        camera.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                takePhoto();
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private class MarkerEditTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            btnSave.setVisibility(View.VISIBLE);
        }
    }
}
