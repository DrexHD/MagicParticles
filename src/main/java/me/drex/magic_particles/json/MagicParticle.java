package me.drex.magic_particles.json;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class MagicParticle {

    public String name;
    public ParticleFrame[] frames;

    // Constructor used for config serialization
    public MagicParticle() {}

    public MagicParticle(String name, ParticleFrame[] frames) {
        this.name = name;
        this.frames = frames;
    }

    public void display(CommandSourceStack source) {
        for (ParticleFrame frame : frames) {
            for (ServerPlayer serverPlayer : source.getLevel().players()) {
                frame.sendParticles(source, serverPlayer);
            }
        }
    }

}
