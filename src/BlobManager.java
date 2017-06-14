import javafx.scene.Group;

/**
 * Class to keep track of info on all Blobs created
 */
class BlobManager {
    static Blob mostSuccessfulBlob;
    private static int totalBlobs = 0;
    static char maxGeneration;

    static void addBlob() { totalBlobs += 1; }

    static void report(Group currentlyLiving) {
        // Build group deets
        double livingBlobs = currentlyLiving.getChildren().size();
        double radii = 0.0;
        double speeds = 0.0;
        int growthRates = 0;
        double mutRates = 0.0;
        double spawnRatios = 0.0;
        double spawnThresholds = 0.0;
        double carnivories = 0.0;
        int xenophobias = 0;
        int attacks = 0;
        int defences = 0;
        int altruisms = 0;
        double generosities = 0.0;
        for (int i=0; i<currentlyLiving.getChildren().size(); i++) {
            Blob blobby = ((Blob) currentlyLiving.getChildren().get(i));
            radii += blobby.getMaxSize();
            speeds += Math.sqrt(Math.pow(blobby.getSpeedX(), 2.0) + Math.pow(blobby.getSpeedY(), 2.0));
            growthRates += blobby.getPowerGrowthRate();
            mutRates += blobby.getMutationRate();
            spawnRatios += blobby.getSpawnRatio();
            spawnThresholds += blobby.getSpawnThreshold();
            carnivories += blobby.getCarnivory();
            xenophobias += blobby.getXenophobia();
            attacks += blobby.getAttack();
            defences += blobby.getDefence();
            altruisms += blobby.getAltruismThreshold();
            generosities += blobby.getGenerosity();

            // Check if a living Blob is most successful Blob to have lived
            if (blobby.getChildrenSpawned() > mostSuccessfulBlob.getChildrenSpawned())
                mostSuccessfulBlob = blobby;
        }
        radii = radii / livingBlobs;
        speeds = speeds / livingBlobs;
        growthRates = (int) (growthRates / livingBlobs);
        mutRates = mutRates / livingBlobs;
        spawnRatios = spawnRatios / livingBlobs;
        carnivories = carnivories/livingBlobs;
        xenophobias = (int) (xenophobias / livingBlobs);
        attacks = (int) (attacks / livingBlobs);
        defences = (int) (defences / livingBlobs);
        altruisms = (int) (altruisms / livingBlobs);
        generosities = generosities / livingBlobs;

        System.out.println("\nAltogether, " + totalBlobs + " Blobs have been spawned over " +
                (maxGeneration-65) + " generations." );
        System.out.println("Currently there are " + livingBlobs + " blobs alive");
        System.out.println("Total battles: " + Interactions.totalBattles);
        System.out.println("Total kills: " + Interactions.totalKills);
        System.out.println("Total altruistic acts: " + Interactions.totalHelps);
        System.out.println("Average Blob size: " + Math.round(radii));
        System.out.println("Average speed: " + Math.round(speeds));
        System.out.println("Average growth rate: " + growthRates);
        System.out.println("Average mutation rate: " + mutRates);
        System.out.println("Average spawn ratio: " + spawnRatios);
        System.out.println("Average carnivory: " + carnivories);
        System.out.println("Average attack: " + attacks);
        System.out.println("Average defence: " + defences);
        System.out.println("Average altruism: " + altruisms);
        System.out.println("Average xenophobia: " + xenophobias);
        System.out.println("Average generosity: " + generosities);


        /*System.out.println("Most successful Blob to have lived: " + mostSuccessfulBlob.getName() + ", with " +
                mostSuccessfulBlob.getChildrenSpawned() + " children spawned, " +
                "size of " + Math.round(mostSuccessfulBlob.getRadius()) + ", speed of " +
                Math.round(mostSuccessfulBlob.getFavSpeed()) + ", mutation rate of " +
                mostSuccessfulBlob.getMutationRate() + ",\nspawn ratio of " + mostSuccessfulBlob.getSpawnRatio() +
                ", scavenger ability of " + mostSuccessfulBlob.getCarnivory() + " and spawn threshold of " +
                mostSuccessfulBlob.getSpawnThreshold());*/

/*        Blob youngBlob = (Blob) currentlyLiving.getChildren().get(currentlyLiving.getChildren().size()-1);
        System.out.println("\nLatest Blob: " + youngBlob.getName() + ", with size of " +
                Math.round(youngBlob.getRadius()) + ", speed of " + Math.round(youngBlob.getFavSpeed()) +
                ", mutation rate of " + youngBlob.getMutationRate() + ",\nspawn ratio of " +
                youngBlob.getSpawnRatio() + ", scavenger ability of " + youngBlob.getCarnivory() +
                " and spawn threshold of " + youngBlob.getSpawnThreshold());*/
    }

    static void postMortem(Blob blobby) {
        if (blobby.getChildrenSpawned() > mostSuccessfulBlob.getChildrenSpawned())
            mostSuccessfulBlob = blobby;
    }

    static int getTotalBlobs() { return totalBlobs; }
}