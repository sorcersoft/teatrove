/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class com_go_trove_file_SystemFileBuffer */

#ifndef _Included_com_go_trove_file_SystemFileBuffer
#define _Included_com_go_trove_file_SystemFileBuffer
#ifdef __cplusplus
extern "C" {
#endif
#undef com_go_trove_file_SystemFileBuffer_MAP_RO
#define com_go_trove_file_SystemFileBuffer_MAP_RO 1L
#undef com_go_trove_file_SystemFileBuffer_MAP_RW
#define com_go_trove_file_SystemFileBuffer_MAP_RW 2L
#undef com_go_trove_file_SystemFileBuffer_MAP_COW
#define com_go_trove_file_SystemFileBuffer_MAP_COW 3L
/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    open
 * Signature: (Ljava/lang/String;Z)J
 */
JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_open
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    openMapping
 * Signature: (JIJ)J
 */
JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_openMapping
  (JNIEnv *, jclass, jlong, jint, jlong);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    read
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_read__JJ_3BII
  (JNIEnv *, jobject, jlong, jlong, jbyteArray, jint, jint);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    write
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_write__JJ_3BII
  (JNIEnv *, jobject, jlong, jlong, jbyteArray, jint, jint);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    read
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_read__JJ
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    write
 * Signature: (JJI)V
 */
JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_write__JJI
  (JNIEnv *, jobject, jlong, jlong, jint);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    size
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_size
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    truncate
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_truncate
  (JNIEnv *, jobject, jlong, jlong);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    force
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_force
  (JNIEnv *, jobject, jlong, jboolean);

/*
 * Class:     com_go_trove_file_SystemFileBuffer
 * Method:    close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_close
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
