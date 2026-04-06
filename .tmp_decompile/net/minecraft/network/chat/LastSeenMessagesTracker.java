/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;
import org.jspecify.annotations.Nullable;

public class LastSeenMessagesTracker {
    private final @Nullable LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    private @Nullable MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int i) {
        this.trackedMessages = new LastSeenTrackedEntry[i];
    }

    public boolean addPending(MessageSignature messageSignature, boolean bl) {
        if (Objects.equals((Object)messageSignature, (Object)this.lastTrackedMessage)) {
            return false;
        }
        this.lastTrackedMessage = messageSignature;
        this.addEntry(bl ? new LastSeenTrackedEntry(messageSignature, true) : null);
        return true;
    }

    private void addEntry(@Nullable LastSeenTrackedEntry lastSeenTrackedEntry) {
        int i = this.tail;
        this.tail = (i + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[i] = lastSeenTrackedEntry;
    }

    public void ignorePending(MessageSignature messageSignature) {
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[i];
            if (lastSeenTrackedEntry == null || !lastSeenTrackedEntry.pending() || !messageSignature.equals((Object)lastSeenTrackedEntry.signature())) continue;
            this.trackedMessages[i] = null;
            break;
        }
    }

    public int getAndClearOffset() {
        int i = this.offset;
        this.offset = 0;
        return i;
    }

    public Update generateAndApplyUpdate() {
        int i = this.getAndClearOffset();
        BitSet bitSet = new BitSet(this.trackedMessages.length);
        ObjectArrayList objectList = new ObjectArrayList(this.trackedMessages.length);
        for (int j = 0; j < this.trackedMessages.length; ++j) {
            int k = (this.tail + j) % this.trackedMessages.length;
            LastSeenTrackedEntry lastSeenTrackedEntry = this.trackedMessages[k];
            if (lastSeenTrackedEntry == null) continue;
            bitSet.set(j, true);
            objectList.add((Object)lastSeenTrackedEntry.signature());
            this.trackedMessages[k] = lastSeenTrackedEntry.acknowledge();
        }
        LastSeenMessages lastSeenMessages = new LastSeenMessages((List<MessageSignature>)objectList);
        LastSeenMessages.Update update = new LastSeenMessages.Update(i, bitSet, lastSeenMessages.computeChecksum());
        return new Update(lastSeenMessages, update);
    }

    public int offset() {
        return this.offset;
    }

    public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}

