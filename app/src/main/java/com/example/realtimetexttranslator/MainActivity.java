package com.example.realtimetexttranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.CAMERA;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;


public class MainActivity extends AppCompatActivity {

    private String textToTranslate ="";
    private String toTranslate = "translate";
    private ImageView captureIV ;
    private TextView resultIV;
    private Button snapBtn, detectBtn;
    private Bitmap imageBitmap;
    private final int REQUEST_IMAGE_CAPTURE = 1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureIV = findViewById(R.id.captureIV);
        resultIV = findViewById(R.id.waiting_text);
        snapBtn = findViewById(R.id.SnapBtn);
        detectBtn = findViewById(R.id.detectBtn);
//        textToTranslate = "Your text here";
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermission()){
                    captureImage();
                }
                else {
                    requestPermission();
                }
            }
        });

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                textToTranslate = detectText();
                detectText();
                if(!textToTranslate.equals("")){
                    System.out.println("Sending===================================================================================================Sending");
                    System.out.println("That is the text that we are sending :"+textToTranslate);
                    Intent newActivity = new Intent(MainActivity.this,TranslateActivity.class);
                    newActivity.putExtra("textToTranslate", textToTranslate);
                    startActivity(newActivity);
                }

            }
        });
    }

    private boolean checkPermission(){
        int cameraPermission = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }
    private void captureImage(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager())!=null){
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }


    private void requestPermission(){
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{CAMERA},PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                captureImage();
            }
            else {
                Toast.makeText(this, "Permission Denied..", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
                captureIV.setImageBitmap(imageBitmap);
            }
        }
    }




    private void runTextRecognition(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        TextRecognizerOptions options = TextRecognizerOptions.DEFAULT_OPTIONS;
        TextRecognizer recognizer = TextRecognition.getClient(options);

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Detecting text...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Task<Text> result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        progressDialog.dismiss();
                        processTextRecognitionResult(visionText);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        // Task failed with an exception
                        toTranslate = "No Text";
                        Toast.makeText(MainActivity.this, "Failed to detect text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showManualTextEntryDialog("Text");
                    }
                });
    }

    private void detectText() {
        if (imageBitmap != null) {
            float scaleFactor = 1.2f;
            scaleAndRunTextRecognition(imageBitmap, scaleFactor);
            runTextRecognition(imageBitmap);

        } else {
            showManualTextEntryDialog("Image");
        }
    }

    private void showManualTextEntryDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No "+s+" Detected");
        builder.setMessage("Do you want to enter text manually?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent newActivity = new Intent(MainActivity.this, TranslateActivity.class);
                newActivity.putExtra("textToTranslate", "");
                startActivity(newActivity);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void scaleAndRunTextRecognition(Bitmap originalBitmap, float scaleFactor) {
        new AsyncTask<Bitmap, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Scaling image...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }

            @Override
            protected Bitmap doInBackground(Bitmap... bitmaps) {
                return scaleBitmap(bitmaps[0], scaleFactor);
            }

            @Override
            protected void onPostExecute(Bitmap scaledBitmap) {
                progressDialog.dismiss();
                runTextRecognition(scaledBitmap);
            }
        }.execute(originalBitmap);
    }

    private Bitmap scaleBitmap(Bitmap originalBitmap, float scaleFactor) {
        int newWidth = (int) (originalBitmap.getWidth() * scaleFactor);
        int newHeight = (int) (originalBitmap.getHeight() * scaleFactor);
        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
    }


    private void processTextRecognitionResult(Text visionText) {

        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Detecting text...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringBuilder resultText = new StringBuilder();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                resultText.append(line.getText()).append("\n");
            }
            resultText.append("\n");
        }
        this.textToTranslate = resultText.toString();
        System.out.println("Result===================================================================================================Result");
        System.out.println(textToTranslate);
        progressDialog.dismiss();
        if(textToTranslate.equals(""))
        {
            showManualTextEntryDialog("Text");
        }
    }

}