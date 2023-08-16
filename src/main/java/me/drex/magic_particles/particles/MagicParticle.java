package me.drex.magic_particles.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.ParticleCodec;
import me.drex.magic_particles.particles.particle.AbstractParticle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record MagicParticle(String name, List<AbstractParticle> particles) {

    public static final Codec<MagicParticle> CODEC = new MapCodec.MapCodecCodec<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(MagicParticle::name),
        Codec.list(ParticleCodec.CODEC).fieldOf("particles").forGetter(MagicParticle::particles)
    ).apply(instance, MagicParticle::new)));

    public void display(CommandSourceStack source) {
        for (AbstractParticle frame : particles) {
            for (ServerPlayer serverPlayer : source.getLevel().players()) {
                frame.sendParticles(source, serverPlayer);
            }
        }
    }

}
