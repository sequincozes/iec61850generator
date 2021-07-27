/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import uff.midiacom.goose.GooseEventManager;
import uff.midiacom.model.GooseMessage;

/**
 * @author silvio
 */
public class UC01 extends AbstractUseCase {


    // Replay random
    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC01 extractor = new UC01();
        extractor.attackType = "random_replay";

//        extractor.gooseEventManager = new GooseEventManager(false, 0,  0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        int numReplayInstances = 1000; // 1000 attacks per scenario

        for (int resistence : resistences) {
            for (int run = 1; run <= AbstractUseCase.runs; run++) { // max is 132 (12 x 11)
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.generateReplayAttacksUC1(resistence, "00" + run, numReplayInstances);
                        break;
                    case 2:
                        extractor.generateReplayAttacksUC1(resistence, "0" + run, numReplayInstances);
                        break;
                    default:
                        extractor.generateReplayAttacksUC1(resistence, String.valueOf(run), numReplayInstances);
                        break;
                }
                if (generateSingleRound) {
                    System.exit(0);
                }
            }
        }

        extractor.finishWriting();
    }

    /**
     * @param res                - fault resistence
     * @param num                - number of run
     * @param numReplayInstances - number of attack instances
     * @return random replays
     * @throws java.io.FileNotFoundException
     */
    private void generateReplayAttacksUC1(int res, String num, int numReplayInstances) throws IOException {
        restartCounters();
        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, new double[]{offset + 0.5, offset + 0.6}, 0.00631, offset + 0.01659, 6.33000000000011f, 4, 1000);

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);


        /* Write Header and Columns */
        if (printHeader) {
            writeDefaultHeader();
            printHeader = false;
        }

        // Generating Replay Goose Messages
        for (int i = 0; i < numReplayInstances; i++) {
            // Define the current timestamp
            Random svRandom = new Random(System.nanoTime());
            int svIndex = svRandom.nextInt(formatedCSVFile2.size()); // random index, random SV messages
            Float currentTimestamp = offset + formatedCSVFile.get(svIndex)[0];

            // Pickups one old GOOSE randomly
            GooseMessage randomGoose = gooseEventManager.getGooseMessages().get(randomBetween(0, gooseEventManager.getGooseMessages().size()));
            while (randomGoose.getTimestamp() > currentTimestamp) { // this ensures that the message is old
                // try again
                randomGoose = gooseEventManager.getGooseMessages().get(randomBetween(0, gooseEventManager.getGooseMessages().size()));
                svRandom = new Random(System.nanoTime());
                svIndex = svRandom.nextInt(formatedCSVFile2.size()); // random index, random SV messages
                currentTimestamp = offset + formatedCSVFile.get(svIndex)[0];
            }
            // Sets the current timestamp to the old message
            randomGoose.setTimestamp(currentTimestamp);

            // Pickups the last legitimate message
            GooseMessage lastLegitimateGoose = gooseEventManager.getLastGooseFromSV(currentTimestamp);

            String svHist;
            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i), formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i), formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
            }

            // Write line
            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," + svHist + "," + randomGoose.asCSVFull() + getConsistencyFeaturesAsCSV(randomGoose, currentTimestamp) + "," + label[1];

            write(line);

        }
//@Deprecated
//        for (int i = 0; i < numReplayInstances; i++) {
//            Random svRandom = new Random(System.nanoTime());
//            int svIndex = svRandom.nextInt(formatedCSVFile2.size()); // random index, random SV messages
//
//            Random gooseRandom = new Random(System.nanoTime());
//            int gooseTime = gooseRandom.nextInt(1000); // random index, random SV messages
//
//            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," +
//                    gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull() +
//                    getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(gooseTime)) +
//                    "," + label[1];
//
//            write(line);
//        }

    }

    private void writeCustomHeader(String[] columns, String[] columns2, String[] columnsGOOSEType) throws IOException {
        System.out.println("Not default.");
        write("@relation compiledtraffic");

        for (String column : columns) {
            write("@attribute " + column + " numeric ");
        }

        for (String column : columns2) {
            if (!column.equals("Time"))
                write("@attribute " + column + " numeric ");
        }

        for (int i = 0; i < columnsGOOSE.length; i++) {
            write("@attribute " + columnsGOOSE[i] + " " + columnsGOOSEType[i]);
        }

        write("@attribute @class@ {" +
                label[0] + ", " +
                label[1] + ", " +
                label[2] + ", " +
                label[3] + ", " +
                label[4] + ", " +
                label[5] +
                "}");
        write("@data");
    }

}
