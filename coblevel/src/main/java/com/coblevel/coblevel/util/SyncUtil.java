package com.coblevel.coblevel.util;

import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.coblevel.coblevel.network.CobLevelNetwork;
import com.coblevel.coblevel.network.SyncCapDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class SyncUtil {

    public static void syncToClient(ServerPlayer player) {
        PlayerCapCapability.get(player).ifPresent(data -> {
            SyncCapDataPacket packet = new SyncCapDataPacket(
                data.getCurrentCap(),
                data.getCapturedPokemons(),
                data.getDefeatedTrainers(),
                data.isGymCleared(),
                data.getDeliveredXp()
            );
            CobLevelNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
            );
        });
    }
}
