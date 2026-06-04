package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.events.data.C2SJoinedWorldEventData;

import net.neoforged.bus.api.Event;

public final class C2SJoinedWorldEvent extends Event
{
	private final C2SJoinedWorldEventData data;

	public C2SJoinedWorldEvent(C2SJoinedWorldEventData data)
	{
		this.data = data;
	}

	public C2SJoinedWorldEventData getData() { return data; }
}