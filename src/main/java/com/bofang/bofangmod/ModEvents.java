package com.bofang.bofangmod;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BofangMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {
    
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        BofangMod.LOGGER.info("剧情播放器: 配置文件已重载");
    }
}