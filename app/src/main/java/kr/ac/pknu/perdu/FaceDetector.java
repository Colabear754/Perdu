package kr.ac.pknu.perdu;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

public class FaceDetector {
    private static final String TAG = "FaceDetector";
    private GraphicOverlay overlay;
    private Context context;

    private int previewWidth;
    private int previewHeight;
    private byte[] date = null;
    private byte[] imageBuffer = null;
    private volatile boolean isStart = false;

    private final byte[] lock = new byte[]{0};

    private FaceDetectThread detectThread = null;

    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionImageMetadata metadata;
    private FirebaseVisionFaceDetector detector;
    private FirebaseVisionImage visionImage;

    public FaceDetector(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        previewWidth = 2960;
        previewHeight = 1440;
        imageBuffer = new byte[previewWidth * previewHeight * 3 / 2];
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .enableTracking()
                .build();

        metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(2960)
                .setHeight(1440)
                .setRotation(CameraSurfaceView.getPreviewRotation())
                .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public void setOverlay(GraphicOverlay overlay) {
        this.overlay = overlay;
        overlay.setCameraInfo(previewWidth, previewHeight, CameraPreviewActivity.cameraFacing);
    }

    public void startDetector() {
        isStart = true;
        if (detectThread == null) {
            detectThread = new FaceDetectThread("FaceDetectorThread");
            detectThread.start();
        }
    }

    public void stopDetector() {
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

    //////////////////////////////////////////////////
    // 카메라 데이터 수신 및 처리
    //////////////////////////////////////////////////

    public void receiveFrameData(byte[] bytes) {
        synchronized (lock) {
            if (date == null) {
                date = bytes;
                lock.notifyAll();
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
                synchronized (lock) {
                    if (!isStart) {
                        return;
                    }
                    if (date == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    System.arraycopy(date, 0, imageBuffer, 0, date.length);
                    date = null;
                    visionImage = FirebaseVisionImage.fromByteArray(imageBuffer, metadata);
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
