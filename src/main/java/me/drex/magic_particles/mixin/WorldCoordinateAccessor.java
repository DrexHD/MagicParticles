package me.drex.magic_particles.mixin;

import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldCoordinate.class)
public interface WorldCoordinateAccessor {

    @Accessor("PREFIX_RELATIVE")
    static char getPrefix() {
        throw new AssertionError();
    }

    @Accessor
    double getValue();

}
