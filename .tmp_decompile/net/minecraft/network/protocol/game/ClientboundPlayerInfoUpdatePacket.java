/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.PropertyMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public class ClientboundPlayerInfoUpdatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new);
    private final EnumSet<Action> actions;
    private final List<Entry> entries;

    public ClientboundPlayerInfoUpdatePacket(EnumSet<Action> enumSet, Collection<ServerPlayer> collection) {
        this.actions = enumSet;
        this.entries = collection.stream().map(Entry::new).toList();
    }

    public ClientboundPlayerInfoUpdatePacket(Action action, ServerPlayer serverPlayer) {
        this.actions = EnumSet.of(action);
        this.entries = List.of((Object)((Object)new Entry(serverPlayer)));
    }

    public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> collection) {
        EnumSet<Action[]> enumSet = EnumSet.of(Action.ADD_PLAYER, new Action[]{Action.INITIALIZE_CHAT, Action.UPDATE_GAME_MODE, Action.UPDATE_LISTED, Action.UPDATE_LATENCY, Action.UPDATE_DISPLAY_NAME, Action.UPDATE_HAT, Action.UPDATE_LIST_ORDER});
        return new ClientboundPlayerInfoUpdatePacket(enumSet, collection);
    }

    private ClientboundPlayerInfoUpdatePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.actions = registryFriendlyByteBuf.readEnumSet(Action.class);
        this.entries = registryFriendlyByteBuf.readList(friendlyByteBuf -> {
            EntryBuilder entryBuilder = new EntryBuilder(friendlyByteBuf.readUUID());
            for (Action action : this.actions) {
                action.reader.read(entryBuilder, (RegistryFriendlyByteBuf)((Object)friendlyByteBuf));
            }
            return entryBuilder.build();
        });
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeEnumSet(this.actions, Action.class);
        registryFriendlyByteBuf.writeCollection(this.entries, (friendlyByteBuf, entry) -> {
            friendlyByteBuf.writeUUID(entry.profileId());
            for (Action action : this.actions) {
                action.writer.write((RegistryFriendlyByteBuf)((Object)friendlyByteBuf), (Entry)((Object)entry));
            }
        });
    }

    @Override
    public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerInfoUpdate(this);
    }

    public EnumSet<Action> actions() {
        return this.actions;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public List<Entry> newEntries() {
        return this.actions.contains((Object)Action.ADD_PLAYER) ? this.entries : List.of();
    }

    public String toString() {
        return MoreObjects.toStringHelper((Object)this).add("actions", this.actions).add("entries", this.entries).toString();
    }

    public static final class Entry
    extends Record {
        private final UUID profileId;
        private final @Nullable GameProfile profile;
        private final boolean listed;
        private final int latency;
        private final GameType gameMode;
        private final @Nullable Component displayName;
        final boolean showHat;
        final int listOrder;
        final @Nullable RemoteChatSession.Data chatSession;

        Entry(ServerPlayer serverPlayer) {
            this(serverPlayer.getUUID(), serverPlayer.getGameProfile(), true, serverPlayer.connection.latency(), serverPlayer.gameMode(), serverPlayer.getTabListDisplayName(), serverPlayer.isModelPartShown(PlayerModelPart.HAT), serverPlayer.getTabListOrder(), Optionull.map(serverPlayer.getChatSession(), RemoteChatSession::asData));
        }

        public Entry(UUID uUID, @Nullable GameProfile gameProfile, boolean bl, int i, GameType gameType, @Nullable Component component, boolean bl2, int j, @Nullable RemoteChatSession.Data data) {
            this.profileId = uUID;
            this.profile = gameProfile;
            this.listed = bl;
            this.latency = i;
            this.gameMode = gameType;
            this.displayName = component;
            this.showHat = bl2;
            this.listOrder = j;
            this.chatSession = data;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "profileId;profile;listed;latency;gameMode;displayName;showHat;listOrder;chatSession", "profileId", "profile", "listed", "latency", "gameMode", "displayName", "showHat", "listOrder", "chatSession"}, this, object);
        }

        public UUID profileId() {
            return this.profileId;
        }

        public @Nullable GameProfile profile() {
            return this.profile;
        }

        public boolean listed() {
            return this.listed;
        }

        public int latency() {
            return this.latency;
        }

        public GameType gameMode() {
            return this.gameMode;
        }

        public @Nullable Component displayName() {
            return this.displayName;
        }

        public boolean showHat() {
            return this.showHat;
        }

        public int listOrder() {
            return this.listOrder;
        }

        public @Nullable RemoteChatSession.Data chatSession() {
            return this.chatSession;
        }
    }

    public static enum Action {
        ADD_PLAYER((entryBuilder, registryFriendlyByteBuf) -> {
            String string = (String)ByteBufCodecs.PLAYER_NAME.decode(registryFriendlyByteBuf);
            PropertyMap propertyMap = (PropertyMap)ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode(registryFriendlyByteBuf);
            entryBuilder.profile = new GameProfile(entryBuilder.profileId, string, propertyMap);
        }, (registryFriendlyByteBuf, entry) -> {
            GameProfile gameProfile = Objects.requireNonNull(entry.profile());
            ByteBufCodecs.PLAYER_NAME.encode(registryFriendlyByteBuf, gameProfile.name());
            ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(registryFriendlyByteBuf, gameProfile.properties());
        }),
        INITIALIZE_CHAT((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.chatSession = registryFriendlyByteBuf.readNullable(RemoteChatSession.Data::read);
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeNullable(entry.chatSession, RemoteChatSession.Data::write)),
        UPDATE_GAME_MODE((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.gameMode = GameType.byId(registryFriendlyByteBuf.readVarInt());
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.gameMode().getId())),
        UPDATE_LISTED((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.listed = registryFriendlyByteBuf.readBoolean();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeBoolean(entry.listed())),
        UPDATE_LATENCY((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.latency = registryFriendlyByteBuf.readVarInt();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.latency())),
        UPDATE_DISPLAY_NAME((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.displayName = FriendlyByteBuf.readNullable(registryFriendlyByteBuf, ComponentSerialization.TRUSTED_STREAM_CODEC);
        }, (registryFriendlyByteBuf, entry) -> FriendlyByteBuf.writeNullable(registryFriendlyByteBuf, entry.displayName(), ComponentSerialization.TRUSTED_STREAM_CODEC)),
        UPDATE_LIST_ORDER((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.listOrder = registryFriendlyByteBuf.readVarInt();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.listOrder)),
        UPDATE_HAT((entryBuilder, registryFriendlyByteBuf) -> {
            entryBuilder.showHat = registryFriendlyByteBuf.readBoolean();
        }, (registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeBoolean(entry.showHat));

        final Reader reader;
        final Writer writer;

        private Action(Reader reader, Writer writer) {
            this.reader = reader;
            this.writer = writer;
        }

        public static interface Reader {
            public void read(EntryBuilder var1, RegistryFriendlyByteBuf var2);
        }

        public static interface Writer {
            public void write(RegistryFriendlyByteBuf var1, Entry var2);
        }
    }

    static class EntryBuilder {
        final UUID profileId;
        @Nullable GameProfile profile;
        boolean listed;
        int latency;
        GameType gameMode = GameType.DEFAULT_MODE;
        @Nullable Component displayName;
        boolean showHat;
        int listOrder;
        @Nullable RemoteChatSession.Data chatSession;

        EntryBuilder(UUID uUID) {
            this.profileId = uUID;
        }

        Entry build() {
            return new Entry(this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.showHat, this.listOrder, this.chatSession);
        }
    }
}

