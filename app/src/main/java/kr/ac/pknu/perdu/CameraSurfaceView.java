package kr.ac.pknu.perdu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraSurfaceView extends ViewGroup implements SurfaceHolder.Callback {
    String TAG = "CameraSurfaceView";

    private int mCameraID;
    private SurfaceHolder surfaceHolder;
    private android.hardware.Camera camera = null;
    private Camera.CameraInfo cameraInfo;
    public List<Camera.Size> previewSizesList;   // 미리보기 사이즈를 요소로 갖는 배열
    private Camera.Size previewSize;    // 미리보기 사이즈를 저장
    private int displayOrientation;
    private boolean isPreview = false;  // 미리보기가 실행 중인지 확인하는 변수
    private AppCompatActivity appCompatActivity;

    public CameraSurfaceView(Context context, AppCompatActivity activity, int cameraID, SurfaceView sView) {
        super(context);

        appCompatActivity = activity;
        mCameraID = cameraID;
        surfaceHolder = sView.getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 카메라 열기를 시도하고 실패하면 에러로그 출력
        try {
            camera = Camera.open(mCameraID);
        } catch (Exception e) {
            Log.e(TAG, "카메라" + mCameraID + "사용 불가." + e.getMessage());
        }
        previewSizesList = camera.getParameters().getSupportedVideoSizes();
        startPreview(mCameraID);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 프리뷰가 변경되거나 회전할 경우 동작
        if (surfaceHolder == null) {
            Log.e(TAG, "프리뷰가 존재하지 않음");
            return;
        }

        try {
            camera.stopPreview();
            Log.d(TAG, "프리뷰 중지");
        } catch (Exception e) {
            Log.e(TAG, "프리뷰 중지 에러 : " + e.getMessage());
        }

        int orientation = calculateOrientation(cameraInfo, displayOrientation);
        camera.setDisplayOrientation(orientation);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            Log.d(TAG, "미리보기 재개");
        } catch (Exception e) {
            Log.e(TAG, "프리뷰 시작 에러 : " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        finishPreview();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (previewSizesList != null)
            previewSize = getPreviewSize(previewSizesList, width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (previewSize != null) {
                previewWidth = previewSize.width;
                previewHeight = previewSize.height;
            }

            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,(width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2, width, (height + scaledChildHeight) / 2);
            }
        }
    }

    public Camera.Size getPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Camera.Size optimalSize = null;
        double minDiff;
        int targetHeight;

        if (sizes == null)
            return null;

        minDiff = Double.MAX_VALUE;
        targetHeight = height;
        // 서피스뷰 사이즈와 맞는 화면 비율과 사이즈를 구함
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // 맞는 화면 비율이 없으면 요청을 무시
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    private int calculateOrientation(Camera.CameraInfo info, int rotation) {
        // 프리뷰와 사진을 회전시킬 각도를 계산하여 반환
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else
            result = (info.orientation - degrees + 360) % 360;

        return result;
    }

    public void capture() {
        // 사진 촬영
        if (camera != null)
            camera.takePicture(null, null, pngCallback);
    }

    Camera.PictureCallback pngCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 촬영한 사진을 비트맵이미지로 만들어 저장
            int orientation = calculateOrientation(cameraInfo, displayOrientation);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] currentData = stream.toByteArray();
            new SaveImageTask().execute(currentData);
        }
    };

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {
        // 사진 저장
        @Override
        protected Void doInBackground(byte[]... bytes) {
            boolean mkdirs;
            FileOutputStream outStream;
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            try {
                File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Perdu");
                if (!path.exists()) {
                    mkdirs = path.mkdirs();
                    if (!mkdirs)
                        Log.d(TAG, path.toString() + " 디렉토리 생성 실패.");
                }
                String fileName = "Perdu_" + timeStamp + ".png";
                File outputFile = new File(path, fileName);

                outStream = new FileOutputStream(outputFile);
                outStream.write(bytes[0]);
                outStream.flush();
                outStream.close();

                Log.d("Perdu", "onPictureTaken - wrote bytes: " + bytes.length + " to " + outputFile.getAbsolutePath());

                camera.startPreview();
                // 갤러리에 반영
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(outputFile));
                getContext().sendBroadcast(mediaScanIntent);

                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    Log.d(TAG, "카메라 미리보기 시작.");
                } catch (Exception e) {
                    Log.d(TAG, "카메라 미리보기 시작 오류." + e.getMessage());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public void changeCamera(int cameraID) {
        // 전/후면 카메라를 전환하는 메소드
        // 기존 카메라 프리뷰를 종료하고 카메라를 반환
        finishPreview();
        // 전환된 카메라를 새로 열고 프리뷰 시작
        try {
            camera = Camera.open(cameraID);
        } catch (Exception e) {
            Log.e(TAG, "카메라" + cameraID + "사용 불가." + e.getMessage());
        }

        startPreview(cameraID);
    }

    public void flashControl(String flashMode) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(flashMode);
        camera.setParameters(parameters);
    }

    public void focus() {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {}
        });
    }

    public int getMaxZoom() {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getMaxZoom();
    }

    public void handleZoom(int zoom) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setZoom(zoom);
        camera.setParameters(parameters);
    }

    private void startPreview(int cameraID) {
        // 카메라 ID를 매개변수로 받아 프리뷰를 생성
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);

        cameraInfo = info;
        displayOrientation = appCompatActivity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = calculateOrientation(cameraInfo, displayOrientation);
        camera.setDisplayOrientation(orientation);
        // 자동 초점
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            Log.d(TAG, (cameraID == 0 ? "후면 " : "전면 ") + "카메라 미리보기 시작.");
            isPreview = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishPreview() {
        // 프리뷰를 종료하고 카메라를 반환하는 메소드
        if (camera != null) {
            if (isPreview)
                camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            isPreview = false;
        }
    }
}