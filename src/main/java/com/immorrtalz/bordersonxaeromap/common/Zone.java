package com.immorrtalz.bordersonxaeromap.common;

import java.util.Set;
import java.util.UUID;

import net.minecraft.world.level.Level;

public class Zone
{
	private final int id;
	private final Level dimensionLevel;
	private String name;
	private final UUID ownerUuid;
	private ZoneType type; // 0 = Public, 1 = Private
	private int borderColor;
	private int fillColor;
	private final Set<Integer> chunkIds;

	public Zone(int id, Level dimensionLevel, String name, UUID ownerUuid, ZoneType type, int borderColor, int fillColor, Set<Integer> chunkIds)
	{
		this.id = id;
		this.dimensionLevel = dimensionLevel;
		this.name = name;
		this.ownerUuid = ownerUuid;
		this.type = type;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.chunkIds = chunkIds;
	}

	public int getId() { return id; }
	public Level getDimensionLevel() { return dimensionLevel; }
	public String getName() { return name; }
	public UUID getOwnerUuid() { return ownerUuid; }
	public ZoneType getType() { return type; }
	public int getBorderColor() { return borderColor; }
	public int getFillColor() { return fillColor; }
	public Set<Integer> getChunkIds() { return chunkIds; }

	public Zone setName(String name)
	{
		this.name = name;
		return this;
	}

	public Zone setType(ZoneType type)
	{
		this.type = type;
		return this;
	}

	public Zone setBorderColor(int borderColor)
	{
		this.borderColor = borderColor;
		return this;
	}

	public Zone setFillColor(int fillColor)
	{
		this.fillColor = fillColor;
		return this;
	}

	public Zone addChunk(int chunkId)
	{
		this.chunkIds.add(chunkId);
		return this;
	}

	public Zone removeChunk(int chunkId)
	{
		this.chunkIds.remove(chunkId);
		return this;
	}

	public Zone addChunks(Set<Integer> chunkIds)
	{
		this.chunkIds.addAll(chunkIds);
		return this;
	}

	public Zone removeChunks(Set<Integer> chunkIds)
	{
		this.chunkIds.removeAll(chunkIds);
		return this;
	}

	public int getChunksArea() { return chunkIds.size(); }
	public int getBlocksArea() { return chunkIds.size() * 256; }
}