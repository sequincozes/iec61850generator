/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.extractor;

import java.io.FileNotFoundException;
import java.io.IOException;

import uff.midiacom.usecases.*;

/**
 * @author silvio
 */
public class ExtractorAgregation {

    public static void main(String[] args) throws FileNotFoundException, IOException {
//        java -jar assessAdFS.jar fs 2 >> fs_uc02.txt
        AbstractUseCase.outputLocation = "/home/silvio/datasets/Full_SV_2021/consistency_v7/10pct/";;
        AbstractUseCase.generateSingleRound = false;
        AbstractUseCase.replace = false;
        AbstractUseCase.runs = 13;

//        runUC01();//
//        runUC02();
//        runUC03();
        runUC04();
//        runUC05();
//        runUC06();
//        runUC07();


        AbstractUseCase.outputLocation = "/home/silvio/datasets/Full_SV_2021/consistency_v7/100pct/";;
        AbstractUseCase.generateSingleRound = false;
        AbstractUseCase.replace = false;
        AbstractUseCase.runs = 132;

//        runUC01(); //done
//        runUC02(); //done
//        runUC03();
//        runUC04();
//        runUC05();
//        runUC06();
//        runUC07();
    }

    private static void runUC01() throws IOException {
        String type = "train";
        int uc = 1;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC01.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC01.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

    private static void runUC02() throws IOException {
        String type = "train";
        int uc = 2;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC02_novo.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC02_novo.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

    private static void runUC03() throws IOException {
        String type = "train";
        int uc = 3;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC03.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC03.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

    private static void runUC04() throws IOException {
        String type = "train";
        int uc = 4;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC04.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC04.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

    private static void runUC05() throws IOException {
        String type = "train";
        int uc = 5;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC05.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC05.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

    private static void runUC06() throws IOException {
        String type = "train";
        int uc = 6;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC06.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC06.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }


    private static void runUC07() throws IOException {
        String type = "train";
        int uc = 7;
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC07.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)

        type = "test";
        AbstractUseCase.printHeader = true; System.out.println("Will run UC00):"); UC00.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC00(noFaults):"); UC00_noFaults.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
        AbstractUseCase.printHeader = false; System.out.println("Will run UC0"+uc+":"); UC07.run("uc0"+uc+"/"+type+".csv"); // 132 is the max number of runs (faults x segments)
    }

}
