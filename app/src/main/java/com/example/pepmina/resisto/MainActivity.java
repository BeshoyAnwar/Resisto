package com.example.pepmina.resisto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_GET_SINGLE_FILE =0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView buckysImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //define Pick from gallery button
        final Button pickFromGalleryButton = findViewById(R.id.pickFromGalleryButton);
        Button buckyButton = (Button) findViewById(R.id.captureButton);
        buckysImageView = (ImageView) findViewById(R.id.imageView);
        //Disable the button if the user has no camera
        if(!hasCamera())
            buckyButton.setEnabled(false);
        // pick from gallery on click method
        // On click of the  button, start startActivityForResult
        pickFromGalleryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                // This segment is used to choose an image from Gallery
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
            }
        });
        buckyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //take photo from camera
                launchCamera(v);
            }
        });
    }
    //Check if the user has a camera
    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }
    //Launching the camera
    public void launchCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Take a picture and pass results along to onActivityResult
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // Get the real path from the URI
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
    // Once we will select an image, the method OnActivityResult()  will be called. We need to handle the data in this method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_GET_SINGLE_FILE) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getPathFromURI(selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        selectedImageUri = Uri.fromFile(f);
                    }
                    // Set the image in ImageView
                    ImageView imageView=findViewById(R.id.imageView);
                    imageView.setImageURI(selectedImageUri);
                    //ImageView((ImageView) findViewById(R.id.imageView)).setImageURI(selectedImageUri);
                }
                else if(requestCode == REQUEST_IMAGE_CAPTURE)
                {
                    //Get the photo
                    Bundle extras = data.getExtras();
                    Bitmap photo = (Bitmap) extras.get("data");
                    buckysImageView.setImageBitmap(photo);
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }
}
