package com.immorrtalz.bordersonxaeromap.client;

import com.immorrtalz.bordersonxaeromap.zone.ChunkRect;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class XaeroMapReflection
{
	private static final String GUI_MAP_CLASS = "xaero.map.gui.GuiMap";
	private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Map<String, Method>> METHOD_CACHE = new ConcurrentHashMap<>();

	private XaeroMapReflection() {}

	public static boolean isXaeroMapScreen(Screen screen) {
		return screen != null && GUI_MAP_CLASS.equals(screen.getClass().getName());
	}

	public static MapState readState(Screen screen) {
		if (!isXaeroMapScreen(screen)) {
			return null;
		}

		Minecraft client = Minecraft.getInstance();
		Object mapProcessor = getFieldValue(screen, "mapProcessor");
		double cameraX = toDouble(getFieldValue(screen, "cameraX"));
		double cameraZ = toDouble(getFieldValue(screen, "cameraZ"));
		double scale = toDouble(getFieldValue(screen, "scale"));
		int mouseBlockX = toInt(getFieldValue(screen, "mouseBlockPosX"));
		int mouseBlockZ = toInt(getFieldValue(screen, "mouseBlockPosZ"));

		Object selectionObj = getFieldValue(screen, "mapTileSelection");
		ChunkRect selection = null;
		if (selectionObj != null) {
			Integer left = invokeInt(selectionObj, "getLeft");
			Integer right = invokeInt(selectionObj, "getRight");
			Integer top = invokeInt(selectionObj, "getTop");
			Integer bottom = invokeInt(selectionObj, "getBottom");
			if (left != null && right != null && top != null && bottom != null) {
				selection = ChunkRect.of(left, top, right, bottom);
			}
		}

		String worldId = null;
		if (mapProcessor != null) {
			Object rawWorldId = invokeNoArgs(mapProcessor, "getCurrentWorldId");
			if (rawWorldId instanceof String value && !value.isBlank()) {
				worldId = value;
			}
		}
		if (worldId == null) {
			worldId = fallbackWorldId(client);
		}

		Object dimObj = getFieldValue(screen, "mouseBlockDim");
		if (dimObj == null && mapProcessor != null) {
			Object mapWorld = invokeNoArgs(mapProcessor, "getMapWorld");
			Object mapDimension = mapWorld == null ? null : invokeNoArgs(mapWorld, "getCurrentDimension");
			dimObj = mapDimension == null ? null : invokeNoArgs(mapDimension, "getDimId");
		}
		String dimensionId = stringifyDimension(dimObj);

		Window window = client.getWindow();
		int framebufferWidth = window.getWidth();
		int framebufferHeight = window.getHeight();
		int scaledWidth = window.getGuiScaledWidth();
		int scaledHeight = window.getGuiScaledHeight();
		int scaleFactor = Math.max(1, (int) Math.round(window.getGuiScale()));

		return new MapState(worldId, dimensionId, framebufferWidth, framebufferHeight, scaledWidth, scaledHeight,
			scaleFactor, cameraX, cameraZ, scale, mouseBlockX, mouseBlockZ, selection);
	}

	private static String fallbackWorldId(Minecraft client) {
		if (client.hasSingleplayerServer()) {
			return "sp:singleplayer";
		}

		ServerData info = client.getCurrentServer();
		if (info != null && info.ip != null && !info.ip.isBlank()) {
			return "mp:" + info.ip.toLowerCase(Locale.ROOT);
		}
		return "mp:unknown";
	}

	private static String stringifyDimension(Object dimObj) {
		if (dimObj == null) {
			return Level.OVERWORLD.location().toString();
		}

		if (dimObj instanceof ResourceKey<?> key) {
			return key.location().toString();
		}

		Object value = invokeNoArgs(dimObj, "getValue");
		if (value == null) {
			value = invokeNoArgs(dimObj, "location");
		}
		if (value != null) {
			return value.toString();
		}

		return Level.OVERWORLD.location().toString();
	}

	private static Object getFieldValue(Object instance, String fieldName) {
		if (instance == null) {
			return null;
		}

		Class<?> clazz = instance.getClass();
		Map<String, Field> classCache = FIELD_CACHE.computeIfAbsent(clazz, ignored -> new ConcurrentHashMap<>());
		Field field = classCache.get(fieldName);
		if (field == null) {
			field = findField(clazz, fieldName);
			if (field == null) {
				return null;
			}
			field.setAccessible(true);
			classCache.put(fieldName, field);
		}

		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	private static Field findField(Class<?> clazz, String fieldName) {
		Class<?> current = clazz;
		while (current != null) {
			try {
				return current.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	private static Object invokeNoArgs(Object instance, String methodName) {
		if (instance == null) {
			return null;
		}

		Class<?> clazz = instance.getClass();
		Map<String, Method> classCache = METHOD_CACHE.computeIfAbsent(clazz, ignored -> new ConcurrentHashMap<>());
		Method method = classCache.get(methodName);
		if (method == null) {
			method = findNoArgMethod(clazz, methodName);
			if (method == null) {
				return null;
			}
			method.setAccessible(true);
			classCache.put(methodName, method);
		}

		try {
			return method.invoke(instance);
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static Method findNoArgMethod(Class<?> clazz, String methodName) {
		Class<?> current = clazz;
		while (current != null) {
			for (Method method : current.getDeclaredMethods()) {
				if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
					return method;
				}
			}
			current = current.getSuperclass();
		}
		return null;
	}

	private static Integer invokeInt(Object instance, String methodName) {
		Object result = invokeNoArgs(instance, methodName);
		return result instanceof Number number ? number.intValue() : null;
	}

	private static int toInt(Object value) {
		return value instanceof Number number ? number.intValue() : 0;
	}

	private static double toDouble(Object value) {
		return value instanceof Number number ? number.doubleValue() : 0.0D;
	}

	public record MapState(
		String worldId,
		String dimensionId,
		int framebufferWidth,
		int framebufferHeight,
		int scaledWidth,
		int scaledHeight,
		int scaleFactor,
		double cameraX,
		double cameraZ,
		double scale,
		int mouseBlockX,
		int mouseBlockZ,
		ChunkRect selection
	)
	{
		public int rawToGuiX(double rawX) { return (int) Math.round(rawX / Math.max(1, scaleFactor)); }
		public int rawToGuiY(double rawY) { return (int) Math.round(rawY / Math.max(1, scaleFactor)); }
		public double guiToRawX(double guiX) { return guiX * Math.max(1, scaleFactor); }
		public double guiToRawY(double guiY) { return guiY * Math.max(1, scaleFactor); }
		public int cursorChunkX() { return mouseBlockX >> 4; }
		public int cursorChunkZ() { return mouseBlockZ >> 4; }
		public ChunkRect selectionOrCursor() { return selection != null ? selection : ChunkRect.single(cursorChunkX(), cursorChunkZ()); }
	}
}