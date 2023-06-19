package me.drex.magic_particles.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import java.io.IOException;

public class ParticleOptionsAdapter extends TypeAdapter<ParticleOptions> {

    @Override
    public void write(JsonWriter jsonWriter, ParticleOptions particleOptions) throws IOException {
        jsonWriter.value(particleOptions.writeToString());
    }

    @Override
    public ParticleOptions read(JsonReader jsonReader) throws IOException {
        try {
            return ParticleArgument.readParticle(new StringReader(jsonReader.nextString()), BuiltInRegistries.PARTICLE_TYPE.asLookup());
        } catch (CommandSyntaxException ex) {
            throw new IOException(ex);
        }
    }
}
