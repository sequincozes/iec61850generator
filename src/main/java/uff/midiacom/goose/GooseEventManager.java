/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.goose;

import uff.midiacom.model.GooseMessage;
import java.util.ArrayList;

/**
 *
 * @author silvio
 */
public final class GooseEventManager {

    private int initialStNum;
    private int initialSqNum;
    private int tIndex;
    private double[] tIntervals; //time in seconds
    private double[] eventsTimestamp;
    private ArrayList<GooseMessage> gooseMessages;
    double delayFromEvent;
    double firstGoose;
    double backoffStartingMult = 6.33000000000011f;
    double minTime;
    long maxTime;
    private boolean initialCbStatus;

    public GooseEventManager(boolean cbStatus, int stNum, int sqNum, int tIndex, double[] eventTimestamp, double delayFromEvent, double firstGoose, double initialBackoffInterval, long minTime, long maxTime) {
        this.initialCbStatus = cbStatus;
        this.initialStNum = stNum;
        this.initialSqNum = sqNum;
        this.tIndex = tIndex;
        this.tIntervals = exponentialBackoff(minTime, maxTime, initialBackoffInterval);
        this.eventsTimestamp = eventTimestamp;
        this.delayFromEvent = delayFromEvent;
        this.firstGoose = firstGoose;
        gooseMessages = generateGooseMessages();
    }

    public GooseEventManager() {

    }

    public static void main(String[] args) {
        GooseEventManager gm = new GooseEventManager(false, 0, 0, 0, new double[]{0.5, 0.6}, 0.00631, 0.01659, 6.33000000000011f, 4, 1000);
        //double[] gooseLegitimateEvents = {0.01659, 0.50631, 0.5103, 0.51429, 0.53949, 0.60627, 0.61026, 0.61425, 0.63945, 0.79821};

        for (GooseMessage g : gm.getGooseMessages()) {
            System.out.println("" + g.getTimestamp() + " - Status: " + g.isCbStatus() + " - StNum: " + g.getStNum() + " - SqNum: " + g.getSqNum());
        }

        System.out.println("Last before 395 is: " + gm.getLastGooseFromSV(0.395).getTimestamp());
        System.out.println("Last before 495 is: " + gm.getLastGooseFromSV(0.495).getTimestamp());
        System.out.println("Last before 550 is: " + gm.getLastGooseFromSV(0.550).getTimestamp());
        System.out.println("Last before 700 is: " + gm.getLastGooseFromSV(0.700).getTimestamp());
        System.out.println("Last before 800 is: " + gm.getLastGooseFromSV(0.800).getTimestamp());
    }

    public GooseMessage getLastGooseFromSV(double timestamp) {
        GooseMessage lastGooseMessage = gooseMessages.get(0);
        for (GooseMessage gooseMessage : gooseMessages) {
            if (gooseMessage.getTimestamp() > timestamp) {
                return lastGooseMessage;
            } else {
                lastGooseMessage = gooseMessage;
            }
        }
        return lastGooseMessage;
    }

    public GooseMessage getPreviousGoose(GooseMessage gooseMessage) {
        for (int i = 0; i < gooseMessages.size(); i++) {
            if (gooseMessage.equals(gooseMessages.get(i))) {
                if (i == 0) {
                    GooseMessage pseudoPast = gooseMessages.get(0); // Pseudo past
                    pseudoPast.setSqNum(pseudoPast.getSqNum() - 1);
                    pseudoPast.setTimestamp(gooseMessages.get(1).getTimestamp() - (gooseMessages.get(1).getTimestamp() - pseudoPast.getTimestamp())); //Copy timestamp from next minus actual
                    pseudoPast.setT(gooseMessages.get(1).getT() - (gooseMessages.get(1).getT() - pseudoPast.getT())); //Copy timestamp from next minus actual
                    return pseudoPast;
                } else {
                    return gooseMessages.get(i - 1);
                }
            }
        }
        return null;
    }

