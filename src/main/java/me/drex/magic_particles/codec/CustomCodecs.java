package me.drex.magic_particles.codec;

import com.mojang.serialization.Codec;
import net.minecraft.commands.arguments.EntityAnchorArgument;

public class CustomCodecs {

    public static final Codec<EntityAnchorArgument.Anchor> ANCHOR = Codec.stringResolver(anchor -> switch (anchor) {
        case EYES -> "eyes";
        case FEET -> "feet";
    }, EntityAnchorArgument.Anchor::getByName);
    
}
