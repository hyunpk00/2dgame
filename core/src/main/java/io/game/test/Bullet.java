package io.game.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class Bullet {
    private Sprite sprite;
    private Texture texture;
    private float x, y;
    private float vx, vy;
    private float radius;
    private boolean active;

    // 시각 효과
    private Color color;
    private float lifetime;
    private float age;

    public Bullet(String texturePath, float x, float y, float vx, float vy, float radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.active = true;
        this.lifetime = 5f;
        this.age = 0f;

        // 텍스처 로드
        texture = new Texture(texturePath);
        sprite = new Sprite(texture);

        // 스프라이트 설정
        float size = radius * 2f;
        sprite.setSize(size, size);
        sprite.setOriginCenter();

        // 기본 색상
        color = new Color(1f, 1f, 1f, 1f);
        sprite.setColor(color);
    }

    // 색상 설정
    public void setColor(Color color) {
        this.color = color;
        sprite.setColor(color);
    }

    public void update(float dt, Player player, GameWorld world) {
        if (!active) return;

        age += dt;
        if (age > lifetime) {
            active = false;
            return;
        }

        // 위치 업데이트
        x += vx * dt;
        y += vy * dt;

        // 화면 밖으로 나가면 비활성화
        if (x < -20 || x > world.getWidth() + 20 ||
            y < -20 || y > world.getHeight() + 20) {
            active = false;
        }

        // 각도 변경
        float angle = (float)Math.toDegrees(Math.atan2(vy, vx));
        sprite.setRotation(angle);
    }

    public void render(SpriteBatch batch) {
        if (!active) return;

        sprite.setPosition(x - radius, y - radius);
        sprite.draw(batch);
    }

    // 플레이어와 충돌 체크
    public boolean checkCollision(Player player) {
        if (!active) return false;

        float dx = x - player.getX();
        float dy = y - player.getY();
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        return distance < (radius + player.getRadius());
    }

    public void dispose() {
        texture.dispose();
    }

    // Getters
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getRadius() { return radius; }
}
