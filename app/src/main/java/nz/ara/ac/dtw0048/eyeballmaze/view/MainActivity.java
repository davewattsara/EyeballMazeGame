package nz.ara.ac.dtw0048.eyeballmaze.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import nz.ara.ac.dtw0048.eyeballmaze.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startGameClicked(View view) {
        Intent intent = new Intent(this, LevelActivity.class);
        startActivity(intent);
    }
}