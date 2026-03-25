import org.gradle.kotlin.dsl.replace

plugins {
    id("dev.kikugie.stonecutter")
    id("org.jetbrains.changelog") version "2.2.0"
}
stonecutter active "1.21.11"

changelog {
    path = rootProject.file("CHANGELOG.md").path
}

stonecutter parameters {
    replacements.string(eval(current.version, "<=1.21.10")) {
        replace("Identifier", "ResourceLocation")
        replace("net.minecraft.util.Util", "net.minecraft.Util")
    }
}