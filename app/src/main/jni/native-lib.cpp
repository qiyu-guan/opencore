#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_opencore_app_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("OpenCore Native Engine v13.0");
}
