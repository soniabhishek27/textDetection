
package com.example.newtext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText mResultEt;
    ImageView mPreviewIv,cropImageView;


    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String StoragePermission[];

    Uri image_uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar= getSupportActionBar();
        actionBar.setSubtitle("Click Image Button to add Image");

        mResultEt = findViewById(R.id.resultEt);
        mPreviewIv = findViewById(R.id.imageIv);
        cropImageView= findViewById(R.id.cropImageView);

        //camera Permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        StoragePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    //action bar menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    //handle action bar selected items

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addImage) {
            showImageImportDialog();
        }
        if (id == R.id.settings) {

        }
        if (id==R.id.Exit)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {

        String[] items = {"Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //set title
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0)  // 0 for camera
                {
                    if (!checkCameraPermission()) {
                        //camera permission not allowed request for it
                        requestCameraPermission();
                    } else {
                        //permission allowed take picture
                        pickCamera();

                    }

                }
                if (which == 1) // 1 for gallery
                {
                    if (!checkStoragePermission()) {
                        //Storage permission not allowed request for it
                        requestStoragePermission();

                    } else {
                        //permission is allowed select the image from the gallery
                        pickGallery();
                    }
                }

            }
        });
        dialog.create().show(); // show dialog
    }
    public void pickGallery()
    {
        //intent to pick the images from the gallery
        Intent intent= new Intent(Intent.ACTION_PICK);
        // set intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }
    public void pickCamera()
    {
        // intent to take image from the camera it will also save the images
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image To text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING_BACK", Camera.CameraInfo.CAMERA_FACING_BACK);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);

    }


    public boolean checkStoragePermission()     {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;

    }


    public void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this,
                StoragePermission, STORAGE_REQUEST_CODE);
    }


    public void requestCameraPermission()
    {
        ActivityCompat.requestPermissions(this,
                cameraPermission, CAMERA_REQUEST_CODE);
    }


    private boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

            return result && result1;

    }

    // handle permission result

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)  {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0)
                {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted)
                    {
                        pickCamera();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if(grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted)
                    {
                        pickGallery();
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }

        }
    }

    //Handle Image result


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //got image from camera
        if (resultCode == RESULT_OK)
        {
            if(requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                //get the image from the gallery and crop it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);

            }

            if(requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                // get the image from the camera and crop it
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        //get cropped image
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
             if( resultCode == RESULT_OK)
             {
                 Uri resultUri = result.getUri();

                 //set the image to view
                 mPreviewIv.setImageURI(resultUri);


                 //get drawable bitmap from textRecognition

                 BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                 Bitmap bitmap = bitmapDrawable.getBitmap();


                 TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                 if(!textRecognizer.isOperational())
                 {
                     Toast.makeText(MainActivity.this,"Camera Loading Failed ",Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                     SparseArray<TextBlock> items = textRecognizer.detect(frame);
                     StringBuilder stringBuilder = new StringBuilder();

                     // get all the text from string builer util it is empty

                     for(int i=0; i<items.size(); i++)
                     {

                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue());
                            //stringBuilder.append("");

                     }

                     // set the detected text to the textView
                     mResultEt.setText(stringBuilder.toString());
                 }
             }
             else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
             {
                 Exception error = result.getError();
                 Toast.makeText(this,""+error, Toast.LENGTH_SHORT).show();
             }

        }
    }
}
