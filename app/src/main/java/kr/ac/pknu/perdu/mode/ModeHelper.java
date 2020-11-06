package kr.ac.pknu.perdu.mode;

public class ModeHelper {
    float x = 0f, y = 0f; // 포커스 변수
    int touchZoom = 0;  // 줌 변수
    int visibility; // 공통 변수

    public ModeHelper(float x, float y, int visibility) {
        this.x = x;
        this.y = y;
        this.visibility = visibility;
    }

    public ModeHelper(int touchZoom, int visibility) {
        this.touchZoom = touchZoom;
        this.visibility = visibility;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getTouchZoom() {
        return touchZoom;
    }

    public void setTouchZoom(int touchZoom) {
        this.touchZoom = touchZoom;
    }

    public int getVisibility() {
        return visibility;
    }
}