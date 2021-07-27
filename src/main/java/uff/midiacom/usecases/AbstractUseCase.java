/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

import uff.midiacom.goose.GooseEventManager;
import uff.midiacom.model.GooseMessage;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * @author silvio
 */
public abstract class AbstractUseCase extends SVFeatureComposer {
    public static boolean USE_OFFSET = true;
    static float offset;
    public static int runs = 132;
    public static boolean generateSingleRound = false;
    BufferedWriter bw;
    public static String outputLocation = "/home/silvio/datasets/Full_SV_2021/consistency_v5/";
    static String outputFile;
    GooseEventManager gooseEventManager;
    public static boolean printHeader = false;
    boolean defaultHeader = true;
    String attackType = "Abstract Attack";
    String[] label = {"normal", "random_replay", "inverse_replay", "masquerade_fake_fault", "masquerade_fake_normal", "injection", "high_StNum", "poisoned_high_rate"};//,"poisoned_high_rate_consistent"};
    String columnsGOOSE[] = {"GooseTimestamp", "SqNum", "StNum", "cbStatus", "frameLen", "ethDst", "ethSrc", "ethType", "gooseTimeAllowedtoLive", "gooseAppid", "gooseLen", "TPID", "gocbRef", "datSet", "goID", "test", "confRev", "ndsCom", " numDatSetEntries", "APDUSize", "protocol"};

    static int initialStNum;
    static int initialSqNum;
    public static boolean replace = true;

    public AbstractUseCase() {
        initialStNum = randomBetween(0, 5000);
        initialSqNum = randomBetween(0, 5000);
        if (USE_OFFSET) {
            offset = randomBetween(0, 5000); // offset
        } else {
            offset = 0;
        }
    }

    float restartCounters() {
//        System.out.println("Last counters " + initialSqNum + "/" + initialStNum);
        initialStNum = randomBetween(0, 5000);
        initialSqNum = randomBetween(0, 5000);
//        System.out.println("New counters " + initialSqNum + "/" + initialStNum);
        if (USE_OFFSET) {
            offset = randomBetween(0, 5000); // offset
        } else {
            offset = 0;
        }
        return offset;
    }

    float defineCounters(int initialStNum, int initialSqNum, float offset) {
//        System.out.println("Last counters " + initialSqNum + "/" + initialStNum);
        this.initialStNum = initialStNum;
        this.initialSqNum = initialSqNum;
//        System.out.println("New counters " + initialSqNum + "/" + initialStNum);
        this.offset = offset;
        return offset;
    }

    public static int randomBetween(int min, int max) {
        return new Random(System.nanoTime()).nextInt(max - min) + min;
    }

    protected String joinColumns(ArrayList<Float[]> formatedCSVFile, ArrayList<Float[]> formatedCSVFile2, String columns[], String columns2[], int line) {
        String content = "";
        for (int i = 0; i < columns.length; i++) {
            float value = formatedCSVFile.get(line)[i];
            content = content.concat(value + ",");
        }

        for (int i = 1; i < columns2.length; i++) {
            float value = formatedCSVFile2.get(line)[i];
            if (columns2.length - 1 == i) {
                content = content.concat(String.valueOf(value));
            } else {
                content = content.concat(value + ",");
            }
        }
        return content;
    }

    protected String joinColumns(ArrayList<Float[]> formatedCSVFile, double timeGambi, ArrayList<Float[]> formatedCSVFile2, String columns[], String columns2[], int line) {
        String content = "";
        for (int i = 0; i < columns.length; i++) {
            float value = formatedCSVFile.get(line)[i];
            if (i == 0) {
                content = content.concat(value + timeGambi + ",");
            } else {
                content = content.concat(value + ",");
            }
        }

        for (int i = 1; i < columns2.length; i++) {
            float value = formatedCSVFile2.get(line)[i];
            if (columns2.length - 1 == i) {
                content = content.concat(String.valueOf(value));
            } else {
                content = content.concat(value + ",");
            }
        }
        return content;
    }

