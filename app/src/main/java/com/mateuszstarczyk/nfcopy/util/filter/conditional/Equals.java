package com.mateuszstarczyk.nfcopy.util.filter.conditional;

import java.util.Arrays;

import com.mateuszstarczyk.nfcopy.util.NfcComm;
import com.mateuszstarczyk.nfcopy.util.filter.FilterInitException;

/**
 * A simple conditional that checks if the provided data is equal to the provided pattern.
 */
public class Equals extends Conditional {
    public Equals(byte[] pattern, TARGET target) throws FilterInitException {
        super(pattern, target);
    }

    public Equals(byte[] pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    public Equals(byte pattern, TARGET target, ANTICOLFIELD field) throws FilterInitException {
        super(pattern, target, field);
    }

    @Override
    protected boolean checkNfcData(NfcComm nfcdata) {
        return Arrays.equals(nfcdata.getData(), mMatchPattern);
    }

    @Override
    protected boolean checkUidData(NfcComm nfcdata) {
        return Arrays.equals(nfcdata.getUid(), mMatchPattern);
    }

    @Override
    protected boolean checkAtqaData(NfcComm nfcdata) {
        return Arrays.equals(nfcdata.getAtqa(), mMatchPattern);
    }

    @Override
    protected boolean checkHistData(NfcComm nfcdata) {
        return Arrays.equals(nfcdata.getHist(), mMatchPattern);
    }

    @Override
    protected boolean checkSakData(NfcComm nfcdata) {
        return nfcdata.getSak() == mMatchByte;
    }
}
