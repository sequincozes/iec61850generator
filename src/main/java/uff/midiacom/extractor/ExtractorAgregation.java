/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.extractor;

import java.io.FileNotFoundException;
import java.io.IOException;

import uff.midiacom.usecases.*;

import static uff.midiacom.usecases.AbstractUseCase.generateSingleRound;

/**
 * @author silvio
 */
public class ExtractorAgregation {

    public static void main(String[] args) throws FileNotFoundException, IOException {
//        java -jar assessAdFS.jar fs 2 >> fs_uc02.txt
        AbstractUseCase.outputLocation = "/home/silvio/datasets/Full_SV_2021/consistency_v6/";;
        AbstractUseCase.generateSingleRound = false;
        AbstractUseCase.replace = false;
        AbstractUseCase.printHeader = true;
        AbstractUseCase.runs = 1;
        System.out.println("Will run UC00:"); UC00.run("train-1run.csv");
        AbstractUseCase.printHeader = false;
        System.out.println("Will run UC00_noFaults:"); UC00_noFaults.run("train_1Run.csv"); // 132 is the max number of runs (faults x segments)
        System.out.println("Will run UC01:"); UC01.run("train.csv"); //132
        System.out.println("Will run UC02:"); UC02.run("uc02/uc02.csv");
        System.out.println("Will run UC03:"); UC03.run("uc03/uc03.csv");
        System.out.println("Will run UC04:"); UC04.run("uc04/uc04.csv");
        System.out.println("Will run UC05:"); UC05.run("uc05/uc05.csv");
        System.out.println("Will run UC06:"); UC06.run("uc06/uc06.csv");
        System.out.println("Will run UC07:"); UC07.run("uc07/uc07.csv");

    }

}
