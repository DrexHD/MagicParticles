package me.drex.magic_particles.particles.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.CustomCodecs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;

public class SimpleParticle extends AbstractParticle {

    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("simple");
    public static final MapCodec<SimpleParticle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.INT.optionalFieldOf("count", 1).forGetter(SimpleParticle::count),
        Vec3.CODEC.fieldOf("pos").forGetter(SimpleParticle::pos),
        Vec3.CODEC.optionalFieldOf("delta", Vec3.ZERO).forGetter(SimpleParticle::delta),
        Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(SimpleParticle::speed),
        ParticleTypes.CODEC.fieldOf("particle_type").forGetter(SimpleParticle::particleOptions),
        CustomCodecs.ANCHOR.optionalFieldOf("anchor", EntityAnchorArgument.Anchor.FEET).forGetter(SimpleParticle::anchor),
        Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(SimpleParticle::origin),
        Display.BillboardConstraints.CODEC.optionalFieldOf("billboard", Display.BillboardConstraints.FIXED).forGetter(SimpleParticle::billboard)
    ).apply(instance, SimpleParticle::new));

    private final int count;
    private final Vec3 pos;
    private final Vec3 delta;
    private final float speed;
    private final ParticleOptions particleOptions;

    public SimpleParticle(int count, Vec3 pos, Vec3 delta, float speed, ParticleOptions particleOptions, EntityAnchorArgument.Anchor anchor, Vec3 origin, Display.BillboardConstraints billboard) {
        super(LOCATION, CODEC, anchor, origin, billboard);
        this.count = count;
        this.pos = pos;
        this.delta = delta;
        this.speed = speed;
        this.particleOptions = particleOptions;
    }

    public int count() {
        return count;
    }

    public Vec3 pos() {
        return pos;
    }

    public Vec3 delta() {
        return delta;
    }

    public float speed() {
        return speed;
    }

    public ParticleOptions particleOptions() {
        return particleOptions;
    }

    @Override
    public void sendParticles(CommandSourceStack source, ServerPlayer player) {
        sendParticles(source, player, particleOptions, false, pos.toVector3f(), count, delta, speed);
    }
}
