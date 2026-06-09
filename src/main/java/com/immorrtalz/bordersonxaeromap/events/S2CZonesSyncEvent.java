package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.events.data.S2CZonesSyncEventData;

import net.neoforged.bus.api.Event;

public final class S2CZonesSyncEvent extends Event
{
	private final S2CZonesSyncEventData data;

	public S2CZonesSyncEvent(S2CZonesSyncEventData data) { this.data = data; }

	public S2CZonesSyncEventData getData() { return data; }
}