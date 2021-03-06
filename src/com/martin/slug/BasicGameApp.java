package com.martin.slug;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.view.EntityView;
import com.almasb.fxgl.input.*;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.settings.GameSettings;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.Map;

public class BasicGameApp extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Cosby Hunter");
        settings.setVersion("0.1");
        settings.setMenuEnabled(true);
        settings.setMenuKey(KeyCode.ESCAPE);
    }

    public enum EntityType {
        BULLET, ENEMY
    }


    private Entity player;
    private PlayerControl playerControl;


    @Override
    protected void initGame(){

        initTreasure();
        initPlayer();
        spawnEnemy();

    }

    private void initTreasure() {
        Entity treasure = new Entity();
        treasure.getPositionComponent().setValue(getWidth() / 2, getHeight() / 2);
        treasure.getViewComponent().setView(new Rectangle(40, 40, Color.YELLOW));

        getGameWorld().addEntity(treasure);
    }

    private void initPlayer() {
        player = new Entity();
        player.getPositionComponent().setValue(getWidth() / 20, getHeight() / 20);
        player.setViewFromTexture("gamesprite.png");

        WeaponComponent weapon = new WeaponComponent();
        weapon.setDamage(2);
        weapon.setFireRate(1.0);
        weapon.setMaxAmmo(10);

        player.addComponent(weapon);

        playerControl = new PlayerControl();
        player.addComponent(playerControl);

        getGameWorld().addEntity(player);
    }

    @Override
    protected void initInput(){
        Input input = getInput();

        input.addInputMapping(new InputMapping("Shoot", KeyCode.F));


        // Se Martin, her tilføjer vi en action handler til SPACE
        input.addAction(new UserAction("Shoot Space") {
            @Override
            protected void onAction(){
                // Jeg laver en random Point2D variable, fordi den er nødvendig for skudretning (du må finde ud af, hvilket point du vil sigte efter)
                Point2D p = player.getCenter();
                // Så kalder jeg din shoot metode
                playerControl.shoot(p);
            }

        }, KeyCode.SPACE);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction(){
                player.translateX(5);
                getGameState().increment("pixelsMoved", +5);
            }

        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.translateX(-5); // move left 5 pixels
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                player.translateY(-5); // move up 5 pixels
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                player.translateY(5); // move down 5 pixels
                getGameState().increment("pixelsMoved", +5);
            }
        }, KeyCode.S);

        // input.addAction(new UserAction("Shoot") {
        //    @Override
        //    protected void onAction() {
        //        player.translate();
        //    }
        // }, KeyCode.SPACE);

    }

    @Override
    protected void initPhysics() {
        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                BulletComponent bulletData = bullet.getComponent(BulletComponent.class);

                bulletData.setHp(bulletData.getHp() - 1);

                HPComponent hp = enemy.getComponent(HPComponent.class);
                hp.decrement(bulletData.getDamage() + player.getComponent(WeaponComponent.class).getDamage());
                if (hp.getValue() <= 0)
                    enemy.removeFromWorld();

                if (bulletData.getHp() <= 0)
                    bullet.removeFromWorld();
            }
        });
    }

    @Override
    protected void onUpdate(double tpf) {

    }

    @OnUserAction(name = "Shoot", type = ActionType.ON_ACTION_BEGIN)
    public void shoot() {
        playerControl.shoot(getInput().getVectorToMouse(player.getPositionComponent().getValue()));
    }

    private void spawnEnemy() {
        Entity enemy = new Entity();
        enemy.getTypeComponent().setValue(EntityType.ENEMY);
        enemy.getPositionComponent().setValue(500,200);
        enemy.getViewComponent().setTexture("cosbyboss.png", true);

        enemy.addComponent(new CollidableComponent(true));
        enemy.addComponent(new HPComponent(5));
        enemy.addComponent(new EnemyControl(new Point2D(getWidth() / 2, getHeight() / 2)));

        getGameWorld().addEntity(enemy);
    }


    @Override
    protected void initUI() {
        Text textPixels = new Text();
        textPixels.setTranslateX(25); // x = 50
        textPixels.setTranslateY(25); // y = 100
        getGameScene().setBackgroundRepeat("background.png");

        textPixels.textProperty().bind(getGameState().intProperty("pixelsMoved").asString());

        getGameScene().addUINode(textPixels); // add to the scene graph
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("pixelsMoved", 0);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
