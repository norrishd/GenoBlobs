import javafx.scene.Group;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * Contains various methods to deal with Blob movement, bouncing off walls and other Blobs
 */
class Interactions {

    private static Random randy = new Random();
    private static int GENERATIONS_TO_CHECK = 3;    // for the commonAncestry() method

    static int totalBattles = 0;
    static int totalKills = 0;
    static int totalHelps = 0;
    /**
     * Method to check whether Blob is still on the board. If it goes outside, the sign of its X or Y values is
     * made either positive or negative as needed. Velocity in the other plane is also jiggled slightly
     * to prevent Blobs spawning trapped in a 1D plane
     */
    static void keepBlobOnBoard(Blob aBlob) {
        int velJiggle = (randy.nextInt(20) - 10);
        if (aBlob.getCenterX() <= aBlob.getRadius()) {
            aBlob.setSpeedX(Math.abs(aBlob.getSpeedX()+1));
            aBlob.setSpeedY(aBlob.getSpeedY()+velJiggle);}
        if (aBlob.getCenterX() >= (Viewer.GAME_WIDTH - aBlob.getRadius())) {
            aBlob.setSpeedX(-Math.abs(aBlob.getSpeedX()+1));
            aBlob.setSpeedY(aBlob.getSpeedY()+velJiggle);}
        if (aBlob.getCenterY() <= aBlob.getRadius()) {
            aBlob.setSpeedY(Math.abs(aBlob.getSpeedY()+1));
            aBlob.setSpeedX(aBlob.getSpeedX()+velJiggle);}
        if (aBlob.getCenterY() >= (Viewer.GAME_HEIGHT - aBlob.getRadius())) {
            aBlob.setSpeedY(-Math.abs(aBlob.getSpeedY()+1));
            aBlob.setSpeedX(aBlob.getSpeedX()+velJiggle); }

            if (aBlob.getSpeed() < aBlob.getFavSpeed()) {
                aBlob.setSpeedX(1.005*aBlob.getSpeedX());
                aBlob.setSpeedY(1.005*aBlob.getSpeedY());
            } else if (aBlob.getSpeed() > aBlob.getFavSpeed()) {
                aBlob.setSpeedX(0.995*aBlob.getSpeedX());
                aBlob.setSpeedY(0.995*aBlob.getSpeedY());
        }
    }

    /**
     * Big ol' function to handle Blob collisions
     * 1. Checks whether axis-aligned bounding boxes are overlapping
     * 2. If so, checks if Blob radii actually overlapping
     * 3. If so, checks if they're related
     * 4. If not, calculates checkInteraction
     */
    static void checkInteraction(Group blobGroup) {
        // Array to track which Blobs are safe to spawn (not overlapping another Blob)
        int[] overlappingBlobs = new int[blobGroup.getChildren().size()];
        for (int i = 0; i < blobGroup.getChildren().size(); i++) {
            for (int j = i + 1; j < blobGroup.getChildren().size(); j++) {
                Blob blob1 = (Blob) blobGroup.getChildren().get(i);
                Blob blob2 = (Blob) blobGroup.getChildren().get(j);

                // 1. Broad phase check for Axis-aligned bounding box (AABB) overlap
                // Code stolen from http://gamedevelopment.tutsplus.com/tutorials/when-worlds-collide-simulating-circle-circle-collisions--gamedev-769
                if (blob1.getCenterX() + blob1.getRadius() > blob2.getCenterX() - blob2.getRadius() &&
                        blob1.getCenterX() - blob1.getRadius() < blob2.getCenterX() + blob2.getRadius() &&
                        blob1.getCenterY() + blob1.getRadius() > blob2.getCenterY() - blob2.getRadius() &&
                        blob1.getCenterY() - blob1.getRadius() < blob2.getCenterY() + blob2.getRadius()) {

                    // AABBs are overlapping.
                    // 2. Check if Blobs have collided
                    double distance = Math.sqrt((Math.pow((blob2.getCenterX() - blob1.getCenterX()), 2)) +
                            (Math.pow((blob2.getCenterY() - blob1.getCenterY()), 2)));

                    // 3. Blobs have collided. Note this in their overlapping tally. Initiate altruism/hostility checks
                    if (distance < blob1.getRadius() + blob2.getRadius()) {
                        overlappingBlobs[i] += 1;
                        overlappingBlobs[j] += 1;

                        // 3.1. Check whether to exhibit altruism based on similarity
                        Attitude blob1attitude;
                        Attitude blob2attitude;
                        blob1attitude = assessAltruism(blob1, blob2);
                        blob2attitude = assessAltruism(blob2, blob1);

                        // 3.2. Check whether to ignore using phylogeny
                        if (blob1attitude == Attitude.UNDECIDED)
                            blob1attitude = assessCommonAncestry(blob1, blob2);
                        if (blob2attitude == Attitude.UNDECIDED)
                            blob2attitude = assessCommonAncestry(blob2, blob1);

                        // 3.3. Check whether to attack using xenophobia
                        if (blob1attitude == Attitude.UNDECIDED)
                            blob1attitude = assessHostility(blob1, blob2);
                        if (blob2attitude == Attitude.UNDECIDED)
                            blob2attitude = assessHostility(blob2, blob1);

                        // 4. Carry out relevant interactions
                        if (blob1attitude == Attitude.FRIENDLY && blob2attitude != Attitude.HOSTILE &&
                                !blob1.recentlyHelped.contains(blob2.getName())) {
                            help(blob1, blob2);
                        } if (blob2attitude == Attitude.FRIENDLY && blob1attitude != Attitude.HOSTILE &&
                                !blob2.recentlyHelped.contains(blob1.getName())) {
                            help(blob2, blob1);
                        } else if (blob1attitude == Attitude.HOSTILE || blob2attitude == Attitude.HOSTILE) {
                            battle(blob1, blob1attitude, blob2, blob2attitude);
                            bounce(blob1, blob2);
                        }
                        /*else if (blob1attitude == Attitude.WARY || blob2attitude == Attitude.WARY) {
                            bounce(blob1, blob2);
                        }*/
                    }
                }
            }
            // Update number of overlapping Blobs
            for (int b = 0; b < overlappingBlobs.length; b++) {
                ((Blob) blobGroup.getChildren().get(b)).setSafeToSpawn(overlappingBlobs[b]);
            }
        }
    }

