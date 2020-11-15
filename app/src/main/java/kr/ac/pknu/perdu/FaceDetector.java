package kr.ac.pknu.perdu;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FaceDetector {
    private static final String TAG = "FaceDetector";
    private GraphicOverlay overlay;
    private Context context;

    private int previewWidth;
    private int previewHeight;
    private byte[] data = null;
    private byte[] imageBuffer = null;
    private volatile boolean isStart = false;

    private final byte[] detectLock = new byte[]{0};

    private FaceDetectThread detectThread = null;

    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionImageMetadata metadata;
    private FirebaseVisionFaceDetector detector;
    private FirebaseVisionImage visionImage;
    private FirebaseVisionFace detectedFace;

    public FaceDetector(Context context) {
        this.context = context;
        init();
    }

    //////////////////////////////////////////////////
    // 초기화
    //////////////////////////////////////////////////
    private void init() {
        previewWidth = CameraSurfaceView.DEFAULT_PREVIEW_WIDTH;
        previewHeight = CameraSurfaceView.DEFAULT_PREVIEW_HEIGHT;
        imageBuffer = new byte[previewWidth * previewHeight * 3 / 2];
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .enableTracking()
                .build();

        metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(previewWidth)
                .setHeight(previewHeight)
                .setRotation(CameraPreviewActivity.rotation)
                .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public void setOverlay(GraphicOverlay overlay) {
        this.overlay = overlay;
        overlay.setCameraInfo(previewWidth, previewHeight, CameraPreviewActivity.cameraFacing);
    }

    public void startDetecting() {
        isStart = true;
        if (detectThread == null) {
            detectThread = new FaceDetectThread("FaceDetectorThread");
            detectThread.start();
        }
    }

    public void stopDetecting() {
        isStart = false;
        overlay.clear();
        if (detectThread != null) {
            try {
                detectThread.join(1000);
                detectThread = null;
                detector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public FirebaseVisionFace getDetectedFace() {
        return detectedFace;
    }

    //////////////////////////////////////////////////
    // 카메라 데이터 수신 및 처리
    //////////////////////////////////////////////////
    public void receiveFrameData(byte[] bytes) {
        synchronized (detectLock) {
            if (data == null) {
                data = bytes;
                detectLock.notifyAll();
            }
        }
    }

    class FaceDetectThread extends Thread {
        public FaceDetectThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                synchronized (detectLock) {
                    if (!isStart) {
                        return;
                    }
                    if (data == null) {
                        try {
                            detectLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        System.arraycopy(data, 0, imageBuffer, 0, data.length);
                        data = null;
                        visionImage = FirebaseVisionImage.fromByteArray(imageBuffer, metadata);
                    }
                }
                detector.detectInImage(visionImage).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
            }
        }
    }

    /**
     * detect success callback
     */
    private OnSuccessListener<List<FirebaseVisionFace>> onSuccessListener = new OnSuccessListener<List<FirebaseVisionFace>>() {
        @Override
        public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
            overlay.clear();
            for (int i = 0; i < firebaseVisionFaces.size(); ++i) {
                FirebaseVisionFace face = firebaseVisionFaces.get(i);
                FaceGraphic faceGraphic = new FaceGraphic(overlay);
                overlay.add(faceGraphic);
                faceGraphic.updateFace(face, CameraPreviewActivity.cameraFacing);
                detectedFace = face;
            }
        }
    };

    /**
     * detect fail callback
     */
    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Log.e(TAG, "onFailure: face detect error");
        }
    };
}
