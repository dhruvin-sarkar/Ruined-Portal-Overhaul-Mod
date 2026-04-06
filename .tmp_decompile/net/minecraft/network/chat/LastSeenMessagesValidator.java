/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;
import org.jspecify.annotations.Nullable;

public class LastSeenMessagesValidator {
    private final int lastSeenCount;
    private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList();
    private @Nullable MessageSignature lastPendingMessage;

    public LastSeenMessagesValidator(int i) {
        this.lastSeenCount = i;
        for (int j = 0; j < i; ++j) {
            this.trackedMessages.add(null);
        }
    }

    public void addPending(MessageSignature messageSignature) {
        if (!messageSignature.equals((Object)this.lastPendingMessage)) {
            this.trackedMessages.add((Object)new LastSeenTrackedEntry(messageSignature, true));
            this.lastPendingMessage = messageSignature;
        }
    }

    public int trackedMessagesCount() {
        return this.trackedMessages.size();
    }

    public void applyOffset(int i) throws ValidationException {
        int j = this.trackedMessages.size() - this.lastSeenCount;
        if (i < 0 || i > j) {
            throw new ValidationException("Advanced last seen window by " + i + " messages, but expected at most " + j);
        }
        this.trackedMessages.removeElements(0, i);
    }

    public LastSeenMessages applyUpdate(LastSeenMessages.Update update) throws ValidationException {
        this.applyOffset(update.offset());
        ObjectArrayList objectList = new ObjectArrayList(update.acknowledged().cardinality());
        if (update.acknowledged().length() > this.lastSeenCount) {
            throw new ValidationException("Last seen update contained " + update.acknowledged().length() + " messages, but maximum window size is " + this.lastSeenCount);
        }
        for (int i = 0; i < this.lastSeenCount; ++i) {
            boolean bl = update.acknowledged().get(i);
            LastSeenTrackedEntry lastSeenTrackedEntry = (LastSeenTrackedEntry)((Object)this.trackedMessages.get(i));
            if (bl) {
                if (lastSeenTrackedEntry == null) {
                    throw new ValidationException("Last seen update acknowledged unknown or previously ignored message at index " + i);
                }
                this.trackedMessages.set(i, (Object)lastSeenTrackedEntry.acknowledge());
                objectList.add((Object)lastSeenTrackedEntry.signature());
                continue;
            }
            if (lastSeenTrackedEntry != null && !lastSeenTrackedEntry.pending()) {
                throw new ValidationException("Last seen update ignored previously acknowledged message at index " + i + " and signature " + String.valueOf((Object)lastSeenTrackedEntry.signature()));
            }
            this.trackedMessages.set(i, null);
        }
        LastSeenMessages lastSeenMessages = new LastSeenMessages((List<MessageSignature>)objectList);
        if (!update.verifyChecksum(lastSeenMessages)) {
            throw new ValidationException("Checksum mismatch on last seen update: the client and server must have desynced");
        }
        return lastSeenMessages;
    }

    public static class ValidationException
    extends Exception {
        public ValidationException(String string) {
            super(string);
        }
    }
}

