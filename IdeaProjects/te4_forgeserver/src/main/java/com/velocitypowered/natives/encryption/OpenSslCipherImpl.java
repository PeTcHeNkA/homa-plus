package com.velocitypowered.natives.encryption;

import plus.fserver.api.Keep;
import java.security.GeneralSecurityException;


@Keep
class OpenSslCipherImpl {
    static native long init(byte[] key, boolean encrypt) throws GeneralSecurityException;

    static native void process(long ctx, long source, int len, long dest);

    static native void free(long ptr);
}