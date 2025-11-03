package io.game.test;

import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;

public class Level {
    private int levelNumber;
    private Color backgroundColor;
    private String backgroundImagePath;

    // 물리 상수
    private float dragK;
    private float maxSpeed;
    private float flickCooldown; // 플릭 쿨타임

    // 레벨 목표
    private float survivalTime; // 생존해야 하는 시간

    // 시작 위치
    private float playerStartX;
    private float playerStartY;

    // 적 리스트
    private ArrayList<EnemyData> enemies;

    // 장애물 리스트
    private ArrayList<ObstacleData> obstacles;

    // 레벨 난이도
    private float bulletDensity; // 탄막 밀도

    // 장애물 동적 스폰 설정
    private boolean enableObstacleSpawn; // 장애물 동적 생성 여부
    private float obstacleSpawnInterval; // 장애물 생성 간격
    private int maxObstacles;            // 최대 장애물 수
    private float obstacleLifetime;      // 장애물 생존 시간

    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.enemies = new ArrayList<EnemyData>();
        this.obstacles = new ArrayList<ObstacleData>();
        this.bulletDensity = 1.0f;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setBackgroundImage(String imagePath) {
        this.backgroundImagePath = imagePath;
    }

    public void setDragK(float dragK) {
        this.dragK = dragK;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setFlickCooldown(float flickCooldown) {
        this.flickCooldown = flickCooldown;
    }

    public void setSurvivalTime(float survivalTime) {
        this.survivalTime = survivalTime;
    }

    public void setPlayerStart(float x, float y) {
        this.playerStartX = x;
        this.playerStartY = y;
    }

    public void setBulletDensity(float density) {
        this.bulletDensity = density;
    }

    public void setObstacleSpawn(boolean enable, float spawnInterval, int maxObstacles, float lifetime) {
        this.enableObstacleSpawn = enable;
        this.obstacleSpawnInterval = spawnInterval;
        this.maxObstacles = maxObstacles;
        this.obstacleLifetime = lifetime;
    }

    public void addEnemy(EnemyData enemy) {
        enemies.add(enemy);
    }

    public void addObstacle(ObstacleData obstacle) {
        obstacles.add(obstacle);
    }

    // Getters
    public int getLevelNumber() { return levelNumber; }
    public Color getBackgroundColor() { return backgroundColor; }
    public String getBackgroundImagePath() { return backgroundImagePath; }
    public float getDragK() { return dragK; }
    public float getMaxSpeed() { return maxSpeed; }
    public float getFlickCooldown() { return flickCooldown; }
    public float getSurvivalTime() { return survivalTime; }
    public float getPlayerStartX() { return playerStartX; }
    public float getPlayerStartY() { return playerStartY; }
    public ArrayList<EnemyData> getEnemies() { return enemies; }
    public ArrayList<ObstacleData> getObstacles() { return obstacles; }
    public float getBulletDensity() { return bulletDensity; }
    public boolean isObstacleSpawnEnabled() { return enableObstacleSpawn; }
    public float getObstacleSpawnInterval() { return obstacleSpawnInterval; }
    public int getMaxObstacles() { return maxObstacles; }
    public float getObstacleLifetime() { return obstacleLifetime; }

    // 적 데이터 저장 클래스
    public static class EnemyData {
        public float x, y, radius;
        public Enemy.ShootPattern pattern;
        public float shootCooldown;
        public float bulletSpeed;
        public int bulletsPerShot;
        public Enemy.MovementPattern movementPattern;
        public float moveSpeed;
        public float movementRange;

        public EnemyData(float x, float y, float radius,
                         Enemy.ShootPattern pattern,
                         float shootCooldown,
                         float bulletSpeed,
                         int bulletsPerShot,
                         Enemy.MovementPattern movementPattern,
                         float moveSpeed,
                         float movementRange) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.pattern = pattern;
            this.shootCooldown = shootCooldown;
            this.bulletSpeed = bulletSpeed;
            this.bulletsPerShot = bulletsPerShot;
            this.movementPattern = movementPattern;
            this.moveSpeed = moveSpeed;
            this.movementRange = movementRange;
        }
    }

    // 장애물 데이터 저장 클래스
    public static class ObstacleData {
        public Obstacle.ObstacleType type;
        public float x, y;
        public float width, height; // 사각형
        public float radius;        // 원형
        public String texturePath;  // 텍스처 경로
        public boolean isMoving;    // 이동 여부
        public float moveSpeed;     // 이동 속도
        public float lifetime;      // 생존 시간

        // 사각형 장애물 생성자
        public ObstacleData(Obstacle.ObstacleType type, float x, float y, float width, float height, String texturePath) {
            this(type, x, y, width, height, texturePath, false, 0f, 0f);
        }

        // 사각형 장애물 생성자 (이동 가능)
        public ObstacleData(Obstacle.ObstacleType type, float x, float y, float width, float height, String texturePath, boolean isMoving, float moveSpeed) {
            this(type, x, y, width, height, texturePath, isMoving, moveSpeed, 0f);
        }

        // 사각형 장애물 생성자 (이동 가능 + lifetime)
        public ObstacleData(Obstacle.ObstacleType type, float x, float y, float width, float height, String texturePath, boolean isMoving, float moveSpeed, float lifetime) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texturePath = texturePath;
            this.isMoving = isMoving;
            this.moveSpeed = moveSpeed;
            this.lifetime = lifetime;
        }
    }
}
