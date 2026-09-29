package com.immorrtalz.bordersonxaeromap.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.joml.Matrix4f;

import com.immorrtalz.bordersonxaeromap.BordersOnXaeroMapClient;
import com.immorrtalz.bordersonxaeromap.common.Zone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;

public class ZoneMapRenderer
{
	private static class ZoneCache
	{
		long[] ids;
		byte[] masks;
		List<ZonePart> parts;
	}

	private static class ZonePart { int minX, maxX, minZ, maxZ, labelX, labelZ; }

	private record ZoneRenderData(Zone zone, ZoneCache cache, int fillColor, int borderColor, int labelColor) {}

	// For boarders masking
	private static final int TOP = 1, BOTTOM = 2, LEFT = 4, RIGHT = 8;
	private static final Map<Zone, ZoneCache> CACHE = new WeakHashMap<>();

	/* Camera is the origin, camX/camZ are coords of a block camera is centered on, scale is count of window pixels per block */
	public static void render(GuiGraphics guiGraphics, double camX, double camZ, double scale, String currentDimension)
	{
		Minecraft mc = Minecraft.getInstance();
		List<Zone> zones = BordersOnXaeroMapClient.getAllZones();
		if (zones == null) return;

		double guiScale = mc.getWindow().getGuiScale();
		double windowHalfWidth = mc.getWindow().getWidth() / 2.0 / scale;
		double windowHalfHeight = mc.getWindow().getHeight() / 2.0 / scale;

		// Visible viewport rectangle in world space
		double left = camX - windowHalfWidth;
		double right = camX + windowHalfWidth;
		double top = camZ - windowHalfHeight;
		double bottom = camZ + windowHalfHeight;

		double borderThickness = Math.min(3 / scale, 4); // 3 physical pixels, capped at max 4 world blocks thick

		VertexConsumer vc = guiGraphics.bufferSource().getBuffer(RenderType.gui());
		Matrix4f m = guiGraphics.pose().last().pose();
		
		// Filter to current dimension
		List<ZoneRenderData> zonesRenderData = new ArrayList<>();

		for (Zone zone : zones)
		{
			if (!zone.getDimension().equals(currentDimension)) continue;
			zonesRenderData.add(new ZoneRenderData(zone, getCache(zone), zone.getFillColor(), zone.getBorderColor(), zone.getLabelColor()));
		}

		// Ordered rendering
		enum RenderPass
		{
			FILLS,
			BORDERS,
			LABELS
		}

		for (RenderPass currentPass : RenderPass.values())
		{
			for (ZoneRenderData zoneRenderData : zonesRenderData)
			{
				ZoneCache zoneCache = zoneRenderData.cache;

				if (currentPass == RenderPass.FILLS || currentPass == RenderPass.BORDERS)
				{
					for (int i = 0; i < zoneCache.ids.length; i++)
					{
						double x0 = ChunkPos.getX(zoneCache.ids[i]) * 16;
						double z0 = ChunkPos.getZ(zoneCache.ids[i]) * 16;

						double x1 = x0 + 16;
						double z1 = z0 + 16;

						// Skip chunk draw if it's completely off-screen
						if (x1 < left || x0 > right || z1 < top || z0 > bottom) continue;

						// Skip chunk fill draw if it's fully transparent
						if (currentPass == RenderPass.FILLS && (zoneRenderData.fillColor >>> 24) != 0)
							drawRect(vc, m, camX, camZ, x0, z0, x1, z1, zoneRenderData.fillColor);

						if (currentPass == RenderPass.BORDERS)
						{
							// Mask for borders drawing (0 - no border, 1 - top, 2 - bottom, 4 - left, 8 - right, 15 - all sides)
							int mask = zoneCache.masks[i];

							// Extension by borderThickness for inner convex corners
							double extLeft		= (mask & LEFT) == 0 ? borderThickness : 0;
							double extRight	= (mask & RIGHT) == 0 ? borderThickness : 0;
							double extTop		= (mask & TOP) == 0 ? borderThickness : 0;
							double extBottom	= (mask & BOTTOM) == 0 ? borderThickness : 0;

							if ((mask & TOP) != 0)		drawRect(vc, m, camX, camZ, x0 - extLeft, z0, x1 + extRight, z0 + borderThickness, zoneRenderData.borderColor);
							if ((mask & BOTTOM) != 0)	drawRect(vc, m, camX, camZ, x0 - extLeft, z1 - borderThickness, x1 + extRight, z1, zoneRenderData.borderColor);
							if ((mask & LEFT) != 0)		drawRect(vc, m, camX, camZ, x0, z0 - extTop, x0 + borderThickness, z1 + extBottom, zoneRenderData.borderColor);
							if ((mask & RIGHT) != 0)	drawRect(vc, m, camX, camZ, x1 - borderThickness, z0 - extTop, x1, z1 + extBottom, zoneRenderData.borderColor);
						}
					}
				}
				else if (currentPass == RenderPass.LABELS)
				{
					Font font = mc.font;
					double fontSize = guiScale / scale;
					PoseStack pose = guiGraphics.pose();

					String zoneName = zoneRenderData.zone.getName();

					for (ZonePart part : zoneCache.parts)
					{
						double partLeft = part.minX * 16;
						double partRight = (part.maxX + 1) * 16;
						double partTop = part.minZ * 16;
						double partBottom = (part.maxZ + 1) * 16;

						// Skip label draw if this part is completely off-screen
						if (partRight < left || partLeft > right || partBottom < top || partTop > bottom) continue;

						double lx = part.labelX * 16 + 8;
						double lz = part.labelZ * 16 + 8;

						int textWidth = font.width(zoneName);
						int backdropPadding = 2; // In font units
						int backdropColor = 0x99000000; // 60% opacity

						pose.pushPose();
						pose.translate((float)(lx - camX), (float)(lz - camZ), 0);
						pose.scale((float)fontSize, (float)fontSize, 1);

						// Backdrop
						guiGraphics.fill(-textWidth / 2 - backdropPadding,
							-font.lineHeight / 2 - backdropPadding,
							textWidth / 2 + backdropPadding,
							font.lineHeight / 2 + backdropPadding,
							backdropColor);

						// Text
						guiGraphics.drawString(font, zoneName, -textWidth / 2, -font.lineHeight / 2, zoneRenderData.labelColor, false);

						pose.popPose();
					}
				}
			}
		}

		guiGraphics.flush();
	}

