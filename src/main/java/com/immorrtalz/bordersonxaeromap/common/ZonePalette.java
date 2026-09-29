package com.immorrtalz.bordersonxaeromap.common;

import java.util.Map;

import net.minecraft.network.chat.Component;

public class ZonePalette
{
	public enum Color
	{
		RED,
		ORANGE,
		YELLOW,
		LIME,
		GREEN,
		CYAN,
		LIGHT_BLUE,
		BLUE,
		PURPLE,
		MAGENTA,
		PINK,
		BROWN,
		BLACK,
		GRAY,
		LIGHT_GRAY,
		WHITE
	}

	private static final Map<Color, Integer> COLORS = Map.ofEntries(
		Map.entry(Color.RED, 0xFFCC2929),
		Map.entry(Color.ORANGE, 0xFFD97D21),
		Map.entry(Color.YELLOW, 0xFFD9A816),
		Map.entry(Color.LIME, 0xFFADCC14),
		Map.entry(Color.GREEN, 0xFF55CC3D),
		Map.entry(Color.CYAN, 0xFF29CCB1),
		Map.entry(Color.LIGHT_BLUE, 0xFF21ABD9),
		Map.entry(Color.BLUE, 0xFF456DE5),
		Map.entry(Color.PURPLE, 0xFFAA4CD9),
		Map.entry(Color.MAGENTA, 0xFFCC47B6),
		Map.entry(Color.PINK, 0xFFD94174),
		Map.entry(Color.BROWN, 0xFF592412),
		Map.entry(Color.BLACK, 0xFF1A1A1A),
		Map.entry(Color.GRAY, 0xFF404040),
		Map.entry(Color.LIGHT_GRAY, 0xFF808080),
		Map.entry(Color.WHITE, 0xFFCCCCCC));

	public enum ColorPurpose
	{
		FILL,
		BORDER,
		LABEL
	}
	
	private final static int BORDER_OPACITY = 0xFF;
	private final static int FILL_OPACITY = 0x32;
	private final static int LABEL_OPACITY = 0xFF;
	private final static int LABEL_WHITE_ADD = 0x20;

	public static int getColor(Color color, ColorPurpose colorPurpose)
	{
		int baseColor = COLORS.get(color);

		switch (colorPurpose)
		{
			case BORDER: return withAlpha(baseColor, BORDER_OPACITY);
			case FILL: return withAlpha(baseColor, FILL_OPACITY);
			case LABEL: return withAlpha(blendWhite(baseColor, LABEL_WHITE_ADD), LABEL_OPACITY);
			default: return baseColor;
		}
	}

	public static int getNextColor(Color currentColor, ColorPurpose colorPurpose)
		{ return getColor(COLORS.keySet().stream().toList().get(getNextColorIndex(currentColor)), colorPurpose); }

	public static Component getColorName(Color color) { return Component.translatable("borders_on_xaero_map.color." + color.name().toLowerCase()); }

	private static int withAlpha(int color, int alpha) { return alpha << 24 | color & 0xFFFFFF; }
	private static int stripAlpha(int color) { return color & 0xFFFFFF; }

	private static int getNextColorIndex(Color color) { return (color.ordinal() + 1) % COLORS.size(); }

	// blends the color toward white by `amount`/255, per channel, clamped
	private static int blendWhite(int color, int amount)
	{
		int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;

		r = Math.min(255, r + amount);
		g = Math.min(255, g + amount);
		b = Math.min(255, b + amount);

		return (r << 16) | (g << 8) | b;
	}
}