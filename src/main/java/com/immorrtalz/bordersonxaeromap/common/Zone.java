package com.immorrtalz.bordersonxaeromap.common;

import java.util.Set;
import java.util.UUID;

import org.joml.Vector2f;

import com.immorrtalz.bordersonxaeromap.common.ZonePalette.Color;
import com.immorrtalz.bordersonxaeromap.common.ZonePalette.ColorPurpose;

import net.minecraft.world.level.ChunkPos;

public class Zone
{
	private final int id;
	private final String dimension;
	private String name;
	private final UUID ownerUuid;
	private ZonePrivacyType privacyType;
	private Color borderColor;
	private Color fillColor;
	private final Set<Long> chunkIds;

	public Zone(int id, String dimension, String name, UUID ownerUuid, ZonePrivacyType privacyType, Color borderColor, Color fillColor, Set<Long> chunkIds)
	{
		this.id = id;
		this.dimension = dimension;
		this.name = name;
		this.ownerUuid = ownerUuid;
		this.privacyType = privacyType;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.chunkIds = chunkIds;
	}

	public Zone(int id, String dimension, String name, String ownerUuid, ZonePrivacyType privacyType, Color borderColor, Color fillColor, Set<Long> chunkIds)
	{
		this(id, dimension, name, UUID.fromString(ownerUuid), privacyType, borderColor, fillColor, chunkIds);
	}

	public int getId() { return id; }
	public String getDimension() { return dimension; }
	public String getName() { return name; }
	public UUID getOwnerUuid() { return ownerUuid; }
	public ZonePrivacyType getType() { return privacyType; }
	public int getBorderColor() { return ZonePalette.getColor(borderColor, ColorPurpose.BORDER); }
	public int getFillColor() { return ZonePalette.getColor(fillColor, ColorPurpose.FILL); }
	public int getLabelColor() { return ZonePalette.getColor(fillColor, ColorPurpose.LABEL); }
	public Set<Long> getChunkIds() { return chunkIds; }

	public Zone setName(String name)
	{
		this.name = name;
		return this;
	}

	public Zone setType(ZonePrivacyType privacyType)
	{
		this.privacyType = privacyType;
		return this;
	}

	public Zone setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
		return this;
	}

	public Zone setFillColor(Color fillColor)
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