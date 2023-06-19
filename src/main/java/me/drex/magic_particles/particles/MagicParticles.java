package me.drex.magic_particles.particles;

import me.drex.magic_particles.json.BezierCurve;
import me.drex.magic_particles.json.MagicParticle;
import me.drex.magic_particles.json.ParticleFrame;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public class MagicParticles {

    public static MagicParticle smallHeart() {
        WorldCoordinates pos = relative(0, 2.5, 0);
        WorldCoordinates delta = absolute(0, 0, 0);
        BezierCurve[] bezierCurves = new BezierCurve[]{
                new BezierCurve(zero(), relative(-0.5, 0.5, 0), zero(), zero()),
                new BezierCurve(relative(-0.5, 0.5, 0), relative(0, 0.5, 0), relative(-1.0, 1.0, 0), relative(0, 1.5, 0)),
                new BezierCurve(relative(0, 0.5, 0), relative(0.5, 0.5, 0), relative(0, 1.5, 0), relative(1.0, 1.0, 0)),
                new BezierCurve(relative(0.5, 0.5, 0), zero(), relative(0.5, 0.5, 0), zero()),
        };
        ParticleFrame particleFrameConfig = new ParticleFrame(1, pos, delta, 0, new DustParticleOptions(new Vector3f(1, 0, 0), 0.5f), bezierCurves);
        return new MagicParticle("Small Heart", new ParticleFrame[]{particleFrameConfig});
    }

    public static MagicParticle rainyCloud() {
        return new MagicParticle("Rainy Cloud", new ParticleFrame[]{
                new ParticleFrame(5, relative(0, 3.5, 0), absolute(0.5, 0, 0.5), 0, ParticleTypes.RAIN, null),
                new ParticleFrame(5, relative(0, 3.6, 0), absolute(0.65, 0.15, 0.65), 0, ParticleTypes.CLOUD, null),
        });
    }

    public static MagicParticle musician() {
        return new MagicParticle("Musician", new ParticleFrame[]{
                new ParticleFrame(1, zero(), absolute(0.4, 0.2, 0.4), 24, ParticleTypes.NOTE, null)
        });
    }

    public static MagicParticle rich() {
        return new MagicParticle("Rich", new ParticleFrame[]{
                new ParticleFrame(1, zero(), absolute(0.25, 0.1, 0.25), 0.002f, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.EMERALD_BLOCK.defaultBlockState()), null),
                new ParticleFrame(1, zero(), absolute(0.25, 0.1, 0.25), 0.002f, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIAMOND_BLOCK.defaultBlockState()), null),
                new ParticleFrame(1, zero(), absolute(0.25, 0.1, 0.25), 0.002f, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.defaultBlockState()), null),
        });
    }

    public static WorldCoordinates zero() {
        return relative(0, 0, 0);
    }

    public static WorldCoordinates relative(double x, double y, double z) {
        return worldCoordinates(x, y, z, true);
    }

    public static WorldCoordinates absolute(double x, double y, double z) {
        return worldCoordinates(x, y, z, false);
    }

    public static WorldCoordinates worldCoordinates(double x, double y, double z, boolean relative) {
        return new WorldCoordinates(
                new WorldCoordinate(relative, x),
                new WorldCoordinate(relative, y),
                new WorldCoordinate(relative, z)
        );
    }

}
