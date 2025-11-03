package io.game.test;

import com.badlogic.gdx.audio.Sound;

public class GameWorld {
    // 월드 크기
    private final float width;
    private final float height;

    // 물리 상수
    private float dragK;          // 공기저항 계수
    private float maxSpeed;       // 최대 속도

    // 효과음
    private Sound wallBounceSound;

    public GameWorld(float width, float height) {
        this.width = width;
        this.height = height;

        // 기본값
        this.dragK = 1.0f;
        this.maxSpeed = 800f;
    }

    // 레벨별 물리 설정
    public void setPhysics(float dragK, float maxSpeed) {
        this.dragK = dragK;
        this.maxSpeed = maxSpeed;
    }

    // 효과음 설정
    public void setWallBounceSound(Sound sound) {
        this.wallBounceSound = sound;
    }

    // 플레이어 물리 업데이트
    public void updatePlayerPhysics(Player player, float dt) {
        // 공기저항 적용
        float vx = player.getVx();
        float vy = player.getVy();

        vx -= vx * dragK * dt;
        vy -= vy * dragK * dt;

        // 속도가 너무 작으면 0으로
        if (Math.abs(vx) < 5f) vx = 0f;
        if (Math.abs(vy) < 5f) vy = 0f;

        // 최대 속도 제한
        float speed = (float)Math.sqrt(vx * vx + vy * vy);
        if (speed > maxSpeed) {
            vx = (vx / speed) * maxSpeed;
            vy = (vy / speed) * maxSpeed;
        }

        player.setVx(vx);
        player.setVy(vy);
    }

    // 경계 충돌 처리
    public boolean handleBoundaryCollision(Player player) {
        float radius = player.getRadius();
        float px = player.getX();
        float py = player.getY();
        float vx = player.getVx();
        float vy = player.getVy();
        boolean collided = false;

        // 좌우 경계
        float left = radius;
        float right = width - radius;
        if (px < left) {
            player.setX(left);
            player.setVx(-vx * 0.5f); // 반사
            collided = true;
        } else if (px > right) {
            player.setX(right);
            player.setVx(-vx * 0.5f);
            collided = true;
        }

        // 상하 경계
        float bottom = radius;
        float top = height - radius;
        if (py < bottom) {
            player.setY(bottom);
            player.setVy(-vy * 0.5f);
            collided = true;
        } else if (py > top) {
            player.setY(top);
            player.setVy(-vy * 0.5f);
            collided = true;
        }

        // 벽에 튕겼을 때 효과음 재생
        if (collided && wallBounceSound != null) {
            wallBounceSound.play(0.5f); // 볼륨
        }

        return collided;
    }

    // Getters
    public float getDragK() { return dragK; }
    public float getMaxSpeed() { return maxSpeed; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    // 플레이어 전체 업데이트
    public void updatePlayer(Player player, float dt) {
        float vx = player.getVx();
        float vy = player.getVy();

        player.setX(player.getX() + vx * dt);
        player.setY(player.getY() + vy * dt);

        updatePlayerPhysics(player, dt);

        handleBoundaryCollision(player);

        player.update(dt);
    }

    // 랜덤 장애물 생성
    public Obstacle spawnRandomObstacle(Player player, float lifetime) {
        Obstacle.ObstacleType[] types = {
            Obstacle.ObstacleType.BOUNCY,
            Obstacle.ObstacleType.SLOW_ZONE
        };
        Obstacle.ObstacleType type = types[(int)(Math.random() * types.length)];

        // 랜덤 위치
        float minX = 150f;
        float maxX = width - 50f;
        float minY = 300f;
        float maxY = height - 50f;

        float x = minX + (float)Math.random() * (maxX - minX);
        float y = minY + (float)Math.random() * (maxY - minY);

        // 플레이어와 너무 가까우면 재생성
        float dx = x - player.getX();
        float dy = y - player.getY();
        float distance = (float)Math.sqrt(dx * dx + dy * dy);
        if (distance < 200f) {
            // 플레이어와 반대편에 생성
            x = width - x;
            y = height - y;
        }

        // 고정 크기
        float obstacleWidth = 70;   // 고정 가로 크기
        float obstacleHeight = 70;   // 고정 세로 크기

        // 텍스처 선택
        String texture = (type == Obstacle.ObstacleType.BOUNCY) ? "ice.png" : "snow.png";

        // 랜덤 이동 속도
        float moveSpeed = 60f + (float)Math.random() * 80f;  // 60~140

        // 장애물 생성
        Obstacle obstacle = new Obstacle(
            type, x, y, obstacleWidth, obstacleHeight, texture,
            true,  // 이동함
            moveSpeed,
            width, height,
            lifetime  // 생명주기
        );
        return obstacle;
    }
}
