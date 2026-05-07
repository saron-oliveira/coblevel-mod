package com.coblevel.coblevel.event;

import com.coblevel.coblevel.CobLevel;
import com.coblevel.coblevel.cap.PlayerCapCapability;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID)
public class LevelCapEnforcer {

    /**
     * Intercepts Cobblemon's level-up event and cancels it if
     * the new level would exceed the player's current cap.
     */
    public static void registerCobblemonEvents() {
        CobblemonEvents.LEVEL_UP_EVENT.subscribe(event -> {
            // Find the owning player of this Pokémon
            if (event.getPokemon().getOwnerPlayer() instanceof ServerPlayer owner) {
                PlayerCapCapability.get(owner).ifPresent(data -> {
                    int cap = data.getCurrentCap();
                    if (event.getLevel() > cap) {
                        event.cancel();
                        CobLevel.LOGGER.debug(
                            "[CobLevel] Blocked level-up for {}'s Pokémon: {} > cap {}",
                            owner.getName().getString(), event.getLevel(), cap
                        );
                    }
                });
            }
            return kotlin.Unit.INSTANCE;
        });
    }
}