    public ArrayList<GooseMessage> generateGooseMessages() {
        ArrayList<GooseMessage> gooseMessages = new ArrayList<>();
        int stNum = getInitialStNum();
        int sqNum = getInitialSqNum();
        boolean cbStatus = isInitialCbStatus();

        gooseMessages.add(new GooseMessage(toInt(cbStatus), stNum, sqNum, getFirstGoose(), getFirstGoose()));              // periodic message

        for (double eventTimestamp : getEventsTimestamp()) {
            // Status change
            cbStatus = !cbStatus;
            stNum = stNum + 1;
            sqNum = 0;

            double timestamp = getDelayFromEvent() + eventTimestamp;
            double t = timestamp;
            for (double interval : gettIntervals()) { // GOOSE BURST MODE
                if (eventTimestamp == getEventsTimestamp()[0] && interval >= getEventsTimestamp()[1]) {
                    break;
                } else {
                    gooseMessages.add(new GooseMessage(
                            toInt(cbStatus), // current status
                            stNum, // same stNum
                            sqNum++, // increase sqNum
                            timestamp, // current timestamp
                            t // timestamp of last st change
                    ));
                    timestamp = timestamp + interval;                               // burst mode

                }
            }
        }
        return gooseMessages;
    }

    public double[] exponentialBackoff(long minTime, long maxTime, double intervalMultiplier) {
        long retryIntervalMs = minTime;

        ArrayList<Double> tIntervals = new ArrayList<>();
        do {
            tIntervals.add(retryIntervalMs / 1000.0);
            retryIntervalMs *= intervalMultiplier;
            if (retryIntervalMs > maxTime) {
                intervalMultiplier = intervalMultiplier + 0.001;
                retryIntervalMs = minTime;
            } else if (retryIntervalMs == maxTime) {
                tIntervals.add(retryIntervalMs / 1000.0);
                break;
            }

        } while (retryIntervalMs <= maxTime);

        int i = 0;
        double[] arrayIntervals = new double[tIntervals.size() + 1];
        arrayIntervals[i++] = tIntervals.get(0); // first two retransmission are on same period
        for (double ti : tIntervals) {
            arrayIntervals[i++] = ti;
        }
        return arrayIntervals;
    }

    public int getInitialStNum() {
        return initialStNum;
    }

    public void setInitialStNum(int initialStNum) {
        this.initialStNum = initialStNum;
    }

    public int getInitialSqNum() {
        return initialSqNum;
    }

    public void setInitialSqNum(int initialSqNum) {
        this.initialSqNum = initialSqNum;
    }

    public int gettIndex() {
        return tIndex;
    }

    public void settIndex(int tIndex) {
        this.tIndex = tIndex;
    }

    public double[] gettIntervals() {
        return tIntervals;
    }

    public void settIntervals(double[] tIntervals) {
        this.tIntervals = tIntervals;
    }

    public double[] getEventsTimestamp() {
        return eventsTimestamp;
    }

    public void setEventsTimestamp(double[] eventsTimestamp) {
        this.eventsTimestamp = eventsTimestamp;
    }

    public ArrayList<GooseMessage> getGooseMessages() {
        return gooseMessages;
    }

    public void setGooseMessages(ArrayList<GooseMessage> gooseMessages) {
        this.gooseMessages = gooseMessages;
    }

    public double getDelayFromEvent() {
        return delayFromEvent;
    }

    public void setDelayFromEvent(double delayFromEvent) {
        this.delayFromEvent = delayFromEvent;
    }

    public double getFirstGoose() {
        return firstGoose;
    }

    public void setFirstGoose(double firstGoose) {
        this.firstGoose = firstGoose;
    }

    public double getBackoffStartingMult() {
        return backoffStartingMult;
    }

    public void setBackoffStartingMult(double backoffStartingMult) {
        this.backoffStartingMult = backoffStartingMult;
    }

    public double getMinTime() {
        return minTime;
    }

    public void setMinTime(double minTime) {
        this.minTime = minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public boolean isInitialCbStatus() {
        return initialCbStatus;
    }

    public void setInitialCbStatus(boolean initialCbStatus) {
        this.initialCbStatus = initialCbStatus;
    }

    private int toInt(boolean cbStatus) {
        if (cbStatus == false) {
            return 0;
        } else {
            return 1;
        }

    }

}
