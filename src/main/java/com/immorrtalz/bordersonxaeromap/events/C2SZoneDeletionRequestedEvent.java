package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.events.EventBus.IEvent;

public final class C2SZoneDeletionRequestedEvent implements IEvent
{
	private final int zoneId;

	public C2SZoneDeletionRequestedEvent(int zoneId) { this.zoneId = zoneId; }

	public int getZoneId() { return zoneId; }
}