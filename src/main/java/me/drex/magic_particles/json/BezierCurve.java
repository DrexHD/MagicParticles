package me.drex.magic_particles.json;

import net.minecraft.commands.arguments.coordinates.WorldCoordinates;

public class BezierCurve {

    public WorldCoordinates p0; // start
    public WorldCoordinates p3; // end
    public WorldCoordinates p1;
    public WorldCoordinates p2;
    public int steps = 50;

    // Constructor used for config serialization
    public BezierCurve() {}

    public BezierCurve(WorldCoordinates p0, WorldCoordinates p3, WorldCoordinates p1, WorldCoordinates p2) {
        this.p0 = p0;
        this.p3 = p3;
        this.p1 = p1;
        this.p2 = p2;
    }

}
