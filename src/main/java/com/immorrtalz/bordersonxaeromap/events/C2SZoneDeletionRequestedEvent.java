package com.immorrtalz.bordersonxaeromap.events;

import net.neoforged.bus.api.Event;

public final class C2SZoneDeletionRequestedEvent extends Event
{
	private final int zoneId;

	public C2SZoneDeletionRequestedEvent(int zoneId) { this.zoneId = zoneId; }

	public int getZoneId() { return zoneId; }
}