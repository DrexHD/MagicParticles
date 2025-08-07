package me.drex.magic_particles.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.ParticleCodec;
import me.drex.magic_particles.particles.particle.AbstractParticle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public record MagicParticle(String name, List<AbstractParticle> particles) {

    public static final Codec<MagicParticle> CODEC = new MapCodec.MapCodecCodec<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(MagicParticle::name),
        Codec.list(ParticleCodec.CODEC).fieldOf("particles").forGetter(MagicParticle::particles)
    ).apply(instance, MagicParticle::new)));

    public void display(CommandSourceStack source) {
        for (AbstractParticle frame : particles) {
            List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            frame.collectParticlePackets(source, packets::add);

            List<ClientboundBundlePacket> bundles = new ArrayList<>();
            for (int start = 0; start < packets.size(); start += BundlerInfo.BUNDLE_SIZE_LIMIT) {
                int end = Math.min(start + BundlerInfo.BUNDLE_SIZE_LIMIT, packets.size());
                bundles.add(new ClientboundBundlePacket(packets.subList(start, end)));
            }

            for (ServerPlayer serverPlayer : source.getLevel().players()) {
                BlockPos blockPos = serverPlayer.blockPosition();
                if (blockPos.closerToCenterThan(source.getPosition(), 32.0)) {
                    for (ClientboundBundlePacket bundle : bundles) {
                        serverPlayer.connection.send(bundle);
                    }
                }
            }
        }
    }

}
