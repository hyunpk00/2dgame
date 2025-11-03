package io.game.test;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraManager {
    private OrthographicCamera camera;

    // 줌 설정
    private float baseZoom = 0.8f; // 기본 줌 레벨 (작을수록 확대)
    private float targetZoom;
    private float zoomSpeed = 2.0f;

    // 카메라 흔들림
    private boolean shaking = false;
    private float shakeIntensity = 0f;
    private float shakeDuration = 0f;
    private float shakeTimer = 0f;
    private Vector2 shakeOffset = new Vector2();

    // 부드러운 추적
    private float smoothSpeed = 0.1f;

    // 월드 경계
    private float worldWidth;
    private float worldHeight;

    public CameraManager(OrthographicCamera camera, float worldWidth, float worldHeight) {
        this.camera = camera;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.targetZoom = baseZoom;
        camera.zoom = baseZoom;
    }

    public void update(float dt, float targetX, float targetY) {
        // 줌 부드럽게 조정
        if (Math.abs(camera.zoom - targetZoom) > 0.01f) {
            camera.zoom += (targetZoom - camera.zoom) * zoomSpeed * dt;
        }

        // 카메라 흔들림 처리
        if (shaking) {
            shakeTimer += dt;

            if (shakeTimer < shakeDuration) {
                // 랜덤 흔들림 오프셋
                float progress = shakeTimer / shakeDuration;
                float currentIntensity = shakeIntensity * (1f - progress); // 점점 약해짐

                shakeOffset.x = (float)(Math.random() * 2 - 1) * currentIntensity;
                shakeOffset.y = (float)(Math.random() * 2 - 1) * currentIntensity;
            } else {
                // 흔들림 종료
                shaking = false;
                shakeOffset.set(0, 0);
            }
        }

        // 플레이어를 부드럽게 추적
        float effectiveWidth = worldWidth * camera.zoom;
        float effectiveHeight = worldHeight * camera.zoom;

        float minX = effectiveWidth / 2f;
        float maxX = worldWidth - effectiveWidth / 2f;
        float minY = effectiveHeight / 2f;
        float maxY = worldHeight - effectiveHeight / 2f;

        float clampedX = MathUtils.clamp(targetX, minX, maxX);
        float clampedY = MathUtils.clamp(targetY, minY, maxY);

        // 부드러운 이동
        camera.position.x += (clampedX - camera.position.x) * smoothSpeed;
        camera.position.y += (clampedY - camera.position.y) * smoothSpeed;

        // 흔들림 적용
        camera.position.x += shakeOffset.x;
        camera.position.y += shakeOffset.y;

        camera.update();
    }

    // 카메라 흔들림
    public void shake(float intensity, float duration) {
        this.shaking = true;
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.shakeTimer = 0f;
    }

    // 게임오버 강한 흔들림
    public void gameOverShake() {
        shake(50f, 0.8f); // 강도 50, 0.8초
    }


    // Getters
    public float getZoom() { return camera.zoom; }
    public float getCurrentZoom() { return camera.zoom; }
    public boolean isShaking() { return shaking; }
    public OrthographicCamera getCamera() { return camera; }
}
