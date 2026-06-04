package com.immorrtalz.bordersonxaeromap.events;

import net.neoforged.bus.api.Event;

public final class S2CErrorEvent extends Event
{
	private final String message;

	public S2CErrorEvent(String message)
	{
		this.message = message;
	}

	public String getMessage() { return message; }
}