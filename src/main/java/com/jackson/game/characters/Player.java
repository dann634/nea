package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import com.jackson.io.TextIO;
import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Player extends Character {

    private int xPos;
    private int yPos;
    private Label displayNameLabel;
    private Line aimingLine;
    private ImageView handImageView;
    private SimpleBooleanProperty isHoldingGun;
    private TranslateTransition attackTranslate;
    private final SimpleIntegerProperty agilityLevel;
    private final SimpleIntegerProperty strengthLevel;
    private final SimpleIntegerProperty defenceLevel;
    private final SimpleIntegerProperty agilityXP;
    private final SimpleIntegerProperty strengthXP;
    private final SimpleIntegerProperty defenceXP;
    private int[] currentItemOffsets;


    public Player(SimpleBooleanProperty isHoldingGun) {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initDisplayNameLabel();
        this.aimingLine = initAimingLine();
        currentItemOffsets = new int[]{0, 0, 0};

        this.isHoldingGun = isHoldingGun;
        aimingLine.visibleProperty().bind(isHoldingGun);
        isHoldingGun.addListener((observableValue, aBoolean, t1) -> {
            handImageView.setRotate(t1 ? 0 : isModelFacingRight.get() ? 45 : -45);
            if(!handImageView.getImage().getUrl().contains("pistol")) {
                handImageView.setScaleX(t1 ? 0.5 : 0.3);
                handImageView.setScaleY(t1 ? 0.5 : 0.3);
            }
        });


        agilityLevel = new SimpleIntegerProperty(1);
        strengthLevel = new SimpleIntegerProperty(1);
        defenceLevel = new SimpleIntegerProperty(1);

        agilityXP = new SimpleIntegerProperty(0);
        strengthXP = new SimpleIntegerProperty(0);
        defenceXP = new SimpleIntegerProperty(0);

        initHandRectangle();

        isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
            attackTranslate.stop(); //Fixes Attack and Turn Bug
            attackTranslate.setByX((newValue) ? 20 : -20);
            handImageView.setTranslateX(newValue ? currentItemOffsets[1] : currentItemOffsets[0]);
            handImageView.setNodeOrientation((newValue) ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            if(isHoldingGun.get()) {
                handImageView.setRotate(0);
            } else {
                handImageView.setRotate((newValue) ? 45 : -45);
            }
        });
    }

    protected void initHandRectangle() {
        // TODO: 12/12/2023 move this to player class
        handImageView = new ImageView();
        handImageView.yProperty().bind(yProperty().add(5));
        handImageView.xProperty().bind(xProperty());
        handImageView.setRotate(45);
        handImageView.setScaleY(0.3);
        handImageView.setScaleX(0.3);
        handImageView.setTranslateX(currentItemOffsets[1]);
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
        if(item == null) {
            itemName = "fist";
        } else {
            itemName = item.getItemName();
        }
        handImageView.setImage(new Image("file:src/main/resources/images/" + itemName + ".png"));
        handImageView.setVisible(true);
        //Offsets

        currentItemOffsets = getOffsets(itemName);
        handImageView.setTranslateY(currentItemOffsets[2]);
        handImageView.setTranslateX(isModelFacingRight.get() ? currentItemOffsets[1] : currentItemOffsets[0]);
    }

    private void initDisplayNameLabel() {
        displayNameLabel = new Label(TextIO.readFile("src/main/resources/settings/settings.txt").get(0));
        displayNameLabel.translateXProperty().bind(xProperty().subtract(displayNameLabel.getWidth() / 2));
        displayNameLabel.translateYProperty().bind(yProperty().subtract(15));
        displayNameLabel.setStyle("-fx-font-weight: bold");
        displayNameLabel.setVisible(false); // not for singleplayer
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

    public Label getDisplayNameLabel() {
        return displayNameLabel;
    }

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
    }

    @Override
    public void attack(Entity item) {
        if(item != null && !item.isUsable()) {
            return;
        }
        attackTranslate.play();
    }

    @Override
    public double getAttackDamage() {
        return super.getAttackDamage() + strengthLevel.get();
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
        if(1 - (defenceLevel.get() * 0.005) <= 0.3) {
            amount *= 0.3;
        } else {
            amount *= 1 - (defenceLevel.get() * 0.005); //reduce damage by 0.5% each level
        }
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

    public void setHoldingGun(boolean value) {
        isHoldingGun.set(value);
    }

}
