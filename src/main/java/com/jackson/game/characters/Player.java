package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import com.jackson.ui.GameController;
import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player extends Character implements PlayerInterface {

    private int xPos;
    private int yPos;
    private final Line aimingLine;
    private ImageView handImageView;
    private final SimpleBooleanProperty isHoldingGun;
    private TranslateTransition attackTranslate;
    private final PauseTransition shootingPause;
    private final SimpleIntegerProperty agilityLevel;
    private final SimpleIntegerProperty strengthLevel;
    private final SimpleIntegerProperty defenceLevel;
    private final SimpleIntegerProperty agilityXP;
    private final SimpleIntegerProperty strengthXP;
    private final SimpleIntegerProperty defenceXP;
    private int[] currentItemOffsets;
    private String itemInHand;
    private final SimpleIntegerProperty ammo;



    public Player(SimpleBooleanProperty isHoldingGun, SimpleIntegerProperty ammo) {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        this.aimingLine = initAimingLine();
        currentItemOffsets = new int[]{0, 0, 0};

        this.isHoldingGun = isHoldingGun;
        this.ammo = ammo;
        aimingLine.visibleProperty().bind(isHoldingGun);

        this.shootingPause = new PauseTransition();
        shootingPause.setOnFinished(e -> aimingLine.setStroke(Color.RED));

        agilityLevel = new SimpleIntegerProperty(1);
        strengthLevel = new SimpleIntegerProperty(1);
        defenceLevel = new SimpleIntegerProperty(1);

        agilityXP = new SimpleIntegerProperty(0);
        strengthXP = new SimpleIntegerProperty(0);
        defenceXP = new SimpleIntegerProperty(0);

        initHandRectangle();

        isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
            attackTranslate.setByX((newValue) ? 20 : -20);
            handImageView.xProperty().bind(xProperty().add(newValue ? currentItemOffsets[1] : currentItemOffsets[0]));
            handImageView.setNodeOrientation((newValue) ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            if(isHoldingGun.get()) {
                handImageView.setRotate(0);
            } else {
                handImageView.setRotate((newValue) ? 45 : -45);
            }
        });
    }

    protected void initHandRectangle() {
        handImageView = new ImageView();
        handImageView.yProperty().bind(yProperty().add(5));
        handImageView.xProperty().bind(xProperty());
        handImageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        attackTranslate = new TranslateTransition();
        attackTranslate.setNode(handImageView);
        attackTranslate.setCycleCount(2);
        attackTranslate.setAutoReverse(true);
        attackTranslate.setRate(4);
        attackTranslate.setByX(20);
    }

    public void updateBlockInHand(Entity item) {
        String itemName;
        itemName = item == null ? "fist" : item.getItemName();
        itemInHand = itemName;

        List<String> gunList = new ArrayList<>(List.of("rifle", "sniper", "pistol"));
        if(item != null && gunList.contains(item.getItemName())) {
            isHoldingGun.set(true);
            gunList.remove(2);

            //Update for rifle and sniper
            handImageView.setRotate(0);
            handImageView.setScaleX(0.5);
            handImageView.setScaleY(0.5);
            if(!gunList.contains(item.getItemName())) {
                //Pistol
                handImageView.setScaleX(0.3);
                handImageView.setScaleY(0.3);
            }
        } else {
            isHoldingGun.set(false);
            //Everything else
            handImageView.setRotate(isModelFacingRight.get() ? 45 : -45);
            handImageView.setScaleX(0.3);
            handImageView.setScaleY(0.3);
        }

        System.out.println("file:src/main/resources/images/" + itemName + ".png");
        handImageView.setImage(new Image("file:src/main/resources/images/" + itemName + ".png"));
        handImageView.setVisible(true);
        //Offsets

        currentItemOffsets = getOffsets(itemName);
        handImageView.yProperty().bind(yProperty().add(currentItemOffsets[2]).add(5));
        handImageView.xProperty().bind(xProperty().add(isModelFacingRight.get() ? currentItemOffsets[1] : currentItemOffsets[0]));
    }



    private Line initAimingLine() {
        Line line = new Line();
        line.setStroke(Color.RED);
        line.setStrokeWidth(4);
        line.setStartX(getX() + 16);
        line.setStartY(getY() + 32);
        line.setEndX(0);
        line.setEndY(0);
        return line;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public void addXPos(int value) {
        xPos += value;
    }

    public void addYPos(int value) {
        yPos += value;
    }

    public ImageView getHandRectangle() {
        return handImageView;
    }

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
    }

    @Override
    public void attack(Entity item) {
        if(item != null && !item.isUsable() || shootingPause.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        if(isHoldingGun.get()) {
            if(ammo.get() <= 0) {
                return;
            }
            //Shoot gun
            ammo.set(ammo.get() - 1);
            aimingLine.setStroke(Color.BLACK);
            int cooldownTime = switch (item.getItemName()) {
                case "pistol" -> 600;
                case "rifle" -> 300;
                case "sniper" -> 1500;
                default -> Integer.MAX_VALUE;
            };
            shootingPause.setDuration(Duration.millis(cooldownTime));
            shootingPause.play();
            return;
        }
        attackTranslate.play();
    }

    @Override
    public double getAttackDamage() {
        return super.getAttackDamage() + strengthLevel.get() + getWeaponInHandDamage();
    }


    public double getWeaponInHandDamage() {
        if(itemInHand.equals("fist")) {
            return 0;
        }

        //Guns
        if(itemInHand.equals("pistol")) return 10;
        if(itemInHand.equals("rifle")) return 20;
        if(itemInHand.equals("sniper")) return 50;

        double multiplier = 1;
        if(itemInHand.contains("wood")) {
            multiplier = 1.4;
        } else if(itemInHand.contains("stone")) {
            multiplier = 1.6;
        } else if(itemInHand.contains("metal")) {
            multiplier = 2;
        }
        if(itemInHand.contains("sword")) return 20 * multiplier;
        if(itemInHand.contains("pickaxe")) return 10 * multiplier;
        if(itemInHand.contains("shovel")) return 8 * multiplier;
        if(itemInHand.contains("axe")) return 15 * multiplier;
        return 0;
    }
    public void addStrengthXP(int amount) {
        strengthXP.set(strengthXP.get() + amount);
        if((50 * Math.pow(strengthLevel.get(), 1.5) < strengthXP.get())) {
            strengthLevel.set(strengthLevel.get() + 1);
        }
    }
    public void addAgilityXP(int amount) {
        agilityXP.set(agilityXP.get() + amount);
        if((50 * Math.pow(agilityLevel.get(), 1.5) < agilityXP.get())) {
            agilityLevel.set(agilityLevel.get() + 1);
        }
    }
    public void addDefenceXP(int amount) {
        defenceXP.set(defenceXP.get() + amount);
        if((50 * Math.pow(defenceLevel.get(), 1.5)) < defenceXP.get()) {
            defenceLevel.set(defenceLevel.get() + 1);
        }
    }

    public SimpleIntegerProperty agilityLevelProperty() {
        return agilityLevel;
    }
    public SimpleIntegerProperty strengthLevelProperty() {
        return strengthLevel;
    }
    public SimpleIntegerProperty defenceLevelProperty() {
        return defenceLevel;
    }
    public SimpleIntegerProperty agilityXPProperty() {
        return agilityXP;
    }
    public SimpleIntegerProperty strengthXPProperty() {
        return strengthXP;
    }
    public SimpleIntegerProperty defenceXPProperty() {
        return defenceXP;
    }

    @Override
    public boolean takeDamage(double amount) {
        if(new Random().nextDouble(100) < agilityLevel.get() * 0.5) {
            return false; //Dodge
        }
        addDefenceXP(10);
        //reduce damage by 0.5% each level
        amount *= Math.max(1 - (defenceLevel.get() * 0.005), 0.3);
        if(health.get() - amount < 0) {
            health.set(0);
            return true;
        }
        return super.takeDamage(amount);
    }

    public int getStrengthLevel() {
        return this.strengthLevel.get();
    }

    public int getAgilityLevel() {
        return agilityLevel.get();
    }

    public int getDefenceLevel() {
        return defenceLevel.get();
    }

    public int getAgilityXP() {
        return agilityXP.get();
    }

    public int getStrengthXP() {
        return strengthXP.get();
    }

    public int getDefenceXP() {
        return defenceXP.get();
    }

    private int[] getOffsets(String itemName) {

        if(GameController.lookupTable.containsKey(itemName) || itemName.equals("fist")) {
            return new int[]{-25, 9, 0};
        }

        if(itemName.equals("rifle") || itemName.contains("sniper")) {
            return new int[]{-50, -10, -5};
        }

        if(itemName.equals("coal")) {
            return new int[]{-35, 0, -10};
        }

        return new int[]{-62, -3, -25};
    }

    public void setStrength(int level, int xp) {
        strengthLevel.set(level);
        strengthXP.set(xp);
    }

    public void setAgility(int level, int xp) {
        agilityLevel.set(level);
        agilityXP.set(xp);
    }

    public void setDefence(int level, int xp) {
        defenceLevel.set(level);
        defenceXP.set(xp);
    }

    public Line getAimingLine() {
        return aimingLine;
    }

    public void updateAimingLine(int x, int y) {
        aimingLine.setEndX(x);
        aimingLine.setEndY(y);
    }

    public TranslateTransition getAttackTranslate() {
        return attackTranslate;
    }


    public PauseTransition getShootingPause() {
        return shootingPause;
    }

    public SimpleBooleanProperty isHoldingGunProperty() {
        return isHoldingGun;
    }

    public int getAmmo() {
        return ammo.get();
    }

    public void setAmmo(int ammo) {
        if(ammo < 0) {
            System.err.println("Error: Ammo must be greater than 0");
            return;
        }
        this.ammo.set(ammo);
    }



}
