package kr.ac.pknu.perdu;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class ModePager extends ViewPager {
    // 터치 이벤트를 부여하기 위해 뷰페이저를 상속하여 만든 페이저
    // 줌 변수들
    private double pre_interval_X = 0; // X 터치 간격
    private double pre_interval_Y = 0; // Y 터치 간격
    private int touch_zoom = 0; // 줌 크기

    public ModePager(Context context) {
        super(context);
    }

    public ModePager(Context context, AttributeSet attr) {
        super(context, attr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 터치로 초점을 맞추고 멀티터치로 줌을 구현하기 위한 터치이벤트
        Handler handler = new Handler();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:   // 한손가락 터치 시 포커스 맞춤
                if (event.getPointerCount() == 1)
                MainActivity.focusOval.setX(event.getX());
                MainActivity.focusOval.setY(event.getY());
                MainActivity.focusOval.setVisibility(View.VISIBLE);
                MainActivity.cameraView.focus();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.focusOval.setVisibility(View.GONE);
                    }
                }, 2000);
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    MainActivity.zoomLayout.setVisibility(View.VISIBLE);
                    double interval_X = Math.abs(event.getX(0) - event.getX(1));   // 두 손가락 사이 X좌표 절대값
                    double interval_Y = Math.abs(event.getY(0) - event.getY(1));   // 두 손가락 사이 Y좌표 절대값

                    if (pre_interval_X < interval_X && pre_interval_Y < interval_Y) {
                        // 시크바에 전달하여 줌 인
                        touch_zoom++;
                        MainActivity.zoomSeekBar.setProgress(touch_zoom);
                    }

                    if (pre_interval_X > interval_X && pre_interval_Y > interval_Y) {
                        // 시크바에 전달하여 줌 아웃
                        touch_zoom--;
                        MainActivity.zoomSeekBar.setProgress(touch_zoom);
                    }
                    pre_interval_X = Math.abs(event.getX(0) - event.getX(1));  // 현재 X 값을 이전 값에 저장
                    pre_interval_Y = Math.abs(event.getY(0) - event.getY(1));  // 현재 Y 값을 이전 값에 저장
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.zoomLayout.setVisibility(View.GONE);
                    }
                }, 2000);
        }
        return super.onTouchEvent(event);
    }
}