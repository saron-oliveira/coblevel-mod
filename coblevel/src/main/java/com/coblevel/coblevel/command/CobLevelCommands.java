package com.coblevel.coblevel.command;

import com.coblevel.coblevel.cap.CapProgression;
import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.coblevel.coblevel.mission.MissionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

public class CobLevelCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(Commands.literal("coblevel")

            // /coblevel progress — shows mission progress
            .then(Commands.literal("progress")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    MissionManager.showProgress(player);
                    return 1;
                })
            )

            // /coblevel deliverxp <amount> — player manually delivers XP toward mission
            .then(Commands.literal("deliverxp")
                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        long amount = LongArgumentType.getLong(ctx, "amount");

                        // Consume player's XP levels as currency (1 XP level = 10 XP points)
                        int playerXpLevels = player.experienceLevel;
                        long maxDeliverable = playerXpLevels * 10L;
                        long toDeliver = Math.min(amount, maxDeliverable);

                        if (toDeliver <= 0) {
                            player.sendSystemMessage(Component.literal(
                                "Você não tem XP suficiente para entregar!"
                            ).withStyle(ChatFormatting.RED));
                            return 0;
                        }

                        int levelsToConsume = (int) Math.ceil(toDeliver / 10.0);
                        player.giveExperienceLevels(-levelsToConsume);

                        PlayerCapCapability.get(player).ifPresent(data -> data.addDeliveredXp(toDeliver));

                        player.sendSystemMessage(Component.literal(
                            "Entregou " + toDeliver + " XP! (" + levelsToConsume + " níveis consumidos)"
                        ).withStyle(ChatFormatting.AQUA));

                        MissionManager.tryAdvanceCap(player);
                        return 1;
                    })
                )
            )

            // Admin subcommands (requires op level 2)
            .then(Commands.literal("admin")
                .requires(src -> src.hasPermission(2))

                // /coblevel admin setcap <player> <cap>
                .then(Commands.literal("setcap")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("cap", IntegerArgumentType.integer(
                                CapProgression.INITIAL_CAP, CapProgression.MAX_CAP))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                int newCap = IntegerArgumentType.getInteger(ctx, "cap");

                                PlayerCapCapability.get(target).ifPresent(data -> {
                                    data.setCurrentCap(newCap);
                                    data.resetMissionProgress();
                                });

                                ctx.getSource().sendSuccess(() -> Component.literal(
                                    "Cap de " + target.getName().getString() + " definido para " + newCap
                                ).withStyle(ChatFormatting.GREEN), true);

                                target.sendSystemMessage(Component.literal(
                                    "Seu level cap foi definido para " + newCap + " por um admin."
                                ).withStyle(ChatFormatting.YELLOW));
                                return 1;
                            })
                        )
                    )
                )

                // /coblevel admin resetmissions <player>
                .then(Commands.literal("resetmissions")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                            PlayerCapCapability.get(target).ifPresent(PlayerCapCapability.PlayerCapProvider -> {
                                // handled via capability reference
                            });
                            PlayerCapCapability.get(target).ifPresent(data -> data.resetMissionProgress());

                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "Missões de " + target.getName().getString() + " resetadas."
                            ).withStyle(ChatFormatting.GREEN), true);
                            return 1;
                        })
                    )
                )

                // /coblevel admin info <player>
                .then(Commands.literal("info")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                            MissionManager.showProgress(target);
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("Progresso de " + target.getName().getString() + " exibido no chat deles.")
                                    .withStyle(ChatFormatting.GRAY), false);
                            return 1;
                        })
                    )
                )
            )
        );
    }
}
