import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

/**
 * JavaFX class that runs simulation and draws everything
 */
public class Viewer extends Application {

    static final double GAME_WIDTH = 1350;
    static final double GAME_HEIGHT = 680;

    private final Group root = new Group();
    private final Group blobGroup = new Group();

    private Random r = new Random();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Habitat");
        Scene scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT, Color.MEDIUMAQUAMARINE);

        /* Spawn Lucas
          Start with radius(10-50), mutationRate E(0.25, 0.5, 0.75), growthRate(10-16), spawnRatio(0.5),
          attack(0), defence(3), xenophobia(0), carnivory(0.0), altruismThreshold(195075), generosity(0.0), 1/2 power, speed(0-40)

          Blob (double centerX, double centerY, double radius, Paint fill, String name, double maxSize,
          double mutationRate, int powerGrowthRate, double spawnRatio, int attack, int defence, int xenophobia,
          double carnivory, int altruism, double generosity, int power, double favSpeed, double speedX, double speedY) {
         */
        double rad1 = r.nextDouble()*50.0+10.0;
        double speed1 = r.nextInt(30);
        blobGroup.getChildren().add(new Blob(GAME_WIDTH/2 - 100, GAME_HEIGHT/2, rad1, Color.rgb(r.nextInt(255),
                r.nextInt(255), r.nextInt(255)), "A0", rad1, r.nextDouble(), (int) ((r.nextDouble()+1)*8+3), 0.5,
                rad1/10.0, rad1/10.0, 192000, r.nextDouble(), 195000, 0.0, (int) rad1*150, speed1, -speed1/2, speed1/2));

        double rad2 = r.nextDouble()*40.0+20.0;
        double speed2 = r.nextInt(30);
        blobGroup.getChildren().add(new Blob(GAME_WIDTH/2 + 100, GAME_HEIGHT/2 + 50, rad2, Color.rgb(r.nextInt(255),
                r.nextInt(255), r.nextInt(255)), "A1", rad2, r.nextDouble(), (int) ((r.nextDouble()*1)+8+3), 0.5,
                rad2/10.0, rad2/10.0, 192000, r.nextDouble(), 195000, 0.0, (int) rad2*150, speed2, speed2/2, speed2/2));
        double rad3 = r.nextDouble()*40.0+10.0;
        double speed3 = r.nextInt(30);
        blobGroup.getChildren().add(new Blob(GAME_WIDTH/2, GAME_HEIGHT/2 - 100, rad3, Color.rgb(r.nextInt(255),
                r.nextInt(255), r.nextInt(255)), "A2", rad3, r.nextDouble(), (int) ((r.nextDouble()+1)*8+3), 0.5,
                rad3/10.0, rad3/10.0, 192000, r.nextDouble(), 195000, 0.0, (int) rad3*150, speed3, speed3/2, -speed3/2));

        BlobManager.mostSuccessfulBlob = (Blob) blobGroup.getChildren().get(0);

        root.getChildren().add(blobGroup);

        primaryStage.setScene(scene);

        // Timeline that makes all the action happen
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(30),
                handler -> {

                    for (int blobInd=0; blobInd<blobGroup.getChildren().size(); blobInd++) {
                        Blob blobster = (Blob) blobGroup.getChildren().get(blobInd);

                        blobster.updateAppearance();
                        blobster.grow();
                        blobster.upDatePosition();

                        // Spawn Blobs.
                        if (blobster.readyToSpawn() && blobGroup.getChildren().size() <= 150) {
                            blobGroup.getChildren().add(blobster.spawnChild());
                            if (BlobManager.getTotalBlobs() % 50 == 0)
                                BlobManager.report(blobGroup);
                        }
                    }

                    // Bounce all Blobs off walls or each other
                    for (int ind=0; ind<blobGroup.getChildren().size(); ind++) {
                        Interactions.keepBlobOnBoard((Blob) blobGroup.getChildren().get(ind));
                    }
                    Interactions.checkInteraction(blobGroup);

                    // Remove dead Blobs
                    for (int tracker=blobGroup.getChildren().size()-1; tracker>=0; tracker--) {
                        Blob blobby = (Blob) blobGroup.getChildren().get(tracker);
                        if (blobby.getDamage() > blobby.getPower()){
                            Interactions.totalKills += 1;
                            BlobManager.postMortem(blobby);
                            blobGroup.getChildren().remove(tracker);}
                    }
                    // Kill random Blob once too crowded
                    /*if (blobGroup.getChildren().size() > 100) {
                        int unluckyBlob = r.nextInt(blobGroup.getChildren().size());
                        BlobManager.postMortem((Blob) blobGroup.getChildren().get(unluckyBlob));
                        System.out.println("Killed a random");
                        blobGroup.getChildren().remove(unluckyBlob);
                        }*/
                }
                ));

        timeline.setCycleCount(Animation.INDEFINITE);
        BlobManager.report(blobGroup);
        timeline.play();

        primaryStage.show();
    }
}