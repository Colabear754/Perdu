package kr.ac.pknu.perdu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
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

import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import kr.ac.pknu.perdu.adapter.AspectRatioSpinnerAdapter;
import kr.ac.pknu.perdu.adapter.FlashSpinnerAdapter;
import kr.ac.pknu.perdu.adapter.ModePagerAdapter;
import kr.ac.pknu.perdu.itemlist.ListItem;
import kr.ac.pknu.perdu.mode.ModeHelper;
import kr.ac.pknu.perdu.mode.ModeItem1;
import kr.ac.pknu.perdu.mode.ModeItem2;
import kr.ac.pknu.perdu.mode.ModeItem3;

public class CameraPreviewActivity extends AppCompatActivity implements AutoPermissionsListener, CameraApiCallback {
    private static final String TAG = "Perdu";
    private static final int REQUEST_SETTING = 1001;
    private static final int REQUEST_EMOTION_SELECT = 1002;
    private static final int REQUEST_POSE_SELECT = 1003;
    private static final int EMOTION = 100001;
    private static final int EMOTION_ITEM1 = 100001101; // 웃는 표정
    private static final int EMOTION_ITEM2 = 100001102; // 윙크
    private static final int EMOTION_ITEM3 = 100001103; // 놀람
    private static final int EMOTION_ITEM4 = 100001104; // 메롱
    private static final int POSE = 200001;
    private static final int POSE_ITEM1 = 200001101;
    private static final int POSE_ITEM2 = 200001102;
    private static final int POSE_ITEM3 = 200001103;
    private static final int POSE_ITEM4 = 200001104;

    private CameraSurfaceView cameraView;   // 카메라 미리보기 뷰
    private SurfaceView surfaceView;    // 미리보기를 표시하기 위한 서피스뷰

    private final int[] flashIcons = {R.drawable.flash_auto_icon, R.drawable.flash_on_icon, R.drawable.flash_off_icon};    // 플래시 아이콘
    private final String[] aspectRatioIcons = {"3 : 4", "9 : 16", "1 : 1"}; // 화면 비율 아이콘

    private ModePager pager;    // 뷰페이저
    private TextView emotion;   // 표정 모드 텍스트뷰
    private TextView normal;    // 일반 모드 텍스트뷰
    private TextView pose;  // 자세 모드 텍스트뷰
    private ImageView selectedMode; // 선택된 모드를 표시
    private LinearLayout modeLayout;    // 모드 변경 레이아웃
    private final ModeItem1 modeItem1 = new ModeItem1();
    private final ModeItem2 modeItem2 = new ModeItem2();
    private final ModeItem3 modeItem3 = new ModeItem3();

    private ConstraintLayout menuLayout;    // 상단 메뉴 레이아웃
    private ImageView focusOval;    // 수동 초점 맞출 때 나오는 원

    private SeekBar zoomSeekBar;    // 줌을 나타내는 시크바
    private TextView zoomTextView;  // 줌 배율을 나타내는 텍스트뷰
    private LinearLayout zoomLayout;    // 줌 관련 내용을 보여주기 위한 레이아웃

    protected static int cameraFacing;    // 카메라 전환 변수
    private boolean selected;   // 설정에서 아이템이 선택되어 모드가 변경되었는지 확인하기 위한 논리 변수
    private boolean isAutoCaptureStart; // 자동 캡쳐가 시작되었는지 확인하기 위한 논리 변수
    private boolean inCaptureProgress = false;

    private ListItem selectedEmotion;  // 선택된 표정
    private ListItem selectedPose;  // 선택된 자세
    protected static int rotation;

    private final byte[] autoCaptureLock = new byte[]{0};

    private AutoCaptureThread autoCaptureThread = null;

    private int selectedItemID = -1;

