package me.drex.magic_particles.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import eu.pb4.playerdata.api.PlayerDataApi;
import me.drex.magic_particles.json.MagicParticle;
import me.drex.magic_particles.particles.ParticleManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.*;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.magic_particles.MagicParticlesMod.PARTICLE;

public class MagicParticlesCommand {

    private static final Predicate<CommandSourceStack> ROOT_PREDICATE = Permissions.require("magic-particles.root", 2);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_MAGIC_PARTICLES = (context, builder) -> SharedSuggestionProvider.suggest(ParticleManager.INSTANCE.particleMap().keySet(), builder);

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
        Map<String, MagicParticle> particles = ParticleManager.INSTANCE.particleMap();
        MutableComponent component = Component.translatable("text.magic_particles.title",
                        Component.literal(String.valueOf(particles.size())).withStyle(ChatFormatting.YELLOW)
                ).withStyle(ChatFormatting.GOLD)
                .append(
                        Component.translatable("text.magic_particles.disable")
                                .withStyle(ChatFormatting.RED)
                                .withStyle(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp disable"))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("text.magic_particles.disable.hover").withStyle(ChatFormatting.RED)))
                                )
                ).append(Component.literal(" | ").withStyle(ChatFormatting.GRAY));
        component.append(ComponentUtils.formatList(particles.entrySet().stream().sorted(Comparator.comparing(o -> o.getValue().name)).toList(), entry ->
                Component.literal(entry.getValue().name)
                        .withStyle(ChatFormatting.GREEN)
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.literal(entry.getValue().name)
                                                        .withStyle(ChatFormatting.GOLD)
                                                        .append("\n")
                                                        .append(Component.literal(entry.getKey()).withStyle(ChatFormatting.DARK_GRAY))
                                                        .append("\n")
                                                        .append(Component.translatable("text.magic_particles.particle.hover").withStyle(ChatFormatting.GREEN))
                                        )
                                ).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp set " + entry.getKey()))
                        ))
        );
        ctx.getSource().sendSuccess(() -> component, false);
        return particles.size();
    }


    private static int reload(CommandContext<CommandSourceStack> ctx) {
        if (ParticleManager.INSTANCE.load()) {
            // TODO: feedback
        } else {
            // TODO: feedback
        }
        return 1;
    }

    private static int setParticle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String particle = StringArgumentType.getString(ctx, "particle");
        if (!ParticleManager.INSTANCE.particleMap().containsKey(particle)) {
            // TODO: send feedback
            //ctx.getSource().sendFailure(Component.translatable(translationKey("unknown")));
            return 0;
        } else {
            PlayerDataApi.setGlobalDataFor(ctx.getSource().getPlayerOrException(), PARTICLE, StringTag.valueOf(particle));
            // TODO: send feedback
            //ctx.getSource().sendSuccess(Component.translatable(translationKey("set"), ConfigManager.INSTANCE.config().magicParticle.particles.get(particle).name), false);
            return 1;
        }
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        PlayerDataApi.setGlobalDataFor(ctx.getSource().getPlayerOrException(), PARTICLE, null);
        // TODO: send feedback
        return 1;
    }
}
