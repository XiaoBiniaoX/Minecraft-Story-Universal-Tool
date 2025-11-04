package com.bofang.bofangmod;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandBinSay {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("binsay")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("start")
                .then(Commands.argument("filename", StringArgumentType.string())
                    .executes(context -> {
                        String fileName = StringArgumentType.getString(context, "filename");
                        return startPlayback(context.getSource(), fileName);
                    })))
            .then(Commands.literal("stop")
                .executes(context -> stopAllPlayback(context.getSource()))
                .then(Commands.argument("filename", StringArgumentType.string())
                    .executes(context -> {
                        String fileName = StringArgumentType.getString(context, "filename");
                        return stopPlayback(context.getSource(), fileName);
                    })))
        );
    }

    private static int startPlayback(CommandSourceStack source, String fileName) {
        boolean success = BofangMod.messageManager.startPlayback(fileName, source.getServer());
        if (!success) {
            source.sendFailure(Component.literal("找不到你要的 " + fileName + " 喵"));
            return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int stopPlayback(CommandSourceStack source, String fileName) {
        BofangMod.messageManager.stopPlayback(fileName);
        source.sendSuccess(() -> Component.literal("已停止播放: " + fileName), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopAllPlayback(CommandSourceStack source) {
        BofangMod.messageManager.stopAllPlaybacks();
        source.sendSuccess(() -> Component.literal("已停止所有播放"), true);
        return Command.SINGLE_SUCCESS;
    }
}