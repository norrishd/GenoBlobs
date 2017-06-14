import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class for creating Blob objects
 *
 * Blob naming convention: <char><int><char><int>
 *     -> char indicates the generation and int indicates which index a Blob was amongst its siblings
 *
 */

public class Blob extends Circle {

    final static double ENERGY_EXCHANGE_EFFICIENCY = 0.5;

    private String name;                // a unique identifier for the Blob describing its lineage
    private double maxSize;             // determines adult radius size. Spawn in at half max
    private double mutationRate;        // determines how radically children vary from self
    private int powerGrowthRate;
    private int spawnThreshold;         // splits when power exceeds this value
    private double spawnRatio;          // how much power it gives to child Blob

    // Aggression fields
    private double attack;
    private double defence;
    private int xenophobia;             // how different another Blob must be for it to attack (0-195075)
    private double carnivory;           // how much power it gains when killing another Blob

    // Altruism fields
    private int altruismThreshold;      // threshold for recognising another Blob as in-group (0-195075)
    private double generosity;          // how much energy to gift to in-group Blobs
    List<String> recentlyHelped = new ArrayList<>();

    private int power;                  // Splits when power exceeds splitThreshold
    private int bonusPower;
    private int damage;
    private double favSpeed;
    private double speedX;
    private double speedY;
    private int childrenSpawned;
    private int overlappingBlobs;

    Blob (double centerX, double centerY, double radius, Paint fill, String name, double maxSize,
          double mutationRate, int powerGrowthRate, double spawnRatio, double attack, double defence, int xenophobia,
          double carnivory, int altruismThreshold, double generosity, int power, double favSpeed, double speedX, double speedY) {
        super(centerX, centerY, radius, fill);

        this.name = name;
        this.maxSize = maxSize;
        this.mutationRate = mutationRate;
        this.powerGrowthRate = powerGrowthRate;
        this.spawnThreshold = (int) (maxSize*200 + 2000);
        this.spawnRatio = spawnRatio;
        this.attack = attack;
        this.defence = defence;
        this.xenophobia = xenophobia;
        this.carnivory = carnivory;
        this.altruismThreshold = altruismThreshold;
        this.generosity = generosity;

        this.power = power;
        this.bonusPower = 0;
        this.damage = 0;
        this.favSpeed = favSpeed;
        this.speedX = speedX;
        this.speedY = speedY;
        this.childrenSpawned = 0;
        this.overlappingBlobs = 0;
        // Add parent to "helped" list
        this.recentlyHelped.add((name.substring(0,name.length()-2)));

        // Set the stroke details and opacity
        this.setStrokeType(StrokeType.CENTERED);
        this.setStrokeWidth(radius/10);
        this.setStroke(Color.BLACK);
        this.setOpacity(0.8);
    }

    /**
     * Spawns a child Blob by mutating parents Blob's attributes
     */
    Blob spawnChild() {
        Random r = new Random();

        // Radius, speed and power
        double newX = getCenterX() + r.nextGaussian()*getRadius()/4;
        double newY = getCenterY() + r.nextGaussian()*getRadius()/4;
        double newMaxSize = maxSize + r.nextGaussian()*mutationRate*maxSize/20;
        // If ever needed: if (newMaxSize<10)
        //  newMaxSize = newMaxSize = 10 + (10-newMaxSize)

        // Colours. If colour goes outside [0-255], checkInteraction back into range by amount exceeded
        Color parentColor = (Color) this.getFill();
        int newRed = Math.abs((int) (parentColor.getRed()*255 + r.nextGaussian()*mutationRate*30));
        if (newRed > 255) newRed = 255 - (newRed-255);
        int newGreen = Math.abs((int) (parentColor.getGreen()*255 + r.nextGaussian()*mutationRate*30));
        if (newGreen > 255) newGreen = 255 - (newGreen-255);
        int newBlue = Math.abs((int) (parentColor.getBlue()*255 + r.nextGaussian()*mutationRate*30));
        if (newBlue > 255) newBlue = 255 - (newBlue-255);

        String newName = childNameBuilder();
        double newMutRate = Math.abs(mutationRate + r.nextGaussian()/10*mutationRate);
        if (newMutRate > 1.0)
            newMutRate = 1.0 - (newMutRate-1.0);
        int newPowerGrowth = (int) Math.min(40, powerGrowthRate + r.nextGaussian()*mutationRate*2);
        double newSpawnRatio = Math.abs(spawnRatio + r.nextGaussian()/10*mutationRate);
        if (newSpawnRatio > 1.0)
            newSpawnRatio = 1.0 - (newSpawnRatio - 1.0);

        // Interaction fields
        double newAttack = Math.abs(attack + r.nextGaussian()*mutationRate*5);
        // Defence bounded by radius. Must be > 0 or battle combat system may try to divide by 0
        double newDefence = Math.min(defence + r.nextGaussian()*mutationRate*3, getRadius());
        if (newDefence <1.0) newDefence = 1.0;
        int newXeno = (int) Math.min(195075, Math.abs(xenophobia + r.nextGaussian()*mutationRate*3000));
        int newAltruism = (int) Math.min(195075, Math.abs(altruismThreshold + r.nextGaussian()*mutationRate*3000));
        if (newXeno > newAltruism) {
            newXeno = (newXeno+newAltruism)/2-1;
            newAltruism = (newXeno+newAltruism)/2+1;
        }
        double newCarnivory = Math.abs(carnivory + r.nextGaussian()*mutationRate/7);
        if (newCarnivory > 1.0)
            newCarnivory = 1.0 - (newCarnivory-1.0);
        double newGenerosity = Math.abs(generosity + r.nextGaussian()*mutationRate/7);
        if (newGenerosity > 1.0)
            newGenerosity = 1.0 - (newGenerosity-1.0);
        int childPower = (int) (power*spawnRatio*ENERGY_EXCHANGE_EFFICIENCY);
        double newFavSpeed = Math.abs(favSpeed + r.nextGaussian()*mutationRate*3);
        double newXspeed = speedX + speedX*r.nextGaussian()/3;
        double newYspeed = speedY + speedX*r.nextGaussian()/3;

        // Update parent's attributes
        power -= power*spawnRatio;
        this.setOpacity(0.8);
        this.childrenSpawned += 1;
        this.recentlyHelped.add(newName);

        BlobManager.addBlob();

        return new Blob(newX, newY, newMaxSize/2, Color.rgb(newRed, newGreen, newBlue), newName,
                newMaxSize, newMutRate, newPowerGrowth, newSpawnRatio, newAttack, newDefence, newXeno,
                newCarnivory, newAltruism, newGenerosity, childPower, newFavSpeed, newXspeed, newYspeed);
    }

