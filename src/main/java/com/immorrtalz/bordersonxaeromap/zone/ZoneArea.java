package com.immorrtalz.bordersonxaeromap.zone;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ZoneArea
{
	private final String id;
	private String name;
	private int borderColor;
	private int fillColor;
	private final Set<Long> chunkKeys;
	private int minChunkX;
	private int maxChunkX;
	private int minChunkZ;
	private int maxChunkZ;

	ZoneArea(String id, String name, int borderColor, int fillColor)
	{
		this.id = id;
		this.name = name;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.chunkKeys = new HashSet<>();
		resetBounds();
	}

	public String getId() { return id; }

	public String getName() { return name; }
	void setName(String name) { this.name = name; }

	public int getBorderColor() { return borderColor; }
	void setBorderColor(int borderColor) { this.borderColor = borderColor; }

	public int getFillColor() { return fillColor; }
	void setFillColor(int fillColor) { this.fillColor = fillColor; }

	public Set<Long> getChunkKeys() { return Collections.unmodifiableSet(chunkKeys); }

	public int getChunkCount() { return chunkKeys.size(); }
	public int getMinChunkX() { return minChunkX; }
	public int getMaxChunkX() { return maxChunkX; }
	public int getMinChunkZ() { return minChunkZ; }
	public int getMaxChunkZ() { return maxChunkZ; }

	void addChunk(long chunkKey)
	{
		if (chunkKeys.add(chunkKey))
		{
			int chunkX = ZoneRepository.unpackChunkX(chunkKey);
			int chunkZ = ZoneRepository.unpackChunkZ(chunkKey);
			if (chunkX < minChunkX) minChunkX = chunkX;
			if (chunkX > maxChunkX) maxChunkX = chunkX;
			if (chunkZ < minChunkZ) minChunkZ = chunkZ;
			if (chunkZ > maxChunkZ) maxChunkZ = chunkZ;
		}
	}

	void removeChunk(long chunkKey)
	{
		if (chunkKeys.remove(chunkKey))
			recalculateBounds();
	}

	private void recalculateBounds()
	{
		resetBounds();

		for (long key : chunkKeys)
		{
			int chunkX = ZoneRepository.unpackChunkX(key);
			int chunkZ = ZoneRepository.unpackChunkZ(key);

			if (chunkX < minChunkX) minChunkX = chunkX;
			if (chunkX > maxChunkX) maxChunkX = chunkX;
			if (chunkZ < minChunkZ) minChunkZ = chunkZ;
			if (chunkZ > maxChunkZ) maxChunkZ = chunkZ;
		}
	}

	private void resetBounds()
	{
		minChunkX = Integer.MAX_VALUE;
		maxChunkX = Integer.MIN_VALUE;
		minChunkZ = Integer.MAX_VALUE;
		maxChunkZ = Integer.MIN_VALUE;
	}
}