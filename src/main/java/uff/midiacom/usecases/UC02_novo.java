/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import uff.midiacom.goose.GooseEventManager;
import uff.midiacom.model.GooseMessage;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author silvio
 */
public class UC02_novo extends AbstractUseCase {

    public static void run(String filename) throws IOException {
        outputFile = outputLocation + filename;
        UC02_novo extractor = new UC02_novo();
        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        for (int resistence : resistences) {
            for (int run = 1; run <= AbstractUseCase.runs; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.runInverseReplayUC02(resistence, "00" + run);
                        break;
                    case 2:
                        extractor.runInverseReplayUC02(resistence, "0" + run);
                        break;
                    default:
                        extractor.runInverseReplayUC02(resistence, String.valueOf(run));
                        break;
                }
            }
        }

        extractor.finishWriting();
    }


    private void runInverseReplayUC02(int res, String num) throws IOException {
        float offset = restartCounters();
        gooseEventManager = new GooseEventManager(false, initialStNum, initialSqNum, new double[]{offset + 0.5, offset + 0.6}, 0.00631, offset + 0.01659, 6.33000000000011f, 4, 1000);

        /* Malicious code for inverse replay */
        generateInverseReplay();

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        double[] labelRange = {0.5, 0.6};

        /* Write Header and Columns */
        if (printHeader) {
            writeDefaultHeader();
            printHeader = false;
        }

        /* Write Payload */
        int numLines = formatedCSVFile2.size() - 1;
//        numLines = 10;
        for (int i = 0; i < numLines; i++) {
            float time = formatedCSVFile2.get(i)[0];
            GooseMessage goose = gooseEventManager.getLastGooseFromSV(time);
            String svHist;
            if (i > 0) {
                svHist = getSVHistorical(formatedCSVFile.get(i - 1), formatedCSVFile.get(i), formatedCSVFile2.get(i - 1), formatedCSVFile2.get(i));
            } else {
                svHist = getSVHistorical(formatedCSVFile.get(i), formatedCSVFile.get(i), formatedCSVFile2.get(i), formatedCSVFile2.get(i)); // just to initialize
            }

            String line = "";
            if (goose.isInverseReplay){
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + svHist + "," + goose.asCSVFull() + getConsistencyFeaturesAsCSV(goose, time) + "," + label[2];
            } else {
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + svHist + "," + goose.asCSVFull() + getConsistencyFeaturesAsCSV(goose, time) + "," + label[0];
            }
            write(line);
//            System.out.println(goose.asDebug()+"|"+getConsistencyFeaturesAsCSV(goose, time).replace(",","|"));
        }
    }

    private void generateInverseReplay() {
        for (int i = 0; i <= 10; i++) {
            int capturedMessageIndex = randomBetween(0, 4);
            GooseMessage replayMessage = gooseEventManager.getGooseMessages().get(capturedMessageIndex).copy();
            replayMessage.isInverseReplay = true;
//            System.out.println("a- replayMessageIndex: " + capturedMessageIndex + "(Timestamp: " + replayMessage.getTimestamp() + " (1 a " + originalSize + ")");
            double oldTime = replayMessage.getTimestamp();
            // If the captured message is pre-fault, retransmit it during the fault
            if (replayMessage.getTimestamp() < (offset + 0.5)) {
                float newTime = randomBetween(500, 600);
                newTime = newTime / 1000;
                newTime = newTime + offset;
                replayMessage.setTimestamp(newTime);
//                System.out.println("Retransmit faulting GOOSE AFTER the fault: index " + capturedMessageIndex + "(" + oldTime + "), new Timestamp: " + replayMessage.getTimestamp() + ", Status: " + replayMessage.getCbStatus());
            } else if (replayMessage.getTimestamp() > (offset + 0.5) && replayMessage.getTimestamp() < (offset + 0.6)) {
                float newTime = randomBetween(600, 999);
                newTime = newTime / 1000;
                newTime = newTime + offset;
                replayMessage.setTimestamp(newTime);
//                System.out.println("Retransmit pre-fault GOOSE During the fault: index " + capturedMessageIndex + "(" + oldTime + "), new Timestamp: " + replayMessage.getTimestamp() + ", Status: " + replayMessage.getCbStatus());
            } else {
                System.err.println("Estado inesperado: sorteou mensagem PÓS-falta para retransmitir: " + capturedMessageIndex + "->" + replayMessage.getTimestamp());
                for (int index = 0; index < gooseEventManager.getGooseMessages().size() - 1; index++) {
                    System.err.println(" (" + index + ") " + gooseEventManager.getGooseMessages().get(index).getTimestamp() + " / " + +gooseEventManager.getGooseMessages().get(index).getCbStatus());
                }
                System.exit(1);
            }
//            System.out.println("d- replayMessageIndex: " + capturedMessageIndex + "(Timestamp: " + replayMessage.getTimestamp() + " (1 a " + originalSize + ")");
            gooseEventManager.getGooseMessages().add(replayMessage);
        }
    }


}
