package io.game.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import java.util.ArrayList;

public class LevelManager {
    private ArrayList<Level> levels;
    private int currentLevelIndex;

    // 로드된 게임 오브젝트들
    private Player player;
    private ArrayList<Enemy> enemies;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Bullet> bullets;

    // 배경 관련
    private Color currentBackgroundColor;
    private Texture currentBackgroundTexture;

    // 타이머
    private float survivalTimer;
    private float obstacleSpawnTimer;

    // 월드 정보
    private float worldWidth;
    private float worldHeight;

    // 효과음
    private Sound bounceObstacleSound;
    private Sound slowZoneSound;

    public LevelManager(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        levels = new ArrayList<Level>();
        currentLevelIndex = 0;
        createLevels();
    }

    private void createLevels() {
        createLevel1();
        createLevel2();
        createLevel3();
    }

    // LEVEL 1
    private void createLevel1() {
        Level level1 = new Level(1);

        level1.setBackgroundColor(new Color(1f, 1f, 1f, 1f));
        level1.setBackgroundImage("level1_background.png");

        // 공기저항
        level1.setDragK(1.2f);
        level1.setMaxSpeed(800f);

        // 쿨타임
        level1.setFlickCooldown(0.6f);

        // 생존 시간
        level1.setSurvivalTime(30f);

        // 시작 위치
        level1.setPlayerStart(640f, 150f);

        // 탄막 밀도
        level1.setBulletDensity(1.0f);

        // 적
        level1.addEnemy(new Level.EnemyData(
            320f, 500f, 35f,
            Enemy.ShootPattern.CIRCLE,
            3.0f,
            200f,
            8,
            Enemy.MovementPattern.HORIZONTAL,
            1.0f,
            80f
        ));

        level1.addEnemy(new Level.EnemyData(
            960f, 500f, 35f,
            Enemy.ShootPattern.RANDOM,
            2.5f,
            200f,
            8,
            Enemy.MovementPattern.VERTICAL,
            1.0f,
            80f
        ));

        levels.add(level1);
    }

    // LEVEL 2
    private void createLevel2() {
        Level level2 = new Level(2);

        level2.setBackgroundColor(new Color(1f, 1f, 1f, 1f));
        level2.setBackgroundImage("level2_background.png");

        // 공기저항
        level2.setDragK(1.0f);
        level2.setMaxSpeed(900f);

        // 쿨타임
        level2.setFlickCooldown(0.8f);

        // 생존 시간
        level2.setSurvivalTime(45f);

        // 시작 위치
        level2.setPlayerStart(640f, 150f);

        // 탄막 밀도
        level2.setBulletDensity(1.25f);

        // 적
        level2.addEnemy(new Level.EnemyData(
            320f, 500f, 35f,
            Enemy.ShootPattern.CIRCLE,
            2.0f,
            250f,
            6,
            Enemy.MovementPattern.CIRCLE,
            0.8f,
            60f
        ));

        level2.addEnemy(new Level.EnemyData(
            960f, 500f, 35f,
            Enemy.ShootPattern.AIMED,
            2.0f,
            240f,
            5,
            Enemy.MovementPattern.STATIONARY,
            1.2f,
            100f
        ));

        // ===== 움직이는 튕기는 블록 4개 =====
        level2.addObstacle(new Level.ObstacleData(
            Obstacle.ObstacleType.BOUNCY,
            400f, 400f, 128f, 30f,
            "cloud.png",
            true,
            80f
        ));
        level2.addObstacle(new Level.ObstacleData(
            Obstacle.ObstacleType.BOUNCY,
            880f, 400f, 128f, 30f,
            "cloud.png",
            true,
            80f
        ));
        level2.addObstacle(new Level.ObstacleData(
            Obstacle.ObstacleType.BOUNCY,
            400f, 400f, 128f, 30f,
            "cloud.png",
            true,
            80f
        ));
        level2.addObstacle(new Level.ObstacleData(
            Obstacle.ObstacleType.BOUNCY,
            880f, 400f, 128f, 30f,
            "cloud.png",
            true,
            80f
        ));

        levels.add(level2);
    }

    // LEVEL 3
    private void createLevel3() {
        Level level3 = new Level(3);

        level3.setBackgroundColor(new Color(1f, 1f, 1f, 1f));
        level3.setBackgroundImage("level3_background.png");

        // 공기저항
        level3.setDragK(0.8f);
        level3.setMaxSpeed(1000f);

        // 플릭 쿨타임
        level3.setFlickCooldown(1.0f);

        // 생존 시간
        level3.setSurvivalTime(60f);

        // 시작 위치
        level3.setPlayerStart(640f, 150f);

        // 탄막 밀도
        level3.setBulletDensity(1.5f);

        // 적
        level3.addEnemy(new Level.EnemyData(
            320f, 550f, 40f,
            Enemy.ShootPattern.RANDOM,
            2.0f,
            200f,
            6,
            Enemy.MovementPattern.FIGURE_EIGHT,
            0.4f,
            100f
        ));

        level3.addEnemy(new Level.EnemyData(
            960f, 550f, 40f,
            Enemy.ShootPattern.AIMED,
            2.0f,
            200f,
            6,
            Enemy.MovementPattern.CIRCLE,
            1.0f,
            90f
        ));

        // 장애물 동적 스폰
        level3.setObstacleSpawn(
            true,
            1.5f,
            8,
            4.0f
        );

        levels.add(level3);
    }

