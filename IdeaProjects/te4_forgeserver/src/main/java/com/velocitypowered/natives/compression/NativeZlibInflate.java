package com.velocitypowered.natives.compression;

import plus.fserver.api.Keep;
import java.util.zip.DataFormatException;

/**
 * Represents a native interface for zlib's inflate functions.
 */
@Keep
class NativeZlibInflate {
    static native long init();
    static native long free(long ctx);

    static native boolean process(long ctx, long sourceAddress, int sourceLength,
        long destinationAddress, int destinationLength) throws DataFormatException;
}