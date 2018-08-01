package com.handen.memes;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class LinkActivity extends AppCompatActivity {

    private EditText linkEditText;
    private Button doneButton;
    private ImageButton backButton;
    private TextView titleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

        ActionBar actionBar = getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
            actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.action_bar);
        }
        else {
            getSupportActionBar().setDisplayShowHomeEnabled(false); //не показываем иконку приложения
            getSupportActionBar().setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(R.layout.action_bar);
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleTextView = findViewById(R.id.titleTextView);
        titleTextView.setText("Добавить группу");

        linkEditText = findViewById(R.id.linkEditText);
        doneButton = findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
