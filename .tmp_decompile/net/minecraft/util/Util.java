/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Function
 *  com.google.common.base.Ticker
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.util.concurrent.MoreExecutors
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectLists
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceImmutableList
 *  it.unimi.dsi.fastutil.objects.ReferenceList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.google.common.base.Function;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.CharPredicate;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.TracingExecutor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final TracingExecutor BACKGROUND_EXECUTOR = Util.makeExecutor("Main");
    private static final TracingExecutor IO_POOL = Util.makeIoExecutor("IO-Worker-", false);
    private static final TracingExecutor DOWNLOAD_POOL = Util.makeIoExecutor("Download-", true);
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static final int LINEAR_LOOKUP_THRESHOLD = 8;
    private static final Set<String> ALLOWED_UNTRUSTED_LINK_PROTOCOLS = Set.of((Object)"http", (Object)"https");
    public static final long NANOS_PER_MILLI = 1000000L;
    public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        public long read() {
            return timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = string -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T> Collector<T, ?, List<T>> toMutableList() {
        return Collectors.toCollection(Lists::newArrayList);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
        return property.getName((Comparable)object);
    }

    public static String makeDescriptionId(String string, @Nullable Identifier identifier) {
        if (identifier == null) {
            return string + ".unregistered_sadface";
        }
        return string + "." + identifier.getNamespace() + "." + identifier.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return Util.getNanos() / 1000000L;
    }

    public static long getNanos() {
        return timeSource.getAsLong();
    }

    public static long getEpochMillis() {
        return Instant.now().toEpochMilli();
    }

    public static String getFilenameFormattedDateTime() {
        return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
    }

    private static TracingExecutor makeExecutor(final String string) {
        Object executorService;
        int i = Util.maxAllowedExecutorThreads();
        if (i <= 0) {
            executorService = MoreExecutors.newDirectExecutorService();
        } else {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            executorService = new ForkJoinPool(i, forkJoinPool -> {
                final String string2 = "Worker-" + string + "-" + atomicInteger.getAndIncrement();
                ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool){

                    @Override
                    protected void onStart() {
                        TracyClient.setThreadName((String)string2, (int)string.hashCode());
                        super.onStart();
                    }

                    @Override
                    protected void onTermination(@Nullable Throwable throwable) {
                        if (throwable != null) {
                            LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                        } else {
                            LOGGER.debug("{} shutdown", (Object)this.getName());
                        }
                        super.onTermination(throwable);
                    }
                };
                forkJoinWorkerThread.setName(string2);
                return forkJoinWorkerThread;
            }, Util::onThreadException, true);
        }
        return new TracingExecutor((ExecutorService)executorService);
    }

    public static int maxAllowedExecutorThreads() {
        return Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxThreads());
    }

    private static int getMaxThreads() {
        String string = System.getProperty(MAX_THREADS_SYSTEM_PROPERTY);
        if (string != null) {
            try {
                int i = Integer.parseInt(string);
                if (i >= 1 && i <= 255) {
                    return i;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", new Object[]{MAX_THREADS_SYSTEM_PROPERTY, string, 255});
            }
        }
        return 255;
    }

    public static TracingExecutor backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static TracingExecutor ioPool() {
        return IO_POOL;
    }

    public static TracingExecutor nonCriticalIoPool() {
        return DOWNLOAD_POOL;
    }

    public static void shutdownExecutors() {
        BACKGROUND_EXECUTOR.shutdownAndAwait(3L, TimeUnit.SECONDS);
        IO_POOL.shutdownAndAwait(3L, TimeUnit.SECONDS);
    }

    private static TracingExecutor makeIoExecutor(String string, boolean bl) {
        AtomicInteger atomicInteger = new AtomicInteger(1);
        return new TracingExecutor(Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            String string2 = string + atomicInteger.getAndIncrement();
            TracyClient.setThreadName((String)string2, (int)string.hashCode());
            thread.setName(string2);
            thread.setDaemon(bl);
            thread.setUncaughtExceptionHandler(Util::onThreadException);
            return thread;
        }));
    }

    public static void throwAsRuntime(Throwable throwable) {
        throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    private static void onThreadException(Thread thread, Throwable throwable) {
        Util.pauseInIde(throwable);
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ReportedException) {
            ReportedException reportedException = (ReportedException)throwable;
            Bootstrap.realStdoutPrintln(reportedException.getReport().getFriendlyReport(ReportType.CRASH));
            System.exit(-1);
        }
        LOGGER.error("Caught exception in thread {}", (Object)thread, (Object)throwable);
    }

    public static @Nullable Type<?> fetchChoiceType(DSL.TypeReference typeReference, String string) {
        if (!SharedConstants.CHECK_DATA_FIXER_SCHEMA) {
            return null;
        }
        return Util.doFetchChoiceType(typeReference, string);
    }

    private static @Nullable Type<?> doFetchChoiceType(DSL.TypeReference typeReference, String string) {
        Type type;
        block2: {
            type = null;
            try {
                type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey((int)SharedConstants.getCurrentVersion().dataVersion().version())).getChoiceType(typeReference, string);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)string);
                if (!SharedConstants.IS_RUNNING_IN_IDE) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void runNamed(Runnable runnable, String string) {
        block16: {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try (Zone zone = TracyClient.beginZone((String)string, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                    runnable.run();
                    break block16;
                }
                finally {
                    thread.setName(string2);
                }
            }
            try (Zone zone2 = TracyClient.beginZone((String)string, (boolean)SharedConstants.IS_RUNNING_IN_IDE);){
                runnable.run();
            }
        }
    }

    public static <T> String getRegisteredName(Registry<T> registry, T object) {
        Identifier identifier = registry.getKey(object);
        if (identifier == null) {
            return "[unregistered]";
        }
        return identifier.toString();
    }

    public static <T> Predicate<T> allOf() {
        return object -> true;
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> predicate) {
        return predicate;
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2) {
        return object -> predicate.test(object) && predicate2.test(object);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3) {
        return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4) {
        return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object) && predicate4.test(object);
    }

    public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4, Predicate<? super T> predicate5) {
        return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object) && predicate4.test(object) && predicate5.test(object);
    }

    @SafeVarargs
    public static <T> Predicate<T> allOf(Predicate<? super T> ... predicates) {
        return object -> {
            for (Predicate predicate : predicates) {
                if (predicate.test(object)) continue;
                return false;
            }
            return true;
        };
    }

    public static <T> Predicate<T> allOf(List<? extends Predicate<? super T>> list) {
        return switch (list.size()) {
            case 0 -> Util.allOf();
            case 1 -> Util.allOf(list.get(0));
            case 2 -> Util.allOf(list.get(0), list.get(1));
            case 3 -> Util.allOf(list.get(0), list.get(1), list.get(2));
            case 4 -> Util.allOf(list.get(0), list.get(1), list.get(2), list.get(3));
            case 5 -> Util.allOf(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
            default -> {
                Predicate[] predicates = (Predicate[])list.toArray(Predicate[]::new);
                yield Util.allOf(predicates);
            }
        };
    }

    public static <T> Predicate<T> anyOf() {
        return object -> false;
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> predicate) {
        return predicate;
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2) {
        return object -> predicate.test(object) || predicate2.test(object);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3) {
        return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4) {
        return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object) || predicate4.test(object);
    }

    public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4, Predicate<? super T> predicate5) {
        return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object) || predicate4.test(object) || predicate5.test(object);
    }

    @SafeVarargs
    public static <T> Predicate<T> anyOf(Predicate<? super T> ... predicates) {
        return object -> {
            for (Predicate predicate : predicates) {
                if (!predicate.test(object)) continue;
                return true;
            }
            return false;
        };
    }

    public static <T> Predicate<T> anyOf(List<? extends Predicate<? super T>> list) {
        return switch (list.size()) {
            case 0 -> Util.anyOf();
            case 1 -> Util.anyOf(list.get(0));
            case 2 -> Util.anyOf(list.get(0), list.get(1));
            case 3 -> Util.anyOf(list.get(0), list.get(1), list.get(2));
            case 4 -> Util.anyOf(list.get(0), list.get(1), list.get(2), list.get(3));
            case 5 -> Util.anyOf(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
            default -> {
                Predicate[] predicates = (Predicate[])list.toArray(Predicate[]::new);
                yield Util.anyOf(predicates);
            }
        };
    }

    public static <T> boolean isSymmetrical(int i, int j, List<T> list) {
        if (i == 1) {
            return true;
        }
        int k = i / 2;
        for (int l = 0; l < j; ++l) {
            for (int m = 0; m < k; ++m) {
                T object2;
                int n = i - 1 - m;
                T object = list.get(m + l * i);
                if (object.equals(object2 = list.get(n + l * i))) continue;
                return false;
            }
        }
        return true;
    }

    public static int growByHalf(int i, int j) {
        return (int)Math.max(Math.min((long)i + (long)(i >> 1), 0x7FFFFFF7L), (long)j);
    }

    @SuppressForbidden(reason="Intentional use of default locale for user-visible date")
    public static DateTimeFormatter localizedDateFormatter(FormatStyle formatStyle) {
        return DateTimeFormatter.ofLocalizedDateTime(formatStyle);
    }

    public static OS getPlatform() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OS.WINDOWS;
        }
        if (string.contains("mac")) {
            return OS.OSX;
        }
        if (string.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (string.contains("linux")) {
            return OS.LINUX;
        }
        if (string.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    public static boolean isAarch64() {
        String string = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return string.equals("aarch64");
    }

    public static URI parseAndValidateUntrustedUri(String string) throws URISyntaxException {
        URI uRI = new URI(string);
        String string2 = uRI.getScheme();
        if (string2 == null) {
            throw new URISyntaxException(string, "Missing protocol in URI: " + string);
        }
        String string3 = string2.toLowerCase(Locale.ROOT);
        if (!ALLOWED_UNTRUSTED_LINK_PROTOCOLS.contains(string3)) {
            throw new URISyntaxException(string, "Unsupported protocol in URI: " + string);
        }
        return uRI;
    }

    public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = iterator.next();
        if (object != null) {
            T object3 = object2;
            while (true) {
                if (object3 == object) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                object3 = iterator.next();
            }
        }
        return object2;
    }

    public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = null;
        while (iterator.hasNext()) {
            T object3 = iterator.next();
            if (object3 == object) {
                if (object2 != null) break;
                object2 = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : object);
                break;
            }
            object2 = object3;
        }
        return object2;
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<? super T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static <K extends Enum<K>, V> Map<K, V> makeEnumMap(Class<K> class_, java.util.function.Function<K, V> function) {
        EnumMap<Enum, V> enumMap = new EnumMap<Enum, V>(class_);
        for (Enum enum_ : (Enum[])class_.getEnumConstants()) {
            enumMap.put(enum_, function.apply(enum_));
        }
        return enumMap;
    }

    public static <K, V1, V2> Map<K, V2> mapValues(Map<K, V1> map, java.util.function.Function<? super V1, V2> function) {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> function.apply((Object)entry.getValue())));
    }

    public static <K, V1, V2> Map<K, V2> mapValuesLazy(Map<K, V1> map, Function<V1, V2> function) {
        return Maps.transformValues(map, function);
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (list.size() == 1) {
            return ((CompletableFuture)list.getFirst()).thenApply(ObjectLists::singleton);
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> list.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, completableFuture::completeExceptionally).applyToEither((CompletionStage)completableFuture, java.util.function.Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, throwable -> {
            if (completableFuture.completeExceptionally((Throwable)throwable)) {
                for (CompletableFuture completableFuture2 : list) {
                    completableFuture2.cancel(true);
                }
            }
        }).applyToEither((CompletionStage)completableFuture, java.util.function.Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        objectArrayList.size(list.size());
        CompletableFuture[] completableFutures = new CompletableFuture[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            int j = i;
            completableFutures[i] = list.get(i).whenComplete((object, throwable) -> {
                if (throwable != null) {
                    consumer.accept((Throwable)throwable);
                } else {
                    objectArrayList.set(j, object);
                }
            });
        }
        return CompletableFuture.allOf(completableFutures).thenApply(void_ -> objectArrayList);
    }

    public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            runnable.run();
        }
        return optional;
    }

    public static <T> Supplier<T> name(final Supplier<T> supplier, Supplier<String> supplier2) {
        if (SharedConstants.DEBUG_NAMED_RUNNABLES) {
            final String string = supplier2.get();
            return new Supplier<T>(){

                @Override
                public T get() {
                    return supplier.get();
                }

                public String toString() {
                    return string;
                }
            };
        }
        return supplier;
    }

    public static Runnable name(final Runnable runnable, Supplier<String> supplier) {
        if (SharedConstants.DEBUG_NAMED_RUNNABLES) {
            final String string = supplier.get();
            return new Runnable(){

                @Override
                public void run() {
                    runnable.run();
                }

                public String toString() {
                    return string;
                }
            };
        }
        return runnable;
    }

    public static void logAndPauseIfInIde(String string) {
        LOGGER.error(string);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static void logAndPauseIfInIde(String string, Throwable throwable) {
        LOGGER.error(string, throwable);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static <T extends Throwable> T pauseInIde(T throwable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);
            Util.doPause(throwable.getMessage());
        }
        return throwable;
    }

    public static void setPause(Consumer<String> consumer) {
        thePauser = consumer;
    }

    private static void doPause(String string) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            thePauser.accept(string);
        }
    }

    public static String describeError(Throwable throwable) {
        if (throwable.getCause() != null) {
            return Util.describeError(throwable.getCause());
        }
        if (throwable.getMessage() != null) {
            return throwable.getMessage();
        }
        return throwable.toString();
    }

    public static <T> T getRandom(T[] objects, RandomSource randomSource) {
        return objects[randomSource.nextInt(objects.length)];
    }

    public static int getRandom(int[] is, RandomSource randomSource) {
        return is[randomSource.nextInt(is.length)];
    }

    public static <T> T getRandom(List<T> list, RandomSource randomSource) {
        return list.get(randomSource.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> list, RandomSource randomSource) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, randomSource));
    }

    private static BooleanSupplier createRenamer(final Path path, final Path path2) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(path, path2, new CopyOption[0]);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to rename", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + String.valueOf(path) + " to " + String.valueOf(path2);
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                }
                catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", (Throwable)iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + String.valueOf(path);
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + String.valueOf(path) + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier ... booleanSuppliers) {
        for (BooleanSupplier booleanSupplier : booleanSuppliers) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean runWithRetries(int i, String string, BooleanSupplier ... booleanSuppliers) {
        for (int j = 0; j < i; ++j) {
            if (Util.executeInSequence(booleanSuppliers)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", new Object[]{string, j, i});
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)string);
        return false;
    }

    public static void safeReplaceFile(Path path, Path path2, Path path3) {
        Util.safeReplaceOrMoveFile(path, path2, path3, false);
    }

    public static boolean safeReplaceOrMoveFile(Path path, Path path2, Path path3, boolean bl) {
        if (Files.exists(path, new LinkOption[0]) && !Util.runWithRetries(10, "create backup " + String.valueOf(path3), Util.createDeleter(path3), Util.createRenamer(path, path3), Util.createFileCreatedCheck(path3))) {
            return false;
        }
        if (!Util.runWithRetries(10, "remove old " + String.valueOf(path), Util.createDeleter(path), Util.createFileDeletedCheck(path))) {
            return false;
        }
        if (!Util.runWithRetries(10, "replace " + String.valueOf(path) + " with " + String.valueOf(path2), Util.createRenamer(path2, path), Util.createFileCreatedCheck(path)) && !bl) {
            Util.runWithRetries(10, "restore " + String.valueOf(path) + " from " + String.valueOf(path3), Util.createRenamer(path3, path), Util.createFileCreatedCheck(path));
            return false;
        }
        return true;
    }

    public static int offsetByCodepoints(String string, int i, int j) {
        int k = string.length();
        if (j >= 0) {
            for (int l = 0; i < k && l < j; ++l) {
                if (!Character.isHighSurrogate(string.charAt(i++)) || i >= k || !Character.isLowSurrogate(string.charAt(i))) continue;
                ++i;
            }
        } else {
            for (int l = j; i > 0 && l < 0; ++l) {
                if (!Character.isLowSurrogate(string.charAt(--i)) || i <= 0 || !Character.isHighSurrogate(string.charAt(i - 1))) continue;
                --i;
            }
        }
        return i;
    }

    public static Consumer<String> prefix(String string, Consumer<String> consumer) {
        return string2 -> consumer.accept(string + string2);
    }

    public static DataResult<int[]> fixedSize(IntStream intStream, int i) {
        int[] is = intStream.limit(i + 1).toArray();
        if (is.length != i) {
            Supplier<String> supplier = () -> "Input is not a list of " + i + " ints";
            if (is.length >= i) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(is, i));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)is);
    }

    public static DataResult<long[]> fixedSize(LongStream longStream, int i) {
        long[] ls = longStream.limit(i + 1).toArray();
        if (ls.length != i) {
            Supplier<String> supplier = () -> "Input is not a list of " + i + " longs";
            if (ls.length >= i) {
                return DataResult.error(supplier, (Object)Arrays.copyOf(ls, i));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success((Object)ls);
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> list, int i) {
        if (list.size() != i) {
            Supplier<String> supplier = () -> "Input is not a list of " + i + " elements";
            if (list.size() >= i) {
                return DataResult.error(supplier, list.subList(0, i));
            }
            return DataResult.error(supplier);
        }
        return DataResult.success(list);
    }

    public static void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                }
                catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    public static void copyBetweenDirs(Path path, Path path2, Path path3) throws IOException {
        Path path4 = path.relativize(path3);
        Path path5 = path2.resolve(path4);
        Files.copy(path3, path5, new CopyOption[0]);
    }

    public static String sanitizeName(String string, CharPredicate charPredicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(i -> charPredicate.test((char)i) ? Character.toString((char)i) : "_").collect(Collectors.joining());
    }

    public static <K, V> SingleKeyCache<K, V> singleKeyCache(java.util.function.Function<K, V> function) {
        return new SingleKeyCache<K, V>(function);
    }

    public static <T, R> java.util.function.Function<T, R> memoize(final java.util.function.Function<T, R> function) {
        return new java.util.function.Function<T, R>(){
            private final Map<T, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + String.valueOf(function) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

            @Override
            public R apply(T object, U object2) {
                return this.cache.computeIfAbsent(Pair.of(object, object2), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + String.valueOf(biFunction) + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> toShuffledList(Stream<T> stream, RandomSource randomSource) {
        ObjectArrayList objectArrayList = (ObjectArrayList)stream.collect(ObjectArrayList.toList());
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static IntArrayList toShuffledList(IntStream intStream, RandomSource randomSource) {
        int i;
        IntArrayList intArrayList = IntArrayList.wrap((int[])intStream.toArray());
        for (int j = i = intArrayList.size(); j > 1; --j) {
            int k = randomSource.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }
        return intArrayList;
    }

    public static <T> List<T> shuffledCopy(T[] objects, RandomSource randomSource) {
        ObjectArrayList objectArrayList = new ObjectArrayList((Object[])objects);
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> objectArrayList, RandomSource randomSource) {
        ObjectArrayList objectArrayList2 = new ObjectArrayList(objectArrayList);
        Util.shuffle(objectArrayList2, randomSource);
        return objectArrayList2;
    }

    public static <T> void shuffle(List<T> list, RandomSource randomSource) {
        int i;
        for (int j = i = list.size(); j > 1; --j) {
            int k = randomSource.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }
    }

    public static <T> CompletableFuture<T> blockUntilDone(java.util.function.Function<Executor, CompletableFuture<T>> function) {
        return Util.blockUntilDone(function, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(java.util.function.Function<Executor, T> function, Predicate<T> predicate) {
        int i;
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();
        T object = function.apply(blockingQueue::add);
        while (!predicate.test(object)) {
            try {
                Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (runnable == null) continue;
                runnable.run();
            }
            catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if ((i = blockingQueue.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)i);
        }
        return object;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> list) {
        int i = list.size();
        if (i < 8) {
            return list::indexOf;
        }
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap(i);
        object2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            object2IntMap.put(list.get(j), j);
        }
        return object2IntMap;
    }

    public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> list) {
        int i = list.size();
        if (i < 8) {
            ReferenceImmutableList referenceList = new ReferenceImmutableList(list);
            return arg_0 -> ((ReferenceList)referenceList).indexOf(arg_0);
        }
        Reference2IntOpenHashMap reference2IntMap = new Reference2IntOpenHashMap(i);
        reference2IntMap.defaultReturnValue(-1);
        for (int j = 0; j < i; ++j) {
            reference2IntMap.put(list.get(j), j);
        }
        return reference2IntMap;
    }

    public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> unaryOperator) {
        Dynamic dynamic = (Dynamic)typed.write().getOrThrow();
        return Util.readTypedOrThrow(type, (Dynamic)unaryOperator.apply(dynamic), true);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic) {
        return Util.readTypedOrThrow(type, dynamic, false);
    }

    public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic, boolean bl) {
        DataResult dataResult = type.readTyped(dynamic).map(Pair::getFirst);
        try {
            if (bl) {
                return (Typed)dataResult.getPartialOrThrow(IllegalStateException::new);
            }
            return (Typed)dataResult.getOrThrow(IllegalStateException::new);
        }
        catch (IllegalStateException illegalStateException) {
            CrashReport crashReport = CrashReport.forThrowable(illegalStateException, "Reading type");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Info");
            crashReportCategory.setDetail("Data", dynamic);
            crashReportCategory.setDetail("Type", type);
            throw new ReportedException(crashReport);
        }
    }

    public static <T> List<T> copyAndAdd(List<T> list, T object) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).addAll(list).add(object).build();
    }

    public static <T> List<T> copyAndAdd(T object, List<T> list) {
        return ImmutableList.builderWithExpectedSize((int)(list.size() + 1)).add(object).addAll(list).build();
    }

    public static <K, V> Map<K, V> copyAndPut(Map<K, V> map, K object, V object2) {
        return ImmutableMap.builderWithExpectedSize((int)(map.size() + 1)).putAll(map).put(object, object2).buildKeepingLast();
    }

    public static enum OS {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getOpenUriArguments(URI uRI) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRI.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getOpenUriArguments(URI uRI) {
                return new String[]{"open", uRI.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String string2) {
            this.telemetryName = string2;
        }

        public void openUri(URI uRI) {
            try {
                Process process = Runtime.getRuntime().exec(this.getOpenUriArguments(uRI));
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't open location '{}'", (Object)uRI, (Object)iOException);
            }
        }

        public void openFile(File file) {
            this.openUri(file.toURI());
        }

        public void openPath(Path path) {
            this.openUri(path.toUri());
        }

        protected String[] getOpenUriArguments(URI uRI) {
            String string = uRI.toString();
            if ("file".equals(uRI.getScheme())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void openUri(String string) {
            try {
                this.openUri(new URI(string));
            }
            catch (IllegalArgumentException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)string, (Object)exception);
            }
        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }
}

