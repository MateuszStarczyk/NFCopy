#include "nfcd.h"
#include "vendor/adbi/hook.h"
#include <string.h>
/**
 * Commands of the broadcom configuration interface
 */
#define CFG_TYPE_ATQA  0x31
#define CFG_TYPE_SAK   0x32
#define CFG_TYPE_UID   0x33
#define CFG_TYPE_HIST  0x59

static void uploadConfig(struct s_chip_config config);

struct s_chip_config origValues = { 0 };
struct s_chip_config patchValues = { 0 };

NFC_SetStaticRfCback *nci_orig_SetRfCback;
NFC_SetConfig *nci_orig_NfcSetConfig;
NFC_SendData  *nfc_orig_sendData;
NFC_Deactivate  *nfc_orig_deactivate;

NFA_StopRfDiscovery  *nfa_orig_stop_rf_discovery;
NFA_DisablePolling *nfa_orig_disable_polling;
NFA_StartRfDiscovery *nfa_orig_start_rf_discovery;
NFA_EnablePolling *nfa_orig_enable_polling;

tCE_CB *ce_cb;

void nci_SetRfCback(tNFC_CONN_CBACK *p_cback) {
    hook_precall(&hook_rfcback);
    nci_orig_SetRfCback(p_cback);
    hook_postcall(&hook_rfcback);
}

extern "C" tNFC_STATUS nci_NfcSetConfig (uint8_t size, uint8_t *tlv) {
    LOG("HOOKNFC: nci_NfcSetConfig() ENTER");
    hook_precall(&hook_config);
    tNFC_STATUS r = nci_orig_NfcSetConfig(size, tlv);
    hook_postcall(&hook_config);
    LOG("HOOKNFC: nci_NfcSetConfig() LEAVE");
    return r;
}

/**
 * hooked SetRfCback implementation.
 * call the original function, but modify the control structure if the patch is enabled
 */
void hook_SetRfCback(tNFC_CONN_CBACK *p_cback) {
    LOGD("hook_SetRfCback");
    nci_SetRfCback(p_cback);
    if(p_cback != NULL && patchEnabled) {
        // fake that the default aid is selected
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_CC_FILE_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_NDEF_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_T4T_APP_SELECTED);
        ce_cb->mem.t4t.status &= ~ (CE_T4T_STATUS_REG_AID_SELECTED);
        ce_cb->mem.t4t.status |= CE_T4T_STATUS_WILDCARD_AID_SELECTED;
    }
}

tNFC_STATUS hook_NfcDeactivate(UINT8 deactivate_type) {
    hook_precall(&hook_deactivate);
    tNFC_STATUS r;
    LOG("HOOKNFC deactivate(), we got %d", deactivate_type);
    r = nfc_orig_deactivate(deactivate_type);
    hook_postcall(&hook_deactivate);
    return r;
}

tNFC_STATUS hook_NfcSenddata(UINT8 conn_id, BT_HDR *p_data) {
    hook_precall(&hook_senddata);
    LOG("HOOKNFC senddata() offset: %d, len: %d", p_data->offset, p_data->len);
    loghex("HOOKNFC data:",  ((UINT8 *)(p_data + 1) + p_data->offset), 16);
    tNFC_STATUS r = nfc_orig_sendData(conn_id, p_data);
    hook_postcall(&hook_senddata);
    return r;
}

tNFA_STATUS  hook_NfaStopRfDiscovery(void) {
    hook_precall(&hook_nfa_stop_rf_discovery);
    LOG("HOOKNFC hook_NfaStopRfDiscovery()");
    tNFA_STATUS r = nfa_orig_stop_rf_discovery();
    hook_postcall(&hook_nfa_stop_rf_discovery);
    return r;
}

tNFA_STATUS  hook_NfaDisablePolling(void) {
    hook_precall(&hook_nfa_disable_polling);
    LOG("HOOKNFC hook_nfa_disable_polling()");
    tNFA_STATUS r = nfa_orig_disable_polling();
    hook_postcall(&hook_nfa_disable_polling);
    return r;
}

tNFA_STATUS hook_NfaStartRfDiscovery() {
    hook_precall(&hook_nfa_start_rf_discovery);
    LOG("HOOKNFC hook_NfaStartRfDiscovery()")
    tNFA_STATUS r = nfa_orig_start_rf_discovery();
    hook_postcall(&hook_nfa_start_rf_discovery);
    return r;
}

