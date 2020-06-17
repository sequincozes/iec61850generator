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
import java.util.Scanner;
import java.util.StringTokenizer;
import uff.midiacom.goose.GooseEventManager;
import uff.midiacom.model.GooseMessage;

/**
 *
 * @author silvio
 */
public abstract class AbstractUseCase {

    BufferedWriter bw;
    static String outputLocation = "/home/silvio/datasets/Full_SV_2020/consistency_v3/";
    static String outputFile;
    GooseEventManager gooseEventManager;
    boolean printHeader = true;
    boolean defaultHeader = true;
    String attackType = "Abstract Attack";
    String[] label = {"normal", "random_replay", "inverse_replay", "masquerade_fake_fault", "masquerade_fake_normal", "injection", "poisoned_high_rate"};
    String columnsGOOSE[] = {"GooseTimestamp","SqNum", "StNum", "cbStatus","frameLen", "ethDst", "ethSrc", "ethType", "gooseTimeAllowedtoLive", "gooseAppid", "gooseLen", "TPID", "gocbRef", "datSet", "goID", "test", "confRev", "ndsCom", " numDatSetEntries", "APDUSize", "protocol"};      
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

    protected ArrayList<Float[]> consumeFloat(String file, int scale, String columns[]) {
        ArrayList<Float[]> formatedCSVFile = new ArrayList<>();
        try {
            File myObj = new File(file);
            try ( Scanner myReader = new Scanner(myObj)) {
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
                                float token = Float.valueOf(next) * scale;
                                int column = ((t + 1) / 2) - 1;
                                tokenLine[column] = token;
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
        FileOutputStream fos = new FileOutputStream(fout);
        bw = new BufferedWriter(new OutputStreamWriter(fos));
    }

    protected void finishWriting() throws FileNotFoundException, IOException {
        bw.close();
    }

    protected void writeDefaultHeader() throws IOException {
        write("@relation compiledtraffic");
        write("@attribute Time numeric");
        write("@attribute isbA numeric");
        write("@attribute isbB numeric");
        write("@attribute isbC numeric");
        write("@attribute ismA numeric");
        write("@attribute ismB numeric");
        write("@attribute ismC numeric");
        write("@attribute vsbA numeric");
        write("@attribute vsbB numeric");
        write("@attribute vsbC numeric");
        write("@attribute vsmA numeric");
        write("@attribute vsmB numeric");
        write("@attribute vsmC numeric");
        write("@attribute GooseTimestamp numeric");
        write("@attribute SqNum numeric");
        write("@attribute StNum numeric");
        write("@attribute cbStatus numeric");
        write("@attribute frameLen numeric");
        write("@attribute ethDst {01:a0:f4:08:2f:77, FF:FF:FF:FF:FF:FF}");
        write("@attribute ethSrc {FF:FF:FF:FF:FF:FF, 00:a0:f4:08:2f:77}");
        write("@attribute ethType {0x000077b7, 0x000088b8}");
        write("@attribute gooseTimeAllowedtoLive numeric");
        write("@attribute gooseAppid {0x00003002, 0x00003001}");
        write("@attribute gooseLen numeric");
        write("@attribute TPID {0x7101, 0x8100}");
        write("@attribute gocbRef {LD/LLN0$IntLockB, LD/LLN0$GO$gcbA}");
        write("@attribute datSet {LD/LLN0$IntLockA, AA1C1Q01A1LD0/LLN0$InterlockingC}");
        write("@attribute goID {InterlockingF, InterlockingA}");
        write("@attribute test {TRUE, FALSE}");
        write("@attribute confRev numeric");
        write("@attribute ndsCom {TRUE, FALSE}");
        write("@attribute numDatSetEntries numeric");
        write("@attribute APDUSize numeric");
        write("@attribute protocol {SV, GOOSE}");
        write("@attribute stDiff numeric");
        write("@attribute sqDiff numeric");
        write("@attribute gooseLenghtDiff numeric");
        write("@attribute cbStatusDiff numeric");
        write("@attribute apduSizeDiff numeric");
        write("@attribute frameLenthDiff numeric");
        write("@attribute timestampDiff numeric");
        write("@attribute tDiff numeric");
        String classLine = "@attribute @class@ {"
                + label[0] + ", "
                + label[1] + ", "
                + label[2] + ", "
                + label[3] + ", "
                + label[4] + ", "
                + label[5] + ", "
                + label[6]
                + "}";

        write(classLine);
        write("@data");
    }

    protected String getConsistencyFeaturesAsCSV(double time) {
        GooseMessage gm = gooseEventManager.getLastGooseFromSV(time);
        GooseMessage prev = gooseEventManager.getPreviousGoose(gm);
        int stDiff = gm.getStNum() - prev.getStNum();
        int sqDiff = gm.getSqNum() - prev.getSqNum();
        int gooseLenghtDiff = gm.getGooseLen() - prev.getGooseLen();
        int cbStatusDiff = gm.isCbStatus() - prev.isCbStatus();
        int apduSizeDiff = gm.getApduSize() - prev.getApduSize();
        int frameLenthDiff = gm.getFrameLen() - prev.getFrameLen();
        double timestampDiff = gm.getTimestamp() - prev.getTimestamp();
        double tDiff = gm.getT() - prev.getT();

        //ystem.out.println("Goose (st/sq/time): " + gm.getStNum() + "," + gm.getSqNum() + "," + time + ", Coisinhas:" + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", " + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", " + timestampDiff + ", " + tDiff);
        return "," + stDiff + ", " + sqDiff + ", " + gooseLenghtDiff + ", "
                + cbStatusDiff + ", " + apduSizeDiff + ", " + frameLenthDiff + ", "
                + timestampDiff + ", " + tDiff;
    }
}