    // Find how related two Blobs are based on colour
    private static int assessRelatedness(Blob blob1, Blob blob2) {
        Color color1 = (Color) blob1.getFill();
        Color color2 = (Color) blob2.getFill();

        return (int) (195075 - Math.pow(((color1.getRed() - color2.getRed()) * 255), 2) -
                Math.pow(((color1.getGreen() - color2.getGreen()) * 255), 2) -
                Math.pow(((color1.getBlue() - color2.getBlue()) * 255), 2));
    }

    // Decide whether a Blob will be altruistic to another based on colour
    private static Attitude assessAltruism(Blob selfBlob, Blob otherBlob) {
        int relatedness = assessRelatedness(selfBlob, otherBlob);

        return (relatedness >= selfBlob.getAltruismThreshold() ? Attitude.FRIENDLY : Attitude.UNDECIDED);
    }

    // Decided whether two Blobs are related by ancestry going back x generations
    private static Attitude assessCommonAncestry(Blob selfBlob, Blob otherBlob) {
        String name1 = selfBlob.getName();
        String name2 = otherBlob.getName();
        String[] lineage1 = new String[Math.min(name1.length() / 2, GENERATIONS_TO_CHECK)];
        String[] lineage2 = new String[Math.min(name2.length() / 2, GENERATIONS_TO_CHECK)];
        // Populate ancestor arrays
        for (int gen = 0; gen < lineage1.length; gen++) {
            lineage1[gen] = name1.substring(0, name1.length() - gen * 2); }
        for (int gen = 0; gen < lineage2.length; gen++) {
            lineage2[gen] = name2.substring(0, name2.length() - gen * 2); }

        for (String code1 : lineage1) {
            for (String code2 : lineage2) {
                if (code1.equals(code2)) {
                    return Attitude.NEUTRAL;
                }
            }
        }
        return Attitude.UNDECIDED;
    }

    // Decide whether a Blob will attack another based on relatedness
    private static Attitude assessHostility(Blob selfBlob, Blob otherBlob) {
        int relatedness = assessRelatedness(selfBlob, otherBlob);
        return (relatedness < selfBlob.getXenophobia() ? Attitude.HOSTILE : Attitude.WARY);
    }

    // Exhibit altruism
    private static void help(Blob helper, Blob helpee) {
        helpee.gainPower((int) (helper.getPower()*helper.getGenerosity()*Blob.ENERGY_EXCHANGE_EFFICIENCY));
        helper.gainPower((int) -(helper.getPower()*helper.getGenerosity()));
        helper.recentlyHelped.add(helpee.getName());
        totalHelps += 1;
    }

