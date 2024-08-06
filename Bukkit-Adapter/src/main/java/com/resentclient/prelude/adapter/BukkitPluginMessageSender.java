/*
 * Prelude-API is a plugin to implement features for the Client.
 * Copyright (C) 2024 cire3, Preva1l
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.resentclient.prelude.adapter;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

/*
* Based largely off https://github.com/BadlionClient/BadlionClientModAPI/blob/master/bukkit/src/main/java/net/badlion/bukkitapi/BukkitPluginMessageSender.java
* */
public class BukkitPluginMessageSender extends AbstractBukkitPluginMessageSender {
    @Getter private static BukkitPluginMessageSender instance;

    private static Logger logger;
    private static String versionSuffix;
    private static Method getHandleMethod;

    private static Field playerConnectionField;
    private static Method sendPacketMethod;

    private static Constructor<?> packetPlayOutCustomPayloadConstructor;
    private static Constructor<?> packetPlayOutMinecraftKeyConstructor;
    private static boolean useMinecraftKey;

    // Bukkit 1.8+ support
    private static Class<?> packetDataSerializerClass;
    private static Constructor<?> packetDataSerializerConstructor;

    private static Method wrappedBufferMethod;

    public BukkitPluginMessageSender(Logger _logger) {
        instance = this;

        logger = _logger;

        // Get the v1_X_Y from the end of the package name, e.g. v_1_7_R4 or v_1_12_R1
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        if (parts.length > 0) {
            String suffix = parts[parts.length - 1];
            if (!suffix.startsWith("v")) {
                if (suffix.equals("mockbukkit")) {
                    // running tests, don't bother, none of the following packages/classes/methods/etc exist
                    return;
                }

                throw new RuntimeException("Failed to find version for running Minecraft server, got suffix " + suffix);
            }

            versionSuffix = suffix;

            logger.info("Found version " + versionSuffix);
        }

        // We need to use reflection because Bukkit by default handles plugin messages in a really silly way
        Class<?> craftPlayerClass = getClass("org.bukkit.craftbukkit." + versionSuffix + ".entity.CraftPlayer");
        if (craftPlayerClass == null) {
            throw new RuntimeException("Failed to find CraftPlayer class");
        }

        Class<?> nmsPlayerClass = getClass("net.minecraft.server." + versionSuffix + ".EntityPlayer");
        if (nmsPlayerClass == null) {
            throw new RuntimeException("Failed to find EntityPlayer class");
        }

        Class<?> playerConnectionClass = getClass("net.minecraft.server." + versionSuffix + ".PlayerConnection");
        if (playerConnectionClass == null) {
            throw new RuntimeException("Failed to find PlayerConnection class");
        }

        Class<?> packetPlayOutCustomPayloadClass = getClass("net.minecraft.server." + versionSuffix + ".PacketPlayOutCustomPayload");
        if (packetPlayOutCustomPayloadClass == null) {
            throw new RuntimeException("Failed to find PacketPlayOutCustomPayload class");
        }

        packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, String.class, byte[].class);
        if (packetPlayOutCustomPayloadConstructor == null) {
            // Newer versions of Minecraft use a different custom packet system
            packetDataSerializerClass = getClass("net.minecraft.server." + versionSuffix + ".PacketDataSerializer");
            if (packetDataSerializerClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer class");
            }

            // Netty classes used by newer 1.8 and newer
            Class<?> byteBufClass = getClass("io.netty.buffer.ByteBuf");
            if (byteBufClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or ByteBuf class");
            }

            packetDataSerializerConstructor = getConstructor(packetDataSerializerClass, byteBufClass);
            if (packetDataSerializerConstructor == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer constructor");
            }

            Class<?> unpooledClass = getClass("io.netty.buffer.Unpooled");
            if (unpooledClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or Unpooled class");
            }

            wrappedBufferMethod = getMethod(unpooledClass, "wrappedBuffer", byte[].class);
            if (wrappedBufferMethod == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or wrappedBuffer()");
            }

            // If we made it this far in theory we are on at least 1.8
            packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, String.class, packetDataSerializerClass);
            if (packetPlayOutCustomPayloadConstructor == null) {
                Class<?> minecraftKeyClass = getClass("net.minecraft.server." + versionSuffix + ".MinecraftKey");

                // Fix for Paper in newer versions
                packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, minecraftKeyClass, packetDataSerializerClass);

                if (packetPlayOutCustomPayloadConstructor == null) {
                    throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor 2x");
                } else {
                    useMinecraftKey = true;
                    packetPlayOutMinecraftKeyConstructor = getConstructor(minecraftKeyClass, String.class);
                }
            }
        }

        getHandleMethod = getMethod(craftPlayerClass, "getHandle");
        if (getHandleMethod == null) {
            throw new RuntimeException("Failed to find CraftPlayer.getHandle()");
        }

        playerConnectionField = getField(nmsPlayerClass, "playerConnection");
        if (playerConnectionField == null) {
            throw new RuntimeException("Failed to find EntityPlayer.playerConnection");
        }

        sendPacketMethod = getMethod(playerConnectionClass, "sendPacket");
        if (sendPacketMethod == null) {
            throw new RuntimeException("Failed to find PlayerConnection.sendPacket()");
        }
    }

    @Override
    public void sendPluginMessagePacket(Player player, String channel, Object data) {
        try {
            Object packet;
            // Newer MC version, setup ByteBuf object
            if (packetDataSerializerClass != null) {
                Object byteBuf = wrappedBufferMethod.invoke(null, data);
                Object packetDataSerializer = packetDataSerializerConstructor.newInstance(byteBuf);

                if (useMinecraftKey) {
                    Object key = packetPlayOutMinecraftKeyConstructor.newInstance(channel);
                    packet = packetPlayOutCustomPayloadConstructor.newInstance(key, packetDataSerializer);
                } else {
                    packet = packetPlayOutCustomPayloadConstructor.newInstance(channel, packetDataSerializer);
                }
            } else {
                // Work our magic to make the packet
                packet = packetPlayOutCustomPayloadConstructor.newInstance(channel, data);
            }

            // Work our magic to send the packet
            Object nmsPlayer = getHandleMethod.invoke(player);
            Object playerConnection = playerConnectionField.get(nmsPlayer);
            sendPacketMethod.invoke(playerConnection, packet);

        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            logger.severe("Failed to send Prelude packet to " + player.getName() + "!");
            logger.severe(e.toString());
        }
    }

    public Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (Arrays.equals(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return constructor;
            }
        }

        return null;
    }

    public Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                if (params.length > 0) {
                    if (Arrays.equals(method.getParameterTypes(), params)) {
                        method.setAccessible(true);
                        return method;
                    }
                } else {
                    method.setAccessible(true);
                    return method;
                }
            }
        }

        return null;
    }

    public Field getField(Class<?> clazz, String fieldName) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }

        return null;
    }
}