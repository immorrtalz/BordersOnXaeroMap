package com.immorrtalz.bordersonxaeromap.common;

import java.util.Set;
import java.util.UUID;

import org.joml.Vector2f;

import net.minecraft.world.level.ChunkPos;

public class Zone
{
	private final int id;
	private final String dimension;
	private String name;
	private final UUID ownerUuid;
	private ZoneType type; // 0 = Public, 1 = Private
	private int borderColor;
	private int fillColor;
	private final Set<Long> chunkIds;

	public Zone(int id, String dimension, String name, UUID ownerUuid, ZoneType type, int borderColor, int fillColor, Set<Long> chunkIds)
	{
		this.id = id;
		this.dimension = dimension;
		this.name = name;
		this.ownerUuid = ownerUuid;
		this.type = type;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.chunkIds = chunkIds;
	}

	public Zone(int id, String dimension, String name, String ownerUuid, ZoneType type, int borderColor, int fillColor, Set<Long> chunkIds)
	{
		this(id, dimension, name, UUID.fromString(ownerUuid), type, borderColor, fillColor, chunkIds);
	}

	public int getId() { return id; }
	public String getDimension() { return dimension; }
	public String getName() { return name; }
	public UUID getOwnerUuid() { return ownerUuid; }
	public ZoneType getType() { return type; }
	public int getBorderColor() { return borderColor; }
	public int getFillColor() { return fillColor; }
	public Set<Long> getChunkIds() { return chunkIds; }

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

	public Zone addChunk(long chunkId)
	{
		this.chunkIds.add(chunkId);
		return this;
	}

	public Zone removeChunk(long chunkId)
	{
		this.chunkIds.remove(chunkId);
		return this;
	}

	public Zone addChunks(Set<Long> chunkIds)
	{
		this.chunkIds.addAll(chunkIds);
		return this;
	}

	public Zone removeChunks(Set<Long> chunkIds)
	{
		this.chunkIds.removeAll(chunkIds);
		return this;
	}

	public int getChunksArea() { return chunkIds.size(); }
	public int getBlocksArea() { return chunkIds.size() * 256; }
	public static Vector2f getChunkXZ(long chunkId) { return new Vector2f(ChunkPos.getX(chunkId), ChunkPos.getZ(chunkId)); }
}