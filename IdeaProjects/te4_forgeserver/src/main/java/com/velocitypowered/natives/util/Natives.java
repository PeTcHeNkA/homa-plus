package com.velocitypowered.natives.util;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.natives.compression.JavaVelocityCompressor;
import com.velocitypowered.natives.compression.LibdeflateVelocityCompressor;
import com.velocitypowered.natives.compression.VelocityCompressorFactory;
import com.velocitypowered.natives.encryption.JavaVelocityCipher;
import com.velocitypowered.natives.encryption.NativeVelocityCipher;
import com.velocitypowered.natives.encryption.VelocityCipherFactory;
import plus.fserver.ElcaConfig;
import plus.fserver.api.Keep;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


@Keep
public class Natives {
  private Natives() {}

  private static Runnable copyAndLoadNative(String path) {
    return () -> {
      try {
        InputStream nativeLib = Natives.class.getResourceAsStream(path);
        if (nativeLib == null) {
          throw new IllegalStateException("Native library " + path + " not found.");
        }

        Path tempFile = createTemporaryNativeFilename(path.substring(path.lastIndexOf('.')));
        Files.copy(nativeLib, tempFile, StandardCopyOption.REPLACE_EXISTING);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          try {
            Files.deleteIfExists(tempFile);
          } catch (IOException ignored) {
            // Well, it doesn't matter...
          }
        }));

        try {
          System.load(tempFile.toAbsolutePath().toString());
        } catch (UnsatisfiedLinkError e) {
          throw new RuntimeException("Unable to load native " + tempFile.toAbsolutePath(), e);
        }
      } catch (IOException e) {
        throw new RuntimeException("Unable to copy natives", e);
      }
    };
  }


  private static Path createTemporaryNativeFilename(String ext) throws IOException {
      String temporaryFolderPath = System.getProperty("velocity.natives-tmpdir");
      if (temporaryFolderPath != null) {
          return Files.createTempFile(temporaryFolderPath, "native-" + ext);
      } else {
          return File.createTempFile("velocity-natives", "native-" + ext, ElcaConfig.NATIVES).toPath();
      }
  }


  public static final NativeCodeLoader<VelocityCompressorFactory> compress = new NativeCodeLoader<>(
          ImmutableList.of(
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_X86_64,
                          copyAndLoadNative("/linux_x86_64/velocity-compress.so"),
                          "libdeflate (Linux x86_64)",
                          LibdeflateVelocityCompressor.FACTORY), // compiled with Debian 10
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_AARCH64,
                          copyAndLoadNative("/linux_aarch64/velocity-compress.so"),
                          "libdeflate (Linux aarch64)",
                          LibdeflateVelocityCompressor.FACTORY), // compiled with Fedora 36
                  new NativeCodeLoader.Variant<>(NativeConstraints.MACOS_AARCH64,
                          copyAndLoadNative("/macos_arm64/velocity-compress.dylib"),
                          "libdeflate (macOS ARM64 / Apple Silicon)",
                          LibdeflateVelocityCompressor.FACTORY),
                  new NativeCodeLoader.Variant<>(NativeCodeLoader.ALWAYS, () -> {
                  }, "Java", JavaVelocityCompressor.FACTORY)
          )
  );


  public static final NativeCodeLoader<VelocityCipherFactory> cipher = new NativeCodeLoader<>(
          ImmutableList.of(
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_X86_64,
                          copyAndLoadNative("/linux_x86_64/velocity-cipher.so"), // Any local version
                          "OpenSSL local (Linux x86_64)", NativeVelocityCipher.FACTORY),
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_X86_64,
                          copyAndLoadNative("/linux_x86_64/velocity-cipher-ossl30x.so"), // Debian "Bookworm"
                          "OpenSSL 3.0.x (Linux x86_64)", NativeVelocityCipher.FACTORY),
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_X86_64,
                          copyAndLoadNative("/linux_x86_64/velocity-cipher-ossl11x.so"), // Debian 9
                          "OpenSSL 1.1.x (Linux x86_64)", NativeVelocityCipher.FACTORY),
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_X86_64,
                          copyAndLoadNative("/linux_x86_64/velocity-cipher-ossl10x.so"), // CentOS 7
                          "OpenSSL 1.0.x (Linux x86_64)", NativeVelocityCipher.FACTORY),

                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_AARCH64,
                          copyAndLoadNative("/linux_aarch64/velocity-cipher.so"),
                          "OpenSSL (Linux aarch64)", NativeVelocityCipher.FACTORY), // Any local version
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_AARCH64,
                          copyAndLoadNative("/linux_aarch64/velocity-cipher-ossl30x.so"),
                          "OpenSSL (Linux aarch64)", NativeVelocityCipher.FACTORY), // Fedora 36
                  new NativeCodeLoader.Variant<>(NativeConstraints.LINUX_AARCH64,
                          copyAndLoadNative("/linux_aarch64/velocity-cipher-ossl11x.so"),
                          "OpenSSL 1.1.x (Linux aarch64)", NativeVelocityCipher.FACTORY), // Debian 11

                  new NativeCodeLoader.Variant<>(NativeConstraints.MACOS_AARCH64,
                          copyAndLoadNative("/macos_arm64/velocity-cipher.dylib"),
                          "native (macOS ARM64 / Apple Silicon)",
                          NativeVelocityCipher.FACTORY),

                  new NativeCodeLoader.Variant<>(NativeCodeLoader.ALWAYS, () -> {
                  }, "Java", JavaVelocityCipher.FACTORY)
          )
  );
}