    protected ArrayList<Float[]> consumeFloat(String file, int scale, String columns[]) {
//        int offset = randomBetween(0,1000);
        ArrayList<Float[]> formatedCSVFile = new ArrayList<>();
        try {
            File myObj = new File(file);
            try (Scanner myReader = new Scanner(myObj)) {
                myReader.nextLine(); // Skip blank line
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    if (data.length() > 1) {
                        while (data.charAt(0) == ' ') {
                            data = data.substring(1, data.length());
                        }

                        while (data.trim().contains("  ")) {
                            data = data.replace("  ", " ");
                        }

                        data = data.replace(" ", ",");

                        StringTokenizer stringTokenizer = new StringTokenizer(data, ",", true);
                        int t = 0;
                        Float[] tokenLine = new Float[columns.length];
                        while (stringTokenizer.hasMoreTokens()) {
                            t++;
                            String next = stringTokenizer.nextToken();
                            if (!next.contains(",")) {
                                float feature = Float.valueOf(next) * scale;
                                int column = ((t + 1) / 2) - 1;
                                if (USE_OFFSET) {
                                    if (columns[column].equalsIgnoreCase("Time")) {
                                        feature = feature + offset;
                                    }
                                }
                                tokenLine[column] = feature;
                            }
                        }
                        formatedCSVFile.add(tokenLine);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Erro: " + e.getLocalizedMessage());
        }

        return formatedCSVFile;
    }

    protected void write(String line) throws FileNotFoundException, IOException {
        bw.write(line);
        bw.newLine();
    }

    protected void startWriting() throws FileNotFoundException, IOException {
        File fout = new File(outputFile);
        FileOutputStream fos = new FileOutputStream(fout, !replace);
        bw = new BufferedWriter(new OutputStreamWriter(fos));
    }

    protected void finishWriting() throws FileNotFoundException, IOException {
        bw.close();
    }

    protected void writeDefaultHeader() throws IOException {
        write("@relation compiledtraffic");
        write("@attribute Time numeric");// time-based 1
        write("@attribute isbA numeric"); //SV-related 2
        write("@attribute isbB numeric"); //SV-related 3
        write("@attribute isbC numeric"); //SV-related 4
        write("@attribute ismA numeric"); //SV-related 5
        write("@attribute ismB numeric"); //SV-related 6
        write("@attribute ismC numeric"); //SV-related 7
        write("@attribute vsbA numeric"); //SV-related 8
        write("@attribute vsbB numeric"); //SV-related 9
        write("@attribute vsbC numeric"); //SV-related 10
        write("@attribute vsmA numeric"); //SV-related 11
        write("@attribute vsmB numeric"); //SV-related 12
        write("@attribute vsmC numeric"); //SV-related 13

        write("@attribute isbARmsValue numeric"); //SV-related 2
        write("@attribute isbBRmsValue numeric"); //SV-related 2
        write("@attribute isbCRmsValue numeric"); //SV-related 2
        write("@attribute ismARmsValue numeric"); //SV-related 2
        write("@attribute ismBRmsValue numeric"); //SV-related 2
        write("@attribute ismCRmsValue numeric"); //SV-related 2
        write("@attribute vsbARmsValue numeric"); //SV-related 2
        write("@attribute vsbBRmsValue numeric"); //SV-related 2
        write("@attribute vsbCRmsValue numeric"); //SV-related 2
        write("@attribute vsmARmsValue numeric"); //SV-related 2
        write("@attribute vsmBRmsValue numeric"); //SV-related 2
        write("@attribute vsmCRmsValue numeric"); //SV-related 2
        write("@attribute isbATrapAreaSum numeric"); //SV-related 2
        write("@attribute isbBTrapAreaSum numeric"); //SV-related 2
        write("@attribute isbCTrapAreaSum numeric"); //SV-related 2
        write("@attribute ismATrapAreaSum numeric"); //SV-related 2
        write("@attribute ismBTrapAreaSum numeric"); //SV-related 2
        write("@attribute ismCTrapAreaSum numeric"); //SV-related 2
        write("@attribute vsbATrapAreaSum numeric"); //SV-related 2
        write("@attribute vsbBTrapAreaSum numeric"); //SV-related 2
        write("@attribute vsbCTrapAreaSum numeric"); //SV-related 2
        write("@attribute vsmATrapAreaSum numeric"); //SV-related 2
        write("@attribute vsmBTrapAreaSum numeric"); //SV-related 2
        write("@attribute vsmCTrapAreaSum numeric"); //SV-related 2

        write("@attribute t numeric"); // time-based  14
        write("@attribute GooseTimestamp numeric"); // time-based 15
        write("@attribute SqNum numeric"); // Status-based 16
        write("@attribute StNum numeric"); // Status-based 17
        write("@attribute cbStatus numeric"); // Status-based 18
        write("@attribute frameLen numeric"); //network-based 19
        write("@attribute ethDst {01:a0:f4:08:2f:77, FF:FF:FF:FF:FF:11, FF:FF:FF:FF:FF:22, FF:FF:FF:FF:FF:33, FF:FF:FF:FF:FF:44, FF:FF:FF:FF:FF:55, FF:FF:FF:FF:FF:66, FF:FF:FF:FF:FF:FF, FF:FF:FF:FF:FF:77, FF:FF:FF:FF:FF:AA, FF:FF:FF:FF:FF:BB, FF:FF:FF:FF:FF:CC, FF:FF:FF:FF:FF:DD, FF:FF:FF:FF:FF:EE, FF:FF:FF:FF:FF:AB, FF:FF:FF:FF:FF:AC}"); //network-based 20
        write("@attribute ethSrc {00:a0:f4:08:2f:77, FF:FF:FF:FF:FF:11, FF:FF:FF:FF:FF:22, FF:FF:FF:FF:FF:33, FF:FF:FF:FF:FF:44, FF:FF:FF:FF:FF:55, FF:FF:FF:FF:FF:66, FF:FF:FF:FF:FF:FF, FF:FF:FF:FF:FF:77, FF:FF:FF:FF:FF:AA, FF:FF:FF:FF:FF:BB, FF:FF:FF:FF:FF:CC, FF:FF:FF:FF:FF:DD, FF:FF:FF:FF:FF:EE, FF:FF:FF:FF:FF:AB, FF:FF:FF:FF:FF:AC}"); //network-based 21
        write("@attribute ethType {0x000077b7, 0x000088b8}"); //network-based 22
        write("@attribute gooseTimeAllowedtoLive numeric"); //IED-based 23
        write("@attribute gooseAppid {0x00003002, 0x00003001}");  //IED-based 24
        write("@attribute gooseLen numeric");  //IED-based 25
        write("@attribute TPID {0x7101, 0x8100}");  //IED-based 26
        write("@attribute gocbRef {LD/LLN0$IntLockB, LD/LLN0$GO$gcbA}");  //IED-based 27
        write("@attribute datSet {LD/LLN0$IntLockA, AA1C1Q01A1LD0/LLN0$InterlockingC}");  //IED-based 28
        write("@attribute goID {InterlockingF, InterlockingA}");  //IED-based 29
        write("@attribute test {TRUE, FALSE}");  //IED-based 30
        write("@attribute confRev numeric");  //IED-based 31
        write("@attribute ndsCom {TRUE, FALSE}");  //IED-based 32
        write("@attribute numDatSetEntries numeric");  //IED-based 33
        write("@attribute APDUSize numeric"); //network-based 34
        write("@attribute protocol {SV, GOOSE}"); //network-based 35
        write("@attribute stDiff numeric"); // temporal consistency 36
        write("@attribute sqDiff numeric"); // temporal consistency 37
        write("@attribute gooseLengthDiff numeric"); // temporal consistency 38
        write("@attribute cbStatusDiff numeric"); // temporal consistency 39
        write("@attribute apduSizeDiff numeric"); // temporal consistency 40
        write("@attribute frameLengthDiff numeric"); // temporal consistency 41
        write("@attribute timestampDiff numeric"); // temporal consistency 42
        write("@attribute tDiff numeric"); // temporal consistency 43
        write("@attribute timeFromLastChange numeric"); // temporal consistency 44
        write("@attribute delay numeric"); // temporal consistency 45
        String classLine = "@attribute @class@ {"
                + label[0] + ", "
                + label[1] + ", "
                + label[2] + ", "
                + label[3] + ", "
                + label[4] + ", "
                + label[5] + ", "
                + label[6] + ", "
                + label[7]
                + "}";

        write(classLine);
        write("@data");
    }

    protected String getConsistencyFeaturesAsCSV(GooseMessage gm, double currentSVTime) {

        if (gm.getStNum() == 0) {
            gm.setSqNum(initialSqNum);
            gm.setStNum(initialStNum);
        }
        GooseMessage prev = gooseEventManager.getPreviousGoose(gm);
        int stDiff = gm.getStNum() - prev.getStNum();
        int sqDiff = gm.getSqNum() - prev.getSqNum();
        int gooseLenghtDiff = gm.getGooseLen() - prev.getGooseLen();
        int cbStatusDiff = gm.isCbStatus() - prev.isCbStatus();
        int apduSizeDiff = gm.getApduSize() - prev.getApduSize();
        int frameLenthDiff = gm.getFrameLen() - prev.getFrameLen();
        double timestampDiff = gm.getTimestamp() - prev.getTimestamp();
        double tDiff = (Double.valueOf(gm.getT()) - Double.valueOf(prev.getT()));
        double delay = currentSVTime - gm.getTimestamp();

        //ystem.out.println("Goose (st/sq/time): " + gm.getStNum() + "," + gm.getSqNum() + "," + time + ", Coisinhas:" + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", " + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", " + timestampDiff + ", " + tDiff);
        return "," + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", "
                + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", "
                + timestampDiff + ", " + tDiff + ", " + (gm.getTimestamp() - gm.getT()) + ", " + delay;
    }

//    protected String getConsistencyFeaturesAsCSV(GooseMessage curent, GooseMessage previous) {
//
//        if (curent.getStNum() == 0) {
//            curent.setSqNum(initialSqNum);
//            curent.setStNum(initialStNum);
//        }
//        int stDiff = curent.getStNum() - previous.getStNum();
//        int sqDiff = curent.getSqNum() - previous.getSqNum();
//        int gooseLenghtDiff = curent.getGooseLen() - previous.getGooseLen();
//        int cbStatusDiff = curent.isCbStatus() - previous.isCbStatus();
//        int apduSizeDiff = curent.getApduSize() - previous.getApduSize();
//        int frameLenthDiff = curent.getFrameLen() - previous.getFrameLen();
//        double timestampDiff = curent.getTimestamp() - previous.getTimestamp();
//        double tDiff = (Double.valueOf(curent.getT()) - Double.valueOf(previous.getT()));
//        double timeFromLastChange = curent.getTimestamp() - curent.getT();
//        double delay = currentSVTime - gm.getTimestamp();
//
//        //ystem.out.println("Goose (st/sq/time): " + gm.getStNum() + "," + gm.getSqNum() + "," + time + ", Coisinhas:" + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", " + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", " + timestampDiff + ", " + tDiff);
//        return "," + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", "
//                + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", "
//                + timestampDiff + ", " + tDiff + ", " + timeFromLastChange;
//    }


}