tNFA_STATUS hook_NfaEnablePolling(tNFA_TECHNOLOGY_MASK poll_mask) {
    hook_precall(&hook_nfa_enable_polling);
    LOG("HOOKNFC hook_NfaEnablePolling() 0x%x", poll_mask);
    tNFA_STATUS r = nfa_orig_enable_polling(poll_mask);
    hook_postcall(&hook_nfa_enable_polling);
    return r;
}

/**
 * hooked NfcSetConfig implementation
 */
tNFC_STATUS hook_NfcSetConfig (uint8_t size, uint8_t *tlv) {

    loghex("HOOKNFC NfcSetConfig", tlv, size);
    uint8_t i = 0;
    bool needUpload = false;
    // read the configuration bytestream and extract the values that we indent to override
    // if we are in an active mode and the value gets overridden, then upload our configuration afterwards
    // in any case: safe the values to allow re-uploading when deaktivation the patch
    while (size > i + 2) {
        // first byte: type
        // second byte: len (if len=0, then val=0)
        // following bytes: value (length: len)
        uint8_t type = *(tlv + i);
        uint8_t len  = *(tlv + i + 1);
        uint8_t *valbp = tlv + i + 2;
        uint8_t firstval = len ? *valbp : 0;
        i += 2 + len;

        switch(type) {
            case CFG_TYPE_ATQA:
                needUpload = true;
                origValues.atqa = firstval;
                LOGD("NfcSetConfig Read: ATQA 0x%02x", firstval);
            break;
            case CFG_TYPE_SAK:
                needUpload = true;
                origValues.sak = firstval;
                LOGD("NfcSetConfig Read: SAK  0x%02x", firstval);
            break;
            case CFG_TYPE_HIST:
                needUpload = true;
                if(len > sizeof(origValues.hist)) {
                    LOGE("cannot handle an hist with len=0x%02x", len);
                } else {
                    memcpy(origValues.hist, valbp, len);
                    origValues.uid_len = len;
                    loghex("NfcSetConfig Read: HIST", valbp, len);
                }
            break;
            case CFG_TYPE_UID:
                needUpload = true;
                if(len > sizeof(origValues.uid)) {
                    LOGE("cannot handle an uid with len=0x%02x", len);
                } else {
                    memcpy(origValues.uid, valbp, len);
                    origValues.uid_len = len;
                    loghex("NfcSetConfig Read: UID", valbp, len);
                }
            break;
        }
    }

    tNFC_STATUS r = nci_NfcSetConfig(size, tlv);

    if(needUpload && patchEnabled) {
        // any of our values got modified and we are active -> reupload
        uploadPatchConfig();
    }

    return r;
}

/**
 * write a single config value into a new configuration stream.
 * see uploadConfig()
 */
static void pushcfg(uint8_t *cfg, uint8_t &i, uint8_t type, uint8_t value) {
    cfg[i++] = type;
    if(value) {
      cfg[i++] = 1; // len
      cfg[i++] = value;
    } else {
      cfg[i++] = 0;
    }
}

/**
 * build a new configuration stream and upload it into the broadcom nfc controller
 */
static void uploadConfig(const struct s_chip_config config) {
    // cfg: type1, paramlen1, param1, type2, paramlen2....
    uint8_t cfg[80];
    uint8_t i=0;
    pushcfg(cfg, i, CFG_TYPE_SAK,  config.sak);
    //pushcfg(cfg, i, CFG_TYPE_HIST, config.hist);
    pushcfg(cfg, i, CFG_TYPE_ATQA, config.atqa);

    cfg[i++] = CFG_TYPE_UID;
    cfg[i++] = config.uid_len;

    memcpy(cfg+i, config.uid, config.uid_len);
    i += config.uid_len;

    cfg[i++] = CFG_TYPE_HIST;
    cfg[i++] = config.hist_len;
    memcpy(cfg+i, config.hist, config.hist_len);
    i += config.hist_len;

    nci_NfcSetConfig(i, cfg);
    loghex("HOOKNFC Upload:", cfg, i);
}

void disablePolling() {
    LOG("HOOKNFC disable polling");
    hook_NfaDisablePolling();
    hook_NfcDeactivate(0);
}

void enablePolling() {
    LOG("HOOKNFC enablePolling()");
    hook_NfcDeactivate(3);
    hook_NfaStartRfDiscovery();
    hook_NfaEnablePolling(0xff);
}

/**
 * upload the values we got from the ipc
 */
void uploadPatchConfig() {
    uploadConfig(patchValues);
}

/**
 * upload the values we collected in  NfcSetConfig
 */
void uploadOriginalConfig() {
    uploadConfig(origValues);
}
