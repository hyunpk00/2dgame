package io.game.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Sprite sprite;
    private Texture texture;
    private float radius;

    // 물리 상태
    private float px, py;  // 위치
    private float vx, vy;  // 속도
    private float rotation = 0f;

    // 구르는 효과를 위한 이전 위치
    private float prevX, prevY;

    // 플릭 쿨타임
    private float flickCooldown = 0.8f;      // 쿨타임 시간 (초)
    private float flickCooldownTimer = 0f;   // 현재 쿨타임 타이머

    public Player(String texturePath, float scale, float startX, float startY) {
        texture = new Texture(texturePath);
        sprite = new Sprite(texture);

        radius = (texture.getWidth() * scale) / 2f;
        sprite.setSize(radius * 2f, radius * 2f);
        sprite.setOriginCenter();

        // 초기 위치 / 속도
        px = startX;
        py = startY;
        vx = 0f;
        vy = 0f;

        // 이전 위치 초기화
        prevX = startX;
        prevY = startY;
    }

    public void render(SpriteBatch batch) {
        sprite.setPosition(px - radius, py - radius);

        // 구르는 효과
        sprite.setRotation(rotation);

        sprite.draw(batch);
    }

    public void update(float dt) {
        // 쿨타임 감소
        if (flickCooldownTimer > 0) {
            flickCooldownTimer -= dt;
            if (flickCooldownTimer < 0) {
                flickCooldownTimer = 0;
            }
        }

        // 구르는 효과 계산
        // 이동 거리 계산
        float dx = px - prevX;
        float dy = py - prevY;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.1f) { // 최소 이동 거리 이상일 때만 회전
            // 이동 방향 각도 (라디안)
            float moveAngle = (float)Math.atan2(dy, dx);

            // 공의 둘레
            float circumference = 2f * (float)Math.PI * radius;

            // 회전 각도 계산 (도 단위)
            // 이동 거리에 비례해서 회전
            float rotationChange = (distance / circumference) * 360f;

            // 이동 방향에 수직으로 회전 (공이 구르는 방향)
            // moveAngle에 90도를 더해서 구르는 축을 만듦
            rotation += rotationChange;

            // 회전 각도를 0~360 범위로 유지
            rotation = rotation % 360f;
        }

        // 현재 위치를 이전 위치로 저장
        prevX = px;
        prevY = py;
    }

    public void addImpulse(float ix, float iy) {
        // 쿨타임 체크
        if (flickCooldownTimer > 0) {
            return; // 쿨타임 중이면 무시
        }

        vx += ix;
        vy += iy;

        // 쿨타임 시작
        flickCooldownTimer = flickCooldown;
    }

    // Getters
    public float getX() { return px; }
    public float getY() { return py; }
    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public float getRadius() { return radius; }
    public float getFlickCooldown() { return flickCooldown; }
    public float getFlickCooldownTimer() { return flickCooldownTimer; }
    public float getFlickCooldownPercent() {
        return (flickCooldownTimer / flickCooldown) * 100f;
    }
    public boolean isFlickReady() { return flickCooldownTimer <= 0; }

    // Setters
    public void setX(float x) { this.px = x; }
    public void setY(float y) { this.py = y; }
    public void setVx(float vx) { this.vx = vx; }
    public void setVy(float vy) { this.vy = vy; }
    public void setFlickCooldown(float cooldown) { this.flickCooldown = cooldown; }

    public void dispose() {
        texture.dispose();
    }
}