    public void loadCurrentLevel() {
        Level level = getCurrentLevel();

        // 리소스 정리
        cleanupResources();

        // 배경 설정
        currentBackgroundColor = level.getBackgroundColor();
        currentBackgroundTexture = loadBackgroundTexture(level);

        // 플레이어 생성
        player = new Player("pepe_the_ball.png", 0.05f,
            level.getPlayerStartX(), level.getPlayerStartY());
        player.setFlickCooldown(level.getFlickCooldown());

        // 적 생성
        enemies = new ArrayList<Enemy>();
        for (Level.EnemyData data : level.getEnemies()) {
            Enemy enemy = new Enemy("doge.png",
                data.x, data.y, data.radius,
                data.pattern,
                data.shootCooldown / level.getBulletDensity(),
                data.bulletSpeed,
                data.bulletsPerShot,
                data.movementPattern,
                data.moveSpeed,
                data.movementRange);
            enemies.add(enemy);
        }
        // 장애물 생성
        obstacles = createObstacles(level);

        // 탄환 리스트 초기화
        bullets = new ArrayList<Bullet>();

        // 타이머 초기화
        survivalTimer = 0f;
        obstacleSpawnTimer = 0f;
    }

    private ArrayList<Obstacle> createObstacles(Level level) {
        ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();

        for (Level.ObstacleData data : level.getObstacles()) {
            Obstacle obstacle;

            if (data.type == Obstacle.ObstacleType.PILLAR) {
                obstacle = new Obstacle(data.type, data.x, data.y, data.radius, data.texturePath,
                    data.isMoving, data.moveSpeed, worldWidth, worldHeight, data.lifetime);
            } else {
                obstacle = new Obstacle(data.type, data.x, data.y, data.width, data.height, data.texturePath,
                    data.isMoving, data.moveSpeed, worldWidth, worldHeight, data.lifetime);
            }

            // 효과음 설정
            if (data.type == Obstacle.ObstacleType.BOUNCY && bounceObstacleSound != null) {
                obstacle.setBounceSound(bounceObstacleSound);
            } else if (data.type == Obstacle.ObstacleType.SLOW_ZONE && slowZoneSound != null) {
                obstacle.setSlowZoneSound(slowZoneSound);
            }
            obstacles.add(obstacle);
        }
        return obstacles;
    }

    private Texture loadBackgroundTexture(Level level) {
        String backgroundPath = level.getBackgroundImagePath();
        if (backgroundPath != null && !backgroundPath.isEmpty()) {
            Texture texture = new Texture(Gdx.files.internal(backgroundPath));
            return texture;
        }
        return null;
    }

    private void cleanupResources() {
        if (player != null) {
            player.dispose();
            player = null;
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                enemy.dispose();
            }
            enemies = null;
        }

        if (obstacles != null) {
            for (Obstacle obstacle : obstacles) {
                obstacle.dispose();
            }
            obstacles = null;
        }

        if (bullets != null) {
            for (Bullet bullet : bullets) {
                bullet.dispose();
            }
            bullets = null;
        }

        if (currentBackgroundTexture != null) {
            currentBackgroundTexture.dispose();
            currentBackgroundTexture = null;
        }
    }

    // 효과음 설정
    public void setSounds(Sound bounceObstacleSound, Sound slowZoneSound) {
        this.bounceObstacleSound = bounceObstacleSound;
        this.slowZoneSound = slowZoneSound;
    }

    // 타이머 관리
    public void updateSurvivalTimer(float dt) {
        survivalTimer += dt;
    }

    public void updateObstacleSpawnTimer(float dt) {
        obstacleSpawnTimer += dt;
    }

    public void resetObstacleSpawnTimer() {
        obstacleSpawnTimer = 0f;
    }

    public float getSurvivalTimer() {
        return survivalTimer;
    }

    public float getObstacleSpawnTimer() {
        return obstacleSpawnTimer;
    }

    // Getter
    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Obstacle> getObstacles() { return obstacles; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public Color getCurrentBackgroundColor() { return currentBackgroundColor; }
    public Texture getCurrentBackgroundTexture() { return currentBackgroundTexture; }

    // 레벨 관리
    public Level getCurrentLevel() {
        return levels.get(currentLevelIndex);
    }

    public boolean hasNextLevel() {
        return currentLevelIndex < levels.size() - 1;
    }

    public void nextLevel() {
        if (hasNextLevel()) {
            currentLevelIndex++;
        }
    }

    public void resetToFirstLevel() {
        currentLevelIndex = 0;
    }

    public int getCurrentLevelNumber() {
        return currentLevelIndex + 1;
    }

    public int getTotalLevels() {
        return levels.size();
    }

    public boolean isLastLevel() {
        return currentLevelIndex == levels.size() - 1;
    }

    public void dispose() {
        cleanupResources();
    }
}
