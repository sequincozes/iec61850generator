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
public class UC04 extends AbstractUseCase {


    public static void run(String filename) throws IOException {
        outputFile = outputLocation + filename;
        UC04 extractor = new UC04();
        extractor.attackType = "masquerade_fake_fault";
        // Masquerade
//        extractor.gooseEventManager = new GooseEventManager(false, 0, 0, new double[]{0.3, 1.1}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

        extractor.startWriting();

        int[] resistences = {10, 50, 100};

        for (int resistence : resistences) {
            for (int run = 1; run < 132; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.generateMasqueradeAttacksUC4(resistence, "00" + run);
                        break;
                    case 2:
                        extractor.generateMasqueradeAttacksUC4(resistence, "0" + run);
                        break;
                    default:
                        extractor.generateMasqueradeAttacksUC4(resistence, String.valueOf(run));
                        break;
                }
            }
        }

        extractor.finishWriting();
    }

    /**
     * Masquerade Attacks
     *
     * @param res - fault resistence
     * @param num - number of run
     * @return masquerate goose CB close during fault operation (fake normal)
     * @throws java.io.FileNotFoundException
     */
    private void generateMasqueradeAttacksUC4(int res, String num) throws IOException {
        restartCounters();
        // SV time range to generate a fake fault burst of GOOSE messages
        double[] labelRange = {0.5, 0.6};
        gooseEventManager = new GooseEventManager(true, initialStNum, initialSqNum,  labelRange, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);

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
                writeCustomHeader(columns, columns2, columnsGOOSE, columnsGOOSEType);
            }
            printHeader = false;
        }

        double lastGooseTimestamp = -1;
        for (int i = 0; i < formatedCSVFile2.size() - 1; i++) {
            float time = formatedCSVFile2.get(i)[0];
            String line = "";
            if (time >= labelRange[1]) {
                break;
            } else if (time >= labelRange[0]) {
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + gooseEventManager.getLastGooseFromSV(time).asCSVFull() + getConsistencyFeaturesAsCSV(gooseEventManager.getLastGooseFromSV(time)) + "," + label[3];
                if (gooseEventManager.getLastGooseFromSV(time).getCbStatus() == 0) {
                    write(line);
                }
            }
            if (lastGooseTimestamp != gooseEventManager.getLastGooseFromSV(time).getTimestamp()) {
                lastGooseTimestamp = gooseEventManager.getLastGooseFromSV(time).getTimestamp();
//                System.out.println(gooseEventManager.getLastGooseFromSV(time).asCSVFull());
            }
        }

    }

    private void writeCustomHeader(String[] columns, String[] columns2, String[] columnsGOOSE, String[] columnsGOOSEType) throws IOException {
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
