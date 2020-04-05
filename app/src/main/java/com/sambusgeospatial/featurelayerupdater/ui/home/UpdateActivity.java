package com.sambusgeospatial.featurelayerupdater.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sambusgeospatial.featurelayerupdater.R;

public class UpdateActivity extends AppCompatActivity {

    private static final String KEY_COUNTRY = "NAME_0";
    private static final String KEY_NAME = "NAME_1";
    private static final String KEY_CONFIRMED = "ConfCases";
    private static final String KEY_ACTIVE = "Active_Cases";
    private static final String KEY_RECOVERED = "Recovery";
    private static final String KEY_DEATHS = "Deaths";
    Intent intent;
    TextView confirmedTv, activeTv, recoveredTv, deathTv;
    EditText confirmedEt, activeEt, recoveredEt, deathEt;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        intent = getIntent();
        Toolbar secondaryToolbar = findViewById(R.id.toolbarSec);
        final String title = intent.getStringExtra(KEY_NAME) + ", " + intent.getStringExtra(KEY_COUNTRY);
        secondaryToolbar.setTitle(title);

        confirmedTv = findViewById(R.id.currentConfirmed);
        activeTv = findViewById(R.id.currentActive);
        recoveredTv = findViewById(R.id.currentRecovered);
        deathTv = findViewById(R.id.currentDeath);

        confirmedTv.setText(String.valueOf(intent.getIntExtra(KEY_CONFIRMED, 0)));
        activeTv.setText(String.valueOf(intent.getIntExtra(KEY_ACTIVE, 0)));
        recoveredTv.setText(String.valueOf(intent.getIntExtra(KEY_RECOVERED, 0)));
        deathTv.setText(String.valueOf(intent.getIntExtra(KEY_DEATHS, 0)));

        confirmedEt = findViewById(R.id.confirmed);
        activeEt = findViewById(R.id.active);
        recoveredEt = findViewById(R.id.recovery);
        deathEt = findViewById(R.id.deaths);

        confirmedEt.setText(String.valueOf(intent.getIntExtra(KEY_CONFIRMED, 0)));
        activeEt.setText(String.valueOf(intent.getIntExtra(KEY_ACTIVE, 0)));
        recoveredEt.setText(String.valueOf(intent.getIntExtra(KEY_RECOVERED, 0)));
        deathEt.setText(String.valueOf(intent.getIntExtra(KEY_DEATHS, 0)));

        fab = findViewById(R.id.submit);
        fab.setOnClickListener(v -> {
            if (validateInputs()){
                Intent myIntent = new Intent();
                myIntent.putExtra(KEY_CONFIRMED, Integer.parseInt(confirmedEt.getText().toString()));
                myIntent.putExtra(KEY_ACTIVE, Integer.parseInt(activeEt.getText().toString()));
                myIntent.putExtra(KEY_RECOVERED, Integer.parseInt(recoveredEt.getText().toString()));
                myIntent.putExtra(KEY_DEATHS, Integer.parseInt(deathEt.getText().toString()));
                setResult(100, myIntent);
                finish();
            }
        });
    }

    private boolean validateInputs() {
        if (confirmedEt.getText().toString().equals("")){
            confirmedEt.setError("Enter value!");
            confirmedEt.requestFocus();
            return false;
        } else if (activeEt.getText().toString().equals("")){
            activeEt.setError("Enter value!");
            activeEt.requestFocus();
            return false;
        } else if (recoveredEt.getText().toString().equals("")){
            recoveredEt.setError("Enter value!");
            recoveredEt.requestFocus();
            return false;
        } else if (deathEt.getText().toString().equals("")){
            deathEt.setError("Enter value!");
            deathEt.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}