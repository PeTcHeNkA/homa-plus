package net.minecraft.nbt;

import java.util.zip.*;
import net.minecraft.util.*;
import net.minecraft.crash.*;
import java.io.*;
import javax.annotation.*;


public class CompressedStreamTools {
    public static NBTTagCompound readCompressed(final InputStream is) throws IOException {
        final DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(is)));
        NBTTagCompound nbttagcompound;
        try {
            nbttagcompound = read(datainputstream, NBTSizeTracker.INFINITE);
        } finally {
            datainputstream.close();
        }
        return nbttagcompound;
    }


    public static void writeCompressed(final NBTTagCompound compound, final OutputStream outputStream) throws IOException {
        final DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));
        try {
            write(compound, dataoutputstream);
        } finally {
            dataoutputstream.close();
        }
    }


    public static void safeWrite(final NBTTagCompound compound, final File fileIn) throws IOException {
        final File file1 = new File(fileIn.getAbsolutePath() + "_tmp");
        if (file1.exists()) {
            file1.delete();
        }
        write(compound, file1);
        if (fileIn.exists()) {
            fileIn.delete();
        }
        if (fileIn.exists()) {
            throw new IOException("Failed to delete " + fileIn);
        }
        file1.renameTo(fileIn);
    }


    public static NBTTagCompound read(final DataInputStream inputStream) throws IOException {
        return read(inputStream, NBTSizeTracker.INFINITE);
    }


    public static NBTTagCompound read(final DataInput input, final NBTSizeTracker accounter) throws IOException {
        final NBTBase nbtbase = read(input, 0, accounter);
        if (nbtbase instanceof NBTTagCompound) {
            return (NBTTagCompound)nbtbase;
        }
        throw new IOException("Root tag must be a named compound tag");
    }


    public static void write(final NBTTagCompound compound, final DataOutput output) throws IOException {
        writeTag(compound, output);
    }


    private static void writeTag(final NBTBase tag, final DataOutput output) throws IOException {
        output.writeByte(tag.getId());
        if (tag.getId() != 0) {
            output.writeUTF("");
            tag.write(output);
        }
    }


    private static NBTBase read(final DataInput input, final int depth, final NBTSizeTracker accounter) throws IOException {
        final byte id = input.readByte();
        accounter.read(8L);
        if (id == 0) return NBTTagEnd.INSTANCE;

        NBTSizeTracker.readUTF(accounter, input.readUTF());
        accounter.read(32L);
        try {
            return NBTTagCompound.createAndRead(id, input, depth, accounter);

        } catch (IOException ioexception) {
            final CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
            final CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
            crashreportcategory.addCrashSection("Tag type", id);
            throw new ReportedException(crashreport);
        }
    }


    public static void write(final NBTTagCompound compound, final File fileIn) throws IOException {
        final DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(fileIn));
        try {
            write(compound, dataoutputstream);
        } finally {
            dataoutputstream.close();
        }
    }


    @Nullable
    public static NBTTagCompound read(final File fileIn) throws IOException {
        if (!fileIn.exists()) return null;

        final DataInputStream datainputstream = new DataInputStream(new FileInputStream(fileIn));
        NBTTagCompound nbttagcompound;
        try {
            nbttagcompound = read(datainputstream, NBTSizeTracker.INFINITE);
        } finally {
            datainputstream.close();
        }
        return nbttagcompound;
    }
}