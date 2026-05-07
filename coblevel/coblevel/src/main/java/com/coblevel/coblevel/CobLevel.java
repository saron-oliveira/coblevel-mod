package com.coblevel.coblevel;

import com.coblevel.coblevel.client.ClientSetup;
import com.coblevel.coblevel.command.CobLevelCommands;
import com.coblevel.coblevel.event.BattleEventHandler;
import com.coblevel.coblevel.event.CaptureEventHandler;
import com.coblevel.coblevel.event.LevelCapEnforcer;
import com.coblevel.coblevel.network.CobLevelNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CobLevel.MOD_ID)
public class CobLevel {

    public static final String MOD_ID = "coblevel";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CobLevel() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(BattleEventHandler.class);
        MinecraftForge.EVENT_BUS.register(CaptureEventHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CobLevelNetwork.register();

        // Register Cobblemon Kotlin events
        CaptureEventHandler.registerCobblemonEvents();
        BattleEventHandler.registerCobblemonEvents();
        LevelCapEnforcer.registerCobblemonEvents();

        LOGGER.info("CobLevel initialized successfully.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CobLevelCommands.register(event.getDispatcher());
    }
}
