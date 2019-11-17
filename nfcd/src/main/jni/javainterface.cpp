#include <string.h>
#include "nfcd.h"

extern "C" {
    JNIEXPORT jboolean JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_isEnabled(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_setEnabled(JNIEnv* env, jobject javaThis, jboolean enabled);
    JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyte atqa, jbyte sak, jbyteArray _hist, jbyteArray _uid);
    JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_disablePolling(JNIEnv* env, jobject javaThis);
    JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_enablePolling(JNIEnv* env, jobject javaThis);
}


extern "C" JNIEXPORT jboolean JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_isEnabled(JNIEnv* env, jobject javaThis) {
    return patchEnabled;
}

extern "C" JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_disablePolling(JNIEnv* env, jobject javaThis) {
    disablePolling();
}

extern "C" JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_enablePolling(JNIEnv* env, jobject javaThis) {
    enablePolling();
}

extern "C" JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_setEnabled(JNIEnv* env, jobject javaThis, jboolean enabled) {
    patchEnabled = enabled;
    if(enabled) {
        uploadPatchConfig();
    } else {
        uploadOriginalConfig();
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_mateuszstarczyk_nfcopy_xposed_Native_uploadConfiguration(JNIEnv* env, jobject javaThis, jbyte atqa, jbyte sak, jbyteArray _hist, jbyteArray _uid) {
    jsize uid_len = env->GetArrayLength(_uid);
    jsize hist_len = env->GetArrayLength(_hist);
    if(uid_len > sizeof(s_chip_config().uid) || hist_len > sizeof(s_chip_config().hist)) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception, "uid or hist bigger than buffer");
    }


    patchValues.atqa = atqa;
    patchValues.sak = sak;
    patchValues.hist_len = hist_len;
    patchValues.uid_len = uid_len;

    jbyte* uid = env->GetByteArrayElements(_uid, 0);
    jbyte* hist = env->GetByteArrayElements(_hist, 0);

    memcpy(patchValues.uid, uid, uid_len);
    memcpy(patchValues.hist, hist, hist_len);

    env->ReleaseByteArrayElements(_uid, uid, 0);
    env->ReleaseByteArrayElements(_hist, hist, 0);
}