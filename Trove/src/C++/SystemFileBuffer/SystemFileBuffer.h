#ifndef _SystemFileBuffer_
#define _SystemFileBuffer_

#include <windows.h>
#include "com_go_trove_file_SystemFileBuffer.h"
#include "com_go_trove_file_MappedFileBuffer.h"

// Throw an exception with an error description for GetLastError.
jint ThrowNew(JNIEnv* env, jclass clazz, DWORD errorCode);

// Throw a java.io.IOException with an error description for GetLastError.
jint ThrowNewIOException(JNIEnv* env, DWORD errorCode);

#endif