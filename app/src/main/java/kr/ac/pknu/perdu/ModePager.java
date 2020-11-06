package kr.ac.pknu.perdu;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import kr.ac.pknu.perdu.mode.ModeHelper;

public class ModePager extends ViewPager {
    // 터치 이벤트를 부여하기 위해 뷰페이저를 상속하여 만든 페이저
    // 줌 변수들
    private double pre_interval_X = 0; // X 터치 간격
    private double pre_interval_Y = 0; // Y 터치 간격
    private int touchZoom = 0; // 줌 크기
    private Context context;

    public ModePager(Context context) {
        super(context);
        this.context = context;
    }

    public ModePager(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Intent intent = new Intent();
        // 터치로 초점을 맞추고 멀티터치로 줌을 구현하기 위한 터치이벤트
        ModeHelper helper;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:   // 한손가락 터치 시 포커스 맞춤
                if (event.getPointerCount() == 1) {
                    helper = new ModeHelper(event.getX(), event.getY(), View.VISIBLE);
                    ((CameraPreviewActivity) context).setFocus(helper);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    helper = new ModeHelper(touchZoom, View.VISIBLE);
                    double interval_X = Math.abs(event.getX(0) - event.getX(1));   // 두 손가락 사이 X좌표 절대값
                    double interval_Y = Math.abs(event.getY(0) - event.getY(1));   // 두 손가락 사이 Y좌표 절대값

                    if (pre_interval_X < interval_X && pre_interval_Y < interval_Y) {
                        // 시크바에 전달하여 줌 인
                        touchZoom++;
                        helper.setTouchZoom(touchZoom);
                    }

                    if (pre_interval_X > interval_X && pre_interval_Y > interval_Y) {
                        // 시크바에 전달하여 줌 아웃
                        touchZoom--;
                        helper.setTouchZoom(touchZoom);
                    }
                    ((CameraPreviewActivity) context).setZoom(helper);
                    pre_interval_X = Math.abs(event.getX(0) - event.getX(1));  // 현재 X 값을 이전 값에 저장
                    pre_interval_Y = Math.abs(event.getY(0) - event.getY(1));  // 현재 Y 값을 이전 값에 저장
                }
                break;
            case MotionEvent.ACTION_UP:
                helper = new ModeHelper(0, View.GONE);
                ((CameraPreviewActivity) context).setZoom(helper);
                break;
        }
        return super.onTouchEvent(event);
    }
}