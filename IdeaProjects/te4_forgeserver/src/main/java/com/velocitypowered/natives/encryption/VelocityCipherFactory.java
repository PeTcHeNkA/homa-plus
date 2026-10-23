package com.velocitypowered.natives.encryption;

import plus.fserver.api.Keep;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

/**
 * A factory interface for {@link VelocityCipher}.
 */
@Keep
public interface VelocityCipherFactory {
    VelocityCipher forEncryption(SecretKey key) throws GeneralSecurityException;
    VelocityCipher forDecryption(SecretKey key) throws GeneralSecurityException;
}