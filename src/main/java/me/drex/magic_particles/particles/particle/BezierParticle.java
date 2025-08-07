package me.drex.magic_particles.particles.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.magic_particles.codec.CustomCodecs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

public class BezierParticle extends AbstractParticle {

    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("bezier");
    public static final MapCodec<BezierParticle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.list(Codec.list(Vec3.CODEC)).fieldOf("curves").forGetter(BezierParticle::curves),
        Codec.INT.fieldOf("steps").forGetter(BezierParticle::steps),
        Codec.FLOAT.optionalFieldOf("speed", 0f).forGetter(BezierParticle::speed),
        ParticleTypes.CODEC.fieldOf("particle_type").forGetter(BezierParticle::particleOptions),
        CustomCodecs.ANCHOR.optionalFieldOf("anchor", EntityAnchorArgument.Anchor.FEET).forGetter(BezierParticle::anchor),
        Vec3.CODEC.optionalFieldOf("origin", Vec3.ZERO).forGetter(BezierParticle::origin),
        Display.BillboardConstraints.CODEC.optionalFieldOf("billboard", Display.BillboardConstraints.FIXED).forGetter(BezierParticle::billboard)
    ).apply(instance, BezierParticle::new));

    private final List<List<Vec3>> curves;
    private final int steps;
    private final float speed;
    private final ParticleOptions particleOptions;

    public BezierParticle(List<List<Vec3>> curves, int steps, float speed, ParticleOptions particleOptions, EntityAnchorArgument.Anchor anchor, Vec3 origin, Display.BillboardConstraints billboard) {
        super(LOCATION, CODEC, anchor, origin, billboard);
        this.curves = curves;
        this.steps = steps;
        this.speed = speed;
        this.particleOptions = particleOptions;
    }

    @Override
    public void collectParticlePackets(CommandSourceStack source, Consumer<ClientboundLevelParticlesPacket> collector) {
        double step = 1D / steps;
        for (List<Vec3> curve : curves) {
            for (double t = 0; t < 1; t += step) {
                Vector3f offset = calculateBezierCurve(curve, t);
                collector.accept(createParticlePacket(source, particleOptions, false, offset, 1, Vec3.ZERO, speed, getBillboardRotation(source)));
            }
            // debug control points
            /*for (int i = 0; i < curve.size(); i++) {
                Vec3 control = curve.get(i);
                collector.accept(sendParticles(source, player, new DustParticleOptions(new Vector3f(0, (float) i / curves.size(), 0), 0.5f), false, control, 1, delta, speed));
            }*/
        }
    }

    // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Explicit_definition
    private Vector3f calculateBezierCurve(List<Vec3> points, double t) {
        Vector3f sum = new Vector3f();
        int n = points.size() - 1;
        for (int i = 0; i <= n; i++) {
            Vector3f summand = new Vector3f(points.get(i).toVector3f());
            double scalar = binomialCoefficient(n, i) * Math.pow(1 - t, n - i) * Math.pow(t, i);
            sum.add(summand.mul((float) scalar));
        }
        return sum;
    }

    // https://www.geeksforgeeks.org/binomial-coefficient-dp-9/
    private static int binomialCoefficient(int n, int k) {
        int[][] dp = new int[n + 1][k + 1];
        int i, j;
        for (i = 0; i <= n; i++) {
            for (j = 0; j <= Math.min(i, k); j++) {
                if (j == 0 || j == i)
                    dp[i][j] = 1;
                else
                    dp[i][j] = dp[i - 1][j - 1] + dp[i - 1][j];
            }
        }
        return dp[n][k];
    }

    public List<List<Vec3>> curves() {
        return curves;
    }

    public float speed() {
        return speed;
    }

    public ParticleOptions particleOptions() {
        return particleOptions;
    }

    public int steps() {
        return steps;
    }
}
