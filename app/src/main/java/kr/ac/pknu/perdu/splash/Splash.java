package kr.ac.pknu.perdu.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import kr.ac.pknu.perdu.MainActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("state", "launch");
        startActivity(intent);
        finish();
    }
}