package com.bofang.bofangmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class JsonMessageManager {
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask;
    private List<MessageEntry> currentMessages;
    private int currentIndex;
    private MinecraftServer currentServer;
    private Gson gson;
    private boolean isPlaying;

    public JsonMessageManager() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.isPlaying = false;
    }

    public void createExampleJson() {
        try {
            Path textPath = Config.getTextPath();
            if (!Files.exists(textPath)) {
                Files.createDirectories(textPath);
            }
            
            Path exampleFile = textPath.resolve("example.json");
            if (!Files.exists(exampleFile)) {
                List<MessageEntry> example = new ArrayList<>();
                example.add(new MessageEntry("欢迎来到服务器！", 20));
                example.add(new MessageEntry("/say 这是一条指令", 40));
                example.add(new MessageEntry("剧情播放结束", 60));
                
                String json = gson.toJson(example);
                Files.write(exampleFile, json.getBytes());
            }
        } catch (IOException e) {
            BofangMod.LOGGER.error("Failed to create example JSON", e);
        }
    }

    public boolean startPlayback(String fileName, MinecraftServer server) {
        stopPlayback();
        
        try {
            Path jsonFile = Config.getTextPath().resolve(fileName + ".json");
            if (!Files.exists(jsonFile)) {
                return false;
            }
            
            String jsonContent = new String(Files.readAllBytes(jsonFile));
            Type listType = new TypeToken<List<MessageEntry>>(){}.getType();
            List<MessageEntry> messages = gson.fromJson(jsonContent, listType);
            
            if (messages == null || messages.isEmpty()) {
                return false;
            }
            
            this.currentMessages = messages;
            this.currentIndex = 0;
            this.currentServer = server;
            this.isPlaying = true;
            
            scheduleNextMessage();
            return true;
            
        } catch (Exception e) {
            BofangMod.LOGGER.error("Failed to start playback", e);
            return false;
        }
    }

    public void stopPlayback() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
            currentTask = null;
        }
        currentMessages = null;
        currentIndex = 0;
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void scheduleNextMessage() {
        if (!isPlaying || currentMessages == null || currentIndex >= currentMessages.size()) {
            isPlaying = false;
            return;
        }
        
        MessageEntry entry = currentMessages.get(currentIndex);
        currentTask = scheduler.schedule(() -> {
            if (isPlaying) {
                executeMessage(entry);
                currentIndex++;
                scheduleNextMessage();
            }
        }, entry.delay * 50L, TimeUnit.MILLISECONDS);
    }

    private void executeMessage(MessageEntry entry) {
        if (currentServer == null || !isPlaying) return;
        
        if (entry.message.startsWith("/")) {
            String command = entry.message.substring(1);
            currentServer.getCommands().performPrefixedCommand(currentServer.createCommandSourceStack(), command);
        } else {
            currentServer.getPlayerList().broadcastSystemMessage(Component.literal(entry.message), false);
        }
    }

    public static class MessageEntry {
        public String message;
        public int delay;

        public MessageEntry() {}

        public MessageEntry(String message, int delay) {
            this.message = message;
            this.delay = delay;
        }
    }
}