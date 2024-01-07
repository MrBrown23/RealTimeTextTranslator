package com.example.realtimetexttranslator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class TranslateActivity extends AppCompatActivity {
    String[] fromLanguages = {"From","English", "Arabic", "French", "German"};
    String[] toLanguages = {"TO","English", "Arabic", "French", "German"};

//    private String inputText;
//
//    // Constructor that accepts a string
//    public TranslateActivity(String inputText) {
//        this.inputText = inputText;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transnlate);
         String inputText = getIntent().getStringExtra("textToTranslate");
        if(inputText!=null){
            System.out.println(inputText);
        }
    }
}