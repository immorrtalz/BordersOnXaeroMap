package com.immorrtalz.bordersonxaeromap.zone;

import net.minecraft.network.chat.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ZoneDimension
{
	private final Map<String, ZoneArea> zonesById = new LinkedHashMap<>();
	private final Map<Long, String> chunkToZoneId = new LinkedHashMap<>();

	public Collection<ZoneArea> getZones() { return Collections.unmodifiableCollection(zonesById.values()); }
	public ZoneArea getZoneById(String zoneId) { return zonesById.get(zoneId); }
	public String getZoneIdAtChunk(int chunkX, int chunkZ) { return chunkToZoneId.get(ZoneRepository.packChunk(chunkX, chunkZ)); }

	public ZoneArea getZoneAtChunk(int chunkX, int chunkZ)
	{
		String zoneId = getZoneIdAtChunk(chunkX, chunkZ);
		return zoneId == null ? null : zonesById.get(zoneId);
	}

	public String nextDefaultZoneName()
	{
		int i = 1;

		while (true)
		{
			String candidate = Component.translatable("borders_on_xaero_map.zone_prefix").getString() + i;
			boolean nameTaken = false;

			for (ZoneArea zone : zonesById.values())
			{
				if (zone.getName().equalsIgnoreCase(candidate))
				{
					nameTaken = true;
					break;
				}
			}

			if (!nameTaken) return candidate;
			i++;
		}
	}

	ZoneArea createZone(String name, int borderColor, int fillColor, Set<Long> chunkKeys)
	{
		String id = UUID.randomUUID().toString();
		ZoneArea zone = new ZoneArea(id, name, borderColor, fillColor);
		zonesById.put(id, zone);
		addChunksToZone(zone, chunkKeys);

		if (zone.getChunkCount() == 0)
		{
			zonesById.remove(id);
			return null;
		}

		return zone;
	}

	int addChunksToZone(String zoneId, Set<Long> chunkKeys)
	{
		ZoneArea zone = zonesById.get(zoneId);
		if (zone == null) return 0;

		int before = zone.getChunkCount();
		addChunksToZone(zone, chunkKeys);
		return zone.getChunkCount() - before;
	}

	int removeChunksFromZone(String zoneId, Set<Long> chunkKeys)
	{
		ZoneArea zone = zonesById.get(zoneId);
		if (zone == null) return 0;

		int removed = 0;

		for (long key : chunkKeys)
		{
			String currentOwner = chunkToZoneId.get(key);

			if (!zoneId.equals(currentOwner)) continue;

			chunkToZoneId.remove(key);
			zone.removeChunk(key);
			removed++;
		}

		if (zone.getChunkCount() == 0)
			zonesById.remove(zoneId);

		return removed;
	}

	boolean deleteZone(String zoneId)
	{
		ZoneArea zone = zonesById.remove(zoneId);

		if (zone == null) return false;

		for (long key : zone.getChunkKeys())
			chunkToZoneId.remove(key);

		return true;
	}

	boolean renameZone(String zoneId, String newName)
	{
		ZoneArea zone = zonesById.get(zoneId);

		if (zone == null) return false;

		zone.setName(newName);
		return true;
	}

	boolean setBorderColor(String zoneId, int newBorderColor)
	{
		ZoneArea zone = zonesById.get(zoneId);

		if (zone == null) return false;

		zone.setBorderColor(newBorderColor);
		return true;
	}

	boolean cycleBorderColor(String zoneId)
	{
		ZoneArea zone = zonesById.get(zoneId);

		if (zone == null) return false;

		zone.setBorderColor(ZonePalette.nextBorderColor(zone.getBorderColor()));
		return true;
	}

	boolean setFillColor(String zoneId, int newFillColor)
	{
		ZoneArea zone = zonesById.get(zoneId);

		if (zone == null) return false;

		zone.setFillColor(newFillColor);
		return true;
	}

	boolean cycleFillColor(String zoneId)
	{
		ZoneArea zone = zonesById.get(zoneId);

		if (zone == null) return false;

		zone.setFillColor(ZonePalette.nextFillColor(zone.getFillColor()));
		return true;
	}

	void addLoadedZone(ZoneArea loadedZone)
	{
		zonesById.put(loadedZone.getId(), loadedZone);
		addChunksToZone(loadedZone, loadedZone.getChunkKeys());
	}

	private void addChunksToZone(ZoneArea zone, Set<Long> chunkKeys)
	{
		for (long key : chunkKeys)
		{
			String previousOwnerId = chunkToZoneId.get(key);

			if (previousOwnerId != null && !previousOwnerId.equals(zone.getId()))
			{
				ZoneArea previousOwner = zonesById.get(previousOwnerId);

				if (previousOwner != null)
				{
					previousOwner.removeChunk(key);

					if (previousOwner.getChunkCount() == 0)
						zonesById.remove(previousOwnerId);
				}
			}

			chunkToZoneId.put(key, zone.getId());
			zone.addChunk(key);
		}
	}
}