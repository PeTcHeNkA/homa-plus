package com.velocitypowered.natives.compression;

import com.google.common.base.Preconditions;
import com.velocitypowered.natives.util.BufferPreference;
import io.netty.buffer.ByteBuf;
import plus.fserver.api.Keep;
import java.util.zip.DataFormatException;

/**
 * Implements deflate compression using the {@code libdeflate} native C library.
 */
public class LibdeflateVelocityCompressor implements VelocityCompressor {
  @Keep
  public static final VelocityCompressorFactory FACTORY = LibdeflateVelocityCompressor::new;

  private final long inflateCtx;
  private final long deflateCtx;
  private boolean disposed = false;

  private LibdeflateVelocityCompressor(int level) {
    int correctedLevel = level == -1 ? 6 : level;
    if (correctedLevel > 12 || correctedLevel < 1) {
      throw new IllegalArgumentException("Invalid compression level " + level);
    }

    this.inflateCtx = NativeZlibInflate.init();
    this.deflateCtx = NativeZlibDeflate.init(correctedLevel);
  }

  @Override
  public void inflate(ByteBuf source, ByteBuf destination, int uncompressedSize)
      throws DataFormatException {
    ensureNotDisposed();

    // libdeflate recommends we work with a known uncompressed size - so we work strictly within
    // those parameters. If the uncompressed size doesn't match the compressed size, then we will
    // throw an exception from native code.
    destination.ensureWritable(uncompressedSize);

    long sourceAddress = source.memoryAddress() + source.readerIndex();
    long destinationAddress = destination.memoryAddress() + destination.writerIndex();

    NativeZlibInflate.process(inflateCtx, sourceAddress, source.readableBytes(), destinationAddress,
        uncompressedSize);
    destination.writerIndex(destination.writerIndex() + uncompressedSize);
  }

  @Override
  public void deflate(ByteBuf source, ByteBuf destination) throws DataFormatException {
    ensureNotDisposed();

    while (true) {
      long sourceAddress = source.memoryAddress() + source.readerIndex();
      long destinationAddress = destination.memoryAddress() + destination.writerIndex();

      int produced = NativeZlibDeflate.process(deflateCtx, sourceAddress, source.readableBytes(),
          destinationAddress, destination.writableBytes());
      if (produced > 0) {
        destination.writerIndex(destination.writerIndex() + produced);
        break;
      } else if (produced == 0) {
        // Insufficient room - enlarge the buffer.
        destination.capacity(destination.capacity() * 2);
      } else {
        throw new DataFormatException("libdeflate returned unknown code " + produced);
      }
    }
  }

  private void ensureNotDisposed() {
    Preconditions.checkState(!disposed, "Object already disposed");
  }

  @Override
  public void close() {
    if (!disposed) {
      NativeZlibInflate.free(inflateCtx);
      NativeZlibDeflate.free(deflateCtx);
    }
    disposed = true;
  }

  @Override
  public BufferPreference preferredBufferType() {
    return BufferPreference.DIRECT_REQUIRED;
  }
}
