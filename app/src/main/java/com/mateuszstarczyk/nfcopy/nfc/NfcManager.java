package com.mateuszstarczyk.nfcopy.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.BlockingQueue;

import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.nfc.hce.ApduService;
import com.mateuszstarczyk.nfcopy.nfc.hce.DaemonConfiguration;
import com.mateuszstarczyk.nfcopy.nfc.reader.DesfireWorkaround;
import com.mateuszstarczyk.nfcopy.nfc.reader.IsoDepReader;
import com.mateuszstarczyk.nfcopy.nfc.reader.NFCTagReader;
import com.mateuszstarczyk.nfcopy.nfc.reader.NfcAReader;
import com.mateuszstarczyk.nfcopy.util.NfcComm;
import com.mateuszstarczyk.nfcopy.util.Utils;
import com.mateuszstarczyk.nfcopy.util.filter.FilterManager;
import com.mateuszstarczyk.nfcopy.util.sink.SinkManager;

/**
 * The NFC Manager is responsible for all NFC Interactions.
 */
public class NfcManager {
    private final String TAG = "NfcManager";

    // NFC Objects
    private Tag mTag;
    private NFCTagReader mReader;
    private ApduService mApduService;

    // Sink Manager
    private SinkManager mSinkManager;
    private Thread mSinkManagerThread;
    private BlockingQueue<NfcComm> mSinkManagerQueue;

    // Filter Manager
    private FilterManager mFilterManager;

    // Workaround
    private DesfireWorkaround mBroadcomWorkaroundRunnable;
    private Thread mBroadcomWorkaroundThread;

    // Context
    private Context mContext;

    private static NfcManager mInstance = null;


    // Constructor
    public NfcManager () {
        mInstance = this;
//        mApduService = new ApduService();
    }


    public static NfcManager getInstance() {
        if (mInstance == null) mInstance = new NfcManager();
        return mInstance;
    }

    public void setContext(Context ctx) {
        mContext = ctx;
    }

    // Reference setters
    /**
     * Set the Reference to the NFC Tag
     * @param tag The NFC Tag object
     */
    public void setTag(Tag tag) {
        mTag = tag;

        boolean found_supported_tag = false;

        // Identify tag type
        for(String type: tag.getTechList()) {
            Log.i(TAG, "setTag: Tag TechList: " + type);
            if("android.nfc.tech.IsoDep".equals(type)) {
                found_supported_tag = true;

                mReader = new IsoDepReader(tag);
                Log.d(TAG, "setTag: Chose IsoDep technology.");
                break;
            } else if("android.nfc.tech.NfcA".equals(type)) {
                found_supported_tag = true;

                mReader = new NfcAReader(tag);
                Log.d(TAG, "setTag: Chose NfcA technology.");
                break;
            }
        }
    }


    /**
     * Set the Reference to the ApduService
     * @param apduService The ApduService object
     */
    public void setApduService(ApduService apduService) {
        mApduService = apduService;
    }


    /**
     * Called when the APDU service is disconnected
     */
    public void unsetApduService() {
        mApduService = null;
    }


    /**
     * Set the Reference to the SinkManager
     * @param sinkManager The SinkManager object
     * @param smq The BlockingQueue connected with the SinkManager
     */
    public void setSinkManager(SinkManager sinkManager, BlockingQueue<NfcComm> smq) {
        mSinkManager = sinkManager;
        mSinkManagerQueue = smq;
    }

    public void unsetSinkManager() {
        mSinkManager = null;
        mSinkManagerQueue = null;
    }

    public SinkManager getSinkManager() {
        return mSinkManager;
    }


    /**
     * Set the reference to the FilterManager
     * @param filterManager The FilterManager object
     */
    public void setFilterManager(FilterManager filterManager) {
        mFilterManager = filterManager;
    }

    // Anticol
    /**
     * Get the Anticollision data of the attached card
     * @return NfcComm object with anticol data
     */
    public NfcComm getAnticolData() {
        // Get Anticol data
        byte[] uid  = mReader.getUID();
        byte[] atqa = mReader.getAtqa();
        byte sak    = mReader.getSak();
        byte[] hist = mReader.getHistoricalBytes();

        Log.d(TAG, "getAnticolData: HIST: " + Utils.bytesToHex(hist));

        // Create NfcComm object
        NfcComm anticol = new NfcComm(atqa, sak, hist, uid);

        // Return NfcComm object w/ anticol data
        return anticol;
    }


    /**
     * Set the Anticollision data in the native code patch
     * @param anticol NfcComm object containing the Anticol data
     */
    public void setAnticolData(NfcComm anticol) {
        // Parse data and transform to proper formats
        byte[] a_atqa = anticol.getAtqa();
        byte atqa = a_atqa.length > 0 ? a_atqa[a_atqa.length-1] : 0;

        byte[] hist = anticol.getHist();
        //byte hist = a_hist.length > 0 ? a_atqa[0] : 0;

        byte sak = anticol.getSak();
        byte[] uid = anticol.getUid();

        // Enable the Native Code Patch
        DaemonConfiguration.getInstance().uploadConfiguration(atqa, sak, hist, uid);
        DaemonConfiguration.getInstance().enablePatch();

        Log.i(TAG, "setAnticolData: Patch enabled");
    }


    /**
     * Stop the Broadcom Workaround thread, if it exists.
     */
    public void stopWorkaround() {
        Log.i(TAG, "stopWorkaround: Workaround thread stopping");
        if (mBroadcomWorkaroundThread != null) mBroadcomWorkaroundThread.interrupt();
        mBroadcomWorkaroundThread = null;
        mBroadcomWorkaroundRunnable = null;
    }


    /**
     * Shut down the NfcManager instance.
     */
    public void shutdown() {
        Log.i(TAG, "shutdown: Stopping workaround, closing connections");
        stopWorkaround();
        if (mReader != null) mReader.closeConnection();
        mReader = null;
        if (mSinkManagerThread != null) mSinkManagerThread.interrupt();
        mSinkManagerQueue = null;
        mSinkManager = null;
        mSinkManagerThread = null;
    }


    /**
     * Start up the NfcManager and related services.
     */
    public void start() {
        if(mSinkManagerThread == null) {
            Log.i(TAG, "start: Starting SinkManager Thread");
            mSinkManagerThread = new Thread(mSinkManager);
            mSinkManagerThread.start();
        } else {
            Log.i(TAG, "start: SinkManager Thread already started");
        }
    }
}