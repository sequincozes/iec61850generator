/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.goose;

import uff.midiacom.model.GooseMessage;
import uff.midiacom.usecases.UseCases;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author silvio
 */
public final class GooseEventManager {

    private int initialStNum;
    private int initialSqNum;
    private double[] tIntervals; //time in seconds
    private double[] eventsTimestamp;
    private ArrayList<GooseMessage> gooseMessages;
    double delayFromEvent = -17;
    double firstGooseTime = -17;
    double backoffStartingMult = 6.33000000000011f;
    double minTime = -17;
    long maxTime = -17;
    private boolean initialCbStatus;
    GooseMessage pseudoPast;

    public GooseEventManager(boolean cbStatus, int stNum, int sqNum, double[] eventTimestamp, double delayFromEvent, double firstGoose, double initialBackoffInterval, long minTime, long maxTime) {
        this.initialCbStatus = cbStatus;
        this.initialStNum = stNum;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.initialSqNum = sqNum;
        this.tIntervals = exponentialBackoff(minTime, maxTime, initialBackoffInterval);
        this.eventsTimestamp = eventTimestamp;
        this.delayFromEvent = delayFromEvent;
        this.firstGooseTime = firstGoose;
        gooseMessages = generateGooseMessages();
    }

    public GooseEventManager(boolean cbStatus, int stNum, int sqNum, double currentTimestamp, double delayFromEvent, double firstGoose, double initialBackoffInterval, long minTime, long maxTime) {
        this.initialCbStatus = cbStatus;
        this.initialStNum = stNum;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.initialSqNum = sqNum;
        this.tIntervals = exponentialBackoff(minTime, maxTime, initialBackoffInterval);
        this.eventsTimestamp = new double[]{};
        this.delayFromEvent = delayFromEvent;
        this.firstGooseTime = firstGoose;
        gooseMessages = generateGooseMessages(currentTimestamp);
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
                    pseudoPast = gooseMessages.get(0).copy(); // Pseudo past
                    double pseudoPastTimestamp = gooseMessages.get(0).getTimestamp() - maxTime;
                    pseudoPast.setTimestamp(pseudoPastTimestamp); //Assume the last message wast sent at now - maxtime
                    pseudoPast.setSqNum(pseudoPast.getSqNum() - 1);
                    return pseudoPast;
                } else {
                    return gooseMessages.get(i - 1);
                }
            }
        }
        return null;
    }

    boolean debug = false;

    public ArrayList<GooseMessage> generateGooseMessages() {
        this.gooseMessages = new ArrayList<>();
        int stNum = getInitialStNum();
        int sqNum = getInitialSqNum();
        boolean cbStatus = isInitialCbStatus();
        GooseMessage periodicGoose = new GooseMessage(toInt(cbStatus), stNum, sqNum, getFirstGooseTime(), getFirstGooseTime());
        this.gooseMessages.add(periodicGoose);              // periodic message

        if (debug) {
            System.out.println(getPreviousGoose(periodicGoose).asCSVFull());
            System.out.println(periodicGoose.asCSVFull());
        }

        for (double eventTimestamp : getEventsTimestamp()) {
            // Status change
            cbStatus = !cbStatus;
            stNum = stNum + 1;
            sqNum = 1;
            double timestamp = getDelayFromEvent() + eventTimestamp;
            double t = timestamp; // new t

//            System.out.println(t);
            for (double interval : gettIntervals()) { // GOOSE BURST MODE
                if (eventTimestamp == getEventsTimestamp()[0] && interval >= getEventsTimestamp()[1]) {
                    break;
                } else {
                    GooseMessage gm = new GooseMessage(
                            toInt(cbStatus), // current status
                            stNum, // same stNum
                            sqNum++, // increase sqNum
                            timestamp, // current timestamp
                            t // timestamp of last st change
                    );
                    if (debug) {
                        System.out.println(gm.asCSVFull());
                    }
                    this.gooseMessages.add(gm);
                    timestamp = timestamp + interval;                               // burst mode
                }
            }
        }
        if (debug) {
            System.exit(0);
        }
        return this.gooseMessages;
    }

    public ArrayList<GooseMessage> generateGooseMessages(double currentTimestamp) {
        this.gooseMessages = new ArrayList<>();
        int stNum = getInitialStNum();
        int sqNum = getInitialSqNum();
        boolean cbStatus = isInitialCbStatus();
        GooseMessage periodicGoose = new GooseMessage(toInt(cbStatus), stNum, sqNum, currentTimestamp, getFirstGooseTime());
        this.gooseMessages.add(periodicGoose);              // periodic message

        if (debug) {
            System.out.println(getPreviousGoose(periodicGoose).asCSVFull());
            System.out.println(periodicGoose.asCSVFull());
        }

        for (double eventTimestamp : getEventsTimestamp()) {
            // Status change
            cbStatus = !cbStatus;
            stNum = stNum + 1;
            sqNum = 1;
            double timestamp = getDelayFromEvent() + eventTimestamp;
            double t = timestamp; // new t

//            System.out.println(t);
            for (double interval : gettIntervals()) { // GOOSE BURST MODE
                if (eventTimestamp == getEventsTimestamp()[0] && interval >= getEventsTimestamp()[1]) {
                    break;
                } else {
                    GooseMessage gm = new GooseMessage(
                            toInt(cbStatus), // current status
                            stNum, // same stNum
                            sqNum++, // increase sqNum
                            timestamp, // current timestamp
                            t // timestamp of last st change
                    );
                    if (debug) {
                        System.out.println(gm.asCSVFull());
                    }
                    this.gooseMessages.add(gm);
                    timestamp = timestamp + interval;                               // burst mode
                }
            }
        }
        if (debug) {
            System.exit(0);
        }
        return this.gooseMessages;
    }
    //@Deprecated
