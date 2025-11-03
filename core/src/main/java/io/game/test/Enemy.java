package io.game.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;

public class Enemy {
    public enum ShootPattern {
        CIRCLE,        // 원형 발사
        AIMED,         // 플레이어 조준
        RANDOM,        // 랜덤 방향
    }

    public enum MovementPattern {
        STATIONARY,    // 고정
        HORIZONTAL,    // 좌우 이동
        VERTICAL,      // 상하 이동
        CIRCLE,        // 원형 이동
        FIGURE_EIGHT   // 8자 이동
    }

    private Sprite sprite;
    private Texture texture;
    private float x, y;
    private float radius;

    // 발사 속성
    private ShootPattern pattern;
    private float shootCooldown;
    private float shootTimer;
    private float bulletSpeed;
    private int bulletsPerShot;

    // 이동 속성
    private MovementPattern movementPattern;
    private float moveSpeed;
    private float startX, startY;  // 초기 위치 (이동 패턴 기준점)
    private float movementRange;   // 이동 범위
    private float movementTime;    // 이동 타이머

    // 패턴 상태
    private float patternAngle; // 나선형 등에 사용

    // 시각 효과
    private Color color;
    private float pulseTime;

    public Enemy(String texturePath, float x, float y, float radius,
                 ShootPattern pattern, float shootCooldown, float bulletSpeed, int bulletsPerShot,
                 MovementPattern movementPattern, float moveSpeed, float movementRange) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.radius = radius;
        this.pattern = pattern;
        this.shootCooldown = shootCooldown;
        this.shootTimer = 0f;
        this.bulletSpeed = bulletSpeed;
        this.bulletsPerShot = bulletsPerShot;
        this.patternAngle = 0f;
        this.pulseTime = 0f;

        // 이동 속성 초기화
        this.movementPattern = movementPattern;
        this.moveSpeed = moveSpeed;
        this.movementRange = movementRange;
        this.movementTime = 0f;

        // 텍스처 로드
        texture = new Texture(texturePath);
        sprite = new Sprite(texture);

        // 스프라이트 설정
        float size = radius * 2f;
        sprite.setSize(size, size);
        sprite.setOriginCenter();
        sprite.setPosition(x - radius, y - radius);

    }

    public void update(float dt) {
        shootTimer += dt;
        pulseTime += dt * 3f;
        movementTime += dt;

        // 이동 패턴 적용
        updateMovement(dt);

        // 펄스 효과
        float pulseFactor = 1f + (float)Math.sin(pulseTime) * 0.15f;
        sprite.setScale(pulseFactor);

        // 회전 효과
        sprite.rotate(30f * dt);

        // 스프라이트 위치 업데이트
        sprite.setPosition(x - radius, y - radius);
    }

    private void updateMovement(float dt) {
        switch (movementPattern) {
            case STATIONARY:
                // 움직이지 않음
                break;

            case HORIZONTAL:
                // 좌우 이동
                float horizontalOffset = (float)Math.sin(movementTime * moveSpeed) * movementRange;
                x = startX + horizontalOffset;
                break;

            case VERTICAL:
                // 상하 이동
                float verticalOffset = (float)Math.sin(movementTime * moveSpeed) * movementRange;
                y = startY + verticalOffset;
                break;

            case CIRCLE:
                // 원형 이동
                float angle = movementTime * moveSpeed;
                x = startX + (float)Math.cos(angle) * movementRange;
                y = startY + (float)Math.sin(angle) * movementRange;
                break;

            case FIGURE_EIGHT:
                // 8자 이동
                float t = movementTime * moveSpeed;
                x = startX + (float)Math.sin(t) * movementRange;
                y = startY + (float)Math.sin(t * 2) * movementRange * 0.5f;
                break;
        }
    }

    // 탄막 발사
    public ArrayList<Bullet> tryShoot(Player player) {
        if (shootTimer < shootCooldown) {
            return null;
        }

        shootTimer = 0f;
        ArrayList<Bullet> bullets = new ArrayList<Bullet>();

        switch (pattern) {
            case CIRCLE:
                bullets = shootCircle();
                break;
            case AIMED:
                bullets = shootAimed(player);
                break;
            case RANDOM:
                bullets = shootRandom();
                break;
        }

        return bullets;
    }

    private ArrayList<Bullet> shootCircle() {
        ArrayList<Bullet> bullets = new ArrayList<Bullet>();
        float angleStep = 360f / bulletsPerShot;

        for (int i = 0; i < bulletsPerShot; i++) {
            float angle = i * angleStep;
            float rad = (float)Math.toRadians(angle);
            float vx = (float)Math.cos(rad) * bulletSpeed;
            float vy = (float)Math.sin(rad) * bulletSpeed;

            Bullet bullet = new Bullet("spike.png", x, y, vx, vy, 8f);
            bullet.setColor(new Color(1f, 1f, 1f, 1f));
            bullets.add(bullet);
        }

        return bullets;
    }


    private ArrayList<Bullet> shootAimed(Player player) {
        ArrayList<Bullet> bullets = new ArrayList<Bullet>();

        if (player == null) return bullets;

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float dirX = dx / distance;
            float dirY = dy / distance;

            Bullet bullet = new Bullet("spike.png", x, y,
                dirX * bulletSpeed, dirY * bulletSpeed, 8f);
            bullet.setColor(new Color(1f, 0.2f, 0.8f, 1f));
            bullets.add(bullet);

            for (int i = 1; i < bulletsPerShot; i++) {
                float spreadAngle = (i - bulletsPerShot / 2) * 15f;
                float rad = (float)Math.toRadians(spreadAngle);
                float cos = (float)Math.cos(rad);
                float sin = (float)Math.sin(rad);

                float newDirX = dirX * cos - dirY * sin;
                float newDirY = dirX * sin + dirY * cos;

                Bullet b = new Bullet("spike.png", x, y,
                    newDirX * bulletSpeed, newDirY * bulletSpeed, 8f);
                b.setColor(new Color(1f, 0.2f, 0.8f, 1f));
                bullets.add(b);
            }
        }

        return bullets;
    }

    private ArrayList<Bullet> shootRandom() {
        ArrayList<Bullet> bullets = new ArrayList<Bullet>();

        for (int i = 0; i < bulletsPerShot; i++) {
            float angle = (float)(Math.random() * 360f);
            float rad = (float)Math.toRadians(angle);
            float speed = bulletSpeed * (0.7f + (float)Math.random() * 0.6f);
            float vx = (float)Math.cos(rad) * speed;
            float vy = (float)Math.sin(rad) * speed;

            Bullet bullet = new Bullet("spike.png", x, y, vx, vy, 8f);
            bullet.setColor(new Color(0.8f, 1f, 0.3f, 1f));
            bullets.add(bullet);
        }

        return bullets;
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void dispose() {
        texture.dispose();
    }

    // Getters
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
    public ShootPattern getPattern() { return pattern; }
}
