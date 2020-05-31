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
 *
 * @author silvio
 */
public class ExtractorAgregation {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        //UC01.run("uc01_fullgoose.csv");
        //UC02.run("uc02_fullgoose.csv");        
        //UC03.run("uc03_fullgoose.csv");        
        UC04.run("uc04_fullgoose.csv");
        //UC05.run("uc05_fullgoose.csv");        
        //UC00.run("uc00_fullgoose.csv");

    }

}
