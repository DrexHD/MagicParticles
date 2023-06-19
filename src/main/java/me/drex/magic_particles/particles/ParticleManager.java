package me.drex.magic_particles.particles;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.drex.magic_particles.json.MagicParticle;
import me.drex.magic_particles.json.adapter.ParticleOptionsAdapter;
import me.drex.magic_particles.json.adapter.WorldCoordinatesAdapter;
import me.drex.vanish.api.VanishAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static me.drex.magic_particles.MagicParticlesMod.PARTICLE;

public class ParticleManager {

    private static final Path PARTICLES_FOLDER = FabricLoader.getInstance().getConfigDir().resolve("magic-particles");
    private static final String FILE_SUFFIX = ".json";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(WorldCoordinates.class, new WorldCoordinatesAdapter())
            .registerTypeHierarchyAdapter(ParticleOptions.class, new ParticleOptionsAdapter())
            .setPrettyPrinting()
            .create();
    private static final Map<String, MagicParticle> defaultParticles = new HashMap<>() {{
        put("heart", MagicParticles.smallHeart());
        put("musician", MagicParticles.musician());
        put("rainy_cloud", MagicParticles.rainyCloud());
        put("rich", MagicParticles.rich());
    }};
    private static final boolean VANISH = FabricLoader.getInstance().isModLoaded("melius-vanish");
    public static final ParticleManager INSTANCE = new ParticleManager();

    private final Map<String, MagicParticle> particleMap = new HashMap<>();

    private ParticleManager() {
    }

    public void init() {
        // TODO:
        load();
        ServerTickEvents.START_SERVER_TICK.register(this::tick);
    }

    public boolean load() {
        // TODO: Logging here (?)
        // MagicParticlesMod.LOGGER.info("Loading magic particles...");
        File folder = PARTICLES_FOLDER.toFile();
        if (folder.mkdirs()) {
            // Save defaults
            for (Map.Entry<String, MagicParticle> entry : defaultParticles.entrySet()) {
                try {
                    Files.writeString(PARTICLES_FOLDER.resolve(entry.getKey() + FILE_SUFFIX), GSON.toJson(entry.getValue()));
                    particleMap.put(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            File[] files = folder.listFiles((FileFilter) new SuffixFileFilter(FILE_SUFFIX));
            if (files == null) {
                // Abstract pathname does not denote a directory, or an I/O error occurred.
                return false;
            }
            ImmutableMap.Builder<String, MagicParticle> builder = ImmutableMap.builder();
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        String json = Files.readString(file.toPath());
                        MagicParticle magicParticle = GSON.fromJson(json, MagicParticle.class);
                        String id = file.getName();
                        id = id.substring(0, id.length() - FILE_SUFFIX.length());
                        builder.put(id, magicParticle);
                    } catch (IOException | JsonSyntaxException e) {
                        e.printStackTrace();
                        //return false;
                    }
                }
            }

            particleMap.clear();
            particleMap.putAll(builder.build());
        }
        return true;
    }

    private void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!shouldShow(player)) continue;
            StringTag tag = PlayerDataApi.getGlobalDataFor(player, PARTICLE, StringTag.TYPE);
            if (tag != null) {
                String particle = tag.getAsString();
                MagicParticle magicParticle = particleMap.get(particle);
                if (magicParticle != null) {
                    magicParticle.display(player.createCommandSourceStack());
                }
            }

        }
    }

    private boolean shouldShow(ServerPlayer player) {
        return (!VANISH || !VanishAPI.isVanished(player)) && !player.isSpectator();
    }

    public Map<String, MagicParticle> particleMap() {
        return particleMap;
    }


}
