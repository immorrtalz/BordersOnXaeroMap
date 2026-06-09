package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.events.data.S2CErrorEventData;

import net.neoforged.bus.api.Event;

public final class S2CErrorEvent extends Event
{
	private final S2CErrorEventData data;

	public S2CErrorEvent(S2CErrorEventData data) { this.data = data; }

	public S2CErrorEventData getData() { return data; }
}