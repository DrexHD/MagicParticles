# Magic Particles

[![Discord](https://img.shields.io/discord/904419828192927885.svg?logo=discord)](https://discord.gg/HeZayd6SxF)

A fabric server side cosmetics mod, that allows administrator to create custom particles using simple json files

## Commands
- `/mp` - List all available magic particles - `magic-particles.root`
- `/mp set <id>` - Set current magic particle -  `magic-particles.particle`
- `/mp disable` - Disable current magic particle - `magic-particles.disable`
- `/mp reload` - Reload magic particles and messages - `magic-particles.reload`

By default, players have access to all magic particles (assuming they have the permissions required to run the command).
Magic particles can be *disabled* for specific players by denying the `magic-particles.particle.<id>` permission.

## Custom Particles

This mod includes more than [40 default particles](https://github.com/DrexHD/MagicParticles/tree/1.21/src/main/resources/magic-particles), which can be useful example
files for understanding the format.

## JSON Format

Each magic particle type needs a human friendly name and a list of particles it consists of
### Root
```json5
{
  "name": "Some name", // Human friendly name
  "particles": [
    // Particle definitions
  ]
}
```

### Particle definition
There are currently 3 particle definition types (simple, image and bezier). All of these share a couple of common 
values in addition to their specialized ones.

#### Simple
```json5
{
  "type": "simple",
  "count": 1, // Amount of particles (optional)
  "pos": [1.0, 2.0, 0.0], // Offset from origin (affected by rotation)
  "delta": [0.1, 0.1, 0.1], // Particle area size (optional)
  "speed": 0, // Particle speed parameter, used by some particle types (optional)
  "particle_type": {
    "type": "minecraft:angry_villager" // Particle type: https://minecraft.wiki/w/Particles_(Java_Edition)#Types_of_particles
    // Some particles types have extra attributes, which need to be specified here
  },
  "anchor": "feet", // The particle origin ["feet"|"eyes"] (optional)
  "origin": [0.0, 0.5, 0.0], // Offset from origin (optional)
  "billboard": "fixed" // What rotation axis should affect "pos" ["fixed", "vertical", "horizontal", "center"] (optional)
}
```

#### Image
```json5
{
  "type": "image",
  "image": "drex.png", // Path to the image, the image needs to be in config/magic-particles/images/<image>
  "size_x": 0.8, // The width of the particle image
  "size_y": 0.8, // The height of the particle image
  "pixel_size": 0.6, // The size of each individual particle
  "pos": [1.0, 2.0, 0.0], // Offset from origin (affected by rotation)
  "anchor": "feet", // The particle origin ["feet"|"eyes"] (optional)
  "origin": [0.0, 0.5, 0.0], // Offset from origin (optional)
  "billboard": "fixed" // What rotation axis should affect "pos" ["fixed", "vertical", "horizontal", "center"] (optional)
}
```

#### Bezier
*(Can't really recommend to use this, creating an image of the desired shape and using the image type is easier)*