package com.badlogic.invaders.android;

/**
 * Created by scott on 2016-06-27.
 */
/**
 * Created by hoanmotsai on 2016-06-20.
 */
public interface NeblinaDelegate {

    public final static String ACTION_NEB_CONNECTED = "com.motsai.neblina.ACTION_NEB_CONNECTED";
    public final static String ACTION_NEB_DEBUG_DATA = "com.motsai.neblina.ACTION_NEB_DEBUG_DATA";
    public final static String ACTION_NEB_FUSION_DATA = "com.motsai.neblina.ACTION_NEB_FUSION_DATA";
    public final static String ACTION_NEB_PMGNT_DATA = "com.motsai.neblina.ACTION_NEB_PMGNT_DATA";
    public final static String ACTION_NEB_LED_DATA = "com.motsai.neblina.ACTION_NEB_LED_DATA";
    public final static String ACTION_NEB_STORAGE_DATA = "com.motsai.neblina.ACTION_NEB_STORAGE_DATA";
    public final static String ACTION_NEB_EEPROM_DATA = "com.motsai.neblina.ACTION_NEB_EEPROM_DATA";


    void initializeNeblina();
    void didReceiveRSSI(int rssi);
    void didReceiveFusionData(int type , byte[] data, boolean errFlag);
    void didReceiveDebugData(int type, byte[] data, boolean errFlag);
    void didReceivePmgntData(int type, byte[] data, boolean errFlag);
    void didReceiveStorageData(int type, byte[] data, boolean errFlag);
    void didReceiveEepromData(int type, byte[] data, boolean errFlag);
    void didReceiveLedData(int type, byte[] data, boolean errFlag);
}
