/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.saveddata.maps;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.saveddata.maps.MapBanner;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapFrame;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MapItemSavedData
extends SavedData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAP_SIZE = 128;
    private static final int HALF_MAP_SIZE = 64;
    public static final int MAX_SCALE = 4;
    public static final int TRACKED_DECORATION_LIMIT = 256;
    private static final String FRAME_PREFIX = "frame-";
    public static final Codec<MapItemSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(mapItemSavedData -> mapItemSavedData.dimension), (App)Codec.INT.fieldOf("xCenter").forGetter(mapItemSavedData -> mapItemSavedData.centerX), (App)Codec.INT.fieldOf("zCenter").forGetter(mapItemSavedData -> mapItemSavedData.centerZ), (App)Codec.BYTE.optionalFieldOf("scale", (Object)0).forGetter(mapItemSavedData -> mapItemSavedData.scale), (App)Codec.BYTE_BUFFER.fieldOf("colors").forGetter(mapItemSavedData -> ByteBuffer.wrap(mapItemSavedData.colors)), (App)Codec.BOOL.optionalFieldOf("trackingPosition", (Object)true).forGetter(mapItemSavedData -> mapItemSavedData.trackingPosition), (App)Codec.BOOL.optionalFieldOf("unlimitedTracking", (Object)false).forGetter(mapItemSavedData -> mapItemSavedData.unlimitedTracking), (App)Codec.BOOL.optionalFieldOf("locked", (Object)false).forGetter(mapItemSavedData -> mapItemSavedData.locked), (App)MapBanner.CODEC.listOf().optionalFieldOf("banners", (Object)List.of()).forGetter(mapItemSavedData -> List.copyOf(mapItemSavedData.bannerMarkers.values())), (App)MapFrame.CODEC.listOf().optionalFieldOf("frames", (Object)List.of()).forGetter(mapItemSavedData -> List.copyOf(mapItemSavedData.frameMarkers.values()))).apply((Applicative)instance, MapItemSavedData::new));
    public final int centerX;
    public final int centerZ;
    public final ResourceKey<Level> dimension;
    private final boolean trackingPosition;
    private final boolean unlimitedTracking;
    public final byte scale;
    public byte[] colors = new byte[16384];
    public final boolean locked;
    private final List<HoldingPlayer> carriedBy = Lists.newArrayList();
    private final Map<Player, HoldingPlayer> carriedByPlayers = Maps.newHashMap();
    private final Map<String, MapBanner> bannerMarkers = Maps.newHashMap();
    final Map<String, MapDecoration> decorations = Maps.newLinkedHashMap();
    private final Map<String, MapFrame> frameMarkers = Maps.newHashMap();
    private int trackedDecorationCount;

    public static SavedDataType<MapItemSavedData> type(MapId mapId) {
        return new SavedDataType<MapItemSavedData>(mapId.key(), () -> {
            throw new IllegalStateException("Should never create an empty map saved data");
        }, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    private MapItemSavedData(int i, int j, byte b, boolean bl, boolean bl2, boolean bl3, ResourceKey<Level> resourceKey) {
        this.scale = b;
        this.centerX = i;
        this.centerZ = j;
        this.dimension = resourceKey;
        this.trackingPosition = bl;
        this.unlimitedTracking = bl2;
        this.locked = bl3;
    }

    private MapItemSavedData(ResourceKey<Level> resourceKey, int i, int j, byte b, ByteBuffer byteBuffer, boolean bl, boolean bl2, boolean bl3, List<MapBanner> list, List<MapFrame> list2) {
        this(i, j, (byte)Mth.clamp(b, 0, 4), bl, bl2, bl3, resourceKey);
        if (byteBuffer.array().length == 16384) {
            this.colors = byteBuffer.array();
        }
        for (MapBanner mapBanner : list) {
            this.bannerMarkers.put(mapBanner.getId(), mapBanner);
            this.addDecoration(mapBanner.getDecoration(), null, mapBanner.getId(), mapBanner.pos().getX(), mapBanner.pos().getZ(), 180.0, mapBanner.name().orElse(null));
        }
        for (MapFrame mapFrame : list2) {
            this.frameMarkers.put(mapFrame.getId(), mapFrame);
            this.addDecoration(MapDecorationTypes.FRAME, null, MapItemSavedData.getFrameKey(mapFrame.entityId()), mapFrame.pos().getX(), mapFrame.pos().getZ(), mapFrame.rotation(), null);
        }
    }

    public static MapItemSavedData createFresh(double d, double e, byte b, boolean bl, boolean bl2, ResourceKey<Level> resourceKey) {
        int i = 128 * (1 << b);
        int j = Mth.floor((d + 64.0) / (double)i);
        int k = Mth.floor((e + 64.0) / (double)i);
        int l = j * i + i / 2 - 64;
        int m = k * i + i / 2 - 64;
        return new MapItemSavedData(l, m, b, bl, bl2, false, resourceKey);
    }

    public static MapItemSavedData createForClient(byte b, boolean bl, ResourceKey<Level> resourceKey) {
        return new MapItemSavedData(0, 0, b, false, false, bl, resourceKey);
    }

    public MapItemSavedData locked() {
        MapItemSavedData mapItemSavedData = new MapItemSavedData(this.centerX, this.centerZ, this.scale, this.trackingPosition, this.unlimitedTracking, true, this.dimension);
        mapItemSavedData.bannerMarkers.putAll(this.bannerMarkers);
        mapItemSavedData.decorations.putAll(this.decorations);
        mapItemSavedData.trackedDecorationCount = this.trackedDecorationCount;
        System.arraycopy(this.colors, 0, mapItemSavedData.colors, 0, this.colors.length);
        return mapItemSavedData;
    }

    public MapItemSavedData scaled() {
        return MapItemSavedData.createFresh(this.centerX, this.centerZ, (byte)Mth.clamp(this.scale + 1, 0, 4), this.trackingPosition, this.unlimitedTracking, this.dimension);
    }

    private static Predicate<ItemStack> mapMatcher(ItemStack itemStack) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        return itemStack2 -> {
            if (itemStack2 == itemStack) {
                return true;
            }
            return itemStack2.is(itemStack.getItem()) && Objects.equals(mapId, itemStack2.get(DataComponents.MAP_ID));
        };
    }

    public void tickCarriedBy(Player player, ItemStack itemStack) {
        if (!this.carriedByPlayers.containsKey(player)) {
            HoldingPlayer holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        Predicate<ItemStack> predicate = MapItemSavedData.mapMatcher(itemStack);
        if (!player.getInventory().contains(predicate)) {
            this.removeDecoration(player.getPlainTextName());
        }
        for (int i = 0; i < this.carriedBy.size(); ++i) {
            HoldingPlayer holdingPlayer2 = this.carriedBy.get(i);
            Player player2 = holdingPlayer2.player;
            String string2 = player2.getPlainTextName();
            if (player2.isRemoved() || !player2.getInventory().contains(predicate) && !itemStack.isFramed()) {
                this.carriedByPlayers.remove(player2);
                this.carriedBy.remove(holdingPlayer2);
                this.removeDecoration(string2);
            } else if (!itemStack.isFramed() && player2.level().dimension() == this.dimension && this.trackingPosition) {
                this.addDecoration(MapDecorationTypes.PLAYER, player2.level(), string2, player2.getX(), player2.getZ(), player2.getYRot(), null);
            }
            if (player2.equals(player) || !MapItemSavedData.hasMapInvisibilityItemEquipped(player2)) continue;
            this.removeDecoration(string2);
        }
        if (itemStack.isFramed() && this.trackingPosition) {
            ItemFrame itemFrame = itemStack.getFrame();
            BlockPos blockPos = itemFrame.getPos();
            MapFrame mapFrame = this.frameMarkers.get(MapFrame.frameId(blockPos));
            if (mapFrame != null && itemFrame.getId() != mapFrame.entityId() && this.frameMarkers.containsKey(mapFrame.getId())) {
                this.removeDecoration(MapItemSavedData.getFrameKey(mapFrame.entityId()));
            }
            MapFrame mapFrame2 = new MapFrame(blockPos, itemFrame.getDirection().get2DDataValue() * 90, itemFrame.getId());
            this.addDecoration(MapDecorationTypes.FRAME, player.level(), MapItemSavedData.getFrameKey(itemFrame.getId()), blockPos.getX(), blockPos.getZ(), itemFrame.getDirection().get2DDataValue() * 90, null);
            MapFrame mapFrame3 = this.frameMarkers.put(mapFrame2.getId(), mapFrame2);
            if (!mapFrame2.equals((Object)mapFrame3)) {
                this.setDirty();
            }
        }
        MapDecorations mapDecorations = itemStack.getOrDefault(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY);
        if (!this.decorations.keySet().containsAll(mapDecorations.decorations().keySet())) {
            mapDecorations.decorations().forEach((string, entry) -> {
                if (!this.decorations.containsKey(string)) {
                    this.addDecoration(entry.type(), player.level(), (String)string, entry.x(), entry.z(), entry.rotation(), null);
                }
            });
        }
    }

    private static boolean hasMapInvisibilityItemEquipped(Player player) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (equipmentSlot == EquipmentSlot.MAINHAND || equipmentSlot == EquipmentSlot.OFFHAND || !player.getItemBySlot(equipmentSlot).is(ItemTags.MAP_INVISIBILITY_EQUIPMENT)) continue;
            return true;
        }
        return false;
    }

    private void removeDecoration(String string) {
        MapDecoration mapDecoration = this.decorations.remove(string);
        if (mapDecoration != null && mapDecoration.type().value().trackCount()) {
            --this.trackedDecorationCount;
        }
        this.setDecorationsDirty();
    }

    public static void addTargetDecoration(ItemStack itemStack, BlockPos blockPos, String string, Holder<MapDecorationType> holder) {
        MapDecorations.Entry entry = new MapDecorations.Entry(holder, blockPos.getX(), blockPos.getZ(), 180.0f);
        itemStack.update(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY, mapDecorations -> mapDecorations.withDecoration(string, entry));
        if (holder.value().hasMapColor()) {
            itemStack.set(DataComponents.MAP_COLOR, new MapItemColor(holder.value().mapColor()));
        }
    }

    private void addDecoration(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, String string, double d, double e, double f, @Nullable Component component) {
        MapDecoration mapDecoration2;
        int i = 1 << this.scale;
        float g = (float)(d - (double)this.centerX) / (float)i;
        float h = (float)(e - (double)this.centerZ) / (float)i;
        MapDecorationLocation mapDecorationLocation = this.calculateDecorationLocationAndType(holder, levelAccessor, f, g, h);
        if (mapDecorationLocation == null) {
            this.removeDecoration(string);
            return;
        }
        MapDecoration mapDecoration = new MapDecoration(mapDecorationLocation.type(), mapDecorationLocation.x(), mapDecorationLocation.y(), mapDecorationLocation.rot(), Optional.ofNullable(component));
        if (!mapDecoration.equals((Object)(mapDecoration2 = this.decorations.put(string, mapDecoration)))) {
            if (mapDecoration2 != null && mapDecoration2.type().value().trackCount()) {
                --this.trackedDecorationCount;
            }
            if (mapDecorationLocation.type().value().trackCount()) {
                ++this.trackedDecorationCount;
            }
            this.setDecorationsDirty();
        }
    }

    private @Nullable MapDecorationLocation calculateDecorationLocationAndType(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float g) {
        byte b = MapItemSavedData.clampMapCoordinate(f);
        byte c = MapItemSavedData.clampMapCoordinate(g);
        if (holder.is(MapDecorationTypes.PLAYER)) {
            Pair<Holder<MapDecorationType>, Byte> pair = this.playerDecorationTypeAndRotation(holder, levelAccessor, d, f, g);
            return pair == null ? null : new MapDecorationLocation((Holder)pair.getFirst(), b, c, (Byte)pair.getSecond());
        }
        if (MapItemSavedData.isInsideMap(f, g) || this.unlimitedTracking) {
            return new MapDecorationLocation(holder, b, c, this.calculateRotation(levelAccessor, d));
        }
        return null;
    }

    private @Nullable Pair<Holder<MapDecorationType>, Byte> playerDecorationTypeAndRotation(Holder<MapDecorationType> holder, @Nullable LevelAccessor levelAccessor, double d, float f, float g) {
        if (MapItemSavedData.isInsideMap(f, g)) {
            return Pair.of(holder, (Object)this.calculateRotation(levelAccessor, d));
        }
        Holder<MapDecorationType> holder2 = this.decorationTypeForPlayerOutsideMap(f, g);
        if (holder2 == null) {
            return null;
        }
        return Pair.of(holder2, (Object)0);
    }

    private byte calculateRotation(@Nullable LevelAccessor levelAccessor, double d) {
        if (this.dimension == Level.NETHER && levelAccessor != null) {
            int i = (int)(levelAccessor.getGameTime() / 10L);
            return (byte)(i * i * 34187121 + i * 121 >> 15 & 0xF);
        }
        double e = d < 0.0 ? d - 8.0 : d + 8.0;
        return (byte)(e * 16.0 / 360.0);
    }

    private static boolean isInsideMap(float f, float g) {
        int i = 63;
        return f >= -63.0f && g >= -63.0f && f <= 63.0f && g <= 63.0f;
    }

    private @Nullable Holder<MapDecorationType> decorationTypeForPlayerOutsideMap(float f, float g) {
        boolean bl;
        int i = 320;
        boolean bl2 = bl = Math.abs(f) < 320.0f && Math.abs(g) < 320.0f;
        if (bl) {
            return MapDecorationTypes.PLAYER_OFF_MAP;
        }
        return this.unlimitedTracking ? MapDecorationTypes.PLAYER_OFF_LIMITS : null;
    }

    private static byte clampMapCoordinate(float f) {
        int i = 63;
        if (f <= -63.0f) {
            return -128;
        }
        if (f >= 63.0f) {
            return 127;
        }
        return (byte)((double)(f * 2.0f) + 0.5);
    }

    public @Nullable Packet<?> getUpdatePacket(MapId mapId, Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            return null;
        }
        return holdingPlayer.nextUpdatePacket(mapId);
    }

    private void setColorsDirty(int i, int j) {
        this.setDirty();
        for (HoldingPlayer holdingPlayer : this.carriedBy) {
            holdingPlayer.markColorsDirty(i, j);
        }
    }

    private void setDecorationsDirty() {
        this.carriedBy.forEach(HoldingPlayer::markDecorationsDirty);
    }

    public HoldingPlayer getHoldingPlayer(Player player) {
        HoldingPlayer holdingPlayer = this.carriedByPlayers.get(player);
        if (holdingPlayer == null) {
            holdingPlayer = new HoldingPlayer(player);
            this.carriedByPlayers.put(player, holdingPlayer);
            this.carriedBy.add(holdingPlayer);
        }
        return holdingPlayer;
    }

    public boolean toggleBanner(LevelAccessor levelAccessor, BlockPos blockPos) {
        double d = (double)blockPos.getX() + 0.5;
        double e = (double)blockPos.getZ() + 0.5;
        int i = 1 << this.scale;
        double f = (d - (double)this.centerX) / (double)i;
        double g = (e - (double)this.centerZ) / (double)i;
        int j = 63;
        if (f >= -63.0 && g >= -63.0 && f <= 63.0 && g <= 63.0) {
            MapBanner mapBanner = MapBanner.fromWorld(levelAccessor, blockPos);
            if (mapBanner == null) {
                return false;
            }
            if (this.bannerMarkers.remove(mapBanner.getId(), (Object)mapBanner)) {
                this.removeDecoration(mapBanner.getId());
                this.setDirty();
                return true;
            }
            if (!this.isTrackedCountOverLimit(256)) {
                this.bannerMarkers.put(mapBanner.getId(), mapBanner);
                this.addDecoration(mapBanner.getDecoration(), levelAccessor, mapBanner.getId(), d, e, 180.0, mapBanner.name().orElse(null));
                this.setDirty();
                return true;
            }
        }
        return false;
    }

    public void checkBanners(BlockGetter blockGetter, int i, int j) {
        Iterator<MapBanner> iterator = this.bannerMarkers.values().iterator();
        while (iterator.hasNext()) {
            MapBanner mapBanner2;
            MapBanner mapBanner = iterator.next();
            if (mapBanner.pos().getX() != i || mapBanner.pos().getZ() != j || mapBanner.equals((Object)(mapBanner2 = MapBanner.fromWorld(blockGetter, mapBanner.pos())))) continue;
            iterator.remove();
            this.removeDecoration(mapBanner.getId());
            this.setDirty();
        }
    }

    public Collection<MapBanner> getBanners() {
        return this.bannerMarkers.values();
    }

    public void removedFromFrame(BlockPos blockPos, int i) {
        this.removeDecoration(MapItemSavedData.getFrameKey(i));
        this.frameMarkers.remove(MapFrame.frameId(blockPos));
        this.setDirty();
    }

    public boolean updateColor(int i, int j, byte b) {
        byte c = this.colors[i + j * 128];
        if (c != b) {
            this.setColor(i, j, b);
            return true;
        }
        return false;
    }

    public void setColor(int i, int j, byte b) {
        this.colors[i + j * 128] = b;
        this.setColorsDirty(i, j);
    }

    public boolean isExplorationMap() {
        for (MapDecoration mapDecoration : this.decorations.values()) {
            if (!mapDecoration.type().value().explorationMapElement()) continue;
            return true;
        }
        return false;
    }

    public void addClientSideDecorations(List<MapDecoration> list) {
        this.decorations.clear();
        this.trackedDecorationCount = 0;
        for (int i = 0; i < list.size(); ++i) {
            MapDecoration mapDecoration = list.get(i);
            this.decorations.put("icon-" + i, mapDecoration);
            if (!mapDecoration.type().value().trackCount()) continue;
            ++this.trackedDecorationCount;
        }
    }

    public Iterable<MapDecoration> getDecorations() {
        return this.decorations.values();
    }

    public boolean isTrackedCountOverLimit(int i) {
        return this.trackedDecorationCount >= i;
    }

    private static String getFrameKey(int i) {
        return FRAME_PREFIX + i;
    }

    public class HoldingPlayer {
        public final Player player;
        private boolean dirtyData = true;
        private int minDirtyX;
        private int minDirtyY;
        private int maxDirtyX = 127;
        private int maxDirtyY = 127;
        private boolean dirtyDecorations = true;
        private int tick;
        public int step;

        HoldingPlayer(Player player) {
            this.player = player;
        }

        private MapPatch createPatch() {
            int i = this.minDirtyX;
            int j = this.minDirtyY;
            int k = this.maxDirtyX + 1 - this.minDirtyX;
            int l = this.maxDirtyY + 1 - this.minDirtyY;
            byte[] bs = new byte[k * l];
            for (int m = 0; m < k; ++m) {
                for (int n = 0; n < l; ++n) {
                    bs[m + n * k] = MapItemSavedData.this.colors[i + m + (j + n) * 128];
                }
            }
            return new MapPatch(i, j, k, l, bs);
        }

        @Nullable Packet<?> nextUpdatePacket(MapId mapId) {
            Collection<MapDecoration> collection;
            MapPatch mapPatch;
            if (this.dirtyData) {
                this.dirtyData = false;
                mapPatch = this.createPatch();
            } else {
                mapPatch = null;
            }
            if (this.dirtyDecorations && this.tick++ % 5 == 0) {
                this.dirtyDecorations = false;
                collection = MapItemSavedData.this.decorations.values();
            } else {
                collection = null;
            }
            if (collection != null || mapPatch != null) {
                return new ClientboundMapItemDataPacket(mapId, MapItemSavedData.this.scale, MapItemSavedData.this.locked, collection, mapPatch);
            }
            return null;
        }

        void markColorsDirty(int i, int j) {
            if (this.dirtyData) {
                this.minDirtyX = Math.min(this.minDirtyX, i);
                this.minDirtyY = Math.min(this.minDirtyY, j);
                this.maxDirtyX = Math.max(this.maxDirtyX, i);
                this.maxDirtyY = Math.max(this.maxDirtyY, j);
            } else {
                this.dirtyData = true;
                this.minDirtyX = i;
                this.minDirtyY = j;
                this.maxDirtyX = i;
                this.maxDirtyY = j;
            }
        }

        private void markDecorationsDirty() {
            this.dirtyDecorations = true;
        }
    }

    record MapDecorationLocation(Holder<MapDecorationType> type, byte x, byte y, byte rot) {
    }

    public record MapPatch(int startX, int startY, int width, int height, byte[] mapColors) {
        public static final StreamCodec<ByteBuf, Optional<MapPatch>> STREAM_CODEC = StreamCodec.of(MapPatch::write, MapPatch::read);

        private static void write(ByteBuf byteBuf, Optional<MapPatch> optional) {
            if (optional.isPresent()) {
                MapPatch mapPatch = optional.get();
                byteBuf.writeByte(mapPatch.width);
                byteBuf.writeByte(mapPatch.height);
                byteBuf.writeByte(mapPatch.startX);
                byteBuf.writeByte(mapPatch.startY);
                FriendlyByteBuf.writeByteArray(byteBuf, mapPatch.mapColors);
            } else {
                byteBuf.writeByte(0);
            }
        }

        private static Optional<MapPatch> read(ByteBuf byteBuf) {
            short i = byteBuf.readUnsignedByte();
            if (i > 0) {
                short j = byteBuf.readUnsignedByte();
                short k = byteBuf.readUnsignedByte();
                short l = byteBuf.readUnsignedByte();
                byte[] bs = FriendlyByteBuf.readByteArray(byteBuf);
                return Optional.of(new MapPatch(k, l, i, j, bs));
            }
            return Optional.empty();
        }

        public void applyToMap(MapItemSavedData mapItemSavedData) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    mapItemSavedData.setColor(this.startX + i, this.startY + j, this.mapColors[i + j * this.width]);
                }
            }
        }
    }
}

