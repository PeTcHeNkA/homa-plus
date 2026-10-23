package com.velocitypowered.natives.compression;

import plus.fserver.api.Keep;

/**
 * Factory for {@link VelocityCompressor}.
 */
@Keep
public interface VelocityCompressorFactory {
    VelocityCompressor create(int level);
}