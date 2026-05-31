package com.immorrtalz.bordersonxaeromap.zone;

import net.minecraft.network.chat.Component;

public final class ZonePalette
{
	private static final int[] COLORS = new int[]
	{
		0xFFE73C3C, // red
		0xFFE67B2E, // orange
		0xFFF1C40F, // yellow
		0xFF9DD924, // olive
		0xFF2ECC2E, // green
		0xFF26CC5D, // seagreen
		0xFF26CCAB, // teal
		0xFF52CDE6, // lightblue
		0xFF5C8EF2, // blue
		0xFFA974F2, // violet
		0xFFDD74F2, // purple
		0xFFFF69B4, // pink
		0xFFECF0F1, // white
		0xFF1A1A1A, // black
	};

	private static final String[] NAME_KEYS = new String[]
	{
		"borders_on_xaero_map.color.red",
		"borders_on_xaero_map.color.orange",
		"borders_on_xaero_map.color.yellow",
		"borders_on_xaero_map.color.olive",
		"borders_on_xaero_map.color.green",
		"borders_on_xaero_map.color.seagreen",
		"borders_on_xaero_map.color.teal",
		"borders_on_xaero_map.color.lightblue",
		"borders_on_xaero_map.color.blue",
		"borders_on_xaero_map.color.violet",
		"borders_on_xaero_map.color.purple",
		"borders_on_xaero_map.color.pink",
		"borders_on_xaero_map.color.white",
		"borders_on_xaero_map.color.black"
	};

	public static int defaultBorder(int zoneIndex) { return COLORS[Math.floorMod(zoneIndex, COLORS.length)]; }
	public static int defaultFill(int zoneIndex) { return withAlpha(defaultBorder(zoneIndex), 102); }

	public static int nextBorderColor(int current)
	{
		int idx = findIndex(COLORS, stripAlpha(current));
		return COLORS[(idx + 1) % COLORS.length];
	}

	public static int nextFillColor(int current)
	{
		int idx = findIndex(COLORS, stripAlpha(current));
		return withAlpha(COLORS[(idx + 1) % COLORS.length], 102);
	}

	public static Component colorName(int color) { return Component.translatable(NAME_KEYS[findIndex(COLORS, stripAlpha(color))]); }

	private static int withAlpha(int color, int alpha) { return alpha << 24 | color & 0xFFFFFF; }
	private static int stripAlpha(int color) { return color & 0xFFFFFF; }

	private static int findIndex(int[] array, int rgbColor)
	{
		for (int i = 0; i < array.length; i++)
		{
			if ((array[i] & 0xFFFFFF) == rgbColor)
				return i;
		}

		return 0;
	}
}