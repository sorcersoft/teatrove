/* ====================================================================
 * Trove - Copyright (c) 1997-2001 Walt Disney Internet Group
 * ====================================================================
 * The Tea Software License, Version 1.1
 *
 * Copyright (c) 2000 Walt Disney Internet Group. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Walt Disney Internet Group (http://opensource.go.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Tea", "TeaServlet", "Kettle", "Trove" and "BeanDoc" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact opensource@dig.com.
 *
 * 5. Products derived from this software may not be called "Tea",
 *    "TeaServlet", "Kettle" or "Trove", nor may "Tea", "TeaServlet",
 *    "Kettle", "Trove" or "BeanDoc" appear in their name, without prior
 *    written permission of the Walt Disney Internet Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE WALT DISNEY INTERNET GROUP OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 *
 * For more information about Tea, please see http://opensource.go.com/.
 */

#include <malloc.h>
#include <zlib.h>
#include "com_go_trove_util_Deflater.h"

static jfieldID mStrategyID;
static jfieldID mLevelID;
static jfieldID mSetParamsID;
static jfieldID mFinishedID;
static jfieldID mInputOffsetID;
static jfieldID mInputLengthID;

void throwOutOfMemoryError(JNIEnv* env, char* msg) {
	jclass clazz = env->FindClass("java/lang/OutOfMemoryError");
	env->ThrowNew(clazz, msg);
}

void throwIllegalArgumentException(JNIEnv* env, char* msg) {
	jclass clazz = env->FindClass("java/lang/IllegalArgumentException");
	env->ThrowNew(clazz, msg);
}

void throwInternalError(JNIEnv* env, char* msg) {
	jclass clazz = env->FindClass("java/lang/InternalError");
	env->ThrowNew(clazz, msg);
}

JNIEXPORT void JNICALL Java_com_go_trove_util_Deflater_initIDs
  (JNIEnv* env, jclass clazz)
{
	mStrategyID = env->GetFieldID(clazz, "mStrategy", "I");
	mLevelID = env->GetFieldID(clazz, "mLevel", "I");
	mSetParamsID = env->GetFieldID(clazz, "mSetParams", "Z");
	mFinishedID = env->GetFieldID(clazz, "mFinished", "Z");
	mInputOffsetID = env->GetFieldID(clazz, "mInputOffset", "I");
	mInputLengthID = env->GetFieldID(clazz, "mInputLength", "I");
}

JNIEXPORT jlong JNICALL Java_com_go_trove_util_Deflater_init
  (JNIEnv* env, jobject obj, jint strategy, jint level, jboolean nowrap)
{
    z_streamp zstrm = (z_streamp)calloc(1, sizeof(z_stream));

    if (zstrm == 0) {
		throwOutOfMemoryError(env, NULL);
		return (jlong)0;
    }

	int result = deflateInit2(zstrm, level, Z_DEFLATED,
							  nowrap ? -MAX_WBITS : MAX_WBITS,
							  8, strategy);

	switch (result) {
	case Z_OK:
		return (jlong)zstrm;

	case Z_MEM_ERROR:
		free(zstrm);
		throwOutOfMemoryError(env, NULL);
		return (jlong)0;

	case Z_STREAM_ERROR:
		free(zstrm);
		throwIllegalArgumentException(env, NULL);
		return (jlong)0;
	
	default:
		char* msg = zstrm->msg;
		free(zstrm);
		throwInternalError(env, msg);
		return (jlong)0;
	}
}

JNIEXPORT void JNICALL Java_com_go_trove_util_Deflater_setDictionary
  (JNIEnv* env, jobject obj, jlong strm, jbyteArray b, jint off, jint len)
{
	z_streamp zstrm = (z_streamp)strm;

	Bytef* dict = (Bytef*)env->GetPrimitiveArrayCritical(b, NULL);
	if (dict == NULL) {
		throwOutOfMemoryError(env, NULL);
		return;
	}

	int result = deflateSetDictionary(zstrm, dict, len);

	env->ReleasePrimitiveArrayCritical(b, dict, JNI_ABORT);

	switch (result) {
		case Z_OK:
		return;

	case Z_STREAM_ERROR:
		throwIllegalArgumentException(env, NULL);
		return;
	
	default:
		throwInternalError(env, NULL);
		return;
	}
}

JNIEXPORT jint JNICALL Java_com_go_trove_util_Deflater_deflate
  (JNIEnv* env, jobject obj, jlong strm, jint flushOpt, jboolean setParams,
   jbyteArray inBuf, jint inOff, jint inLen,
   jbyteArray outBuf, jint outOff, jint outLen)
{
	if (outBuf == NULL) {
		return 0;
	}

	z_streamp zstrm = (z_streamp)strm;

	Bytef* in;
	if (inBuf == NULL) {
		in = NULL;
		inOff = 0;
		inLen = 0;
	}
	else {
		in = (Bytef*)env->GetPrimitiveArrayCritical(inBuf, NULL);
		if (in == NULL) {
			throwOutOfMemoryError(env, NULL);
			return 0;
		}
	}

	Bytef* out = (Bytef*)env->GetPrimitiveArrayCritical(outBuf, NULL);
	if (out == NULL) {
		throwOutOfMemoryError(env, NULL);
		return 0;
	}

	zstrm->next_in = in + inOff;
	zstrm->avail_in = inLen;
	zstrm->next_out = out + outOff;
	zstrm->avail_out = outLen;

	int result;
	if (setParams) {
		int level = env->GetIntField(obj, mLevelID);
		int strategy = env->GetIntField(obj, mStrategyID);
		env->SetBooleanField(obj, mSetParamsID, JNI_FALSE);
		result = deflateParams(zstrm, level, strategy);
	}
	else {
		result = deflate(zstrm, flushOpt);
	}

	env->ReleasePrimitiveArrayCritical(inBuf, in, JNI_ABORT);
	env->ReleasePrimitiveArrayCritical(outBuf, out, 0);

	switch (result) {
	case Z_STREAM_END:
		env->SetBooleanField(obj, mFinishedID, JNI_TRUE);
	case Z_OK:
		env->SetIntField(obj, mInputOffsetID, inOff + inLen - zstrm->avail_in);
		env->SetIntField(obj, mInputLengthID, zstrm->avail_in);
		break;

	case Z_BUF_ERROR:
		return 0;

	default:
		throwInternalError(env, NULL);
	}

	return outLen - zstrm->avail_out;
}

JNIEXPORT jint JNICALL Java_com_go_trove_util_Deflater_getAdler
  (JNIEnv* env, jobject obj, jlong strm)
{
	return (jint)((z_streamp)strm)->adler;
}

JNIEXPORT jint JNICALL Java_com_go_trove_util_Deflater_getTotalIn
  (JNIEnv* env, jobject obj, jlong strm)
{
	return (jint)((z_streamp)strm)->total_in;
}

JNIEXPORT jint JNICALL Java_com_go_trove_util_Deflater_getTotalOut
  (JNIEnv* env, jobject obj, jlong strm)
{
	return (jint)((z_streamp)strm)->total_out;
}

JNIEXPORT void JNICALL Java_com_go_trove_util_Deflater_reset
  (JNIEnv* env, jobject obj, jlong strm)
{
	deflateReset((z_streamp)strm);
}

JNIEXPORT void JNICALL Java_com_go_trove_util_Deflater_end
  (JNIEnv* env, jobject obj, jlong strm)
{

	deflateEnd((z_streamp)strm);
}

