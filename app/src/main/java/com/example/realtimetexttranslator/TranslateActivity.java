package com.example.realtimetexttranslator;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;



import com.google.android.gms.tasks.Task;
import  com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import  com.google.mlkit.nl.translate.TranslatorOptions;


import java.util.ArrayList;
import java.util.Locale;


public class TranslateActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView mieIV;
    private MaterialButton translateBtn;
    private ImageView appLogo;
    private TextView translateIV;

    String[] fromLanguage = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "French", "German",  "Hindi", "Spanish", "Urdu", "Welch"};
    String[] toLanguage = {"from", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech",  "French", "German", "Hindi","Spanish", "Urdu", "Welch"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    String languageCode, fromLanguageCode, toLanguageCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transnlate);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.sourceEditText);
        translateBtn = findViewById(R.id.translationBtn);
        translateIV = findViewById(R.id.TranslatedTextIV);
        mieIV = findViewById(R.id.miceIV);
        appLogo = findViewById(R.id.appLogo);

        String inputText = getIntent().getStringExtra("textToTranslate");
        System.out.println("Here is the inputshit"+inputText);
        System.out.println("Test===================================================================================================Test");
        if(!inputText.equals("")){
            System.out.println("===================================================================================================");
            sourceText.setText(inputText);
            System.out.println(inputText);
        }

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);



        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


        mieIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate");
                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(TranslateActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        appLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newActivity = new Intent(TranslateActivity.this,MainActivity.class);
                startActivity(newActivity);
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translateIV.setVisibility(View.VISIBLE);
                translateIV.setText("");
                if(sourceText.getText().toString().isEmpty()){
                    Toast.makeText(TranslateActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                }
                else if(fromLanguageCode == "none"){
                    Toast.makeText(TranslateActivity.this, "Please select Source Language", Toast.LENGTH_SHORT).show();

                }
                else if(toLanguageCode == "none"){
                    Toast.makeText(TranslateActivity.this, "Please select Target Language", Toast.LENGTH_SHORT).show();

                }
                else {
                    translateText(fromLanguageCode, toLanguageCode, sourceText.getText().toString());
                }
            }
        });



    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String source) {
        translateIV.setText("Downloading model please wait!");
        System.out.println(source);
        System.out.println(fromLanguageCode+toLanguageCode);
        try{
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setTargetLanguage(toLanguageCode)
                    .setSourceLanguage(fromLanguageCode)
                    .build();
            Translator translator = Translation.getClient(options);

            String sourceString = source;
            ProgressDialog progressDialog = new ProgressDialog(TranslateActivity.this);
            progressDialog.setMessage("Downloading the translation model...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                }
            });


            Task<String> result = translator.translate(sourceString).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    translateIV.setText(s);
//                            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TranslateActivity.this, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception exception){
            Toast.makeText(TranslateActivity.this, ""+exception.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceText.setText(result.get(0));
        }
    }
    //    String[] toLanguage = {"from", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech",  "French", "German", "Hindi","Spanish", "Urdu", "Welch"};

    private String getLanguageCode(String language) {
        String languageCode = "none";
        switch (language){
            case "English":
                languageCode = "en";
                break;
            case "Afrikaans":
                languageCode = "af";
                break;
            case "Arabic":
                languageCode = "ar";
                break;
            case "Belarusian":
                languageCode = "be";
                break;
            case "Bulgarian":
                languageCode = "bg";
                break;
            case "Bengali":
                languageCode = "bn";
                break;
            case "Catalan":
                languageCode = "ca";
                break;
            case "Czech":
                languageCode = "cs";
                break;
            case "French":
                languageCode = "fr";
                break;
            case "German":
                languageCode = "de";
                break;
            case "Hindi":
                languageCode = "hi";
                break;
            case "Spanish":
                languageCode = "es";
                break;
            case "Urdu":
                languageCode = "ur";
                break;
            case "Welch":
                languageCode = "cy";
                break;
            default:
                languageCode="none";
        }
        return languageCode;
    }
}
