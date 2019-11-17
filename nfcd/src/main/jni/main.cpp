
#include "dlext_namespaces.h"
#include "nfcd.h"
#include "vendor/adbi/hook.h"
#include <dlfcn.h>
#include <unistd.h>
#include <malloc.h>
//#include <dlext.h>


bool patchEnabled = false;
struct hook_t hook_config;
struct hook_t hook_rfcback;
struct hook_t hook_senddata;
struct hook_t hook_deactivate;
struct hook_t hook_nfa_stop_rf_discovery;
struct hook_t hook_nfa_disable_polling;
struct hook_t hook_nfa_start_rf_discovery;
struct hook_t hook_nfa_enable_polling;
typedef int (*fn)(void);
static void onHostEmulationLoad(JNIEnv *jni, jclass _class, void *data);
static void hookNative();
const char *hooklibfile = "/system/lib/libnfc-nci.so";
static const char *getLibPath(void) {
#ifndef __aarch64__
    return "/system/lib/";
#else
    return "/system/lib64/";
#endif
}
//static int
//callback(struct dl_phdr_info *info, size_t size, void *data)
//{
//    int j;
//
//    LOGI("name=%s (%d segments)\n", info->dlpi_name,
//           info->dlpi_phnum);
//
//    for (j = 0; j < info->dlpi_phnum; j++)
//        LOGI("\t\t header %2d: address=%10p\n", j,
//               (void *) (info->dlpi_addr + info->dlpi_phdr[j].p_vaddr));
//    return 0;
//}
//struct android_namespace_t *ns =
//        android_create_namespace(
//                "trustme",
//                "/system/lib/",
//                "/system/lib/",
//                ANDROID_NAMESPACE_TYPE_SHARED |
//                ANDROID_NAMESPACE_TYPE_ISOLATED,
//                "/system/:/data/:/vendor/:/data:/mnt/expand:/system/lib/",
//                nullptr);
//
//const android_dlextinfo dlextinfo = {
//        .flags = ANDROID_DLEXT_VALID_FLAG_BITS,//ANDROID_DLEXT_USE_NAMESPACE,
//        .library_namespace = ns,
//};

static void onModuleLoad() __attribute__((constructor));

void onModuleLoad() {
    LOGI("onModuleLoad::begin");

    hookNative();
    LOGI("onModuleLoad::end");
}


/**
 * find a native symbol and hook it
 */
static void findAndHook(struct hook_t* eph, void* handle, const char *symbol, void* hookf, void **original) {
    LOGI("HOOKNFC file handle %x", (unsigned int)handle);
    fn f = reinterpret_cast<fn>(dlsym(handle, "_Z13NFC_SetConfighPh"));
    LOGI("HOOKNFC test: (%s), %s", f, dlerror());
    *original = dlsym(handle, symbol);
    LOGI("HOOKNFC %s", dlerror());
    if(hook(eph, (unsigned int)*original, hookf) != -1) {
        LOGI("HOOKNFC hooked: %s", symbol);
    }
}

/**
 * hook into native functions of the libnfc-nci broadcom nfc driver
 */
static void hookNative() {
    //////////////////////////
//    dl_iterate_phdr(callback, NULL);
    //////////////////////////
    if(access(hooklibfile, F_OK) == -1) {
        LOGE("could not access %s to load symbols", hooklibfile);
        return;
    }

    const char *lib_path = getLibPath();

    struct android_namespace_t *ns = android_create_namespace(
            "trustme",
            lib_path,
            lib_path,
            ANDROID_NAMESPACE_TYPE_SHARED |
            ANDROID_NAMESPACE_TYPE_ISOLATED,
            "/system/:/data/:/vendor/",
            NULL);

    const android_dlextinfo dlextinfo = {
            .flags = ANDROID_DLEXT_USE_NAMESPACE,
            .library_namespace = ns,
    };


    // Access granted
    LOGI("HOOKNFC try to open libnfc-nci.so");
    void *handle = android_dlopen_ext(hooklibfile, RTLD_LOCAL | RTLD_LAZY, &dlextinfo);
//    LOGI("HOOKNFC file so %x", (unsigned int)so);
    // Access granted
//    void *handle = dlopen(hooklibfile, RTLD_LAZY);

////    disable_namespace();
//    void *handle = dlopen(hooklibfile, 0);//, &dlextinfo);
//    void *handle = dlmopen(hooklibfile, RTLD_LOCAL | RTLD_NOW, &dlextinfo);

    findAndHook(&hook_config,  handle, "NFC_SetConfig",        (void*)&hook_NfcSetConfig, (void**)&nci_orig_NfcSetConfig);
    findAndHook(&hook_rfcback, handle, "NFC_SetStaticRfCback", (void*)&hook_SetRfCback,   (void**)&nci_orig_SetRfCback);

    findAndHook(&hook_senddata, handle, "NFC_SendData", (void*)&hook_NfcSenddata, (void**)&nfc_orig_sendData);
    findAndHook(&hook_deactivate, handle, "NFC_Deactivate", (void*)&hook_NfcDeactivate, (void**)&nfc_orig_deactivate);

    findAndHook(&hook_nfa_stop_rf_discovery, handle, "NFA_StopRfDiscovery", (void*) &hook_NfaStopRfDiscovery,  (void**) &nfa_orig_stop_rf_discovery);
    findAndHook(&hook_nfa_disable_polling, handle, "NFA_DisablePolling", (void*) &hook_NfaDisablePolling, (void**) &nfa_orig_disable_polling);
    findAndHook(&hook_nfa_start_rf_discovery, handle, "NFA_StartRfDiscovery", (void*) &hook_NfaStartRfDiscovery, (void**) &nfa_orig_start_rf_discovery);
    findAndHook(&hook_nfa_enable_polling, handle, "NFA_EnablePolling", (void*) &hook_NfaEnablePolling, (void**) &nfa_orig_enable_polling);

    // find pointer to ce_t4t control structure
    ce_cb = (tCE_CB*)dlsym(handle, "ce_cb");
}

/**
 * simple logging function for byte buffers
 */
void loghex(const char *desc, const uint8_t *data, const int len) {
    int strlen = len * 3 + 1;
    char *msg = (char *) malloc((size_t) strlen);
    for (uint8_t i = 0; i < len; i++) {
        sprintf(msg + i * 3, " %02x", (unsigned int) *(data + i));
    }
    LOGI("%s%s",desc, msg);
    free(msg);
}