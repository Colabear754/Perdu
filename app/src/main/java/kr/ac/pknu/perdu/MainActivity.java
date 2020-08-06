package kr.ac.pknu.perdu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import kr.ac.pknu.perdu.adapter.AspectRatioSpinnerAdapter;
import kr.ac.pknu.perdu.adapter.FlashSpinnerAdapter;
import kr.ac.pknu.perdu.adapter.ModePagerAdapter;
import kr.ac.pknu.perdu.mode.ModeItem1;
import kr.ac.pknu.perdu.mode.ModeItem2;
import kr.ac.pknu.perdu.mode.ModeItem3;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    static CameraSurfaceView cameraView;   // 카메라 미리보기 뷰
    SurfaceView surfaceView;    // 미리보기를 표시하기 위한 서피스뷰
    // UI 변수들
    int[] flashIcon = {R.drawable.flash_auto_icon, R.drawable.flash_on_icon, R.drawable.flash_off_icon};    // 플래시 아이콘
    int[] aspectratioIcon = {R.drawable.aspectratio_icon1, R.drawable.aspectratio_icon2, R.drawable.aspectratio_icon3}; // 화면 비율 아이콘
    ModePager pager;    // 뷰페이저
    TextView emotion;   // 표정 모드 텍스트뷰
    TextView normal;    // 일반 모드 텍스트뷰
    TextView pose;  // 자세 모드 텍스트뷰
    final ModeItem1 modeItem1 = new ModeItem1();
    final ModeItem2 modeItem2 = new ModeItem2();
    final ModeItem3 modeItem3 = new ModeItem3();
    LinearLayout modeLayout;    // 모드 변경 레이아웃
    ImageView selectedMode; // 선택된 모드를 표시
    ConstraintLayout menuLayout;    // 상단 메뉴 레이아웃
    static ImageView focusOval;    // 수동 초점 맞출 때 나오는 원
    static SeekBar zoomSeekBar;    // 줌을 나타내는 시크바
    TextView zoomTextView;  // 줌 배율을 나타내는 텍스트뷰
    static LinearLayout zoomLayout;    // 줌 관련 내용을 보여주기 위한 레이아웃
    int cameraFacing;    // 카메라 전환 변수

    public static final int setting = 1001;
    public static final int emotionSelect = 1002;
    public static final int poseSelect = 1003;
    private int itemID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.previewFrame);

        AutoPermissions.Companion.loadAllPermissions(this, 101);    // 자동 권한 요청
        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;    // 후면 카메라를 기본 카메라로 설정
        startCamera();  // 미리보기 시작
        // 여기부터 상단 버튼을 위한 스피너 코드
        menuLayout = findViewById(R.id.menuLayout);
        Spinner flashSpinner = findViewById(R.id.flashSpinner);
        Spinner aspectratioSpinner = findViewById(R.id.aspectratioSpinner);

        FlashSpinnerAdapter flashSpinnerAdapter = new FlashSpinnerAdapter(getApplicationContext(), flashIcon);
        AspectRatioSpinnerAdapter aspectRatioSpinnerAdapter = new AspectRatioSpinnerAdapter(getApplicationContext(), aspectratioIcon);
        flashSpinner.setAdapter(flashSpinnerAdapter);
        flashSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        cameraView.flashControl("auto");
                        flashToastShow("자동");
                        break;
                    case 1:
                        cameraView.flashControl("on");
                        flashToastShow("켜짐");
                        break;
                    case 2:
                        cameraView.flashControl("off");
                        flashToastShow("꺼짐");
                        break;
                    default: break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        aspectratioSpinner.setAdapter(aspectRatioSpinnerAdapter);

        // 여기부터 모드 변경을 위한 뷰페이저 코드
        pager = findViewById(R.id.modePager);
        pager.setOffscreenPageLimit(3);
        ModePagerAdapter adapter = new ModePagerAdapter(getSupportFragmentManager());
        adapter.addItem(modeItem1);
        adapter.addItem(modeItem2);
        adapter.addItem(modeItem3);
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        pager.bringToFront();

        // 여기부터 글자 클릭으로 모드 변경을 하기 위한 코드
        emotion = findViewById(R.id.emtion);
        normal = findViewById(R.id.normal);
        pose = findViewById(R.id.pose);
        modeLayout = findViewById(R.id.modeLayout);
        modeLayout.bringToFront();
        selectedMode = findViewById(R.id.selectedMode);

        emotion.setOnClickListener(new View.OnClickListener() {
            // 표정 모드 클릭시
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(0);
            }
        });

        normal.setOnClickListener(new View.OnClickListener() {
            // 일반 모드 클릭시
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(1);
            }
        });

        pose.setOnClickListener(new View.OnClickListener() {
            // 자세 모드 클릭시
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(2);
            }
        });

        // 여기부터 모드 변경에 따른 UI 변경에 대한 코드
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int prePosition = 1;    // 이전 페이지를 나타내기 위한 변수

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                // 모드가 변경되면 모드를 나타내는 텍스트뷰 위치를 이동하는 메소드
                ScaleAnimation increase, decrease;
                ObjectAnimator trans_anim;
                Handler handler = new Handler();
                float scale = (float) emotion.getWidth() / normal.getWidth();
                float trans = emotion.getWidth() - (float) normal.getWidth() / 3;
                switch (position) {
                    case 0:
                        if (prePosition == 1) {
                            increase = new ScaleAnimation(1, scale, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            increase.setDuration(200);
                            increase.setFillAfter(true);
                            selectedMode.startAnimation(increase);
                        }
                        trans_anim = ObjectAnimator.ofFloat(modeLayout, "translationX", trans);
                        trans_anim.setDuration(200);
                        trans_anim.start();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                popupSelectItem(emotionSelect);
                            }
                        }, 500);
                        break;
                    case 1:
                        decrease = new ScaleAnimation(scale, 1, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        decrease.setDuration(200);
                        decrease.setFillAfter(true);
                        selectedMode.startAnimation(decrease);
                        trans_anim = ObjectAnimator.ofFloat(modeLayout, "translationX", 0f);
                        trans_anim.start();
                        break;
                    case 2:
                        if (prePosition == 1) {
                            increase = new ScaleAnimation(1, scale, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            increase.setDuration(200);
                            increase.setFillAfter(true);
                            selectedMode.startAnimation(increase);
                        }
                        trans_anim = ObjectAnimator.ofFloat(modeLayout, "translationX", -trans);
                        trans_anim.setDuration(200);
                        trans_anim.start();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                popupSelectItem(poseSelect);
                            }
                        }, 500);
                        break;
                    default:
                        break;
                }
                prePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // 줌 관련 코드
        zoomSeekBar = findViewById(R.id.zoomSeekBar);
        zoomTextView = findViewById(R.id.zoomTextView);
        zoomLayout = findViewById(R.id.zoomLayout);
        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float zoom;
                if (progress < 10)
                    zoom = 1 + (float) progress / 10;
                else
                    zoom = (float) (progress + 1) / 10;
                String zoomMag = "× " + zoom;
                zoomTextView.setText(zoomMag);
                if (cameraView.getMaxZoom() < progress)
                    progress = cameraView.getMaxZoom();
                else if (progress < 0)
                    progress = 0;
                cameraView.handleZoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        focusOval = findViewById(R.id.focusOval);
    }

    void startCamera() {
        // 서피스뷰 객체를 생성하여 카메라 미리보기 시작
        cameraView = new CameraSurfaceView(this, this, cameraFacing, surfaceView);
    }

    // 여기부터 권한 요청 메소드 3개
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    public void onDenied(int i, @NonNull String[] strings) {
    }

    public void onGranted(int i, @NonNull String[] strings) {
    }
    // 여기 까지 권한 요청 메소드 3개

    private void flashToastShow(String flashMode) {
        // 플래시 설정을 변경할 때 나오는 토스트 메시지
        final Toast toastView = Toast.makeText(getApplicationContext(), "플래시 : " + flashMode, Toast.LENGTH_SHORT);
        toastView.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, menuLayout.getHeight() + 30);
        toastView.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toastView.cancel();
            }
        }, 1000);
    }

    private void popupSelectItem(int requestCode) {
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivityForResult(intent, requestCode);
    }

    public void onCaptureButton(View v) {
        // 촬영 버튼 터치 메소드
        cameraView.capture();
    }

    public void onGalleryButton(View v) {
        // 갤러리 버튼 터치 메소드
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.gallery_scale);
        v.startAnimation(anim);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivity(intent);
    }

    public void onChangeFacingButton(View v) {
        // 카메라 전환 버튼 터치 메소드
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.selfie_rotate);
        v.startAnimation(anim);
        cameraFacing = (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;   // 현재 카메라의 상태에 따라 전면 또는 후면으로 전환
        cameraView.changeCamera(cameraFacing);
    }

    public void onSettingButton(View v) {
        // 설정 버튼 터치 메소드
        popupSelectItem(setting);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // 설정 버튼에서 선택한 아이템의 코드를 받아옴
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED) {
            itemID = resultCode;
        }

        if (requestCode == emotionSelect) {
            if (resultCode == RESULT_CANCELED)
                modeItem1.setTextView("표정이 선택되지 않았습니다.", 0xFF0000);
        }

        else if (requestCode == poseSelect) {
            if (resultCode == RESULT_CANCELED)
                modeItem3.setTextView("자세가 선택되지 않았습니다.", 0xFF0000);
        }
    }
}