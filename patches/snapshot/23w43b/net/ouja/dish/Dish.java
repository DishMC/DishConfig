package net.ouja.dish;

import com.mojang.logging.LogUtils;
import net.minecraft.server.dedicated.DedicatedServer;
import net.ouja.api.DishAPI;
import net.ouja.api.Player;
import net.ouja.api.Server;
import net.ouja.api.commands.Command;
import net.ouja.api.commands.CommandListener;
import net.ouja.api.dedicated.ServerProperties;
import net.ouja.api.event.EventHandler;
import net.ouja.api.event.EventListener;
import net.ouja.api.network.chat.Component;
import net.ouja.dish.commands.RegisteredCommands;
import net.ouja.dish.entity.DishPlayer;
import net.ouja.dish.plugins.PluginManager;
import net.ouja.dish.plugins.RegisteredEvents;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Dish implements Server {
    private final DedicatedServer server;
    private String dishVersion = "UNKNOWN";
    public static final Logger logger = LogUtils.getLogger();

    public Dish(DedicatedServer server) {
        this.server = server;
        DishAPI.setServer(this);
        getBuild();
        logger.info(String.format("Launching Dish server with the version %s (API Version: %s)", dishVersion, DishAPI.getApiVersion()));
    }

    public void getBuild() {
        try {
            InputStream in = Dish.class.getResourceAsStream("/build.txt");
            if (in == null) return;

            dishVersion = readLines(in)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String[] readLines(InputStream is) throws IOException {
        List<String> list = new ArrayList<>();
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII);
        BufferedReader br = new BufferedReader(isr);

        while(true) {
            String line = br.readLine();
            if (line == null) {
                return list.toArray(new String[0]);
            }

            list.add(line);
        }
    }

    public void initializePlugins() {
        try {
            new PluginManager(new File("plugins"));
        } catch (IOException e) {
            e.printStackTrace(); // todo make an error handler
        }
    }

    @Override
    public @NotNull String getVersion() {
        return this.server.getServerVersion();
    }

    @Override
    public @NotNull ServerProperties getProperties() {
        return new DishProperties(this.server.getProperties());
    }

    @Override
    public @NotNull String getDishVersion() {
        return this.dishVersion;
    }

    @Override
    public @NotNull ArrayList<Player> getOnlinePlayers() {
        return null;
    }

    @Override
    public void registerEvent(EventListener eventListener, Class<?> aClass) {
        for (Method method : eventListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                new RegisteredEvents(method, aClass);
            }
        }
    }

    @Override
    public void registerCommand(Command command) {
        for (Method method : command.getClass().getMethods()) {
            if (method.isAnnotationPresent(CommandListener.class)) {
                CommandListener commandListener = method.getAnnotation(CommandListener.class);
                if (commandListener != null && commandListener.name() != "") {
                    new RegisteredCommands(commandListener.name(), method, command.getClass());
                }
            }
        }
    }

    @Override
    public void broadcast(Component component) {
        for (Player player : DishPlayer.CACHED_PLAYERS.values()) {
            player.sendMessage(component);
        }
    }
}