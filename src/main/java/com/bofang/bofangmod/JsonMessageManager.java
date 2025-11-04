package com.bofang.bofangmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class JsonMessageManager {
    private final ScheduledExecutorService scheduler;
    private final Gson gson;
    private final ConcurrentHashMap<String, PlaybackSession> activeSessions;

    public JsonMessageManager() {
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.activeSessions = new ConcurrentHashMap<>();
    }

    public void createExampleJson() {
        try {
            Path textPath = Config.getTextPath();
            if (!Files.exists(textPath)) {
                Files.createDirectories(textPath);
            }
            
            // 只创建基础的Aexample和Bexample用于演示
            Path aExampleFile = textPath.resolve("Aexample.json");
            if (!Files.exists(aExampleFile)) {
                List<MessageEntry> aExample = new ArrayList<>();
                aExample.add(new MessageEntry("§6§l新增或修改JSON后于游戏都要进行reload喵§r", 20));
                aExample.add(new MessageEntry("/say §a这是一条指令§r", 20));
                aExample.add(new MessageEntry("§e文本tick相互独立，20tick=1s§r", 20));
                
                String json = gson.toJson(aExample);
                Files.write(aExampleFile, json.getBytes(StandardCharsets.UTF_8));
            }
            
            Path bExampleFile = textPath.resolve("Bexample.json");
            if (!Files.exists(bExampleFile)) {
                List<MessageEntry> bExample = new ArrayList<>();
                bExample.add(new MessageEntry("/say §b除了title这种要霸占的指令基本都支持§r", 30));
                bExample.add(new MessageEntry("/say §d这个实例是来测试指令运行的,接下来将执行Aexample的文本指令§r", 30));
                bExample.add(new MessageEntry("/binsay start Aexample", 30));
                
                String json = gson.toJson(bExample);
                Files.write(bExampleFile, json.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // 不打印日志
        }
    }

    public boolean startPlayback(String fileName, MinecraftServer server) {
        try {
            Path jsonFile = Config.getTextPath().resolve(fileName + ".json");
            if (!Files.exists(jsonFile)) {
                return false;
            }
            
            // 使用UTF-8编码读取文件，解决中文乱码问题
            String jsonContent = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<MessageEntry>>(){}.getType();
            List<MessageEntry> messages = gson.fromJson(jsonContent, listType);
            
            if (messages == null || messages.isEmpty()) {
                return false;
            }
            
            PlaybackSession session = new PlaybackSession(fileName, messages, server);
            activeSessions.put(fileName, session);
            session.start();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    public void stopPlayback(String fileName) {
        PlaybackSession session = activeSessions.get(fileName);
        if (session != null) {
            session.stop();
            activeSessions.remove(fileName);
        }
    }

    public void stopAllPlaybacks() {
        for (PlaybackSession session : activeSessions.values()) {
            session.stop();
        }
        activeSessions.clear();
    }

    public boolean isPlaying(String fileName) {
        PlaybackSession session = activeSessions.get(fileName);
        return session != null && session.isPlaying();
    }

    private class PlaybackSession {
        private final String name;
        private final List<MessageEntry> messages;
        private final MinecraftServer server;
        private ScheduledFuture<?> currentTask;
        private int currentIndex;
        private boolean isPlaying;

        public PlaybackSession(String name, List<MessageEntry> messages, MinecraftServer server) {
            this.name = name;
            this.messages = messages;
            this.server = server;
            this.currentIndex = 0;
            this.isPlaying = false;
        }

        public void start() {
            this.isPlaying = true;
            scheduleNextMessage();
        }

        public void stop() {
            this.isPlaying = false;
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(false);
                currentTask = null;
            }
        }

        public boolean isPlaying() {
            return isPlaying;
        }

        private void scheduleNextMessage() {
            if (!isPlaying || currentIndex >= messages.size()) {
                isPlaying = false;
                activeSessions.remove(name);
                return;
            }
            
            MessageEntry entry = messages.get(currentIndex);
            currentTask = scheduler.schedule(() -> {
                if (isPlaying) {
                    executeMessage(entry);
                    currentIndex++;
                    scheduleNextMessage();
                }
            }, entry.delay * 50L, TimeUnit.MILLISECONDS);
        }

        private void executeMessage(MessageEntry entry) {
            if (server == null || !isPlaying) return;
            
            if (entry.message.startsWith("/")) {
                String command = entry.message.substring(1);
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
            } else {
                server.getPlayerList().broadcastSystemMessage(Component.literal(entry.message), false);
            }
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