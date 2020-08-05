package kr.ac.pknu.perdu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class SettingActivity extends AppCompatActivity {
    private int screenWidth, screenHeight;
    private int emotionCode, poseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x, y;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = (int) event.getX();
            y = (int) event.getY();

            Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);    // 터치한 부분의 색상코드를 추출하기 위한 비트맵

            if (x < 0 || y < 0)
                return false;

            int ARGB = bitmap.getPixel(x, y);

            if (Color.alpha(ARGB) != 255)     // 터치한 공간의 알파값이 255가 아니면 액티비티를 종료
                finish();

            return true;
        }
        return false;
    }
}