package com.immorrtalz.bordersonxaeromap.events;

import java.util.Set;

import net.neoforged.bus.api.Event;

public final class C2SZoneEditRequestedEvent extends Event
{
	private final int zoneId;
	private final String name;
	private final int borderColor;
	private final int fillColor;
	private final boolean isPublic;
	private final Set<Long> chunkIds;

	public C2SZoneEditRequestedEvent(int zoneId, String name, int borderColor, int fillColor, boolean isPublic, Set<Long> chunkIds)
	{
		this.zoneId = zoneId;
		this.name = name;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.isPublic = isPublic;
		this.chunkIds = chunkIds;
	}

	public int getZoneId() { return zoneId; }
	public String getName() { return name; }
	public int getBorderColor() { return borderColor; }
	public int getFillColor() { return fillColor; }
	public boolean isPublic() { return isPublic; }
	public Set<Long> getChunkIds() { return chunkIds; }
}