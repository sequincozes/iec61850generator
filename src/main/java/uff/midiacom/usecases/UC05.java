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
public class UC05 extends AbstractUseCase {


    //Injection
    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC05 extractor = new UC05();
        extractor.attackType = "injection";

//        extractor.gooseEventManager = new GooseEventManager(false,  0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        int numReplayInstances = 1000; // 1000 attacks per scenario

        for (int resistence : resistences) {
            for (int run = 1; run < 132; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.generateInjectionAttacksUC5(resistence, "00" + run, numReplayInstances);
                        break;
                    case 2:
                        extractor.generateInjectionAttacksUC5(resistence, "0" + run, numReplayInstances);
                        break;
                    default:
                        extractor.generateInjectionAttacksUC5(resistence, String.valueOf(run), numReplayInstances);
                        break;
                }
            }
        }
        extractor.finishWriting();
    }

    /**
     * Injection Attacks
     *
     * @param res - fault resistence
     * @param num - number of run
     * @return injected goose with inconsistent features
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    private void generateInjectionAttacksUC5(int res, String num, int numInjectionAttacks) throws IOException {
        restartCounters();
        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        /* Generate GOOSE Part */
        String columnsGOOSE[] = {"GooseTimestamp", "SqNum", "StNum", "cbStatus", "frameLen", "ethDst",
                "ethSrc", "ethType", "gooseTimeAllowedtoLive", "gooseAppid", "gooseLen",
                "TPID", "gocbRef", "datSet", "goID", "test", "confRev", "ndsCom", " numDatSetEntries", "APDUSize", "protocol"};

        /* Malicious GOOSE (injected) Field Values */
        String ethDst = GooseMessage.ethDst;
        String ethSrc = GooseMessage.ethSrc;
        String ethType = GooseMessage.ethType;
        String gooseAppid = GooseMessage.gooseAppid;
        String TPID = GooseMessage.TPID;
        String gocbRef = GooseMessage.gocbRef;
        String datSet = GooseMessage.datSet;
        String goID = GooseMessage.goID;
        String test = GooseMessage.test;
        String ndsCom = GooseMessage.ndsCom;
        String protocol = GooseMessage.protocol;

        GooseMessage.ethDst = "FF:FF:FF:FF:FF:FF";
        GooseMessage.ethSrc = "FF:FF:FF:FF:FF:FF";
        GooseMessage.ethType = "0x000077b7";
        GooseMessage.gooseAppid = "0x00003002";
        GooseMessage.TPID = "0x7101";
        GooseMessage.gocbRef = "LD/LLN0$IntLockB";
        GooseMessage.datSet = "AA1C1Q01A1LD0/LLN0$InterlockingC";
        GooseMessage.goID = "InterlockingF";
        GooseMessage.test = "TRUE";
        GooseMessage.ndsCom = "TRUE";
        GooseMessage.protocol = "SV";

        String columnsGOOSEType[] = {"numeric", "numeric", "numeric", "numeric", "numeric", "{" + GooseMessage.ethDst + ", " + ethDst + "}",
                "{" + GooseMessage.ethSrc + ", " + ethSrc + "}", "{" + GooseMessage.ethType + ", " + ethType + "}", "numeric", "{" + GooseMessage.gooseAppid + ", " + gooseAppid + "}", "numeric",
                "{" + GooseMessage.TPID + ", " + TPID + "}", "{" + GooseMessage.gocbRef + ", " + gocbRef + "}", "{" + datSet + ", " + GooseMessage.datSet + "}", "{" + GooseMessage.goID + ", " + goID + "}",
                "{" + GooseMessage.test + ", " + test + "}", "numeric", "{" + GooseMessage.ndsCom + ", " + ndsCom + "}", "numeric", "numeric", "{" + GooseMessage.protocol + ", " + protocol + "}"};

        /* Write Header and Columns */
        if (printHeader) {
            if (defaultHeader) {
                System.out.println("Default header...");
                writeDefaultHeader();
            } else {
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

                String classLine = "@attribute @class@ {" +
                        label[0] + ", " +
                        label[1] + ", " +
                        label[2] + ", " +
                        label[3] + ", " +
                        label[4] + ", " +
                        label[5] +
                        "}";

                write(classLine);
                write("@data");
            }
            printHeader = false;
        }

        for (int i = 0; i < numInjectionAttacks; i++) {
            int minSv = 1;
            int maxSv = formatedCSVFile2.size() - 1;
            int svIndex = randomBetween(minSv, maxSv);

            int minGoose = 0, maxGoose = 1000;
            int gooseTime = randomBetween(minGoose, maxGoose); // random index, random SV messages

            int stNum = randomBetween(0, 10000);
            int sqNum = randomBetween(0, 10000);
            int cbStatus = randomBetween(0, 2);
            float timestamp = Float.valueOf(randomBetween(1, 10000)) / 10000;
            int timeAllowedToLive = randomBetween(0, 100000);
            int confRev = randomBetween(0, 100);

            // Last Goose Message from the random time
            GooseMessage gm = gooseEventManager.getLastGooseFromSV(gooseTime);
            gm.setSqNum(sqNum);
            gm.setStNum(stNum);
            gm.setCbStatus(cbStatus);
            gm.setTimestamp(timestamp);
            gm.setConfRev(confRev);
            gm.setGooseTimeAllowedtoLive(timeAllowedToLive);

            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," + gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull() + getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(gooseTime)) + "," + label[5];
//            System.out.println("Timestamp: "+timestamp+" > LINE > "+gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull());
            write(line);
        }

    }


}
