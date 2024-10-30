package com.velocitypowered.natives.encryption;

import com.velocitypowered.natives.Disposable;
import com.velocitypowered.natives.Native;
import io.netty.buffer.ByteBuf;
import plus.fserver.api.Keep;

/**
 * Implements an AES-CFB8 cipher that either encrypts or decrypts connection data.
 */
@Keep
public interface VelocityCipher extends Disposable, Native {

  /**
   * Encrypts the given {@link ByteBuf} in-place.
   *
   * @param source the buffer to encrypt
   */
    void process(ByteBuf source);
}