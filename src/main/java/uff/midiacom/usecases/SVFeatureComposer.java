package uff.midiacom.usecases;

import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public abstract class SVFeatureComposer {

    public static double getTrapezioArea(double x0, double x1, double fx0, double fx1) {
        double trap;
        trap = (fx1 + fx0) * (x1 - x0) / 2.00;
//        System.out.println(trap);
        return trap;
    }

    public static double getSumTrapezioArea(ArrayList<Double> v) {
        double areat = 0;
        for (int i = 0; i < v.size(); i++) {
            areat = areat + v.get(i);
        }
//        System.out.println(area1t);
        return areat;
    }

    public static double getRmsValue(ArrayList<Double> isbABuffer) { // Function that Calculate Root Mean Square
        int square = 0;
        double mean = 0.0, root = 0.0;

        // Calculate square.
        for (int i = 0; i < isbABuffer.size(); i++) {
            square += pow(isbABuffer.get(i), 2);
        }

        // Calculate Mean.
        mean = (square / (float) (isbABuffer.size()));

        // Calculate Root.
        root = sqrt(mean);

        return root;
    }

    ArrayList<Double> areaAccumulators[] = new ArrayList[]{new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>(), new ArrayList<Double>()};
    ArrayList<Double> buffers[] = new ArrayList[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
    double measureTrapSum[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    double rmsValues[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public String getSVHistorical(Float[] prev, Float[] current, Float[] prev2, Float[] current2) {
        double timePrev = prev[0];
        double timeCurrent = current[0];
        for (int i = 0; i < buffers.length; i++) {
            int pos = i + 1;
            double meassurePrev;
            double measureCurrent;
            // handle multiple files
            if (pos < 10) {
                meassurePrev = prev[pos]; //ignore time (desliza 1)
                measureCurrent = current[pos]; //ignore time  (desliza 1)
            } else if (pos == 11) {
                meassurePrev = prev2[1]; //ignore time (desliza 1)
                measureCurrent = current2[1]; //ignore time  (desliza 1)
            } else  {
                meassurePrev = prev2[2]; //ignore time (desliza 1)
                measureCurrent = current2[2]; //ignore time  (desliza 1)
            }

            double isbATrapArea = getTrapezioArea(timePrev, timeCurrent, meassurePrev, measureCurrent);
            areaAccumulators[i].add(isbATrapArea);
            buffers[i].add(measureCurrent);
            if (buffers[i].size() <= 80) {//((isbACurrent > 0 && isbAPrev > 0) || (isbACurrent < 0 && isbAPrev < 0) || (isbACurrent == 0 && isbAPrev == 0)) {

            } else {
                measureTrapSum[i] = getSumTrapezioArea(areaAccumulators[i]);
                rmsValues[i] = getRmsValue(buffers[i]);
                buffers[i].clear();
                areaAccumulators[i].clear();
            }
        }

        return rmsValues[0] + "," + measureTrapSum[0] + "," +
                rmsValues[1] + "," + measureTrapSum[1] + "," +
                rmsValues[2] + "," + measureTrapSum[2] + "," +
                rmsValues[3] + "," + measureTrapSum[3] + "," +
                rmsValues[4] + "," + measureTrapSum[4] + "," +
                rmsValues[5] + "," + measureTrapSum[5] + "," +
                rmsValues[6] + "," + measureTrapSum[6] + "," +
                rmsValues[7] + "," + measureTrapSum[7] + "," +
                rmsValues[8] + "," + measureTrapSum[8] + "," +
                rmsValues[9] + "," + measureTrapSum[9] + "," +
                rmsValues[10] + "," + measureTrapSum[10] + "," +
                rmsValues[11] + "," + measureTrapSum[11];
    }

}
