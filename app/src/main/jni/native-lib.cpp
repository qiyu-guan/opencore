#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_opencore_app_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    std::string msg = "OpenCore v1.0 | Native Engine Active | 53 Core Advantages";
    return env->NewStringUTF(msg.c_str());
}
