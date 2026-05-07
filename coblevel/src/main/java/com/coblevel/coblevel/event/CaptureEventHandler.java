package com.coblevel.coblevel.event;

import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.coblevel.coblevel.mission.MissionManager;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.coblevel.coblevel.CobLevel;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID)
public class CaptureEventHandler {

    /**
     * Called when a player successfully captures a Pokémon.
     * Uses Cobblemon's Kotlin event via a static registration in CobLevel init.
     */
    public static void registerCobblemonEvents() {
        // Cobblemon uses Kotlin events; we register them here
        CobblemonEvents.POKEMON_CAPTURED.subscribe(event -> {
            if (event.getPlayer() instanceof ServerPlayer serverPlayer) {
                PlayerCapCapability.get(serverPlayer).ifPresent(data -> {
                    data.addCapturedPokemon();
                    CobLevel.LOGGER.debug(
                        "[CobLevel] {} captured a Pokémon (total: {})",
                        serverPlayer.getName().getString(),
                        data.getCapturedPokemons()
                    );
                });
                MissionManager.tryAdvanceCap(serverPlayer);
            }
            return kotlin.Unit.INSTANCE;
        });
    }
}
