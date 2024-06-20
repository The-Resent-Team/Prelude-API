package prelude.adapter.impl;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import prelude.adapter.AbstractBukkitPluginMessageSender;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* Based largely off https://github.com/BadlionClient/BadlionClientModAPI/blob/master/bukkit-1.17/src/main/java/net/badlion/bukkitapi/BukkitPluginMessageSender.java
* */
public class BukkitPluginMessageSender_1_17 extends AbstractBukkitPluginMessageSender {
    @Getter private static BukkitPluginMessageSender_1_17 instance;

    private static Logger logger;

    private static Method getHandleMethod;
    private static Field playerConnectionField;
    private static Method sendPacketMethod;

    private static String versionSuffix;
    private static String versionName;

    private static Constructor<?> packetPlayOutCustomPayloadConstructor;
    private static Constructor<?> packetPlayOutMinecraftKeyConstructor;
    private static Constructor<?> discardedPayloadConstructor;
    private static Method resourceLocationParseMethod;
    private static Class<?> minecraftKeyClass;
    private static Class<?> customPacketPayloadClass;
    private static boolean useMinecraftKey;
    private static boolean usePacketPayload;
    private static boolean useDiscardedPayload;

    // Bukkit 1.8+ support
    private Class<?> packetDataSerializerClass;
    private Method packetDataSerializerWriteBytesMethod;
    private Constructor<?> packetDataSerializerConstructor;

    private Method wrappedBufferMethod;

    public BukkitPluginMessageSender_1_17(Logger _logger) {
        logger = _logger;

        // Get the v1_X_Y from the end of the package name, e.g. v_1_7_R4 or v_1_12_R1
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        if (parts.length > 0) {
            String suffix = parts[parts.length - 1];
            if (!suffix.startsWith("v")) {
                // 1.20.5+ support
                // TODO: In 1.20.5+, the private method `CraftPlayer.sendCustomPayload(ResourceLocation, byte[])` should do the trick and handle all future versions
                if ("craftbukkit".equals(suffix)) {
                    suffix = "";
                } else {
                    if (suffix.equals("mockbukkit")) {
                        // running tests, don't bother, none of the following packages/classes/methods/etc exist
                        return;
                    }
                    throw new RuntimeException("Failed to find version for running Minecraft server, got suffix " + suffix);
                }
            }

            versionSuffix = suffix;
            versionName = Bukkit.getServer().getVersion();

            logger.info("Found version " + versionSuffix + " (" + versionName + ")");
        }

        // We need to use reflection because Bukkit by default handles plugin messages in a really silly way
        // Reflection stuff
        Class<?> craftPlayerClass = getClass(versionSuffix == null || versionSuffix.isEmpty() ? "org.bukkit.craftbukkit.entity.CraftPlayer" : "org.bukkit.craftbukkit." + versionSuffix + ".entity.CraftPlayer");
        if (craftPlayerClass == null) {
            throw new RuntimeException("Failed to find CraftPlayer class");
        }

        Class<?> nmsPlayerClass = getClass("net.minecraft.server.level.EntityPlayer");
        if (nmsPlayerClass == null) {
            throw new RuntimeException("Failed to find EntityPlayer class");
        }

        Class<?> playerConnectionClass = getClass("net.minecraft.server.network.PlayerConnection");
        if (playerConnectionClass == null) {
            throw new RuntimeException("Failed to find PlayerConnection class");
        }

        Class<?> packetPlayOutCustomPayloadClass = getClass("net.minecraft.network.protocol.game.PacketPlayOutCustomPayload");
        if (packetPlayOutCustomPayloadClass == null) {
            packetPlayOutCustomPayloadClass = getClass("net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket");

            if (packetPlayOutCustomPayloadClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload class");
            }
        }

        packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, String.class, byte[].class);
        if (packetPlayOutCustomPayloadConstructor == null) {
            // Newer versions of Minecraft use a different custom packet system
            packetDataSerializerClass = getClass("net.minecraft.network.PacketDataSerializer");
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
                minecraftKeyClass = getClass("net.minecraft.resources.MinecraftKey");

                // Fix for Paper in newer versions
                packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, minecraftKeyClass, packetDataSerializerClass);

                if (packetPlayOutCustomPayloadConstructor == null) {
                    customPacketPayloadClass = getClass("net.minecraft.network.protocol.common.custom.CustomPacketPayload");

                    if (customPacketPayloadClass != null) {
                        packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayloadClass, customPacketPayloadClass);
                        packetDataSerializerWriteBytesMethod = getMethod(packetDataSerializerClass, "c", byte[].class);
                        packetPlayOutMinecraftKeyConstructor = getConstructor(minecraftKeyClass, String.class);
                        usePacketPayload = true;

                        Class<?> discardedPayloadClass = getClass("net.minecraft.network.protocol.common.custom.DiscardedPayload");

                        if (discardedPayloadClass != null) {
                            discardedPayloadConstructor = getConstructor(discardedPayloadClass, minecraftKeyClass, byteBufClass);

                            if (discardedPayloadConstructor != null) {
                                useDiscardedPayload = true;
                            }
                        }

                        // 1.21+
                        if (packetPlayOutMinecraftKeyConstructor == null) {
                            resourceLocationParseMethod = getMethod(minecraftKeyClass, "parse", String.class);
                        }
                    }

