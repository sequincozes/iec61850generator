/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.sv;

import uff.midiacom.usecases.AbstractUseCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author silvio
 */
public class ExtractorSvOnly {

    BufferedWriter bw;
    String outputFile = "/home/silvio/datasets/Full_SV_2020/full_compilation_2.csv";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ExtractorSvOnly extractor = new ExtractorSvOnly();
        
        extractor.startWriting();
        
        int[] resistences = {10, 50, 100};
        
        for (int resistence : resistences) {
            for (int run = 1; run < 132; run++) {
                switch (String.valueOf(run).length()) {
                    case 1:
                        extractor.run(resistence, "00" + run);
                        break;
                    case 2:
                        extractor.run(resistence, "0" + run);
                        break;
                    default:
                        extractor.run(resistence, String.valueOf(run));
                        break;
                }
            }
        }
        
        extractor.finishWriting();
    }

    public void run(int res, String num) throws IOException {

        /* Extract First Part */
        String columns[] = {"Col1", "Col2", "Col3", "Col4", "Col5", "Col6", "Col7", "Col8", "Col9", "Col10", "Col11"};
        ArrayList<Float[]> formatedCSVFile;
        if(AbstractUseCase.USE_OFFSET) {
            formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);
        } else {
            formatedCSVFile = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_01.out", 1, columns);
        }
        /* Extract Second Part */
        String columns2[] = {"Col12", "Col13", "Col14"};
        ArrayList<Float[]> formatedCSVFile2 = consumeFloat("/home/silvio/datasets/Full_SV_2020/resistencia" + res + "/SILVIO_r00" + num + "_02.out", 1, columns2);

        String[] label = {"normal", "falta", "normal"};
        double[] labelRange = {0.5, 0.6};
        for (int i = 0; i < formatedCSVFile2.size() - 1; i++) {
            float time = formatedCSVFile2.get(i)[0];
            String line = "";
            if (time < labelRange[0] & time < labelRange[1]) {
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + label[0];
            } else if (time < labelRange[1]) {
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + label[1];
            } else {
                line = joinColumns(formatedCSVFile, formatedCSVFile2, columns, columns2, i) + "," + label[2];
            }
            write(line);
        }

    }

    public String joinColumns(ArrayList<Float[]> formatedCSVFile, ArrayList<Float[]> formatedCSVFile2, String columns[], String columns2[], int line) {
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

    public ArrayList<Float[]> consumeFloat(String file, int scale, String columns[]) {
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

    private void write(String line) throws FileNotFoundException, IOException {
        bw.write(line);
        bw.newLine();
    }

    private void startWriting() throws FileNotFoundException, IOException {
        File fout = new File(outputFile);
        FileOutputStream fos = new FileOutputStream(fout);
        bw = new BufferedWriter(new OutputStreamWriter(fos));
    }

    private void finishWriting() throws FileNotFoundException, IOException {
        bw.close();
    }

}
