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

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    String TAG = "CameraSurfaceView";

    private int mCameraID;
    private SurfaceHolder surfaceHolder;
    private android.hardware.Camera camera = null;
    private Camera.CameraInfo cameraInfo;
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
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // surfaceCreated에서 오픈한 카메라를 이용해 surfaceView에 출력
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);

        cameraInfo = info;
        displayOrientation = appCompatActivity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = calculateOrientation(cameraInfo, displayOrientation);
        camera.setDisplayOrientation(orientation);
        // 자동 초점 기능
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            Log.d(TAG, "카메라 미리보기 시작.");
            isPreview = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            if (isPreview)
                camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            isPreview = false;
        }
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
        if (camera != null) {
            if (isPreview)
                camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            isPreview = false;
        }
        // 전환된 카메라를 새로 열고 프리뷰 시작
        try {
            camera = Camera.open(cameraID);
        } catch (Exception e) {
            Log.e(TAG, "카메라" + cameraID + "사용 불가." + e.getMessage());
        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);

        cameraInfo = info;
        displayOrientation = appCompatActivity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = calculateOrientation(cameraInfo, displayOrientation);
        camera.setDisplayOrientation(orientation);
        // 자동 초점 기능
        Camera.Parameters parameters = camera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);

        surfaceHolder.addCallback(this);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
            Log.d(TAG, (cameraID == 0 ? "후면 " : "전면 ") + "카메라 미리보기 시작.");
            isPreview = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}