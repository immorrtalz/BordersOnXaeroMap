package com.immorrtalz.bordersonxaeromap.zone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.immorrtalz.bordersonxaeromap.BordersOnXaeroMap;
import com.immorrtalz.bordersonxaeromap.common.Zone;
import com.immorrtalz.bordersonxaeromap.common.ZonePalette;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ZoneRepository
{
	public static final int MAX_SELECTION_CHUNKS = 8192;

	/* private final Path filePath;
	private final Gson gson;
	private final Map<String, Map<String, ZoneDimension>> worldsById;

	public ZoneRepository(Path filePath)
	{
		this.filePath = filePath;
		this.gson = new GsonBuilder().setPrettyPrinting().create();
		this.worldsById = new LinkedHashMap<>();
		load();
	}

	public ZoneDimension getOrCreateDimension(String worldId, String dimensionId)
	{
		String worldKey = normalizeWorldId(worldId);
		String dimKey = normalizeDimensionId(dimensionId);
		return worldsById.computeIfAbsent(worldKey, ignored -> new LinkedHashMap<>()).computeIfAbsent(dimKey, ignored -> new ZoneDimension());
	}

	public ZoneDimension getDimension(String worldId, String dimensionId)
	{
		Map<String, ZoneDimension> byDimension = worldsById.get(normalizeWorldId(worldId));
		if (byDimension == null) return null;
		return byDimension.get(normalizeDimensionId(dimensionId));
	}

	public Zone getZoneAt(String worldId, String dimensionId, int chunkX, int chunkZ)
	{
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return null;
		return dimension.getZoneAtChunk(chunkX, chunkZ);
	}

	public Zone createZone(String worldId, String dimensionId, ChunkRect selection)
	{
		if (selection.area() > MAX_SELECTION_CHUNKS) return null;
		return createZone(worldId, dimensionId, selection.toChunkKeys());
	}

	public Zone createZone(String worldId, String dimensionId, Set<Long> chunkKeys)
	{
		if (chunkKeys == null || chunkKeys.isEmpty() || chunkKeys.size() > MAX_SELECTION_CHUNKS) return null;

		ZoneDimension dimension = getOrCreateDimension(worldId, dimensionId);
		String defaultName = dimension.nextDefaultZoneName();
		int border = ZonePalette.COLORS[0];
		int fill = ZonePalette.COLORS[0];
		Zone zone = dimension.createZone(defaultName, border, fill, chunkKeys);

		if (zone != null) save();
		return zone;
	}

	public int addSelectionToZone(String worldId, String dimensionId, String zoneId, ChunkRect selection)
	{
		if (selection.area() > MAX_SELECTION_CHUNKS) return 0;
		return addChunksToZone(worldId, dimensionId, zoneId, selection.toChunkKeys());
	}

	public int addChunksToZone(String worldId, String dimensionId, String zoneId, Set<Long> chunkKeys)
	{
		if (chunkKeys == null || chunkKeys.isEmpty() || chunkKeys.size() > MAX_SELECTION_CHUNKS) return 0;

		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return 0;

		int changed = dimension.addChunksToZone(zoneId, chunkKeys);
		if (changed > 0) save();
		return changed;
	}

	public int removeSelectionFromZone(String worldId, String dimensionId, String zoneId, ChunkRect selection)
	{
		if (selection.area() > MAX_SELECTION_CHUNKS) return 0;
		return removeChunksFromZone(worldId, dimensionId, zoneId, selection.toChunkKeys());
	}

	public int removeChunksFromZone(String worldId, String dimensionId, int zoneId, Set<Long> chunkKeys)
	{
		if (chunkKeys == null || chunkKeys.isEmpty() || chunkKeys.size() > MAX_SELECTION_CHUNKS) return 0;

		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return 0;

		int changed = dimension.removeChunksFromZone(zoneId, chunkKeys);
		if (changed > 0) save();
		return changed;
	}

	public boolean deleteZone(String worldId, String dimensionId, int zoneId) {
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.deleteZone(zoneId);
		if (result) save();
		return result;
	}

	public boolean renameZone(String worldId, String dimensionId, String zoneId, String newName) {
		String cleanedName = newName == null ? "" : newName.trim();
		if (cleanedName.isEmpty()) return false;

		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.renameZone(zoneId, cleanedName);
		if (result) save();
		return result;
	}

	public boolean setBorderColor(String worldId, String dimensionId, String zoneId, int newBorderColor)
	{
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.setBorderColor(zoneId, newBorderColor);
		if (result) save();
		return result;
	}

	public boolean cycleBorderColor(String worldId, String dimensionId, String zoneId)
	{
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.cycleBorderColor(zoneId);
		if (result) save();
		return result;
	}

	public boolean setFillColor(String worldId, String dimensionId, String zoneId, int newFillColor)
	{
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.setFillColor(zoneId, newFillColor);
		if (result) save();
		return result;
	}

	public boolean cycleFillColor(String worldId, String dimensionId, String zoneId)
	{
		ZoneDimension dimension = getDimension(worldId, dimensionId);
		if (dimension == null) return false;

		boolean result = dimension.cycleFillColor(zoneId);
		if (result) save();
		return result;
	}

	public void save()
	{
		try
		{
			Path parent = filePath.getParent();

			if (parent != null)
				Files.createDirectories(parent);

			SaveRoot root = buildSaveRoot();

			try (Writer writer = Files.newBufferedWriter(filePath))
			{
				gson.toJson(root, writer);
			}
		}
		catch (Exception e)
		{
			BordersOnXaeroMap.LOGGER.error("Failed to save local zones", e);
		}
	}

	private SaveRoot buildSaveRoot()
	{
		SaveRoot root = new SaveRoot();

		for (Map.Entry<String, Map<String, ZoneDimension>> worldEntry : worldsById.entrySet())
		{
			SaveWorld world = new SaveWorld();
			root.worlds.put(worldEntry.getKey(), world);

			for (Map.Entry<String, ZoneDimension> dimEntry : worldEntry.getValue().entrySet())
			{
				SaveDimension dim = new SaveDimension();
				world.dimensions.put(dimEntry.getKey(), dim);

				for (Zone zone : dimEntry.getValue().getZones())
				{
					SaveZone zoneSave = new SaveZone();
					zoneSave.id = zone.getId();
					zoneSave.name = zone.getName();
					zoneSave.borderColor = zone.getBorderColor();
					zoneSave.fillColor = zone.getFillColor();
					zoneSave.chunks = new ArrayList<>(zone.getChunkKeys());
					dim.zones.add(zoneSave);
				}
			}
		}

		return root;
	}

	private void load()
	{
		if (!Files.exists(filePath)) return;

		try (Reader reader = Files.newBufferedReader(filePath))
		{
			SaveRoot root = gson.fromJson(reader, SaveRoot.class);

			if (root == null || root.worlds == null) return;

			loadFromRoot(root);
		}
		catch (Exception e)
		{
			BordersOnXaeroMap.LOGGER.error("Failed to load local zones from {}", filePath, e);
		}
	}

	private void loadFromRoot(SaveRoot root)
	{
		worldsById.clear();

		for (Map.Entry<String, SaveWorld> worldEntry : root.worlds.entrySet())
		{
			String worldId = normalizeWorldId(worldEntry.getKey());
			Map<String, ZoneDimension> byDim = worldsById.computeIfAbsent(worldId, ignored -> new LinkedHashMap<>());
			SaveWorld saveWorld = worldEntry.getValue();

			if (saveWorld == null || saveWorld.dimensions == null) continue;

			for (Map.Entry<String, SaveDimension> dimEntry : saveWorld.dimensions.entrySet())
			{
				String dimId = normalizeDimensionId(dimEntry.getKey());
				SaveDimension saveDimension = dimEntry.getValue();

				if (saveDimension == null || saveDimension.zones == null) continue;

				ZoneDimension dim = byDim.computeIfAbsent(dimId, ignored -> new ZoneDimension());

				for (SaveZone saveZone : saveDimension.zones)
				{
					if (saveZone == null || saveZone.id == null || saveZone.name == null || saveZone.chunks == null) continue;

					Zone loaded = new Zone(saveZone.id, saveZone.name, saveZone.borderColor, saveZone.fillColor);

					for (long key : saveZone.chunks)
						loaded.addChunk(key);

					dim.addLoadedZone(loaded);
				}
			}
		}
	}

	private static String normalizeWorldId(String worldId)
	{
		if (worldId == null || worldId.isBlank())
			return "unknown_world";

		return worldId.trim().toLowerCase(Locale.ROOT);
	}

	private static String normalizeDimensionId(String dimensionId)
	{
		if (dimensionId == null || dimensionId.isBlank()) return "minecraft:overworld";
		return dimensionId.trim().toLowerCase(Locale.ROOT);
	}

	public static long packChunk(int chunkX, int chunkZ) { return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL); }
	public static int unpackChunkX(long packed) { return (int) (packed >> 32); }
	public static int unpackChunkZ(long packed) { return (int) packed; }

	private static final class SaveRoot
	{
		private Map<String, SaveWorld> worlds = new LinkedHashMap<>();
	}

	private static final class SaveWorld
	{
		private Map<String, SaveDimension> dimensions = new LinkedHashMap<>();
	}

	private static final class SaveDimension
	{
		private List<SaveZone> zones = new ArrayList<>();
	}

	private static final class SaveZone
	{
		private String id;
		private String name;
		private int borderColor;
		private int fillColor;
		private List<Long> chunks = new ArrayList<>();
	} */
}