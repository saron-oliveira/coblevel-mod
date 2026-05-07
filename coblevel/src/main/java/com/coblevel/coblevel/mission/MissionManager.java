package com.coblevel.coblevel.mission;

import com.coblevel.coblevel.cap.CapProgression;
import com.coblevel.coblevel.cap.MissionRequirements;
import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.coblevel.coblevel.cap.PlayerCapData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

public class MissionManager {

    /**
     * Tries to advance the player's level cap if all missions are complete.
     * Call this after any mission progress update.
     */
    public static void tryAdvanceCap(ServerPlayer player) {
        PlayerCapCapability.get(player).ifPresent(data -> {
            if (CapProgression.isMaxCap(data.getCurrentCap())) return;

            int phaseIndex = CapProgression.getPhaseForCap(data.getCurrentCap());
            if (phaseIndex < 0) return;

            if (data.hasCompletedAllMissions(phaseIndex)) {
                int newCap = CapProgression.getNextCap(data.getCurrentCap());
                data.setCurrentCap(newCap);
                data.resetMissionProgress();

                // Notify player
                player.sendSystemMessage(Component.literal(
                    "✦ Level Cap aumentado para " + newCap + "! ✦"
                ).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                if (!CapProgression.isMaxCap(newCap)) {
                    int nextPhase = CapProgression.getPhaseForCap(newCap);
                    MissionRequirements nextReq = MissionRequirements.forPhase(nextPhase);
                    player.sendSystemMessage(Component.literal(
                        "Próximas missões: " + nextReq.describe()
                    ).withStyle(ChatFormatting.AQUA));
                } else {
                    player.sendSystemMessage(Component.literal(
                        "Você atingiu o Level Cap máximo de " + newCap + "! Parabéns!"
                    ).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                }
            }
        });
    }

    /**
     * Sends the current mission progress to the player as a chat message.
     */
    public static void showProgress(ServerPlayer player) {
        PlayerCapCapability.get(player).ifPresent(data -> {
            int cap = data.getCurrentCap();
            int phaseIndex = CapProgression.getPhaseForCap(cap);
            MissionRequirements req = MissionRequirements.forPhase(phaseIndex);

            player.sendSystemMessage(Component.literal(
                "═══ CobLevel — Cap Atual: " + cap + " ═══"
            ).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

            sendMissionLine(player, "Capturas",
                data.getCapturedPokemons(), req.captureCount());
            sendMissionLine(player, "Treinadores",
                data.getDefeatedTrainers(), req.trainerCount());
            sendGymLine(player, data.isGymCleared());
            sendXpLine(player, data.getDeliveredXp(), req.xpAmount());

            if (CapProgression.isMaxCap(cap)) {
                player.sendSystemMessage(Component.literal(
                    "Cap máximo atingido!"
                ).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        });
    }

    private static void sendMissionLine(ServerPlayer player, String label, int current, int required) {
        boolean done = current >= required;
        ChatFormatting color = done ? ChatFormatting.GREEN : ChatFormatting.RED;
        String check = done ? "✔" : "✘";
        player.sendSystemMessage(Component.literal(
            check + " " + label + ": " + current + "/" + required
        ).withStyle(color));
    }

    private static void sendGymLine(ServerPlayer player, boolean cleared) {
        ChatFormatting color = cleared ? ChatFormatting.GREEN : ChatFormatting.RED;
        String check = cleared ? "✔" : "✘";
        player.sendSystemMessage(Component.literal(
            check + " Ginásio no cap atual: " + (cleared ? "Concluído" : "Pendente")
        ).withStyle(color));
    }

    private static void sendXpLine(ServerPlayer player, long current, long required) {
        boolean done = current >= required;
        ChatFormatting color = done ? ChatFormatting.GREEN : ChatFormatting.RED;
        String check = done ? "✔" : "✘";
        player.sendSystemMessage(Component.literal(
            check + " XP entregue: " + current + "/" + required
        ).withStyle(color));
    }
}
