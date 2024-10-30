package com.velocitypowered.natives;

import com.velocitypowered.natives.util.BufferPreference;

/**
 * Generic interface for any Velocity native.
 */
public interface Native {
    BufferPreference preferredBufferType();
}