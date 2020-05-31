/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.usecases;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import uff.midiacom.goosegenerator.GooseEventManager;
import uff.midiacom.model.GooseMessage;

/**
 *
 * @author silvio
 */
public class UC02 extends AbstractUseCase{


    /**
    * Replay Attacks
    * @param filename
    * @param res - fault resistence
    * @param num - number of run
    * @param numReplayInstances - number of attack instances 
    * 
    * @return replays randomly inverse goose (CB close during fault or open during normal)
     * @throws java.io.FileNotFoundException
    */
    
    

    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC02 extractor = new UC02();
        extractor.attackType = "Replay (inverse)";
        extractor.gooseEventManager = new GooseEventManager(false, 0, 0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);
        extractor.startWriting();
        
        int[] resistences = {10, 50, 100};
        
        int numReplayInstances = 1000; // 1000 attacks per scenario
        
        for (int resistence : resistences) {
            for (int run = 1; run < 132; run++) {
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
   
    private void generateReplayAttacksUC2(int res, String num, int numReplayInstances) throws IOException {

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

/* Generate GOOSE Part */
        String columnsGOOSE[] = {"GooseTimestamp","SqNum", "StNum", "cbStatus","frameLen", "ethDst", "ethSrc", "ethType", "gooseTimeAllowedtoLive", "gooseAppid", "gooseLen", "TPID", "gocbRef", "datSet", "goID", "test", "confRev", "ndsCom", " numDatSetEntries", "APDUSize", "protocol"};      
        String columnsGOOSEType[] = {"numeric", "numeric", "numeric", "numeric", "numeric", "{" + GooseMessage.ethDst + "}", 
            "{" + GooseMessage.ethSrc + "}", "{" + GooseMessage.ethType + "}", "numeric", "{" + GooseMessage.gooseAppid + "}", "numeric", 
            "{" + GooseMessage.TPID + "}","{" + GooseMessage.gocbRef + "}", "{" + GooseMessage.datSet + "}", "{" + GooseMessage.goID + "}",
            "{" + GooseMessage.test + "}", "numeric", "{" + GooseMessage.ndsCom + "}", "numeric", "numeric", "{" + GooseMessage.protocol + "}"};      
        String[] label = {"normal", "attack"};
  
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
        
        for (int i = 0; i < numReplayInstances; i++) {
            int minSv = 1;
            int maxSv = formatedCSVFile2.size() -1;
            int svIndex = (int)(Math.random() * (maxSv - minSv + 1) + minSv); // random SV messages
            
            int minGoose, maxGoose;
            float time = formatedCSVFile2.get(svIndex)[0];
            if (time < 500) {
                minGoose = 0;
                maxGoose = 500;
            } else  if(time > 500 && time < 600){
                minGoose = 500;
                maxGoose = 600;
            } else {
              minGoose = 600;
              maxGoose = 1000;   
            }

            int gooseTime = (int)(Math.random() * (maxGoose - minGoose + 1) + minGoose); // random GOOSE inconpatible with SV
            
            String line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, svIndex) + "," +gooseEventManager.getLastGooseFromSV(gooseTime).asCSVFull() +"," + label[1];

            //System.out.println("line: "+line);
            write(line);
        }

    }

}