    public GraphicOverlay overlay;
    private FaceDetector faceDetector;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.previewFrame);

        AutoPermissions.Companion.loadAllPermissions(this, 101);    // 자동 권한 요청
        cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;    // 후면 카메라를 기본 카메라로 설정

        overlay = findViewById(R.id.graphicOverlay);
        startCamera();  // 미리보기 시작

        //////////////////////////////////////////////
        // 여기부터 상단 버튼을 위한 스피너 코드
        //////////////////////////////////////////////
        menuLayout = findViewById(R.id.menuLayout);
        Spinner flashSpinner = findViewById(R.id.flashSpinner);
        Spinner aspectRatioSpinner = findViewById(R.id.aspectratioSpinner);

        FlashSpinnerAdapter flashSpinnerAdapter = new FlashSpinnerAdapter(getApplicationContext(), flashIcons);
        AspectRatioSpinnerAdapter aspectRatioSpinnerAdapter = new AspectRatioSpinnerAdapter(getApplicationContext(), aspectRatioIcons);
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
        aspectRatioSpinner.setAdapter(aspectRatioSpinnerAdapter);

        //////////////////////////////////////////////
        // 여기부터 모드 변경을 위한 뷰페이저 코드
        //////////////////////////////////////////////
        pager = findViewById(R.id.modePager);
        pager.setOffscreenPageLimit(3);
        ModePagerAdapter adapter = new ModePagerAdapter(getSupportFragmentManager());
        adapter.addItem(modeItem1);
        adapter.addItem(modeItem2);
        adapter.addItem(modeItem3);
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        pager.bringToFront();

        //////////////////////////////////////////////
        // 여기부터 텍스트를 터치하여 모드 변경을 하기 위한 코드
        //////////////////////////////////////////////
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

        //////////////////////////////////////////////
        // 여기부터 모드 변경에 따른 UI 변경에 대한 코드
        //////////////////////////////////////////////
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
                selectedItemID = -1;
                switch (position) {
                    case 0:
                        if (prePosition == 1) {
                            increase = new ScaleAnimation(1, scale, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                            increase.setDuration(200);
                            increase.setFillAfter(true);
                            selectedMode.startAnimation(increase);
                        }
                        selectedPose = null;
                        trans_anim = ObjectAnimator.ofFloat(modeLayout, "translationX", trans);
                        trans_anim.setDuration(200);
                        trans_anim.start();
                        modeItem1.setTextVisible(false);
                        if (!selected) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    popupSelectItem(REQUEST_EMOTION_SELECT);
                                }
                            }, 500);
                        }
                        else
                            selected = false;
                        break;
                    case 1:
                        stopAutoCapture();
                        faceDetector.stopDetecting();
                        selectedEmotion = null;
                        selectedPose = null;
                        selected = false;
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
                        stopAutoCapture();
                        faceDetector.stopDetecting();
                        selectedEmotion = null;
                        trans_anim = ObjectAnimator.ofFloat(modeLayout, "translationX", -trans);
                        trans_anim.setDuration(200);
                        trans_anim.start();
                        modeItem3.setTextVisible(false);
                        if (!selected) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    popupSelectItem(REQUEST_POSE_SELECT);
                                }
                            }, 500);
                        }
                        else
                            selected = false;
                        break;
                    default:
                        break;
                }
                prePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        //////////////////////////////////////////////
        // 줌 관련 코드
        //////////////////////////////////////////////
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

    @Override
    protected void onStop() {
        super.onStop();
        faceDetector.stopDetecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (selectedEmotion != null || selectedPose != null) {
            faceDetector.startDetecting();
            startAutoCapture();
        }
    }

    //////////////////////////////////////////////
    // 서피스뷰 객체를 생성하여 카메라 미리보기 시작
    //////////////////////////////////////////////
    private void startCamera() {
        cameraView = new CameraSurfaceView(this, this, cameraFacing, surfaceView, this);

        faceDetector = new FaceDetector(this);
        faceDetector.setOverlay(overlay);
    }

    @Override
    public void onPreviewFrameCallback(byte[] data, Camera camera) {
        camera.addCallbackBuffer(data);
        faceDetector.receiveFrameData(data);
    }

    @Override
    public void onNotSupportErrorTip(String message) {

    }

    @Override
    public void onCameraInit(Camera camera) {

    }

    //////////////////////////////////////////////
    // 여기부터 권한 요청 메소드 3개
    //////////////////////////////////////////////
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    public void onDenied(int i, @NonNull String[] strings) {
    }

    public void onGranted(int i, @NonNull String[] strings) {
    }

    //////////////////////////////////////////////
    // 플래시 설정을 변경할 때 나오는 토스트 메시지
    //////////////////////////////////////////////
    private void flashToastShow(String flashMode) {
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

    //////////////////////////////////////////////
    // 설정창 팝업 메소드
    //////////////////////////////////////////////
    private void popupSelectItem(int requestCode) {
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        intent.putExtra("mode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    //////////////////////////////////////////////
    // 버튼 터치 메소드들
    //////////////////////////////////////////////
    public void onCaptureButton(View v) {
        cameraCapture();
    }

    public void onGalleryButton(View v) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.gallery_scale);
        v.startAnimation(anim);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivity(intent);
    }

    public void onChangeFacingButton(View v) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.selfie_rotate);
        v.startAnimation(anim);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraFacing = (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;   // 현재 카메라의 상태에 따라 전면 또는 후면으로 전환
                cameraView.changeCamera(cameraFacing);
            }
        }, 500);
    }

    public void onSettingButton(View v) {
        // 현재 페이지에 따라서 요청코드가 달라짐
        switch (pager.getCurrentItem()) {
            case 0:
                popupSelectItem(REQUEST_EMOTION_SELECT);
                break;
            case 1:
                popupSelectItem(REQUEST_SETTING);
                break;
            case 2:
                popupSelectItem(REQUEST_POSE_SELECT);
                break;
            default: break;
        }
    }

    public void cameraCapture() {
        if (!inCaptureProgress) {
            inCaptureProgress = true;
            cameraView.capture();
        }
    }

    //////////////////////////////////////////////
    // 얼굴 탐지를 위한 회전각
    //////////////////////////////////////////////
    private int getRotationCompensation() {
        int deviceRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        rotationCompensation = (rotationCompensation + 270) % 360;
        int result;
        switch (rotationCompensation) {
            case 0:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                result = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 180:
                result = FirebaseVisionImageMetadata.ROTATION_180;
                break;
            case 270:
                result = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            default:
                result = FirebaseVisionImageMetadata.ROTATION_0;
                Log.e(TAG, "Bad rotation value: " + rotationCompensation);
        }

        return result;
    }

    //////////////////////////////////////////////
    // 단말기가 회전할 때마다 회전각 계산
    //////////////////////////////////////////////
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        rotation = getRotationCompensation();
    }

    //////////////////////////////////////////////
    // 자동캡쳐를 위한 스레드
    //////////////////////////////////////////////
    class AutoCaptureThread extends Thread {
        public AutoCaptureThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                synchronized (autoCaptureLock) {
                    if (selectedEmotion == null && selectedPose == null) {
                        if (!isAutoCaptureStart)
                            return;

                        try {
                            autoCaptureLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (selectedEmotion != null) {
                        FirebaseVisionFace detectedFace = faceDetector.getDetectedFace();
                        if (detectedFace != null) {
                            if (checkEmotion(detectedFace, selectedItemID)) {
                                faceDetector.stopDetecting();

                                try {
                                    cameraCapture();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                faceDetector.startDetecting();

                                inCaptureProgress = false;
                            }
                        }
                    }
                }

                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startAutoCapture() {
        isAutoCaptureStart = true;
        autoCaptureThread = new AutoCaptureThread("AutoCaptureThread");
        autoCaptureThread.start();
    }

    private void stopAutoCapture() {
        isAutoCaptureStart = false;
        if (autoCaptureThread != null) {
            try {
                autoCaptureThread.join(1000);
                autoCaptureThread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //////////////////////////////////////////////
    // 페이저 터치 이벤트로 포커스와 줌을 조절하기 위한 메소드
    //////////////////////////////////////////////
    public void setFocus(@NonNull ModeHelper helper) {
        Handler handler = new Handler();
        focusOval.setX(helper.getX());
        focusOval.setY(helper.getY());
        focusOval.setVisibility(helper.getVisibility());
        cameraView.focus();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                focusOval.setVisibility(View.GONE);
            }
        }, 2000);
    }

    public void setZoom(@NonNull ModeHelper helper) {
        switch (helper.getVisibility()) {
            case View.VISIBLE:
                zoomLayout.setVisibility(helper.getVisibility());
                zoomSeekBar.setProgress(helper.getTouchZoom());
                break;
            case View.GONE:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        zoomLayout.setVisibility(View.GONE);
                    }
                }, 2000);
                break;
            default: break;
        }
    }

    //////////////////////////////////////////////
    // 표정을 체크하여 참, 거짓을 반환
    //////////////////////////////////////////////
    private boolean checkEmotion(FirebaseVisionFace face, int item) {
        switch (item) {
            case EMOTION_ITEM1:
                return face.getSmilingProbability() >= 0.85f;
            case EMOTION_ITEM2:
                return (face.getLeftEyeOpenProbability() <= 0.15f && face.getRightEyeOpenProbability() > 0.8f) || (face.getRightEyeOpenProbability() <= 0.15f && face.getLeftEyeOpenProbability() > 0.8f);
            default:
                return false;
        }
    }

    //////////////////////////////////////////////
    // 설정 버튼에서 선택한 아이템의 객체를 받아와 그에 따른 결과 수행
    //////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                ListItem item =  data.getParcelableExtra("item");
                if (item != null) {
                    switch (item.getMode()) {
                        case EMOTION:
                            selectedEmotion = item;
                            modeItem1.setTextVisible(false);
                            selected = true;
                            pager.setCurrentItem(0);

                            switch (item.getItemID()) {
                                case 101:
                                    modeItem1.setTextView("현재 선택된 표정은 웃는 표정입니다.", 0xFF00EE00);
                                    modeItem1.setTextVisible(true);
                                    break;
                                case 102:
                                    modeItem1.setTextView("현재 선택된 표정은 윙크입니다.", 0xFF00EE00);
                                    modeItem1.setTextVisible(true);
                                    break;
                                case 103:
                                    modeItem1.setTextView("현재 선택된 표정은 놀란 표정입니다.", 0xFF00EE00);
                                    modeItem1.setTextVisible(true);
                                    break;
                                case 104:
                                    modeItem1.setTextView("현재 선택된 표정은 메롱입니다.", 0xFF00EE00);
                                    modeItem1.setTextVisible(true);
                                    break;
                                default:
                                    modeItem1.setTextVisible(false);
                                    break;
                            }

                            faceDetector.startDetecting();
                            startAutoCapture();
                            break;
                        case POSE:
                            selectedPose = item;
                            modeItem3.setTextVisible(false);
                            selected = true;
                            pager.setCurrentItem(2);
                            break;
                        default: break;
                    }

                    selectedItemID = Integer.parseInt(item.getMode() + "" + item.getItemID());
                    Log.i(TAG, "선택된 아이템 ID : " + selectedItemID);
                }
                break;
            case RESULT_CANCELED:
                if (requestCode == REQUEST_EMOTION_SELECT && selectedItemID == -1) {
                    faceDetector.stopDetecting();
                    stopAutoCapture();
                    selectedEmotion = null;
                    modeItem1.setTextView("표정이 선택되지 않았습니다.", 0xFFEE0000);
                    modeItem1.setTextVisible(true);
                }

                else if (requestCode == REQUEST_POSE_SELECT && selectedItemID == -1) {
                    faceDetector.stopDetecting();
                    stopAutoCapture();
                    selectedPose = null;
                    modeItem3.setTextView("자세가 선택되지 않았습니다.", 0xFFEE0000);
                    modeItem3.setTextVisible(true);
                }
                break;
            default: break;
        }
    }
}