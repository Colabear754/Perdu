package kr.ac.pknu.perdu;

import android.hardware.Camera;

public interface CameraApiCallback {
    void onPreviewFrameCallback(byte[] data, Camera camera);
    void onNotSupportErrorTip(String message);

    void onCameraInit(Camera camera);
}
