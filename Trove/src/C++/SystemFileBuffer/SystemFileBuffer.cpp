#include "SystemFileBuffer.h"

JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_open
  (JNIEnv* env, jclass clazz, jstring path, jboolean readOnly)
{
    jint length = env->GetStringLength(path);
    jchar* fullPath = new jchar[length + 5];
    fullPath[0] = L'\\';
    fullPath[1] = L'\\';
    fullPath[2] = L'?';
    fullPath[3] = L'\\';
    env->GetStringRegion(path, 0, length, fullPath + 4);
    fullPath[4 + length] = L'\0';

    HANDLE handle;
	if (readOnly == JNI_TRUE) {
        handle = CreateFileW(fullPath, GENERIC_READ,
							 FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	}
	else {
		handle = CreateFileW(fullPath, GENERIC_READ | GENERIC_WRITE,
							 FILE_SHARE_READ, NULL, OPEN_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
	}

    delete [] fullPath;

    if (handle == INVALID_HANDLE_VALUE) {
        ThrowNewIOException(env, GetLastError());
    }

    return (jlong)handle;
}

JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_openMapping
  (JNIEnv* env, jclass clazz, jlong handle, jint mode, jlong size)
{
    DWORD protect;
    switch (mode) {
    default:
    case 1:
        protect = PAGE_READONLY;
        break;
    case 2:
        protect = PAGE_READWRITE;
        break;
    case 3:
        protect = PAGE_WRITECOPY;
        break;
    }

    DWORD high = (DWORD)(size >> 32);
    DWORD low = (DWORD)size;

    HANDLE mapping =
        CreateFileMapping((HANDLE)handle, NULL, protect, high, low, NULL);

    if (mapping == INVALID_HANDLE_VALUE) {
        ThrowNewIOException(env, GetLastError());
    }

    return (jlong)mapping;
}

JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_read__JJ_3BII
  (JNIEnv* env, jobject obj, jlong handle,
   jlong position, jbyteArray dst, jint offset, jint length)
{
    DWORD amt;

    OVERLAPPED overlapped = {0};
    overlapped.Offset = (jint)position;
    overlapped.OffsetHigh = (jint)(position >> 32);

    jbyte* buffer = (jbyte*)env->GetPrimitiveArrayCritical(dst, NULL);

    if (!ReadFile((HANDLE)handle, buffer + offset, length, &amt, &overlapped)) {
        DWORD errorCode = GetLastError();
        if (errorCode == ERROR_HANDLE_EOF) {
            LONG posHigh = (LONG)(position >> 32);
            DWORD posLow = SetFilePointer((HANDLE)handle, (LONG)position, &posHigh, FILE_BEGIN);

            if (posLow == 0xffffffff && GetLastError() != NO_ERROR) {
                ThrowNewIOException(env, GetLastError());
            }
            else if (!ReadFile((HANDLE)handle, buffer + offset, length, &amt, NULL)) {
                ThrowNewIOException(env, GetLastError());
            }
        }
        else {
            ThrowNewIOException(env, errorCode);
        }
    }

    env->ReleasePrimitiveArrayCritical(dst, buffer, 0);

    return (amt == 0) ? -1 : amt;
}

JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_write__JJ_3BII
  (JNIEnv* env, jobject obj, jlong handle,
   jlong position, jbyteArray src, jint offset, jint length)
{
    DWORD amt;

    OVERLAPPED overlapped = {0};
    overlapped.Offset = (jint)position;
    overlapped.OffsetHigh = (jint)(position >> 32);

    jbyte* buffer = (jbyte*)env->GetPrimitiveArrayCritical(src, NULL);

    if (!WriteFile((HANDLE)handle, buffer + offset, length, &amt, &overlapped)) {
        ThrowNewIOException(env, GetLastError());
    }

    env->ReleasePrimitiveArrayCritical(src, buffer, JNI_ABORT);

    return amt;
}

JNIEXPORT jint JNICALL Java_com_go_trove_file_SystemFileBuffer_read__JJ
  (JNIEnv* env, jobject obj, jlong handle, jlong position)
{
    DWORD amt;

    OVERLAPPED overlapped = {0};
    overlapped.Offset = (jint)position;
    overlapped.OffsetHigh = (jint)(position >> 32);

    unsigned char value;

    if (!ReadFile((HANDLE)handle, &value, 1, &amt, &overlapped)) {
        DWORD errorCode = GetLastError();
        if (errorCode == ERROR_HANDLE_EOF) {
            LONG posHigh = (LONG)(position >> 32);
            DWORD posLow = SetFilePointer((HANDLE)handle, (LONG)position, &posHigh, FILE_BEGIN);

            if (posLow == 0xffffffff && GetLastError() != NO_ERROR) {
                ThrowNewIOException(env, GetLastError());
            }
            else if (!ReadFile((HANDLE)handle, &value, 1, &amt, NULL)) {
                ThrowNewIOException(env, GetLastError());
            }
        }
        else {
            ThrowNewIOException(env, errorCode);
        }
    }

    return (amt < 1) ? -1 : (jint)value;
}

JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_write__JJI
  (JNIEnv* env, jobject obj, jlong handle, jlong position, jint value)
{
    DWORD amt;

    OVERLAPPED overlapped = {0};
    overlapped.Offset = (jint)position;
    overlapped.OffsetHigh = (jint)(position >> 32);

    unsigned char byteValue = (unsigned char)value;

    if (!WriteFile((HANDLE)handle, &byteValue, 1, &amt, &overlapped)) {
        ThrowNewIOException(env, GetLastError());
    }
}

JNIEXPORT jlong JNICALL Java_com_go_trove_file_SystemFileBuffer_size
  (JNIEnv* env, jobject obj, jlong handle)
{
    DWORD sizeHigh;
    DWORD sizeLow = GetFileSize((HANDLE)handle, &sizeHigh);

    if (sizeLow == 0xffffffff && GetLastError() != NO_ERROR) {
        ThrowNewIOException(env, GetLastError());
    }

    return ((jlong)sizeHigh) << 32 | sizeLow;
}

JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_truncate
  (JNIEnv* env, jobject obj, jlong handle, jlong size)
{
    if (size >= Java_com_go_trove_file_SystemFileBuffer_size(env, obj, handle)) {
        return;
    }

    LONG sizeHigh = (LONG)(size >> 32);
    DWORD sizeLow = SetFilePointer((HANDLE)handle, (LONG)size, &sizeHigh, FILE_BEGIN);

    if (sizeLow == 0xffffffff && GetLastError() != NO_ERROR) {
        ThrowNewIOException(env, GetLastError());
    }
    else if (!SetEndOfFile((HANDLE)handle)) {
        ThrowNewIOException(env, GetLastError());
    }
}

JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_force
  (JNIEnv* env, jobject obj, jlong handle, jboolean metaData)
{
    // TODO: How to implement flushing of metaData?
    if (!FlushFileBuffers((HANDLE)handle)) {
        ThrowNewIOException(env, GetLastError());
    }
}

JNIEXPORT void JNICALL Java_com_go_trove_file_SystemFileBuffer_close
  (JNIEnv* env, jobject obj, jlong handle)
{
    if (!CloseHandle((HANDLE)handle)) {
        ThrowNewIOException(env, GetLastError());
    }
}

JNIEXPORT jlong JNICALL Java_com_go_trove_file_MappedFileBuffer_open
  (JNIEnv* env, jclass clazz, jlong handle, jint mode, jlong position, jint size)
{
    DWORD access;
    switch (mode) {
    default:
    case 1:
        access = FILE_MAP_READ;
        break;
    case 2:
        access = FILE_MAP_WRITE;
        break;
    case 3:
        access = FILE_MAP_COPY;
        break;
    }

    DWORD high = (DWORD)(position >> 32);
    DWORD low = (DWORD)position;

    void* addr = MapViewOfFile((HANDLE)handle, access, high, low, size);

    if (addr == NULL) {
        ThrowNewIOException(env, GetLastError());
    }

    return (jlong)addr;
}

JNIEXPORT jint JNICALL Java_com_go_trove_file_MappedFileBuffer_read
  (JNIEnv* env, jobject obj, jbyteArray dst, jint offset, jint length, jlong addr)
{
    jbyte* buffer = (jbyte*)env->GetPrimitiveArrayCritical(dst, NULL);
    memcpy(buffer + offset, (void*)addr, length);
    env->ReleasePrimitiveArrayCritical(dst, buffer, 0);
    return length;
}

JNIEXPORT jint JNICALL Java_com_go_trove_file_MappedFileBuffer_write
  (JNIEnv* env, jobject obj, jbyteArray src, jint offset, jint length, jlong addr)
{
    jbyte* buffer = (jbyte*)env->GetPrimitiveArrayCritical(src, NULL);
    memcpy((void*)addr, buffer + offset, length);
    env->ReleasePrimitiveArrayCritical(src, buffer, JNI_ABORT);
    return length;
}

JNIEXPORT jint JNICALL Java_briano_nio_MappedFileBuffer_read0
  (JNIEnv* env, jobject obj, jlong addr)
{
    return (jint)(*((unsigned char*)addr));
}

JNIEXPORT void JNICALL Java_com_go_trove_file_MappedFileBuffer_write0
  (JNIEnv* env, jobject obj, jlong addr, jint value)
{
    *((unsigned char*)addr) = (unsigned char)value;
}

JNIEXPORT void JNICALL Java_com_go_trove_file_MappedFileBuffer_force
  (JNIEnv* env, jobject obj, jlong addr, jint size)
{
    if (!FlushViewOfFile((void*)addr, size)) {
        ThrowNewIOException(env, GetLastError());
    }
}

jint ThrowNew(JNIEnv* env, jclass clazz, DWORD errorCode) {
    if (errorCode == ERROR_INVALID_HANDLE) {
        return env->ThrowNew(clazz, "FileBuffer closed");
    }

    DWORD length;
	LPTSTR buffer = NULL;

	length = FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | 
						   FORMAT_MESSAGE_FROM_SYSTEM |
						   FORMAT_MESSAGE_ARGUMENT_ARRAY,
						   NULL,
						   errorCode,
						   LANG_NEUTRAL,
						   (LPTSTR)&buffer,
						   0,
						   NULL);
    jint result = -1;
    
    if (buffer != NULL) {
		// Remove trailing linefeed characters:
		while (length > 0) {
			char c = buffer[length - 1];
			if (c == '\r' || c == '\n') {
				buffer[--length] = '\0';
			}
			else {
				break;
			}
		}

        result = env->ThrowNew(clazz, buffer);
        LocalFree(buffer);
	}

    return result;
}

jint ThrowNewIOException(JNIEnv* env, DWORD errorCode) {
    return ThrowNew(env, env->FindClass("java/io/IOException"), errorCode);
}

