package com.bofang.bofangmod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BofangMod.MODID)
public class BofangMod {
    public static final String MODID = "bofangmod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static JsonMessageManager messageManager;

    public BofangMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        messageManager = new JsonMessageManager();
        messageManager.createExampleJson();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandBinSay.register(event.getDispatcher());
    }
}