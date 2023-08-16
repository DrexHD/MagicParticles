package me.drex.magic_particles.codec;

import com.mojang.serialization.*;
import me.drex.magic_particles.particles.particle.AbstractParticle;
import me.drex.magic_particles.particles.particle.BezierParticle;
import me.drex.magic_particles.particles.particle.ImageParticle;
import me.drex.magic_particles.particles.particle.SimpleParticle;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.stream.Stream;

public class ParticleCodec extends MapCodec<AbstractParticle> {

    public static final Codec<AbstractParticle> CODEC = new MapCodec.MapCodecCodec<>(new ParticleCodec());

    private static final Map<ResourceLocation, MapCodec<? extends AbstractParticle>> CODECS = Map.of(
        SimpleParticle.LOCATION, SimpleParticle.CODEC,
        BezierParticle.LOCATION, BezierParticle.CODEC,
        ImageParticle.LOCATION, ImageParticle.CODEC
    );

    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString("type"), ops.createString("config"));
    }

    @Override
    public <T> DataResult<AbstractParticle> decode(DynamicOps<T> ops, MapLike<T> input) {
        var value = ops.getStringValue(input.get("type"));
        return value.flatMap(type -> {
            var id = ResourceLocation.tryParse(type);

            //noinspection unchecked
            var codec = (MapCodec<AbstractParticle>) CODECS.get(id);

            if (codec != null) {
                return codec.decode(ops, input);
            } else {
                String candidates = String.join(" ", CODECS.keySet().stream().map(ResourceLocation::toString).toList());
                return DataResult.error(() -> "Invalid particle type \"" + type + "\", valid types: " + candidates + "!");
            }
        });
    }

    @Override
    public <T> RecordBuilder<T> encode(AbstractParticle input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        var type = input.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? input.location().getPath() : input.location().toString();
        return input.codec().encode(input, ops, prefix.add("type", ops.createString(type)));
    }

}
