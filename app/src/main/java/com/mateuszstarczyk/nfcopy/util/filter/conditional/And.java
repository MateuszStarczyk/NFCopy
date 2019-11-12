package com.mateuszstarczyk.nfcopy.util.filter.conditional;

import com.mateuszstarczyk.nfcopy.util.NfcComm;
import com.mateuszstarczyk.nfcopy.util.filter.FilterInitException;

/**
 * The logical AND of a number of conditionals
 */
public class And extends Conditional {
    public And(Conditional... cond) throws FilterInitException {
        super(cond);
    }

    @Override
    public boolean applies(NfcComm nfcdata) {
        boolean rv = true;
        for (Conditional c : mCondList) {
            rv = rv && c.applies(nfcdata);
            if (!rv) return false;
        }
        return true;
    }

    // All of these functions are never used, but have to be implemented in order to conform to the
    // superclass.
    @Override
    protected boolean checkNfcData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return false;
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return false;
    }
}