    void upDatePosition(){
        setCenterX(getCenterX()+speedX/10);
        setCenterY(getCenterY()+speedY/10);
    }


    void updateAppearance(){
        if (power < spawnThreshold/2) {
            this.setRadius(((3*maxSize)/(4*spawnThreshold))*power+(5*maxSize/8));
            this.setStrokeWidth(this.getRadius()/10); }
        if (power > 8*spawnThreshold/10) {
            this.setOpacity((double)power/(double)spawnThreshold);
        }
    }

    // Builds name for child Blob
    private String childNameBuilder() {
        char generation = name.charAt(name.length()-2);
        char nextGeneration = (char) (generation + 1);
        // Update max generation
        if (nextGeneration > BlobManager.maxGeneration)
            BlobManager.maxGeneration = nextGeneration;
        return name + nextGeneration + childrenSpawned;
    }


    final void grow(){
        double energyGain = (int) Math.max(0, powerGrowthRate - (getRadius()*favSpeed/200 + attack/10 + defence/5));
        if (power < spawnThreshold && energyGain > 0) {
            power += energyGain;
            if (bonusPower >= 100) {
                power += 50;
                bonusPower -= 100;
            }
        }
    }

    void devour(int preyPower) {
        bonusPower += ENERGY_EXCHANGE_EFFICIENCY*preyPower;
    }

    void gainPower(int amount) {
        bonusPower += amount;
    }

    void takeDamage(int enemyAttack) {
        damage += enemyAttack;
    }

    // Returns true if Blob has sufficient power and not overlapping 2 Blobs
    boolean readyToSpawn() {
        return ((power >= spawnThreshold) && (overlappingBlobs <= 2));
    }

    int getAltruismThreshold() { return altruismThreshold; }
    double getAttack() { return attack; }
    double getCarnivory() { return carnivory; }
    int getChildrenSpawned() { return childrenSpawned; }
    int getDamage() { return damage; }
    double getDefence() { return defence; }
    double getGenerosity() { return generosity; }
    double getMaxSize() { return maxSize; }
    double getMutationRate() { return mutationRate; }
    String getName() { return name; }
    int getPower() { return power; }
    int getPowerGrowthRate() { return powerGrowthRate; }
    double getSpawnRatio() { return spawnRatio; }
    double getSpawnThreshold() { return spawnThreshold; }
    double getSpeed() { return Math.sqrt(Math.pow(speedX, 2.0) + Math.pow(speedY, 2.0)); }
    double getFavSpeed() { return favSpeed; }
    void setSpeedX(double value){ speedX = value; }
    double getSpeedX(){ return speedX; }
    void setSpeedY(double value){ speedY = value; }
    double getSpeedY(){ return speedY; }
    int getXenophobia() {return xenophobia; }
    void setSafeToSpawn(int isAlone) { overlappingBlobs = isAlone; }


    /*P
    double attack, double defence, int xenophobia,
    double carnivory, int altruismThreshold, double generosity, childrenSpawned) {*/


    @Override
    public String toString() {
        Color color = (Color) getFill();
        String colorCode = (color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());

        return ("Name: " + name + "\nPower: " + power  + "\nSize: " + getRadius() + "\nMax size: " +
                maxSize + "\nColour: " + colorCode + "\nSpeed " + favSpeed  + "\nGrowth rate: " + powerGrowthRate +
                "\nSpawn threshold: " + spawnThreshold + "\nSpawn ratio: " + spawnRatio + "\nMutation rate: " +
                mutationRate );
    }
}
