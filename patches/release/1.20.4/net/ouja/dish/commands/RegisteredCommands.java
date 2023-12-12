package net.ouja.dish.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.ouja.api.commands.CommandListener;
import net.ouja.dish.Dish;
import net.ouja.dish.entity.DishPlayer;
import net.ouja.dish.network.chat.DishComponent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RegisteredCommands {
    private static final HashMap<String, Method> names = new HashMap<>();
    private static final HashMap<String, Class<?>> classes = new HashMap<>();

    public RegisteredCommands(CommandListener commandListener, Method method, Class<?> commandClass) {
        String name = commandListener.name();
        if (names.get(name) != null) {
            Dish.logger.warn(String.format("[Warning] Command name '%s' already exists. Changing name to '%s%s'%n", name, name, names.size() + 1));
            name = name + (names.size() + 1);
        }

        names.put(name, method);
        classes.put(name, commandClass);

        String finalName = name;
        Commands.getDispatcher().register(Commands.literal(name).executes((cmd) -> {
            DishPlayer player = new DishPlayer(cmd.getSource().getPlayer());
            try {
                return runCommand(finalName, player) ? 1 : 0;
            } catch (Exception e) {
                Component component = Component.literal("An error occurred when running this command").withStyle(ChatFormatting.RED);
                player.sendMessage(DishComponent.fromComponent(component).setColor(String.valueOf(ChatFormatting.RED.getColor().intValue())));
                return 0;
            }
        }));
    }

    public static boolean runCommand(String commandName, DishPlayer player) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Method method = names.get(commandName);
        Class<?> clazz = classes.get(commandName);
        if (method == null || clazz == null) return false;

        method.setAccessible(true);
        return (boolean)method.invoke(clazz.newInstance(), player);
    }
}
