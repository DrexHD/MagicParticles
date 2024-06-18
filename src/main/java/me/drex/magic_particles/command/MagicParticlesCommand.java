package me.drex.magic_particles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.drex.magic_particles.particles.MagicParticle;
import me.drex.magic_particles.particles.ParticleManager;
import me.drex.message.api.LocalizedMessage;
import me.drex.message.api.MessageAPI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;
import java.util.function.Predicate;

import static me.drex.magic_particles.MagicParticlesMod.PARTICLE;

public class MagicParticlesCommand {

    private static final Predicate<CommandSourceStack> ROOT_PREDICATE = Permissions.require("magic-particles.root", 2);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_MAGIC_PARTICLES = (context, builder) -> SharedSuggestionProvider.suggest(ParticleManager.particles().keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> rootNode = dispatcher.register(
            Commands.literal("magic-particles").requires(ROOT_PREDICATE)
                .executes(MagicParticlesCommand::sendList)
                .then(
                    Commands.literal("set")
                        .requires(Permissions.require("magic-particles.particle", 2))
                        .then(
                            Commands.argument("particle", StringArgumentType.string()).suggests(SUGGEST_MAGIC_PARTICLES)
                                .executes(MagicParticlesCommand::setParticle)
                        )
                ).then(
                    Commands.literal("disable")
                        .requires(Permissions.require("magic-particles.disable", 2))
                        .executes(MagicParticlesCommand::disable)
                ).then(
                    Commands.literal("reload")
                        .requires(Permissions.require("magic-particles.reload", 2))
                        .executes(MagicParticlesCommand::reload)
                )
        );

        dispatcher.register(Commands.literal("mp").executes(MagicParticlesCommand::sendList)
            .requires(ROOT_PREDICATE).redirect(rootNode));
    }

    private static int sendList(CommandContext<CommandSourceStack> ctx) {
        Map<String, MagicParticle> particles = ParticleManager.particles();

        MutableComponent list = ComponentUtils.formatList(
            particles.entrySet(),
            LocalizedMessage.localized("text.magic_particles.overview.list.separator"),
            particleEntry -> LocalizedMessage.builder("text.magic_particles.overview.list.element")
                .addPlaceholder("id", particleEntry.getKey())
                .addPlaceholder("name", particleEntry.getValue().name())
                .build()
        );

        ctx.getSource().sendSuccess(() -> LocalizedMessage.builder("text.magic_particles.overview")
            .addPlaceholder("count", particles.size())
            .addPlaceholder("list", list)
            .build(), false);
        return particles.size();
    }


    private static int reload(CommandContext<CommandSourceStack> ctx) {
        MessageAPI.reload();
        if (ParticleManager.load()) {
            Map<String, MagicParticle> particles = ParticleManager.particles();
            ctx.getSource().sendSuccess(() -> LocalizedMessage.localized("text.magic_particles.reload"), false);
            return particles.size();
        } else {
            ctx.getSource().sendFailure(LocalizedMessage.localized("text.magic_particles.error"));
        }
        return 1;
    }

    private static int setParticle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String particle = StringArgumentType.getString(ctx, "particle");
        if (!ParticleManager.particles().containsKey(particle)) {
            ctx.getSource().sendFailure(LocalizedMessage.localized("text.magic_particles.unknown"));
            return 0;
        } else {
            PlayerDataApi.setGlobalDataFor(ctx.getSource().getPlayerOrException(), PARTICLE, StringTag.valueOf(particle));
            String name = ParticleManager.particles().get(particle).name();
            ctx.getSource().sendSuccess(() -> LocalizedMessage.builder("text.magic_particles.set").addPlaceholder("id", particle).addPlaceholder("name", name).build(), false);
            return 1;
        }
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerDataApi.setGlobalDataFor(ctx.getSource().getPlayerOrException(), PARTICLE, null);
        ctx.getSource().sendSuccess(() -> LocalizedMessage.localized("text.magic_particles.unset"), false);
        return 1;
    }
}
