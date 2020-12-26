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
 *
 * @author silvio
 */

//@TODO: ESTE ATAQUE NÃO ESTÁ FINALIZADO - ERA PARA SER IGUAL AO 06 MAS COM CONSISTENCIA
public class UC07 extends AbstractUseCase{

    public static void run(String filename) throws FileNotFoundException, IOException {
        outputFile = outputLocation + filename;
        UC07 extractor = new UC07();
        extractor.gooseEventManager = new GooseEventManager(false, 0, 0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);
        extractor.startWriting();
        
        int[] resistences = {10, 50, 100};
               
        for (int resistence : resistences) {
            for (int run = 1; run < 132; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.runHighHateFloodingUC07(resistence, "00" + run);
                        break;
                    case 2:
                        extractor.runHighHateFloodingUC07(resistence, "0" + run);
                        break;
                    default:
                        extractor.runHighHateFloodingUC07(resistence, String.valueOf(run));
                        break;
                }
            }
        }
        
        extractor.finishWriting();
    }

    /**
     * Increases StNum at high rate + consisent 
     */
    private void runHighHateFloodingUC07(int res, String num) throws IOException {

        /* Extract First Part */
        String columns[] = {"Time", "isbA", "isbB", "isbC", "ismA", "ismB", "ismC", "vsbA", "vsbB", "vsbC", "vsmA"};
        ArrayList<Float[]> formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);

        /* Extract Second Part */
        String columns2[] = {"Time", "vsmB", "vsmC"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        
        /* Generate GOOSE Part */
        String columnsGOOSEType[] = {"numeric", "numeric", "numeric", "numeric", "numeric", "{" + GooseMessage.ethDst + "}", 
            "{" + GooseMessage.ethSrc + "}", "{" + GooseMessage.ethType + "}", "numeric", "{" + GooseMessage.gooseAppid + "}", "numeric", 
            "{" + GooseMessage.TPID + "}","{" + GooseMessage.gocbRef + "}", "{" + GooseMessage.datSet + "}", "{" + GooseMessage.goID + "}",
            "{" + GooseMessage.test + "}", "numeric", "{" + GooseMessage.ndsCom + "}", "numeric", "numeric", "{" + GooseMessage.protocol + "}"};      
        double[] attackRange = {0.3, 0.4};

       /* Write Header and Columns */
        if(printHeader){
            writeDefaultHeader();
            printHeader = false;
        }
             
        ArrayList<GooseMessage> poisonedGooses = new ArrayList<>();
        int stNum = 0;
        int sqNum = 0;
         for (int i = 0; i < formatedCSVFile2.size() - 1; i++) {
            float time = formatedCSVFile2.get(i)[0]; 
            if (time > attackRange[0] && time < attackRange[1]){
                GooseMessage poisonedGoose = gooseEventManager.getLastGooseFromSV(time).copy();
                poisonedGoose.setStNum(stNum++);
                poisonedGoose.setSqNum(sqNum);
                poisonedGoose.setCbStatus(poisonedGoose.getInverseCbStatus());
                poisonedGoose.setTimestamp(time + 0.000005);
                poisonedGoose.setT(time + 0.000005);
                poisonedGooses.add(poisonedGoose);
                //System.out.println("StNum >" + poisonedGoose.getStNum() + "SqNum > "+poisonedGoose.getSqNum()+", Time>"+poisonedGoose.getTimestamp());
            } else if (time > attackRange[1]){
                break; // Generate only messages when no fault is occuring
            }
        }
       
        gooseEventManager.setGooseMessages(poisonedGooses);
        
        for (int i = 0; i < formatedCSVFile2.size() - 1; i++) {
            float time = formatedCSVFile2.get(i)[0]; 
            if (time > attackRange[0] && time < attackRange[1]){
                String line = "";
                GooseMessage currentGoose = gooseEventManager.getLastGooseFromSV(time);               
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," 
                        + currentGoose.asCSVFull() + getConsistencyFeaturesAsCSV(time) + "," + label[7];           
                write(line);
            } else if (time > attackRange[1]){
                break; // Generate only messages when no fault is occuring
            }
        }

    }
     
}
