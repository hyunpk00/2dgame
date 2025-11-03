package io.game.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.audio.Sound;
import java.util.ArrayList;
import java.util.Iterator;

public class Main extends ApplicationAdapter {

    enum GameState { RUNNING, PAUSED, GAME_OVER, LEVEL_COMPLETE, GAME_COMPLETE }

    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private BitmapFont font;
    private CameraManager cameraManager;

    private Sound wallBounceSound;
    private Sound gameOverSound;
    private Sound bounceObstacleSound;
    private Sound slowZoneSound;
    private Sound levelClearSound;
    private Sound gameClearSound;

    private com.badlogic.gdx.audio.Music backgroundMusic;

    private GameWorld world;
    private LevelManager levelManager;

    private boolean dragging = false;
    private float dragStartX, dragStartY;

    private GameState state = GameState.RUNNING;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);
        camera.position.set(WORLD_WIDTH/2f, WORLD_HEIGHT/2f, 0);

        cameraManager = new CameraManager(camera, WORLD_WIDTH, WORLD_HEIGHT);

        // 효과음 로드
        wallBounceSound = Gdx.audio.newSound(Gdx.files.internal("bounce.mp3"));
        gameOverSound = Gdx.audio.newSound(Gdx.files.internal("game_over.mp3"));
        bounceObstacleSound = Gdx.audio.newSound(Gdx.files.internal("bounce.mp3"));
        slowZoneSound = Gdx.audio.newSound(Gdx.files.internal("slow.wav"));
        levelClearSound = Gdx.audio.newSound(Gdx.files.internal("level_clear.wav"));
        gameClearSound = Gdx.audio.newSound(Gdx.files.internal("game_complete.mp3"));

        // 배경음악
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.1f);
        backgroundMusic.play();

        // 게임 월드 생성
        world = new GameWorld(WORLD_WIDTH, WORLD_HEIGHT);
        world.setWallBounceSound(wallBounceSound);

        // 레벨 매니저 생성 및 초기화
        levelManager = new LevelManager(WORLD_WIDTH, WORLD_HEIGHT);
        levelManager.setSounds(bounceObstacleSound, slowZoneSound);
        levelManager.loadCurrentLevel();

        // 레벨의 물리 설정을 월드에 적용
        Level currentLevel = levelManager.getCurrentLevel();
        world.setPhysics(currentLevel.getDragK(), currentLevel.getMaxSpeed());
    }

    @Override
    public void render() {
        handleInput();

        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1/30f);

        if (state == GameState.RUNNING) {
            // 타이머 업데이트
            levelManager.updateSurvivalTimer(dt);

            Level currentLevel = levelManager.getCurrentLevel();
            Player player = levelManager.getPlayer();
            ArrayList<Enemy> enemies = levelManager.getEnemies();
            ArrayList<Obstacle> obstacles = levelManager.getObstacles();
            ArrayList<Bullet> bullets = levelManager.getBullets();

            // 생존 시간 체크
            if (levelManager.getSurvivalTimer() >= currentLevel.getSurvivalTime()) {
                if (levelManager.isLastLevel()) {
                    state = GameState.GAME_COMPLETE;
                    if (gameClearSound != null) {
                        gameClearSound.play(0.5f);
                    }
                } else {
                    state = GameState.LEVEL_COMPLETE;
                    if (levelClearSound != null) {
                        levelClearSound.play(1.0f);
                    }
                }
            }

            // 플레이어 업데이트
            world.updatePlayer(player, dt);

            // 장애물 업데이트
            for (Obstacle obstacle : obstacles) {
                obstacle.update(dt);
            }

            // 만료된 장애물 제거
            Iterator<Obstacle> obstacleIterator = obstacles.iterator();
            while (obstacleIterator.hasNext()) {
                Obstacle obstacle = obstacleIterator.next();
                if (obstacle.isExpired()) {
                    obstacle.dispose();
                    obstacleIterator.remove();
                }
            }

            // 동적 장애물 생성
            if (currentLevel.isObstacleSpawnEnabled()) {
                levelManager.updateObstacleSpawnTimer(dt);

                if (levelManager.getObstacleSpawnTimer() >= currentLevel.getObstacleSpawnInterval() &&
                    obstacles.size() < currentLevel.getMaxObstacles()) {

                    levelManager.resetObstacleSpawnTimer();

                    Obstacle obstacle = world.spawnRandomObstacle(player, currentLevel.getObstacleLifetime());

                    if (obstacle.getType() == Obstacle.ObstacleType.BOUNCY) {
                        obstacle.setBounceSound(bounceObstacleSound);
                    } else if (obstacle.getType() == Obstacle.ObstacleType.SLOW_ZONE) {
                        obstacle.setSlowZoneSound(slowZoneSound);
                    }

                    obstacles.add(obstacle);
                }
            }

            // 장애물 충돌 처리
            for (Obstacle obstacle : obstacles) {
                obstacle.handlePlayerCollision(player);
            }

            // 적 업데이트 및 탄환 발사
            for (Enemy enemy : enemies) {
                enemy.update(dt);

                ArrayList<Bullet> newBullets = enemy.tryShoot(player);
                if (newBullets != null) {
                    bullets.addAll(newBullets);
                }
            }

            // 탄환 업데이트 및 충돌 체크
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update(dt, player, world);

                if (!bullet.isActive()) {
                    bullet.dispose();
                    bulletIterator.remove();
                    continue;
                }

                // 장애물과 충돌 체크
                boolean hitObstacle = false;
                for (Obstacle obstacle : obstacles) {
                    if (obstacle.checkCollisionWithBullet(bullet)) {
                        bullet.setActive(false);
                        hitObstacle = true;
                        break;
                    }
                }

                if (hitObstacle) {
                    bullet.dispose();
                    bulletIterator.remove();
                    continue;
                }

                // 플레이어와 충돌 체크
                if (bullet.checkCollision(player)) {
                    state = GameState.GAME_OVER;
                    cameraManager.gameOverShake();
                    if (gameOverSound != null) {
                        gameOverSound.play(0.2f);
                    }
                    break;
                }
            }

            // 카메라 업데이트
            cameraManager.update(dt, player.getX(), player.getY());

        } else if (state == GameState.GAME_OVER) {
            Player player = levelManager.getPlayer();
            cameraManager.update(dt, player.getX(), player.getY());

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                levelManager.loadCurrentLevel();
                Level currentLevel = levelManager.getCurrentLevel();
                world.setPhysics(currentLevel.getDragK(), currentLevel.getMaxSpeed());
                state = GameState.RUNNING;
            }
        } else if (state == GameState.LEVEL_COMPLETE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (levelClearSound != null) {
                    levelClearSound.stop();
                }
                levelManager.nextLevel();
                levelManager.loadCurrentLevel();
                Level currentLevel = levelManager.getCurrentLevel();
                world.setPhysics(currentLevel.getDragK(), currentLevel.getMaxSpeed());
                state = GameState.RUNNING;
            }
        } else if (state == GameState.GAME_COMPLETE) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (gameClearSound != null) {
                    gameClearSound.stop();
                }
                levelManager.resetToFirstLevel();
                levelManager.loadCurrentLevel();
                Level currentLevel = levelManager.getCurrentLevel();
                world.setPhysics(currentLevel.getDragK(), currentLevel.getMaxSpeed());
                state = GameState.RUNNING;
            }
        }

        // 스크린 클리어
        com.badlogic.gdx.graphics.Color bgColor = levelManager.getCurrentBackgroundColor();
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 모든 스프라이트
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 배경화면
        com.badlogic.gdx.graphics.Texture backgroundTexture = levelManager.getCurrentBackgroundTexture();
        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        }

        // 장애물
        for (Obstacle obstacle : levelManager.getObstacles()) {
            obstacle.render(batch);
        }

        // 적
        for (Enemy enemy : levelManager.getEnemies()) {
            enemy.render(batch);
        }

        // 탄막
        for (Bullet bullet : levelManager.getBullets()) {
            bullet.render(batch);
        }

        // 플레이어
        Player player = levelManager.getPlayer();
        if (player != null) {
            player.render(batch);
        }

        // UI
        renderUI();

        batch.end();
    }

    private void renderUI() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float camZoom = camera.zoom;

        float halfWidth = (WORLD_WIDTH * camZoom) / 2f;
        float halfHeight = (WORLD_HEIGHT * camZoom) / 2f;

        float leftX = camX - halfWidth;
        float topY = camY + halfHeight;

        float margin = 20f;

        // 레벨 정보
        String levelText = "Level " + levelManager.getCurrentLevelNumber() +
            " / " + levelManager.getTotalLevels();
        font.draw(batch, levelText, leftX + margin, topY - margin);

        // 남은 시간
        Level currentLevel = levelManager.getCurrentLevel();
        int timeLeft = (int)(currentLevel.getSurvivalTime() - levelManager.getSurvivalTimer());
        if (timeLeft < 0) timeLeft = 0;
        String timeText = "Time: " + timeLeft + "s";
        font.draw(batch, timeText, leftX + margin, topY - margin - 40);

        // 플릭 쿨다운
        Player player = levelManager.getPlayer();
        if (player != null) {
            if (player.isFlickReady()) {
                font.setColor(0.3f, 1f, 0.3f, 1f); // Green - Ready
                font.draw(batch, "CoolTime: READY", leftX + margin, topY - margin - 80);
            } else {
                font.setColor(1f, 0.3f, 0.3f, 1f); // Red - Cooldown
                String cooldownText = String.format("CoolTime: %.1fs", player.getFlickCooldownTimer());
                font.draw(batch, cooldownText, leftX + margin, topY - margin - 80);
            }
            font.setColor(1f, 1f, 1f, 1f); // Reset color
        }

        float centerX = camX;
        float centerY = camY;

        // 게임 상태 메시지
        if (state == GameState.PAUSED) {
            String pauseText = "PAUSED (Press P to resume)";
            font.draw(batch, pauseText, centerX - 200, centerY);
        } else if (state == GameState.GAME_OVER) {
            String gameOverText = "GAME OVER (Press R to restart)";
            font.draw(batch, gameOverText, centerX - 250, centerY);
        } else if (state == GameState.LEVEL_COMPLETE) {
            String completeText = "LEVEL COMPLETE (Press SPACE for next level)";
            font.draw(batch, completeText, centerX - 350, centerY);
        } else if (state == GameState.GAME_COMPLETE) {
            String completeText = "ALL LEVELS CLEARED!";
            font.draw(batch, completeText, centerX - 200, centerY);
            String restartText = "(Press SPACE to restart)";
            font.draw(batch, restartText, centerX - 200, centerY - 50);
        }
    }

    private void handleInput() {
        // 일시정지/재개
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (state == GameState.RUNNING) {
                state = GameState.PAUSED;
            } else if (state == GameState.PAUSED) {
                state = GameState.RUNNING;
            }
        }

        // 드래그 입력 처리
        if (Gdx.input.justTouched()) {
            dragging = true;
            dragStartX = Gdx.input.getX();
            dragStartY = Gdx.input.getY();
        } else if (dragging && !Gdx.input.isTouched()) {
            dragging = false;
            if (state == GameState.RUNNING) {
                float endX = Gdx.input.getX();
                float endY = Gdx.input.getY();

                float zoomFactor = cameraManager.getCurrentZoom();
                float ix = (dragStartX - endX) * 8f / zoomFactor;
                float iy = (endY - dragStartY) * 8f / zoomFactor;

                Player player = levelManager.getPlayer();
                if (player != null) {
                    player.addImpulse(ix, iy);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();

        // 모든 게임 오브젝트 정리
        if (levelManager != null) {
            levelManager.dispose();
        }

        // 효과음 정리
        if (wallBounceSound != null) {
            wallBounceSound.dispose();
        }
        if (gameOverSound != null) {
            gameOverSound.dispose();
        }
        if (bounceObstacleSound != null) {
            bounceObstacleSound.dispose();
        }
        if (slowZoneSound != null) {
            slowZoneSound.dispose();
        }
        if (levelClearSound != null) {
            levelClearSound.dispose();
        }
        if (gameClearSound != null) {
            gameClearSound.dispose();
        }

        // 배경음악 정리
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }
}
