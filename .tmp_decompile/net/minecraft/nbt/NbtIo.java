/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.util.DelegateDataOutput;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtIo {
    private static final OpenOption[] SYNC_OUTPUT_OPTIONS = new OpenOption[]{StandardOpenOption.SYNC, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING};

    public static CompoundTag readCompressed(Path path, NbtAccounter nbtAccounter) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            CompoundTag compoundTag;
            try (FastBufferedInputStream inputStream2 = new FastBufferedInputStream(inputStream);){
                compoundTag = NbtIo.readCompressed(inputStream2, nbtAccounter);
            }
            return compoundTag;
        }
    }

    private static DataInputStream createDecompressorStream(InputStream inputStream) throws IOException {
        return new DataInputStream(new FastBufferedInputStream(new GZIPInputStream(inputStream)));
    }

    private static DataOutputStream createCompressorStream(OutputStream outputStream) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(outputStream)));
    }

    public static CompoundTag readCompressed(InputStream inputStream, NbtAccounter nbtAccounter) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.createDecompressorStream(inputStream);){
            CompoundTag compoundTag = NbtIo.read(dataInputStream, nbtAccounter);
            return compoundTag;
        }
    }

    public static void parseCompressed(Path path, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);
             FastBufferedInputStream inputStream2 = new FastBufferedInputStream(inputStream);){
            NbtIo.parseCompressed(inputStream2, streamTagVisitor, nbtAccounter);
        }
    }

    public static void parseCompressed(InputStream inputStream, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
        try (DataInputStream dataInputStream = NbtIo.createDecompressorStream(inputStream);){
            NbtIo.parse(dataInputStream, streamTagVisitor, nbtAccounter);
        }
    }

    public static void writeCompressed(CompoundTag compoundTag, Path path) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path, SYNC_OUTPUT_OPTIONS);
             BufferedOutputStream outputStream2 = new BufferedOutputStream(outputStream);){
            NbtIo.writeCompressed(compoundTag, outputStream2);
        }
    }

    public static void writeCompressed(CompoundTag compoundTag, OutputStream outputStream) throws IOException {
        try (DataOutputStream dataOutputStream = NbtIo.createCompressorStream(outputStream);){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    public static void write(CompoundTag compoundTag, Path path) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(path, SYNC_OUTPUT_OPTIONS);
             BufferedOutputStream outputStream2 = new BufferedOutputStream(outputStream);
             DataOutputStream dataOutputStream = new DataOutputStream(outputStream2);){
            NbtIo.write(compoundTag, dataOutputStream);
        }
    }

    public static @Nullable CompoundTag read(Path path) throws IOException {
        if (!Files.exists(path, new LinkOption[0])) {
            return null;
        }
        try (InputStream inputStream = Files.newInputStream(path, new OpenOption[0]);){
            CompoundTag compoundTag;
            try (DataInputStream dataInputStream = new DataInputStream(inputStream);){
                compoundTag = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
            }
            return compoundTag;
        }
    }

    public static CompoundTag read(DataInput dataInput) throws IOException {
        return NbtIo.read(dataInput, NbtAccounter.unlimitedHeap());
    }

    public static CompoundTag read(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        Tag tag = NbtIo.readUnnamedTag(dataInput, nbtAccounter);
        if (tag instanceof CompoundTag) {
            return (CompoundTag)tag;
        }
        throw new IOException("Root tag must be a named compound tag");
    }

    public static void write(CompoundTag compoundTag, DataOutput dataOutput) throws IOException {
        NbtIo.writeUnnamedTagWithFallback(compoundTag, dataOutput);
    }

    public static void parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
        TagType<?> tagType = TagTypes.getType(dataInput.readByte());
        if (tagType == EndTag.TYPE) {
            if (streamTagVisitor.visitRootEntry(EndTag.TYPE) == StreamTagVisitor.ValueResult.CONTINUE) {
                streamTagVisitor.visitEnd();
            }
            return;
        }
        switch (streamTagVisitor.visitRootEntry(tagType)) {
            case HALT: {
                break;
            }
            case BREAK: {
                StringTag.skipString(dataInput);
                tagType.skip(dataInput, nbtAccounter);
                break;
            }
            case CONTINUE: {
                StringTag.skipString(dataInput);
                tagType.parse(dataInput, streamTagVisitor, nbtAccounter);
            }
        }
    }

    public static Tag readAnyTag(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        byte b = dataInput.readByte();
        if (b == 0) {
            return EndTag.INSTANCE;
        }
        return NbtIo.readTagSafe(dataInput, nbtAccounter, b);
    }

    public static void writeAnyTag(Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        tag.write(dataOutput);
    }

    public static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        dataOutput.writeUTF("");
        tag.write(dataOutput);
    }

    public static void writeUnnamedTagWithFallback(Tag tag, DataOutput dataOutput) throws IOException {
        NbtIo.writeUnnamedTag(tag, new StringFallbackDataOutput(dataOutput));
    }

    @VisibleForTesting
    public static Tag readUnnamedTag(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        byte b = dataInput.readByte();
        if (b == 0) {
            return EndTag.INSTANCE;
        }
        StringTag.skipString(dataInput);
        return NbtIo.readTagSafe(dataInput, nbtAccounter, b);
    }

    private static Tag readTagSafe(DataInput dataInput, NbtAccounter nbtAccounter, byte b) {
        try {
            return TagTypes.getType(b).load(dataInput, nbtAccounter);
        }
        catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag type", b);
            throw new ReportedNbtException(crashReport);
        }
    }

    public static class StringFallbackDataOutput
    extends DelegateDataOutput {
        public StringFallbackDataOutput(DataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void writeUTF(String string) throws IOException {
            try {
                super.writeUTF(string);
            }
            catch (UTFDataFormatException uTFDataFormatException) {
                Util.logAndPauseIfInIde("Failed to write NBT String", uTFDataFormatException);
                super.writeUTF("");
            }
        }
    }
}

