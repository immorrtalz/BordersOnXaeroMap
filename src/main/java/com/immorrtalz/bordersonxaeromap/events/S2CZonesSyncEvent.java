package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.common.Zone;
import net.neoforged.bus.api.Event;

public final class S2CZonesSyncEvent extends Event
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