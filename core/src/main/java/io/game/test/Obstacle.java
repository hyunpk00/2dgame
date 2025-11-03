package io.game.test;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

public class Obstacle {
    public enum ObstacleType {
        PILLAR,         // 기둥
        BOUNCY,         // 튕기는 블록
        SLOW_ZONE       // 속도 감소 구역
    }

    private ObstacleType type;
    private float x, y;         // 위치
    private float width, height; // 크기
    private float radius;        // 반지름

    // 스프라이트 렌더링
    private Sprite sprite;
    private Texture texture;

    // 이동 속성
    private boolean canMove;            // 이동 가능 여부
    private float moveSpeed;            // 이동 속도
    private float vx, vy;               // 속도 벡터
    private float directionChangeTime;  // 방향 전환 타이머
    private float directionChangeInterval = 2.0f; // 방향 전환 주기
    private float worldWidth, worldHeight; // 월드 크기

    // 특수 속성
    private float bounceStrength = 1.5f; // 튕김 강도
    private float slowMultiplier = 0.4f; // 속도 감소 배율
    private boolean isPlayerInside = false; // 플레이어가 내부에 있는지

    // 생명주기
    private float lifetime;     // 생존 시간
    private float age;          // 현재 나이
    private boolean isDying;    // 사라지는 중인지

    // 효과음
    private com.badlogic.gdx.audio.Sound bounceSound;    // 튕기는 블록 효과음
    private com.badlogic.gdx.audio.Sound slowZoneSound;  // 얼음 발판 효과음

    // 사각형 장애물
    public Obstacle(ObstacleType type, float x, float y, float width, float height, String texturePath,
                    boolean canMove, float moveSpeed, float worldWidth, float worldHeight, float lifetime) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.canMove = canMove;
        this.moveSpeed = moveSpeed;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.directionChangeTime = 0f;
        this.lifetime = lifetime;
        this.age = 0f;
        this.isDying = false;

        // 랜덤 초기 방향 설정
        if (canMove) {
            setRandomDirection();
        } else {
            this.vx = 0;
            this.vy = 0;
        }

