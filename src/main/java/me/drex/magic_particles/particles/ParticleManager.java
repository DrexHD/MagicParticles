package me.drex.magic_particles.particles;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.drex.vanish.api.VanishAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static me.drex.magic_particles.MagicParticlesMod.LOGGER;
import static me.drex.magic_particles.MagicParticlesMod.PARTICLE;

public class ParticleManager {

    public static final Path PARTICLES_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("magic-particles");
    private static final String FILE_SUFFIX = ".json";
    private static final boolean VANISH = FabricLoader.getInstance().isModLoaded("melius-vanish");

    private static final Map<String, MagicParticle> particles = new HashMap<>();

    public static void init() {
        load();
        ServerTickEvents.START_SERVER_TICK.register(ParticleManager::tick);
    }

    public static boolean load() {
        LOGGER.info("Loading magic particles...");
        File folder = PARTICLES_FOLDER.toFile();

        if (folder.mkdirs()) {
            // Save defaults
            Optional<Path> optionalPath = FabricLoader.getInstance().getModContainer("magic-particles").orElseThrow().findPath("magic-particles");
            if (optionalPath.isPresent()) {
                Path sourceFolder = optionalPath.get();
                try {
                    try (Stream<Path> pathStream = Files.walk(sourceFolder)) {
                        pathStream.forEach(source -> {
                            try {
                                Files.copy(source, PARTICLES_FOLDER.resolve(sourceFolder.relativize(source).toString()), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                LOGGER.error("Failed copy copy default magic particle data \"{}\"", source, e);
                            }
                        });
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to load default magic particles", e);
                }
            } else {
                LOGGER.warn("No default magic particles found");
            }
        }
        File[] files = folder.listFiles((FileFilter) new SuffixFileFilter(FILE_SUFFIX));
        if (files == null) {
            // Abstract pathname does not denote a directory, or an I/O error occurred.
            return false;
        }
        ImmutableMap.Builder<String, MagicParticle> builder = ImmutableMap.builder();
        for (File file : files) {
            if (file.isFile()) {
                String id = file.getName();
                id = id.substring(0, id.length() - FILE_SUFFIX.length());

                try (JsonReader jsonReader = new JsonReader(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8))) {
                    jsonReader.setLenient(false);
                    JsonElement jsonElement = JsonParser.parseReader(jsonReader);
                    DataResult<MagicParticle> dataResult = MagicParticle.CODEC.parse(JsonOps.INSTANCE, jsonElement);
                    String finalId = id;
                    builder.put(id, dataResult.resultOrPartial(s -> {
                        LOGGER.error("Failed to load magic particle \"{}\"", finalId);
                        LOGGER.error(s);
                    }).orElseThrow());
                } catch (Exception e) {
                    LOGGER.error("Failed to load magic particle \"{}\"", id, e);
                    return false;
                }
            }
        }

        particles.clear();
        particles.putAll(builder.build());
        return true;
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!shouldShow(player)) continue;
            StringTag tag = PlayerDataApi.getGlobalDataFor(player, PARTICLE, StringTag.TYPE);
            if (tag != null) {
                String particle = tag.value();
                MagicParticle magicParticle = particles.get(particle);
                if (magicParticle != null) {
                    magicParticle.display(player.createCommandSourceStack());
                }
            }

        }
    }

    private static boolean shouldShow(ServerPlayer player) {
        return (!VANISH || !VanishAPI.isVanished(player)) && !player.isSpectator();
    }

    public static Map<String, MagicParticle> particles() {
        return particles;
    }


}
