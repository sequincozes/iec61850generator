/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import uff.midiacom.goose.GooseEventManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author silvio
 */
public class UC00_noFaults extends AbstractUseCase {
    String endtimeLastRun = "";
    private double firstGoose;
    private double currentGoose;
    public static void run(String filename) throws IOException {
        outputFile = outputLocation + filename;
        UC00_noFaults extractor = new UC00_noFaults();
        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        for (int resistence : resistences) {
            for (int run = 1; run <= AbstractUseCase.runs; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.runNormalUC00(resistence, "00" + run);
                        break;
                    case 2:
                        extractor.runNormalUC00(resistence, "0" + run);
                        break;
                    default:
                        extractor.runNormalUC00(resistence, String.valueOf(run));
                        break;
                }
                if (generateSingleRound) {
                    System.exit(0);
                }
            }
        }

        extractor.finishWriting();
    }

    boolean firstRun = true;



    private void runNormalUC00(int res, String num) throws IOException {
        if (firstRun) {
            offset = restartCounters();
            firstRun = false;
            firstGoose = offset + 0.01659;
            currentGoose = firstGoose;
        } else {
            offset = defineCounters(initialStNum, initialSqNum+1, Float.parseFloat(endtimeLastRun));
            currentGoose = firstGoose + gooseEventManager.getMaxTime();
        }

//        System.out.println("OFFSET: "+offset);

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        /* Write Header and Columns */
        defaultHeader = false;
        if (printHeader) {
            writeDefaultHeader();
            printHeader = false;
        }

        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, currentGoose, 0.00631, firstGoose, 6.33000000000011f, 4, 1000);

        /* Write Payload */
        int numLines = formatedCSVFile2.size() - 1;
        for (int i = 0; i < 2382; i++) {
            float time = formatedCSVFile2.get(i)[0];
            String line = "";
            String svHist;
            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i),formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i),formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
            }
            line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + ","+svHist+","
                    + gooseEventManager.getLastGooseFromSV(time).asCSVFull()
                    + getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(time), time)
                    + "," + label[0];
            write(line);
        }

        /* Write Payload */
        for (int i = 0; i < numLines - 2381; i++) {
            float time = formatedCSVFile2.get(i)[0] + Float.valueOf((float) 0.5);
            String svHist;
            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i),formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i),formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
            }
            String line = "";
            line = joinColumns(formatedCSVFile, 0.5 + (float) 0.000010009, formatedCSVFile2, columns, columns2, i)
                    + "," + svHist + ","
                    + gooseEventManager.getLastGooseFromSV(time).asCSVFull()
                    + getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(time), time)
                    + "," + label[0];
            write(line);
            endtimeLastRun = line.split(",")[0];
        }

    }

}