	/**
	 * Draws a drawRect in world space, with camera at origin, using the given color
	 * @param vc VertexConsumer to draw to
	 * @param m Matrix4f to transform the vertices by
	 * @param camX X position of the camera
	 * @param camZ Z position of the camera
	 * @param x0 X position of the first corner
	 * @param z0 Z position of the first corner
	 * @param x1 X position of the second corner
	 * @param z1 Z position of the second corner
	 * @param color Color to draw with
	 */
	private static void drawRect(VertexConsumer vc, Matrix4f m, double camX, double camZ, double x0, double z0, double x1, double z1, int color)
	{
		float fx0 = (float)(x0 - camX), fz0 = (float)(z0 - camZ);
		float fx1 = (float)(x1 - camX), fz1 = (float)(z1 - camZ);

		vc.addVertex(m, fx0, fz0, 0).setColor(color);
		vc.addVertex(m, fx0, fz1, 0).setColor(color);
		vc.addVertex(m, fx1, fz1, 0).setColor(color);
		vc.addVertex(m, fx1, fz0, 0).setColor(color);
	}

	/**
	 * Returns a ZoneCache for the given zone, building it if necessary
	 * @param zone Zone to get the cache for
	 * @return ZoneCache for the given zone
	 */
	private static ZoneCache getCache(Zone zone)
	{
		ZoneCache cache = CACHE.get(zone);

		if (cache == null || cache.ids.length != zone.getChunkIds().size())
		{
			cache = build(zone);
			CACHE.put(zone, cache);
		}

		return cache;
	}