        // 텍스처 로드
        if (texturePath != null && !texturePath.isEmpty()) {
            texture = new Texture(texturePath);
            sprite = new Sprite(texture);
            sprite.setSize(width, height);
            sprite.setPosition(x - width/2, y - height/2);
            sprite.setOriginCenter();
        }

    }

    // 원형 장애물
    public Obstacle(ObstacleType type, float x, float y, float radius, String texturePath,
                    boolean canMove, float moveSpeed, float worldWidth, float worldHeight, float lifetime) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.canMove = canMove;
        this.moveSpeed = moveSpeed;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.directionChangeTime = 0f;
        this.lifetime = lifetime;
        this.age = 0f;
        this.isDying = false;

        // 랜덤 초기 방향 설정
        if (canMove) {
            setRandomDirection();
        } else {
            this.vx = 0;
            this.vy = 0;
        }

        // 텍스처 로드
        if (texturePath != null && !texturePath.isEmpty()) {
            texture = new Texture(texturePath);
            sprite = new Sprite(texture);
            float size = radius * 2f;
            sprite.setSize(size, size);
            sprite.setPosition(x - radius, y - radius);
            sprite.setOriginCenter();
        }

    }

    private void setRandomDirection() {
        float angle = (float)(Math.random() * 360);
        float rad = (float)Math.toRadians(angle);
        this.vx = (float)Math.cos(rad) * moveSpeed;
        this.vy = (float)Math.sin(rad) * moveSpeed;
    }


    public void update(float dt) {
        // 생명주기 체크
        if (lifetime > 0) {
            age += dt;

            // 생명주기의 80%가 지나면 사라지는 효과 시작
            if (age > lifetime * 0.8f) {
                isDying = true;
            }
        }

        // 이동 처리
        if (canMove) {
            // 위치 업데이트
            x += vx * dt;
            y += vy * dt;

            // 경계 충돌 처리
            boolean bounced = false;
            if (type == ObstacleType.BOUNCY || type == ObstacleType.SLOW_ZONE) {
                // 사각형 장애물
                if (x - width/2 < 0 || x + width/2 > worldWidth) {
                    vx = -vx;
                    x = Math.max(width/2, Math.min(x, worldWidth - width/2));
                    bounced = true;
                }
                if (y - height/2 < 0 || y + height/2 > worldHeight) {
                    vy = -vy;
                    y = Math.max(height/2, Math.min(y, worldHeight - height/2));
                    bounced = true;
                }
            } else {
                // 원형 장애물
                if (x - radius < 0 || x + radius > worldWidth) {
                    vx = -vx;
                    x = Math.max(radius, Math.min(x, worldWidth - radius));
                    bounced = true;
                }
                if (y - radius < 0 || y + radius > worldHeight) {
                    vy = -vy;
                    y = Math.max(radius, Math.min(y, worldHeight - radius));
                    bounced = true;
                }
            }

            // 방향 전환 타이머 (경계에 튕긴게 아닐 때만)
            if (!bounced) {
                directionChangeTime += dt;
                if (directionChangeTime >= directionChangeInterval) {
                    setRandomDirection();
                    directionChangeTime = 0f;
                }
            }

            // 스프라이트 위치 업데이트
            if (sprite != null) {
                if (type == ObstacleType.BOUNCY || type == ObstacleType.SLOW_ZONE) {
                    sprite.setPosition(x - width/2, y - height/2);
                } else {
                    sprite.setPosition(x - radius, y - radius);
                }
            }
        }

        // 애니메이션 효과
        if (type == ObstacleType.BOUNCY) {
            float pulseTime = System.currentTimeMillis() / 200f;
            float scale = 1f + (float)Math.sin(pulseTime) * 0.05f;
            if (sprite != null) {
                sprite.setScale(scale);
            }
        } else if (type == ObstacleType.SLOW_ZONE) {
            if (sprite != null) {
                sprite.rotate(-15f * dt);
            }
        }

        // 사라지는 효과 (페이드 아웃)
        if (isDying && sprite != null && lifetime > 0) {
            float remainingTime = lifetime - age;
            float fadeTime = lifetime * 0.2f;
            if (remainingTime < fadeTime) {
                float alpha = remainingTime / fadeTime;
                Color c = sprite.getColor();
                sprite.setColor(c.r, c.g, c.b, alpha);
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (sprite != null) {
            sprite.draw(batch);
        }
    }

    public boolean checkCollisionWithPlayer(Player player) {
        float px = player.getX();
        float py = player.getY();
        float pr = player.getRadius();

        if (type == ObstacleType.BOUNCY) {
            float closestX = Math.max(x - width/2, Math.min(px, x + width/2));
            float closestY = Math.max(y - height/2, Math.min(py, y + height/2));

            float dx = px - closestX;
            float dy = py - closestY;
            float distanceSquared = dx * dx + dy * dy;

            return distanceSquared < (pr * pr);
        } else if (type == ObstacleType.PILLAR) {
            float dx = px - x;
            float dy = py - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            return distance < (radius + pr);
        } else if (type == ObstacleType.SLOW_ZONE) {
            return (px > x - width/2 && px < x + width/2 &&
                py > y - height/2 && py < y + height/2);
        }

        return false;
    }

    public boolean checkCollisionWithBullet(Bullet bullet) {
        if (type == ObstacleType.SLOW_ZONE) {
            return false;
        }

        float bx = bullet.getX();
        float by = bullet.getY();
        float br = bullet.getRadius();

        if (type == ObstacleType.BOUNCY) {
            float closestX = Math.max(x - width/2, Math.min(bx, x + width/2));
            float closestY = Math.max(y - height/2, Math.min(by, y + height/2));

            float dx = bx - closestX;
            float dy = by - closestY;
            float distanceSquared = dx * dx + dy * dy;

            return distanceSquared < (br * br);
        } else if (type == ObstacleType.PILLAR) {
            float dx = bx - x;
            float dy = by - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);
            return distance < (radius + br);
        }

        return false;
    }

    public void handlePlayerCollision(Player player) {
        if (!checkCollisionWithPlayer(player)) {
            isPlayerInside = false;
            return;
        }

        switch (type) {
            case PILLAR:
                pushPlayerOut(player);
                break;

            case BOUNCY:
                bouncePlayer(player);
                break;

            case SLOW_ZONE:
                if (!isPlayerInside) {
                    applySlow(player);
                    isPlayerInside = true;
                }
                break;
        }
    }

    private void pushPlayerOut(Player player) {
        float px = player.getX();
        float py = player.getY();
        float pr = player.getRadius();

        if (type == ObstacleType.BOUNCY) {
            float closestX = Math.max(x - width/2, Math.min(px, x + width/2));
            float closestY = Math.max(y - height/2, Math.min(py, y + height/2));

            float dx = px - closestX;
            float dy = py - closestY;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            if (distance < pr && distance > 0) {
                float pushX = (dx / distance) * (pr - distance);
                float pushY = (dy / distance) * (pr - distance);

                player.setX(px + pushX);
                player.setY(py + pushY);

                player.setVx(player.getVx() * 0.3f);
                player.setVy(player.getVy() * 0.3f);
            }
        } else if (type == ObstacleType.PILLAR) {
            float dx = px - x;
            float dy = py - y;
            float distance = (float)Math.sqrt(dx * dx + dy * dy);

            if (distance < (radius + pr) && distance > 0) {
                float pushDistance = (radius + pr) - distance;
                float pushX = (dx / distance) * pushDistance;
                float pushY = (dy / distance) * pushDistance;

                player.setX(px + pushX);
                player.setY(py + pushY);

                player.setVx(player.getVx() * 0.3f);
                player.setVy(player.getVy() * 0.3f);
            }
        }
    }

    private void bouncePlayer(Player player) {
        float px = player.getX();
        float py = player.getY();
        float pr = player.getRadius();
        float pvx = player.getVx();
        float pvy = player.getVy();

        float closestX = Math.max(x - width/2, Math.min(px, x + width/2));
        float closestY = Math.max(y - height/2, Math.min(py, y + height/2));

        float dx = px - closestX;
        float dy = py - closestY;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance < pr && distance > 0) {
            float nx = dx / distance;
            float ny = dy / distance;

            float pushDistance = pr - distance;
            player.setX(px + nx * pushDistance);
            player.setY(py + ny * pushDistance);

            float dotProduct = pvx * nx + pvy * ny;

            float newVx = pvx - 2 * dotProduct * nx;
            float newVy = pvy - 2 * dotProduct * ny;

            player.setVx(newVx * bounceStrength);
            player.setVy(newVy * bounceStrength);

            // 튕기는 효과음 재생
            if (bounceSound != null) {
                bounceSound.play(0.6f); // 볼륨 60%
            }
        }
    }

    private void applySlow(Player player) {
        player.setVx(player.getVx() * slowMultiplier);
        player.setVy(player.getVy() * slowMultiplier);

        // 얼음 발판 효과음 재생
        if (slowZoneSound != null) {
            slowZoneSound.play(0.8f); // 볼륨 50%
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    // 효과음 설정
    public void setBounceSound(com.badlogic.gdx.audio.Sound sound) {
        this.bounceSound = sound;
    }

    public void setSlowZoneSound(com.badlogic.gdx.audio.Sound sound) {
        this.slowZoneSound = sound;
    }

    // Getters
    public ObstacleType getType() { return type; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getRadius() { return radius; }
    public boolean canMove() { return canMove; }
    public boolean isExpired() { return lifetime > 0 && age >= lifetime; }
    public boolean isDying() { return isDying; }
}
