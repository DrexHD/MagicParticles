package me.drex.magic_particles;

import me.drex.magic_particles.command.MagicParticlesCommand;
import me.drex.magic_particles.particles.ParticleManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MagicParticlesMod implements DedicatedServerModInitializer {

	public static final String MOD_ID = "magic-particles";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ResourceLocation PARTICLE = new ResourceLocation(MOD_ID, "particle");

	@Override
	public void onInitializeServer() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MagicParticlesCommand.register(dispatcher));
		ParticleManager.INSTANCE.init();
	}

}
