package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.common.Zone;
import com.immorrtalz.bordersonxaeromap.events.EventBus.IEvent;

public final class S2CZonesSyncEvent implements IEvent
{
	private final boolean isPartial;
	private final Zone[] zones;

	public S2CZonesSyncEvent(boolean isPartial, Zone[] zones)
	{
		this.isPartial = isPartial;
		this.zones = zones;
	}

	public boolean isPartial() { return isPartial; }
	public Zone[] getZones() { return zones; }
}