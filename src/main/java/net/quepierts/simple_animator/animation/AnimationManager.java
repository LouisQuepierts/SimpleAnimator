package net.quepierts.simple_animator.animation;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.quepierts.simple_animator.network.ModNetwork;
import net.quepierts.simple_animator.network.packet.data.ClientUpdateAnimationPacket;
import net.quepierts.simple_animator.network.packet.data.ClientUpdateInteractionPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnimationManager implements PreparableReloadListener {
    public static final FileToIdConverter ANIMATION_LISTER = FileToIdConverter.json("animations");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path EXTERMAL_PATH = Path.of("animations");

    private ImmutableMap<ResourceLocation, Animation> animations;
    private ImmutableMap<ResourceLocation, Interaction> interactions;

    @Nullable
    public Animation getAnimation(ResourceLocation location) {
        return animations.get(location);
    }

    @Nullable
    public Interaction getInteraction(ResourceLocation location) {
        return interactions.get(location);
    }

    @Override
    @NotNull
    public CompletableFuture<Void> reload(PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
        CompletableFuture<List<Pair<ResourceLocation, Animation[]>>> animations = load(pResourceManager, pBackgroundExecutor);

        return CompletableFuture.allOf(animations)
                .thenCompose(pPreparationBarrier::wait)
                .thenAcceptAsync((v) -> {
                    ImmutableMap.Builder<ResourceLocation, Animation> animationBuilder = ImmutableMap.builder();
                    ImmutableMap.Builder<ResourceLocation, Interaction> interactionBuilder = ImmutableMap.builder();
                    ImmutableMap<ResourceLocation, Animation> extern = loadExtern();
                    List<Pair<ResourceLocation, Animation[]>> join = animations.join();
                    join.stream()
                            .flatMap((Function<Pair<ResourceLocation, Animation[]>, Stream<Pair<ResourceLocation, Animation>>>) pair -> {
                                if (pair.getSecond().length > 1) {
                                    interactionBuilder.put(pair.getFirst(), new Interaction(
                                            pair.getFirst().withPrefix(Animation.Type.INVITE.path),
                                            pair.getFirst().withPrefix(Animation.Type.REQUESTER.path),
                                            pair.getFirst().withPrefix(Animation.Type.RECEIVER.path)
                                    ));
                                }
                                return Arrays.stream(pair.getSecond())
                                        .map(animation -> new Pair<>(pair.getFirst().withPrefix(animation.getType().path), animation));
                            })
                            .forEach(pair -> animationBuilder.put(pair.getFirst(), pair.getSecond()));
                    this.animations = animationBuilder.putAll(extern).build();
                    this.interactions = interactionBuilder.build();
                });
    }

    private CompletableFuture<List<Pair<ResourceLocation, Animation[]>>> load(ResourceManager pResourceManager, Executor pBackgroundExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            return ANIMATION_LISTER.listMatchingResourceStacks(pResourceManager);
        }, pBackgroundExecutor).thenCompose(map -> {
            List<CompletableFuture<Pair<ResourceLocation, Animation[]>>> list = new ArrayList<>(map.size());

            for (Map.Entry<ResourceLocation, List<Resource>> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                ResourceLocation resourceLocation = ANIMATION_LISTER.fileToId(location);

                for (Resource resource : entry.getValue()) {
                    list.add(CompletableFuture.supplyAsync(() -> {
                        try (Reader reader = resource.openAsReader()) {
                            Animation[] animations = Animation.fromStream(reader);
                            LOGGER.info("Loaded animation {} from {} in data pack {}",  resourceLocation, location, resource.sourcePackId());
                            return Pair.of(resourceLocation, animations);
                        } catch (IOException e) {
                            LOGGER.info("Couldn't read animation {} from {} in data pack {}", resourceLocation, location, resource.sourcePackId());
                            return null;
                        }
                    }));
                }
            }

            return Util.sequence(list)
                    .thenApply(result -> result.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    );
        });
    }

    @Deprecated
    private CompletableFuture<Map<ResourceLocation, Animation>> loadLegacy(ResourceManager pResourceManager, Executor pBackgroundExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            return ANIMATION_LISTER.listMatchingResourceStacks(pResourceManager);
        }, pBackgroundExecutor).thenCompose(map -> {
            List<CompletableFuture<Pair<ResourceLocation, Animation[]>>> list = new ArrayList<>(map.size());

            for (Map.Entry<ResourceLocation, List<Resource>> entry : map.entrySet()) {
                ResourceLocation location = entry.getKey();
                ResourceLocation resourceLocation = ANIMATION_LISTER.fileToId(location);

                for (Resource resource : entry.getValue()) {
                    list.add(CompletableFuture.supplyAsync(() -> {
                        try (Reader reader = resource.openAsReader()) {
                            Animation[] animations = Animation.fromStream(reader);
                            LOGGER.info("Loaded animation {} from {} in data pack {}",  resourceLocation, location, resource.sourcePackId());
                            return Pair.of(resourceLocation, animations);
                        } catch (IOException e) {
                            LOGGER.info("Couldn't read animation {} from {} in data pack {}", resourceLocation, location, resource.sourcePackId());
                            return null;
                        }
                    }));
                }
            }

            return Util.sequence(list)
                    .thenApply(result -> result.stream()
                            .filter(Objects::nonNull)
                            .flatMap((Function<Pair<ResourceLocation, Animation[]>, Stream<Pair<ResourceLocation, Animation>>>) pair -> Arrays.stream(pair.getSecond())
                                    .map(animation -> new Pair<>(pair.getFirst().withPrefix(animation.getType().path), animation)))
                            .collect(Collectors.toUnmodifiableMap(Pair<ResourceLocation, Animation>::getFirst, Pair::getSecond))
                    );
        });
    }

    public Set<ResourceLocation> getAnimationNames() {
        return animations.keySet();
    }

    public Set<ResourceLocation> getInteractionNames() {
        return interactions.keySet();
    }

    public ImmutableMap<ResourceLocation, Animation> loadExtern() {
        final ImmutableMap.Builder<ResourceLocation, Animation> builder = new ImmutableMap.Builder<>();

        if (!Files.exists(EXTERMAL_PATH)) {
            try {
                Files.createDirectories(EXTERMAL_PATH);
            } catch (IOException e) {
                LOGGER.warn("", e);
            }
            LOGGER.info("Nonexistent Animation Path!");
        }
        try {
            Files.walkFileTree(EXTERMAL_PATH, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (Files.isRegularFile(file) && file.toString().endsWith(".json")) {
                        AnimationManager.load(file, builder);
                    }
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Failed to load animations: {}", e.getMessage());
        }

        return builder.build();
    }

    public void handleUpdateAnimations(ClientUpdateAnimationPacket packet) {
        LOGGER.info("Sync Animations From Server");
        Map<ResourceLocation, Animation> animations = packet.getAnimations();
        this.animations = ImmutableMap.copyOf(animations);
    }

    public void handleUpdateInteractions(ClientUpdateInteractionPacket packet) {
        LOGGER.info("Sync Interactions From Server");
        Map<ResourceLocation, Interaction> interactions = packet.getInteractions();
        this.interactions = ImmutableMap.copyOf(interactions);
    }

    private static void load(
            Path path,
            ImmutableMap.Builder<ResourceLocation, Animation> builder
    ) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

            String name = path.getFileName().toString();
            name = name.substring(0, name.length() - 5);
            Animation[] animations = Animation.serialize(object);

            LOGGER.debug("Load External Animation: {}", name);
            for (Animation animation : animations) {
                builder.put(new ResourceLocation("external", animation.getType().path + name), animation);
            }
        } catch (RuntimeException | IOException e) {
            LOGGER.warn("Failed to read resource {}", path, e);
        }
    }

    public void sync(ServerPlayer player) {
        LOGGER.info("Send Animations to Client");
        ModNetwork.sendToPlayer(new ClientUpdateAnimationPacket(this.animations), player);
        ModNetwork.sendToPlayer(new ClientUpdateInteractionPacket(this.interactions), player);
    }

}
