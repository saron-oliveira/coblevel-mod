package com.coblevel.coblevel.event;

import com.coblevel.coblevel.CobLevel;
import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.coblevel.coblevel.cap.CapProgression;
import com.coblevel.coblevel.mission.MissionManager;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID)
public class BattleEventHandler {

    /** How many levels above the player cap the gym's top Pokémon can be. */
    public static final int GYM_LEVEL_TOLERANCE = 6;

    /**
     * Registers Cobblemon battle-end events.
     * Gym counts ONLY if the gym's highest-level Pokémon is within
     * [playerCap, playerCap + GYM_LEVEL_TOLERANCE].
     */
    public static void registerCobblemonEvents() {
        CobblemonEvents.BATTLE_VICTORY.subscribe(event -> {

            for (BattleActor winner : event.getWinners()) {
                if (!(winner instanceof PlayerBattleActor playerActor)) continue;

                ServerPlayer serverPlayer = playerActor.getEntity() instanceof ServerPlayer sp ? sp : null;
                if (serverPlayer == null) continue;

                // Only care about NPC battles
                boolean isNpcBattle = event.getLosers().stream()
                        .anyMatch(loser -> !(loser instanceof PlayerBattleActor));
                if (!isNpcBattle) continue;

                // Retrieve player's current cap
                int[] capHolder = {CapProgression.INITIAL_CAP};
                PlayerCapCapability.get(serverPlayer).ifPresent(data -> capHolder[0] = data.getCurrentCap());
                int playerCap = capHolder[0];

                boolean isGym    = isGymActor(event);
                boolean validGym = isGym && isGymLevelValid(event, playerCap);

                PlayerCapCapability.get(serverPlayer).ifPresent(data -> {
                    if (isGym) {
                        if (validGym) {
                            data.setGymCleared(true);
                            serverPlayer.sendSystemMessage(Component.literal(
                                "✔ Ginásio concluído! (nível compatível com cap " + playerCap + ")"
                            ).withStyle(ChatFormatting.GREEN));
                            CobLevel.LOGGER.debug("[CobLevel] {} cleared a valid gym at cap {}",
                                serverPlayer.getName().getString(), playerCap);
                        } else {
                            int min = playerCap;
                            int max = playerCap + GYM_LEVEL_TOLERANCE;
                            serverPlayer.sendSystemMessage(Component.literal(
                                "✘ Ginásio não contou! O nível máximo do ginásio deve ser entre "
                                + min + " e " + max + " (seu cap atual é " + playerCap + ")."
                            ).withStyle(ChatFormatting.RED));
                        }
                    } else {
                        data.addDefeatedTrainer();
                        CobLevel.LOGGER.debug("[CobLevel] {} defeated trainer #{}",
                            serverPlayer.getName().getString(), data.getDefeatedTrainers());
                    }
                });

                MissionManager.tryAdvanceCap(serverPlayer);
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    /** Returns true if any losing actor is a gym NPC. */
    private static boolean isGymActor(
            com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent event) {
        return event.getLosers().stream().anyMatch(loser -> {
            String t = loser.getClass().getSimpleName().toLowerCase();
            return t.contains("gym") || t.contains("cobgym");
        });
    }

    /**
     * Returns true if the gym's highest-level Pokémon is within
     * [playerCap, playerCap + GYM_LEVEL_TOLERANCE].
     */
    private static boolean isGymLevelValid(
            com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent event,
            int playerCap) {
        return event.getLosers().stream().anyMatch(loser -> {
            String t = loser.getClass().getSimpleName().toLowerCase();
            if (!t.contains("gym") && !t.contains("cobgym")) return false;

            int maxLevel = loser.getPokemonList().stream()
                    .mapToInt(bp -> bp.getEffectedPokemon().getLevel())
                    .max().orElse(0);

            CobLevel.LOGGER.debug("[CobLevel] Gym top level: {}, range: [{}, {}]",
                maxLevel, playerCap, playerCap + GYM_LEVEL_TOLERANCE);

            return maxLevel >= playerCap && maxLevel <= playerCap + GYM_LEVEL_TOLERANCE;
        });
    }
}