	/**
	 * Builds a ZoneCache for the given zone, calculating the border masks and parts
	 * @param zone Zone to build the cache for
	 * @return ZoneCache for the given zone
	 */
	private static ZoneCache build(Zone zone)
	{
		Set<Long> set = zone.getChunkIds();
		ZoneCache cache = new ZoneCache();
		cache.ids = new long[set.size()];
		cache.masks = new byte[set.size()];

		int i = 0;

		for (long id : set)
		{
			int x = ChunkPos.getX(id);
			int z = ChunkPos.getZ(id);
			int mask = 0;

			if (!set.contains(ChunkPos.asLong(x, z - 1))) mask |= TOP;
			if (!set.contains(ChunkPos.asLong(x, z + 1))) mask |= BOTTOM;
			if (!set.contains(ChunkPos.asLong(x - 1, z))) mask |= LEFT;
			if (!set.contains(ChunkPos.asLong(x + 1, z))) mask |= RIGHT;

			cache.ids[i] = id;
			cache.masks[i++] = (byte)mask;
		}

		cache.parts = findParts(set);

		return cache;
	}

	/**
	 * Groups a zone's chunks into separate parts: count chunks touching edge-to-edge or corner-to-corner as one part
	 * @param set Set of chunk IDs to group
	 * @return List of ZonePart objects representing the grouped chunks
	 */
	private static List<ZonePart> findParts(Set<Long> set)
	{
		List<ZonePart> parts = new ArrayList<>();
		Set<Long> unvisited = new HashSet<>(set);

		while (!unvisited.isEmpty())
		{
			long start = unvisited.iterator().next();
			List<Long> group = new ArrayList<>();
			Deque<Long> queue = new ArrayDeque<>();
			queue.add(start);
			unvisited.remove(start);

			while (!queue.isEmpty())
			{
				long id = queue.poll();
				int x = ChunkPos.getX(id);
				int z = ChunkPos.getZ(id);
				group.add(id);

				for (int dx = -1; dx <= 1; dx++)
				{
					for (int dz = -1; dz <= 1; dz++)
					{
						if (dx == 0 && dz == 0) continue;

						long neighbor = ChunkPos.asLong(x + dx, z + dz);
						if (unvisited.remove(neighbor)) queue.add(neighbor);
					}
				}
			}

			parts.add(buildPart(group));
		}

		return parts;
	}

	/**
	 * Builds a ZonePart from a list of chunk IDs
	 * @param group List of chunk IDs to build the part from
	 * @return ZonePart representing the grouped chunks
	 */
	private static ZonePart buildPart(List<Long> group)
	{
		ZonePart part = new ZonePart();

		part.minX = Integer.MAX_VALUE;
		part.maxX = Integer.MIN_VALUE;
		part.minZ = Integer.MAX_VALUE;
		part.maxZ = Integer.MIN_VALUE;

		double meanX = 0;
		double meanZ = 0;

		for (long id : group)
		{
			int x = ChunkPos.getX(id);
			int z = ChunkPos.getZ(id);

			part.minX = Math.min(part.minX, x);
			part.maxX = Math.max(part.maxX, x);
			part.minZ = Math.min(part.minZ, z);
			part.maxZ = Math.max(part.maxZ, z);
			meanX += x;
			meanZ += z;
		}

		meanX /= group.size();
		meanZ /= group.size();
		double best = Double.MAX_VALUE;

		for (long id : group)
		{
			int x = ChunkPos.getX(id);
			int z = ChunkPos.getZ(id);
			double d = (x - meanX) * (x - meanX) + (z - meanZ) * (z - meanZ);

			if (d < best)
			{
				best = d;
				part.labelX = x;
				part.labelZ = z;
			}
		}

		return part;
	}
}