//    public ArrayList<GooseMessage> generateGooseMessagesForUC01() {
//        this.gooseMessages = new ArrayList<>();
//        int stNum = getInitialStNum();
//        int sqNum = getInitialSqNum();
//        boolean cbStatus = isInitialCbStatus();
//        GooseMessage periodicGoose = new GooseMessage(toInt(cbStatus), stNum, sqNum, getFirstGooseTime(), getFirstGooseTime());
//        this.gooseMessages.add(periodicGoose);              // periodic message
//        if (debug) {
//            System.out.println(getPreviousGoose(periodicGoose).asCSVFull());
//            System.out.println(periodicGoose.asCSVFull());
//        }
//
//        for (double eventTimestamp : getEventsTimestamp()) {
//            // Status change
//            cbStatus = !cbStatus;
//            stNum = stNum + 1;
//            sqNum = 1;
//            double timestamp = getDelayFromEvent() + eventTimestamp;
//            double t = timestamp; // new t
//
////            System.out.println(t);
//            for (double interval : gettIntervals()) { // GOOSE BURST MODE
//                if (eventTimestamp == getEventsTimestamp()[0] && interval >= getEventsTimestamp()[1]) {
//                    break;
//                } else {
//                    GooseMessage gm = new GooseMessage(
//                            toInt(cbStatus), // current status
//                            stNum, // same stNum
//                            sqNum++, // increase sqNum
//                            timestamp, // current timestamp
//                            t // timestamp of last st change
//                    );
//                    if (debug) {
//                        System.out.println(gm.asCSVFull());
//                    }
//                    this.gooseMessages.add(gm);
//                    timestamp = timestamp + interval;                               // burst mode
//
//                }
//            }
//        }
//        if (debug) {
//            System.exit(0);
//        }
//        return this.gooseMessages;
//    }

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

    public double[] exponentialBackoffForUC01(long minTime, long maxTime, double intervalMultiplier) {
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

    public double getFirstGooseTime() {
        return firstGooseTime;
    }

    public void setFirstGooseTime(double firstGooseTime) {
        this.firstGooseTime = firstGooseTime;
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

    private long getRandomTime(){
        Random gooseRandom = new Random(System.nanoTime());
        return gooseRandom.nextInt(1000); // random index, random SV messages
    }

}
