package me.drex.magic_particles.json;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleFrame {

    public int count;
    public WorldCoordinates pos;
    public WorldCoordinates delta;
    public float speed;
    public ParticleOptions particleOptions;
    public BezierCurve[] bezierCurves;

    // Constructor used for config serialization
    public ParticleFrame() {}

    public ParticleFrame(int count, WorldCoordinates pos, WorldCoordinates delta, float speed, ParticleOptions particleOptions, BezierCurve[] bezierCurves) {
        this.count = count;
        this.pos = pos;
        this.delta = delta;
        this.speed = speed;
        this.particleOptions = particleOptions;
        this.bezierCurves = bezierCurves;
    }

    public void sendParticles(CommandSourceStack source, ServerPlayer serverPlayer) {
        Vec3 pos = this.pos.getPosition(source);
        Vec3 delta = this.delta.getPosition(source);
        // TODO: mark fields as final and have appropriate constructor instead?
        if (particleOptions == null) return;
        boolean force = false;

        if (bezierCurves == null || bezierCurves.length == 0) {
            sendParticles(source.getLevel(), serverPlayer, particleOptions, force, pos, count, delta, speed);
        } else {
            for (BezierCurve bezierCurve : bezierCurves) {
                source = source.withPosition(pos);
                assert bezierCurve != null;
                Vec3 start = bezierCurve.p0.getPosition(source);
                Vec3 end = bezierCurve.p3.getPosition(source);
                Vec3 p1 = bezierCurve.p1.getPosition(source);
                Vec3 p2 = bezierCurve.p2.getPosition(source);
                double step = 1f / bezierCurve.steps;
                for (float t = 0; t <= 1; t += step) {
                    Vec3 vec3 = getBezierPoint(start, end, p1, p2, t);
                    sendParticles(source.getLevel(), serverPlayer, particleOptions, force, vec3, count, delta, speed);
                }
            }
        }
    }

    public static void sendParticles(ServerLevel level, ServerPlayer player, ParticleOptions particleOptions, boolean force, Vec3 pos, int count, Vec3 delta, float speed) {
        level.sendParticles(player, particleOptions, force, pos.x, pos.y, pos.z, count, delta.x, delta.y, delta.z, speed);
    }

    public static Vec3 getBezierPoint(Vec3 start, Vec3 end, Vec3 p1, Vec3 p2, float t) {
        double x = Math.pow(1 - t, 3) * start.x + 3 * t * Math.pow(1 - t, 2) * p1.x + 3 * Math.pow(t, 2) * (1 - t) * p2.x + Math.pow(t, 3) * end.x;
        double y = Math.pow(1 - t, 3) * start.y + 3 * t * Math.pow(1 - t, 2) * p1.y + 3 * Math.pow(t, 2) * (1 - t) * p2.y + Math.pow(t, 3) * end.y;
        double z = Math.pow(1 - t, 3) * start.z + 3 * t * Math.pow(1 - t, 2) * p1.z + 3 * Math.pow(t, 2) * (1 - t) * p2.z + Math.pow(t, 3) * end.z;
        return new Vec3(x, y, z);
    }

}
