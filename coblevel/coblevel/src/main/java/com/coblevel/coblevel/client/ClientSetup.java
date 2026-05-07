package com.coblevel.coblevel.client;

import com.coblevel.coblevel.CobLevel;
import com.coblevel.coblevel.client.hud.CapHudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // Register HUD renderer on the Forge event bus (client-side)
        MinecraftForge.EVENT_BUS.register(CapHudRenderer.class);
        CobLevel.LOGGER.info("[CobLevel] HUD renderer registered.");
    }
}
