package com.coblevel.coblevel.cap;

import com.coblevel.coblevel.CobLevel;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CobLevel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistrar {

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PlayerCapData.class);
    }
}
