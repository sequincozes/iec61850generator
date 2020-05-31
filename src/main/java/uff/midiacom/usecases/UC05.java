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
import uff.midiacom.goosegenerator.GooseEventManager;
import uff.midiacom.model.GooseMessage;

/**
 *
 * @author silvio
 */
public class UC05 extends AbstractUseCase{

    /**
     * Injection Attacks
     * @param filename
     * @param res - fault resistence
     * @param num - number of run
     * 
     * @return injected goose with inconsistent features
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC05 extractor = new UC05();
        extractor.gooseEventManager = new GooseEventManager(false, 0, 0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);
        
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
                        extractor.generateInjectionAttacksUC5(resistence, "0" +  run, numReplayInstances);
                        break;
                    default:
                        extractor.generateInjectionAttacksUC5(resistence, String.valueOf(run), numReplayInstances);
                        break;
                }
            }
        }
        
        extractor.finishWriting();
    }
   
    private void generateInjectionAttacksUC5(int res, String num, int numInjectionAttacks) throws IOException {

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);
        
        /* Generate GOOSE Part */
        String columnsGOOSE[] = {"GooseTimestamp","SqNum", "StNum", "cbStatus","frameLen", "ethDst", 
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
        String test= GooseMessage.test;
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
        GooseMessage.frameLen = 249;
        GooseMessage.gooseTimeAllowedtoLive = 1000;
        GooseMessage.gooseLen = 227;
        GooseMessage.numDatSetEntries = 26;
        GooseMessage.APDUSize = 219;
        GooseMessage.confRev = 2;

        
        String columnsGOOSEType[] = {"numeric", "numeric", "numeric", "numeric", "numeric", "{" + GooseMessage.ethDst + ", "+ethDst+"}", 
            "{" + GooseMessage.ethSrc + ", "+ethSrc+"}", "{" + GooseMessage.ethType + ", "+ethType+"}", "numeric", "{" + GooseMessage.gooseAppid + ", "+gooseAppid+"}", "numeric", 
            "{" + GooseMessage.TPID + ", "+TPID+"}","{" + GooseMessage.gocbRef + ", "+gocbRef+"}", "{"+datSet+", " + GooseMessage.datSet + "}", "{" + GooseMessage.goID + ", "+goID+"}",
            "{" + GooseMessage.test + ", " + test + "}", "numeric", "{" + GooseMessage.ndsCom + ", " + ndsCom + "}", "numeric", "numeric", "{" + GooseMessage.protocol + ", "+protocol+"}"};      
        String label[] = {"normal","attack"};

         /* Write Header and Columns */
        if(printHeader){
            if(defaultHeader){
                writeDefaultHeader();
            } else {
                write("@relation compiledtraffic");

                for (String column : columns) {
                    write("@attribute "+column+" numeric ");
                }

                for (String column : columns2) {
                    if(!column.equals("Time"))
                    write("@attribute "+column+" numeric ");
                }

                for (int i = 0; i < columnsGOOSE.length; i++) {
                    write("@attribute "+columnsGOOSE[i]+" "+columnsGOOSEType[i]);
                }

                write("@attribute @class@ {"+label[0]+", "+label[1]+"}");
                write("@data");
            }
            printHeader = false;
        }

         for (int i = 0; i < numInjectionAttacks; i++) {
            Random svRandom = new Random();
            int svIndex = svRandom.nextInt(formatedCSVFile2.size()); // random index, random SV messages
            
            Random gooseRandom = new Random(1000);
            int gooseTime = gooseRandom.nextInt(); // random index, random SV messages
            
            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," +gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull()+"," + label[1];

            //System.out.println("line: "+line);
            write(line);
        }

    }
    

}
