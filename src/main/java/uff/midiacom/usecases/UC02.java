/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import uff.midiacom.goose.GooseEventManager;
import uff.midiacom.model.GooseMessage;

/**
 * @author silvio
 */
public class UC02 extends AbstractUseCase {


    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC02 extractor = new UC02();
        extractor.attackType = "inverse_replay";
//        extractor.gooseEventManager = new GooseEventManager(false, 0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);
        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        int numReplayInstances = 1000; // 1000 attacks per scenario

        for (int resistence : resistences) {
            for (int run = 1; run <= AbstractUseCase.runs; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.generateReplayAttacksUC2(resistence, "00" + run, numReplayInstances);
                        break;
                    case 2:
                        extractor.generateReplayAttacksUC2(resistence, "0" + run, numReplayInstances);
                        break;
                    default:
                        extractor.generateReplayAttacksUC2(resistence, String.valueOf(run), numReplayInstances);
                        break;
                }
            }
        }

        extractor.finishWriting();
    }

    /**
     * Replay Attacks
     *
     * @param res                - fault resistence
     * @param num                - number of run
     * @param numReplayInstances - number of attack instances
     * @return replays randomly inverse goose (CB close during fault or open during normal)
     * @throws java.io.FileNotFoundException
     */
    private void generateReplayAttacksUC2(int res, String num, int numReplayInstances) throws IOException {
        restartCounters();
//        offset = 0;
        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, new double[]{offset + 0.5, offset + 0.6}, 0.00631, offset + 0.01659, 6.33000000000011f, 4, 1000);

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        /* Generate GOOSE Part */
        String columnsGOOSE[] = {"GooseTimestamp", "SqNum", "StNum", "cbStatus", "frameLen", "ethDst", "ethSrc", "ethType", "gooseTimeAllowedtoLive", "gooseAppid", "gooseLen", "TPID", "gocbRef", "datSet", "goID", "test", "confRev", "ndsCom", " numDatSetEntries", "APDUSize", "protocol"};
        String columnsGOOSEType[] = {"numeric", "numeric", "numeric", "numeric", "numeric", "{" + GooseMessage.ethDst + "}",
                "{" + GooseMessage.ethSrc + "}", "{" + GooseMessage.ethType + "}", "numeric", "{" + GooseMessage.gooseAppid + "}", "numeric",
                "{" + GooseMessage.TPID + "}", "{" + GooseMessage.gocbRef + "}", "{" + GooseMessage.datSet + "}", "{" + GooseMessage.goID + "}",
                "{" + GooseMessage.test + "}", "numeric", "{" + GooseMessage.ndsCom + "}", "numeric", "numeric", "{" + GooseMessage.protocol + "}"};


        /* Write Header and Columns */
        if (printHeader) {
            if (defaultHeader) {
                writeDefaultHeader();
            } else {
                writeCustomHeader(columns, columns2, columnsGOOSEType, columnsGOOSE);
            }
            printHeader = false;
        }

        for (int i = 0; i < numReplayInstances; i++) {
            int minSv = numReplayInstances/2; //test
            int maxSv = formatedCSVFile2.size() - 1;
            int svIndex = randomBetween(minSv, maxSv);//(int) (Math.random() * (maxSv - minSv + 1) + minSv); // random SV messages

            int minGoose, maxGoose;
            float time = formatedCSVFile2.get(svIndex)[0];

            if (time < (offset+0.5) || time > (offset+0.6)) { // SV during normal operation
                minGoose = 500;
                maxGoose = 600; // GOOSE during fault
//                System.out.println("GOOSE during fault");
            } else { // SV during fault
                if (randomBetween(0, 2) == 1) {// pre or post fault?
                    minGoose = 0;
                    maxGoose = 500; // GOOSE before fault
//                    System.out.println("GOOSE before fault");
                } else {
                    minGoose = 600;
                    maxGoose = 1000; // GOOSE after fault
//                    System.out.println("GOOSE after fault");
                }
            }


            String svHist;
            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i),formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i),formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
            }
            float gooseTime = randomBetween(minGoose, maxGoose); // random GOOSE inconpatible with SV
//            System.out.println("OFFSET: "+offset);
//            System.out.println("GOOSE TIME: "+gooseTime);
            gooseTime = gooseTime /1000;
//            System.out.println("GOOSE TIME: "+gooseTime);
            gooseTime = gooseTime + offset;
//            System.out.println("GOOSE TIME: "+gooseTime);

            GooseMessage lastGooseMessage = gooseEventManager.getLastGooseFromSV(gooseTime);

//            System.out.println("SV:"+time+" |Goose time: "+gooseTime+"("+(minGoose+ offset)+"~"+(maxGoose+ offset)+")");
//            System.out.println("*SV:"+(time-offset)+" |Goose time: "+(gooseTime-offset)+"("+(minGoose)+"~"+(maxGoose)+")");
//            System.out.println("**SV:"+(time-offset)+" |Goose time: "+(lastGooseMessage.getTimestamp()-offset)+"("+(minGoose)+"~"+(maxGoose)+")");

            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex)
                    + "," + svHist + "," + lastGooseMessage.asCSVFull()
                    + getConsistencyFeaturesAsCSV(lastGooseMessage, time) + "," + label[2];
//            System.out.println(line);
//            System.exit(0);

            write(line);
        }

    }

    private void writeCustomHeader(String[] columns, String[] columns2, String[] columnsGOOSEType, String[] columnsGOOSE) throws IOException {
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
