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
                .executes(context -> stopPlayback(context.getSource())))
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

    private static int stopPlayback(CommandSourceStack source) {
        BofangMod.messageManager.stopPlayback();
        return Command.SINGLE_SUCCESS;
    }
}