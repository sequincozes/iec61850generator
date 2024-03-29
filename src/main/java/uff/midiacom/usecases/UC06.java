/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import uff.midiacom.controller.GooseEventManager;
import uff.midiacom.model.GooseMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Poisoning High Status Number - Injection with high stNum
 */
public class UC06 extends AbstractUseCase {
    public static void run(String filename) throws FileNotFoundException, IOException {
        System.out.println("Run: " + filename);
        outputFile = outputLocation + filename;
        UC06 extractor = new UC06();
        extractor.attackType = "injection";

//        extractor.gooseEventManager = new GooseEventManager(false,0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        int numReplayInstances = 1000; // 1000 attacks per scenario

        for (int resistence : resistences) {
            for (int run = 1; run <= AbstractUseCase.runs; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.generatePoisonedStNumGoose(resistence, "00" + run, numReplayInstances);
                        break;
                    case 2:
                        extractor.generatePoisonedStNumGoose(resistence, "0" + run, numReplayInstances);
                        break;
                    default:
                        extractor.generatePoisonedStNumGoose(resistence, String.valueOf(run), numReplayInstances);
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
     * @return Poisoning High Status Number - Injection with high stNum
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void generatePoisonedStNumGoose(int res, String num, int numPoisonedStNum) throws IOException {
        float offset = restartCounters();
        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, new double[]{offset + 0.5, offset + 0.6}, 0.00631, offset + 0.01659, 6.33000000000011f, 4, 1000);

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
            writeDefaultHeader();
            printHeader = false;
        }

        for (int i = 0; i < numPoisonedStNum; i++) {
            int minSv = 1;
            int maxSv = formatedCSVFile2.size() - 1;
            int svIndex = randomBetween(minSv, maxSv);

            int minGoose = 0, maxGoose = 1000;
            int gooseTime = randomBetween(minGoose, maxGoose); // random index, random SV messages

            int stNum = randomBetween(10000, 100000);
            int sqNum = 1;
            int cbStatus = randomBetween(0, 2);
            float timestamp = Float.valueOf(randomBetween(1, 10000)) / 10000;
            timestamp = timestamp + offset;
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

            String svHist;
            float time = 0;

            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i), formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
                time = formatedCSVFile.get(i - 1)[0];
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i), formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
                time = formatedCSVFile.get(i)[0];
            }
            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," + svHist + "," + gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull() + getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(gooseTime), time) + "," + label[6];
//            System.out.println("Timestamp: "+timestamp+" > LINE > "+gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull());
            write(line);
        }

    }


}