    // Alter Blob velocities, using radius as proxy for mass
    // Code from http://gamedevelopment.tutsplus.com/tutorials/when-worlds-collide-simulating-circle-circle-collisions--gamedev-769
    private static void bounce(Blob blob1, Blob blob2) {
        double newVelX1 = (blob1.getSpeedX() * (blob1.getRadius() / 100 - blob2.getRadius() / 100) +
                (2 * blob2.getRadius() / 100 * blob2.getSpeedX()) / (blob1.getRadius() / 100 + blob2.getRadius() / 100));
        double newVelY1 = (blob1.getSpeedY() * (blob1.getRadius() / 100 - blob2.getRadius() / 100) +
                (2 * blob2.getRadius() / 100 * blob2.getSpeedY()) / (blob1.getRadius() / 100 + blob2.getRadius() / 100));
        double newVelX2 = (blob2.getSpeedX() * (blob2.getRadius() / 100 - blob1.getRadius() / 100) +
                (2 * blob1.getRadius() / 100 * blob1.getSpeedX()) / (blob1.getRadius() / 100 + blob2.getRadius() / 100));
        double newVelY2 = (blob2.getSpeedY() * (blob2.getRadius() / 100 - blob1.getRadius() / 100) +
                (2 * blob1.getRadius() / 100 * blob1.getSpeedY()) / (blob1.getRadius() / 100 + blob2.getRadius() / 100));
        blob1.setSpeedX(newVelX1);
        blob1.setSpeedY(newVelY1);
        blob2.setSpeedX(newVelX2);
        blob2.setSpeedY(newVelY2);

        // Move Blobs apart by a few pixels to prevent them being stuck inside each other
        blob1.setCenterX(blob1.getCenterX() + newVelX1 / 10);
        blob1.setCenterY(blob1.getCenterY() + newVelY1 / 10);
        blob2.setCenterX(blob2.getCenterX() + newVelX2 / 10);
        blob2.setCenterY(blob2.getCenterY() + newVelY2 / 10);
    }

    private static void battle(Blob blob1, Attitude blob1attitude, Blob blob2, Attitude blob2attitude) {
        // Modified from Mark's recommended battle algorithm
        totalBattles += 1;
        Blob attacker;
        Blob defender;
        if (blob1attitude.equals(Attitude.HOSTILE) && !blob2attitude.equals(Attitude.HOSTILE)) {
            attacker = blob1;
            defender = blob2;
        } else if (!blob1attitude.equals(Attitude.HOSTILE) && blob2attitude.equals(Attitude.HOSTILE)) {
            attacker = blob2;
            defender = blob1;
        } else if (randy.nextDouble() >= 0.5) {
            attacker = blob1;
            defender = blob2;
        } else {
            attacker = blob2;
            defender = blob1;
        }
        // Normalise attack and defence to how much power Blob has. Attack and def reach max at adulthood (spawnThreshold/2)
        double attackerAttack, attackerDefence, defenderAttack, defenderDefence;
        if (attacker.getPower() < attacker.getSpawnThreshold()/2) {
            attackerAttack = 2 * attacker.getAttack() * (attacker.getPower() / attacker.getSpawnThreshold());
            attackerDefence = 2 * attacker.getDefence() * (attacker.getPower() / attacker.getSpawnThreshold());
        } else {
            attackerAttack = attacker.getAttack();
            attackerDefence = attacker.getDefence();
        }
        if (defender.getPower() <= defender.getSpawnThreshold()/2) {
            defenderAttack = 2 * defender.getAttack() * (defender.getPower() / defender.getSpawnThreshold());
            defenderDefence = 2 * defender.getDefence() * (defender.getPower() / defender.getSpawnThreshold());
        } else {
            defenderAttack = defender.getAttack();
            defenderDefence = defender.getDefence();
        }

        // Made it so that threshold is 0.2-0.8. Means sufficiently small Blobs can't kill sufficiently large ones
        double survivalThreshold1 = randy.nextDouble()*(7.0/10.0)+0.3;
        double survivalThreshold2 = randy.nextDouble()*(7.0/10.0)+0.3;
        if ((attackerAttack / (attackerAttack + defenderDefence) > survivalThreshold1)) {
            defender.takeDamage((int) (attackerAttack*Math.pow(attacker.getRadius(), 2)));
            if (defender.getDamage() > defender.getPower())
                attacker.devour(defender.getPower());
        } else if ((defenderAttack / (defenderAttack + attackerDefence)) > survivalThreshold2) {
            attacker.takeDamage((int) (defenderAttack*Math.pow(defender.getRadius(), 2)));

            if (attacker.getDamage() > attacker.getPower())
                defender.devour(attacker.getPower());
        }
    }

    // Finds collision point of two Blobs. Might be useful later.
    // Also stolen from http://gamedevelopment.tutsplus.com/tutorials/when-worlds-collide-simulating-circle-circle-collisions--gamedev-769
    private void getCollisionCoords(Blob blob1, Blob blob2) {
        double collisionPointX = ((blob1.getCenterX() * blob2.getRadius()) + (blob2.getCenterX() * blob1.getRadius()))
                / (blob1.getRadius() + blob2.getRadius());

        double collisionPointY = ((blob1.getCenterY() * blob2.getRadius()) + (blob2.getCenterY() * blob1.getRadius()))
                / (blob1.getRadius() + blob2.getRadius());
    }
}