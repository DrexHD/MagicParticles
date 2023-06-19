package me.drex.magic_particles.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.magic_particles.mixin.WorldCoordinateAccessor;
import me.drex.magic_particles.mixin.WorldCoordinatesAccessor;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;

import java.io.IOException;

public class WorldCoordinatesAdapter extends TypeAdapter<WorldCoordinates> {

    @Override
    public void write(JsonWriter jsonWriter, WorldCoordinates coordinates) throws IOException {
        WorldCoordinatesAccessor accessor = (WorldCoordinatesAccessor) coordinates;
        jsonWriter.value(String.join(" ", asString(accessor.getX()), asString(accessor.getY()), asString(accessor.getZ())));
    }

    @Override
    public WorldCoordinates read(JsonReader jsonReader) throws IOException {
        try {
            return WorldCoordinates.parseDouble(new StringReader(jsonReader.nextString()), true);
        } catch (CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public static String asString(WorldCoordinate worldCoordinate) {
        boolean relative = worldCoordinate.isRelative();
        double value = ((WorldCoordinateAccessor) worldCoordinate).getValue();
        char prefix = WorldCoordinateAccessor.getPrefix();
        return (relative ? String.valueOf(prefix) : "") + value;
    }

}
