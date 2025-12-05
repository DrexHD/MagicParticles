package me.drex.magic_particles.particles.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public abstract class AbstractParticle {

    private final Identifier location;
    private final MapCodec<AbstractParticle> codec;
    private final EntityAnchorArgument.Anchor anchor;
    private final Vec3 origin;
    private final Display.BillboardConstraints billboard;

    protected AbstractParticle(Identifier location, MapCodec<? extends AbstractParticle> codec, EntityAnchorArgument.Anchor anchor, Vec3 origin, Display.BillboardConstraints billboard) {
        this.location = location;
        //noinspection unchecked
        this.codec = (MapCodec<AbstractParticle>) codec;
        this.anchor = anchor;
        this.origin = origin;
        this.billboard = billboard;
    }

    public Identifier location() {
        return this.location;
    }

    public MapCodec<AbstractParticle> codec() {
        return this.codec;
    }

    public ClientboundLevelParticlesPacket createParticlePacket(CommandSourceStack source, ParticleOptions particleOptions, boolean force, Vector3f offset, int count, Vec3 delta, float speed, Quaternionf billboardRotation) {
        offset.rotate(billboardRotation);
        Vector3f rotatedPosition = anchor.apply(source).toVector3f().add(offset).add(origin.toVector3f());
        return new ClientboundLevelParticlesPacket(particleOptions, /*? if >= 1.21.4 {*/ false, /*?}*/ force, rotatedPosition.x, rotatedPosition.y, rotatedPosition.z, (float) delta.x, (float) delta.y, (float) delta.z, speed, count);
    }

    Quaternionf getBillboardRotation(CommandSourceStack source) {
        Vec2 rotation = source.getRotation();
        float rotX = Mth.wrapDegrees(rotation.x);
        float rotY = Mth.wrapDegrees(rotation.y);
        return switch (billboard) {
            case FIXED -> new Quaternionf();
            case HORIZONTAL -> new Quaternionf().rotationYXZ(0, (float) (-Math.PI) / 180 * rotX, 0.0f);
            case VERTICAL -> new Quaternionf().rotationYXZ((float) Math.PI - (float) Math.PI / 180 * rotY, 0, 0.0f);
            case CENTER ->
                new Quaternionf().rotationYXZ((float) Math.PI - (float) Math.PI / 180 * rotY, (float) (-Math.PI) / 180 * rotX, 0.0f);
        };
    }

    public abstract void collectParticlePackets(CommandSourceStack source, Consumer<ClientboundLevelParticlesPacket> collector);

    public Vec3 origin() {
        return origin;
    }

    public Display.BillboardConstraints billboard() {
        return billboard;
    }

    public EntityAnchorArgument.Anchor anchor() {
        return anchor;
    }
}
