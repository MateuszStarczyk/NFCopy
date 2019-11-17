/*
 *  Collin's Binary Instrumentation Tool/Framework for Android
 *  Collin Mulliner <collin[at]mulliner.org>
 *
 *  (c) 2012,2013
 *
 *  License: LGPL v2.1
 *
 */

#include <android/log.h>
#define LOG(...) __android_log_print(ANDROID_LOG_DEBUG, "ADBI", __VA_ARGS__);

struct hook_t {
	unsigned int jump[3];
	unsigned int store[3];
	unsigned char jumpt[20];
	unsigned char storet[20];
	unsigned int orig;
	unsigned int patch;
	unsigned char thumb;
	unsigned char name[128];
	void *data;
};

extern "C" void hook_cacheflush(unsigned int begin, unsigned int end);
extern "C" void hook_precall(struct hook_t *h);
extern "C" void hook_postcall(struct hook_t *h);
extern "C" int hook(struct hook_t *h, unsigned int addr, void *hookf);
extern "C" void unhook(struct hook_t *h);
