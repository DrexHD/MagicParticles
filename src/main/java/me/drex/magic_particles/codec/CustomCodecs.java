package me.drex.magic_particles.codec;

import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ExtraCodecs;

public class CustomCodecs {

    public static final Codec<EntityAnchorArgument.Anchor> ANCHOR = ExtraCodecs.stringResolverCodec(anchor -> {
        return switch (anchor) {
            case EYES -> "eyes";
            case FEET -> "feet";
            default -> throw new IncompatibleClassChangeError();
        };
    }, EntityAnchorArgument.Anchor::getByName);


}
