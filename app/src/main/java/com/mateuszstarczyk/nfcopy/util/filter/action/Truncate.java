package com.mateuszstarczyk.nfcopy.util.filter.action;

import com.mateuszstarczyk.nfcopy.util.NfcComm;
import com.mateuszstarczyk.nfcopy.util.filter.FilterInitException;

/**
 * Action to truncate the LAST n bytes.
 */
public class Truncate extends Action {
    public Truncate(int offset, TARGET target) throws FilterInitException {
        super(offset, target);
    }

    // No constructor for Anticol bytes => They cannot be truncated (fixed length)

    // Helper function to do the actual truncating
    private byte[] doTruncate(byte[] base) {
        // If the offset is larger than the size of the input data, do nothing
        if (base.length < mOffset) return base;

        byte[] result = new byte[base.length - mOffset];
        System.arraycopy(base, 0, result, 0, base.length - mOffset);
        return result;
    }

    @Override
    protected NfcComm modifyNfcData(NfcComm nfcdata) {
        nfcdata.setData(doTruncate(nfcdata.getData()));
        return nfcdata;
    }

    // Anticol cannot be truncated => Return the original value
    @Override
    protected NfcComm modifyUidData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifyAtqaData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifyHistData(NfcComm nfcdata) {
        return nfcdata;
    }

    @Override
    protected NfcComm modifySakData(NfcComm nfcdata) {
        return nfcdata;
    }
}
