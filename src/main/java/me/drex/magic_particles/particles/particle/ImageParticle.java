package me.drex.magic_particles.particles.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.CustomCodecs;
import me.drex.magic_particles.particles.ParticleManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// TODO: Add rotation and axis variables
public class ImageParticle extends AbstractParticle {

    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("image");
    public static final MapCodec<ImageParticle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("image").forGetter(ImageParticle::image),
        Codec.FLOAT.fieldOf("size_x").forGetter(ImageParticle::sizeX),
        Codec.FLOAT.fieldOf("size_y").forGetter(ImageParticle::sizeY),
        Codec.FLOAT.fieldOf("pixel_size").forGetter(ImageParticle::sizeY),
        Vec3.CODEC.optionalFieldOf("pos", Vec3.ZERO).forGetter(ImageParticle::pos),
        CustomCodecs.ANCHOR.optionalFieldOf("anchor", EntityAnchorArgument.Anchor.FEET).forGetter(ImageParticle::anchor),
        Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(ImageParticle::origin),
        Display.BillboardConstraints.CODEC.optionalFieldOf("billboard", Display.BillboardConstraints.FIXED).forGetter(ImageParticle::billboard)
    ).apply(instance, ImageParticle::new));

    private final String image;
    private final float sizeX;
    private final float sizeY;
    private final float pixelSize;
    private final Vec3 pos;
    private final BufferedImage bufferedImage;
    private final RandomSource random;

    protected ImageParticle(String image, float sizeX, float sizeY, float pixelSize, Vec3 pos, EntityAnchorArgument.Anchor anchor, Vec3 origin, Display.BillboardConstraints billboard) {
        super(LOCATION, CODEC, anchor, origin, billboard);
        this.image = image;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.pixelSize = pixelSize;
        this.pos = pos;
        try {
            bufferedImage = ImageIO.read(ParticleManager.PARTICLES_FOLDER.resolve("images").resolve(image).toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.random = RandomSource.create();
    }

    @Override
    public void sendParticles(CommandSourceStack source, ServerPlayer player) {
        double stepX = sizeX / bufferedImage.getWidth();
        double stepY = sizeY / bufferedImage.getHeight();
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                int color = bufferedImage.getRGB(x, y);

                int red = (color & 0xff0000) >> 16;
                int green = (color & 0xff00) >> 8;
                int blue = color & 0xff;
                int alpha = (color & 0xff000000) >>> 24;

                DustParticleOptions particleOptions = new DustParticleOptions(ARGB.color(red, green, blue), pixelSize);
                double reversedX = bufferedImage.getWidth() - 1 - x;
                double centeredX = reversedX - ((double) (bufferedImage.getWidth() - 1) / 2);
                double reversedY = bufferedImage.getHeight() - 1 - y;
                double centeredY = reversedY - ((double) (bufferedImage.getHeight() - 1) / 2);


                Vec3 vec3 = new Vec3(stepX * centeredX, stepY * centeredY, 0).add(pos);
                if (alpha >= random.nextIntBetweenInclusive(1, 255)) {
                    sendParticles(source, player, particleOptions, false, vec3, 1, Vec3.ZERO, 0);
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

    public float sizeX() {
        return sizeX;
    }

    public float sizeY() {
        return sizeY;
    }
}
