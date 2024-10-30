package com.velocitypowered.natives.compression;

import plus.fserver.api.Keep;


/**
 * Represents a native interface for zlib's deflate functions.
 */
@Keep
class NativeZlibDeflate {
    static native long init(int level);

    static native long free(long ctx);

    static native int process(long ctx, long sourceAddress, int sourceLength, long destinationAddress,
        int destinationLength);
}