                    if (packetPlayOutCustomPayloadConstructor == null) {
                        throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor 2x");
                    }
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

        Field _playerConnectionField;

        if (versionSuffix.contains("v1_17") || versionSuffix.contains("v1_18") || versionSuffix.contains("v1_19")) {
            _playerConnectionField = getField(nmsPlayerClass, "b");
        } else {
            _playerConnectionField = getField(nmsPlayerClass, "c");
        }

        if (_playerConnectionField == null) {
            _playerConnectionField = getField(nmsPlayerClass, "connection");
        }

        if (_playerConnectionField != null) {
            playerConnectionField = _playerConnectionField;
        } else {
            throw new RuntimeException("Failed to find EntityPlayer.playerConnection");
        }

        if (!versionSuffix.contains("v1_17")) {
            final Class<?> packet1_18Class = getClass("net.minecraft.network.protocol.Packet");
            Method _sendPacketMethod;

            if (usePacketPayload) {
                _sendPacketMethod = getMethod(playerConnectionClass.getSuperclass(), "b", packet1_18Class);
            } else {
                _sendPacketMethod = getMethod(playerConnectionClass, "a", packet1_18Class);
            }

            if (_sendPacketMethod == null) {
                _sendPacketMethod = getMethod(playerConnectionClass.getSuperclass(), "send", packet1_18Class);
            }

            if (_sendPacketMethod != null) {
                sendPacketMethod = _sendPacketMethod;
            } else {
                throw new RuntimeException("Failed to find PlayerConnection.send(Packet)");
            }
        } else {
            sendPacketMethod = getMethod(playerConnectionClass, "sendPacket");

            if (sendPacketMethod == null) {
                throw new RuntimeException("Failed to find PlayerConnection.sendPacket()");
            }
        }
    }

    @Override
    public void sendPluginMessagePacket(Player player, String channel, Object data) {
        try {
            Object packet;

            // Newer MC version, setup ByteBuf object
            if (packetDataSerializerClass != null) {
                if (usePacketPayload) {
                    Object payload;

                    if (useDiscardedPayload) {
                        if (packetPlayOutMinecraftKeyConstructor == null) {
                            // 1.21+
                            payload = discardedPayloadConstructor.newInstance(
                                    resourceLocationParseMethod.invoke(null, channel),
                                    wrappedBufferMethod.invoke(null, data)
                            );
                        } else {
                            // 1.20.5+
                            payload = discardedPayloadConstructor.newInstance(
                                    packetPlayOutMinecraftKeyConstructor.newInstance(channel),
                                    wrappedBufferMethod.invoke(null, data)
                            );
                        }
                    } else {
                        // 1.20.2 - 1.20.4
                        payload = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{customPacketPayloadClass}, (proxy, method, args) -> {
                            if (method.getReturnType().equals(minecraftKeyClass)) {
                                return packetPlayOutMinecraftKeyConstructor.newInstance(channel);
                            } else if (args.length == 1 && packetDataSerializerClass.isAssignableFrom(args[0].getClass())) {
                                packetDataSerializerWriteBytesMethod.invoke(args[0], data);
                                return null;
                            }

                            return null;
                        });

                    }
                    packet = packetPlayOutCustomPayloadConstructor.newInstance(payload);
                } else {
                    Object byteBuf = wrappedBufferMethod.invoke(null, data);
                    Object packetDataSerializer = packetDataSerializerConstructor.newInstance(byteBuf);

                    if (useMinecraftKey) {
                        Object key = packetPlayOutMinecraftKeyConstructor.newInstance(channel);
                        packet = packetPlayOutCustomPayloadConstructor.newInstance(key, packetDataSerializer);
                    } else {
                        packet = packetPlayOutCustomPayloadConstructor.newInstance(channel, packetDataSerializer);
                    }
                }
            } else {
                // Work our magic to make the packet
                packet = packetPlayOutCustomPayloadConstructor.newInstance(channel, data);
            }

            // Work our magic to send the packet
            Object nmsPlayer = getHandleMethod.invoke(player);
            Object playerConnection = playerConnectionField.get(nmsPlayer);
            sendPacketMethod.invoke(playerConnection, packet);

        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Failed to send Prelude packet!", throwable);
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
