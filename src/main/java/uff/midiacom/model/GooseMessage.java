/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uff.midiacom.model;

/**
 *
 * @author silvio
 */
public class GooseMessage {

    private int cbStatus;                   // DYNAMICALLY GENERATED 
    private int stNum;                      // DYNAMICALLY GENERATED 
    private int sqNum;                      // DYNAMICALLY GENERATED 
    private double t;                       // DYNAMICALLY GENERATED - Last Goose Change  
    private double timestamp;               // DYNAMICALLY GENERATED  
 
    
    public static int frameLen = 240;
    public static int gooseTimeAllowedtoLive = 11000;
    public static int gooseLen = 226;
    public static int numDatSetEntries = 25;
    public static int APDUSize = 215;
    public static int confRev = 1;

    public static String ethDst = "01:a0:f4:08:2f:77";
    public static String ethSrc = "00:a0:f4:08:2f:77";
    public static String ethType = "0x000088b8";
    public static String gooseAppid = "0x00003001";
    public static String TPID = "0x8100";
    public static String gocbRef = "LD/LLN0$GO$gcbA";
    public static String datSet = "LD/LLN0$IntLockA";
    public static String goID = "InterlockingA";
    public static String test = "FALSE";
    public static String ndsCom = "FALSE";
    public static String protocol = "GOOSE";

    public GooseMessage(int cbStatus, int stNum, int sqNum, double timestamp, double t) {
        this.cbStatus = cbStatus;
        this.stNum = stNum;
        this.sqNum = sqNum;
        this.timestamp = timestamp;
        this.t = t;
    }

    public int isCbStatus() {
        return cbStatus;
    }

    public void setCbStatus(int cbStatus) {
        this.cbStatus = cbStatus;
    }

    public int getStNum() {
        return stNum;
    }

    public void setStNum(int stNum) {
        this.stNum = stNum;
    }

    public int getSqNum() {
        return sqNum;
    }

    public void setSqNum(int sqNum) {
        this.sqNum = sqNum;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public String asCSVFull() {
        return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + cbStatus + ", " + frameLen
                + ", " + ethDst + ", " + ethSrc + ", " + ethType + ", " + gooseTimeAllowedtoLive + ", " + gooseAppid
                + ", " + gooseLen + ", " + TPID + ", " + gocbRef + ", " + datSet
                + ", " + goID + ", " + test + ", " + confRev + ", " + ndsCom
                + ", " + numDatSetEntries + ", " + APDUSize + ", " + protocol;
    }

    public String asCSVCompact() {
        return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + cbStatus;
    }

    public String asCSVinverseStatus() {
        if (cbStatus == 1) {
            return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + 0;
        } else {
            return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + 1;
        }
    }

    public String asCSVMasquerade(boolean resetSqNum) {
        if (resetSqNum) {
            setSqNum(0);
        }
        if (cbStatus == 1) {
            return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + 0;
        } else {
            return getTimestamp() + "," + getSqNum() + "," + getStNum() + "," + 1;
        }
    }

}
