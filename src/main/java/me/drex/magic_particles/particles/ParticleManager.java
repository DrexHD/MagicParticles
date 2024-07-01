package me.drex.magic_particles.particles;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.drex.magic_particles.MagicParticlesMod;
import me.drex.vanish.api.VanishAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            try {
                ParticleManager.tick(server);
            } catch (ConcurrentModificationException e) {
                LOGGER.error("Server wanted to crash, but we said no", e);
                dumpCrashReport(server, e);
            }
        });
    }

    private static void dumpCrashReport(MinecraftServer server, ConcurrentModificationException error) {
        // [VanillaCopy]
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        StringBuilder stringBuilder = new StringBuilder();
        for (ThreadInfo threadInfo : threadInfos) {
            if (threadInfo.getThreadId() == server.getRunningThread().getId()) {
                error.setStackTrace(threadInfo.getStackTrace());
            }
            stringBuilder.append(threadInfo);
            stringBuilder.append("\n");
        }
        CrashReport crashReport = new CrashReport("MagicParticles fake crash", error);
        server.fillSystemReport(crashReport.getSystemReport());
        CrashReportCategory crashReportCategory = crashReport.addCategory("Thread Dump");
        crashReportCategory.setDetail("Threads", stringBuilder);
        Bootstrap.realStdoutPrintln("Crash report:\n" + crashReport.getFriendlyReport());
        File file = new File(new File(server.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-magic-particles.txt");
        if (crashReport.saveToFile(file)) {
            LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
        } else {
            LOGGER.error("We were unable to save this crash report to disk.");
        }
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
                String particle = tag.getAsString();
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
