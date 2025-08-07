package me.drex.magic_particles.particles.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.CustomCodecs;
import me.drex.magic_particles.particles.ParticleManager;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
//? if >= 1.21.2 {
/*import net.minecraft.util.ARGB;
*///?}
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ImageParticle extends AbstractParticle {

    //? if < 1.21.5 {
    public static final Codec<Vec2> VEC_2_CODEC = Codec.FLOAT
                .listOf()
                .comapFlatMap(list -> Util.fixedSize(list, 2).map(listx -> new Vec2(listx.getFirst(), listx.get(1))), vec2 -> List.of(vec2.x, vec2.y));
    //?}

    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("image");
    public static final MapCodec<ImageParticle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("image").forGetter(ImageParticle::image),
        Codec.FLOAT.fieldOf("size_x").forGetter(ImageParticle::sizeX),
        Codec.FLOAT.fieldOf("size_y").forGetter(ImageParticle::sizeY),
        Codec.FLOAT.fieldOf("pixel_size").forGetter(ImageParticle::sizeY),
        Vec3.CODEC.optionalFieldOf("pos", Vec3.ZERO).forGetter(ImageParticle::pos),
        CustomCodecs.ANCHOR.optionalFieldOf("anchor", EntityAnchorArgument.Anchor.FEET).forGetter(ImageParticle::anchor),
        Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(ImageParticle::origin),
        //? if >= 1.21.5 {
        /*Vec2.CODEC.optionalFieldOf("rotation", Vec2.ZERO).forGetter(ImageParticle::rotation),
        *///?} else {
        VEC_2_CODEC.optionalFieldOf("rotation", Vec2.ZERO).forGetter(ImageParticle::rotation),
         //?}
        Display.BillboardConstraints.CODEC.optionalFieldOf("billboard", Display.BillboardConstraints.FIXED).forGetter(ImageParticle::billboard)
    ).apply(instance, ImageParticle::new));

    private final String image;
    private final float sizeX;
    private final float sizeY;
    private final float pixelSize;
    private final Vec3 pos;
    private final Vec2 rotation;
    private final int width;
    private final int height;
    private final int[] colors;
    private final RandomSource random;

    protected ImageParticle(String image, float sizeX, float sizeY, float pixelSize, Vec3 pos, EntityAnchorArgument.Anchor anchor, Vec3 origin, Vec2 rotation, Display.BillboardConstraints billboard) {
        super(LOCATION, CODEC, anchor, origin, billboard);
        this.image = image;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.pixelSize = pixelSize;
        this.pos = pos;
        this.rotation = rotation;
        try {
            BufferedImage bufferedImage = ImageIO.read(ParticleManager.PARTICLES_FOLDER.resolve("images").resolve(image).toFile());
            this.width = bufferedImage.getWidth();
            this.height = bufferedImage.getHeight();
            this.colors = new int[this.width * this.height];
            bufferedImage.getRGB(0, 0, this.width, this.height, this.colors, 0, this.width);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.random = RandomSource.create();
    }

    @Override
    public void collectParticlePackets(CommandSourceStack source, Consumer<ClientboundLevelParticlesPacket> collector) {
        float stepX = sizeX / width;
        float stepY = sizeY / height;

        Quaternionf billboardRotation = getBillboardRotation(source);
        Vector3f posVector = pos.toVector3f();
        Quaternionf rotation = new Quaternionf().rotationYXZ((float) Math.PI - (float) Math.PI / 180 * this.rotation.y, (float) (-Math.PI) / 180 * this.rotation.x, 0.0f);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = colors[y * width + x];

                int red = (color & 0xff0000) >> 16;
                int green = (color & 0xff00) >> 8;
                int blue = color & 0xff;
                int alpha = (color & 0xff000000) >>> 24;

                DustParticleOptions particleOptions = new DustParticleOptions(/*? if >= 1.21.2 {*/ /*ARGB.color(red, green, blue)*//*?} else {*/ new Vector3f((float) red / 255, (float) green / 255, (float) blue / 255) /*?}*/, pixelSize);
                float reversedX = width - 1 - x;
                float centeredX = reversedX - ((float) (width - 1) / 2);
                float reversedY = height - 1 - y;
                float centeredY = reversedY - ((float) (height - 1) / 2);

                Vector3f offset = new Vector3f(stepX * centeredX, stepY * centeredY, 0).add(posVector);
                offset = offset.rotate(rotation);

                // some micro-optimizations to skip random
                if (alpha >= 255 || (alpha > 0 && alpha >= random.nextIntBetweenInclusive(1, 255))) {
                    collector.accept(createParticlePacket(source, particleOptions, false, offset, 1, Vec3.ZERO, 0, billboardRotation));
                }
            }
        }
    }

    public String image() {
        return image;
    }

    public Vec3 pos() {
        return pos;
    }

    public Vec2 rotation() {
        return rotation;
    }

    public float sizeX() {
        return sizeX;
    }

    public float sizeY() {
        return sizeY;
    }
}
