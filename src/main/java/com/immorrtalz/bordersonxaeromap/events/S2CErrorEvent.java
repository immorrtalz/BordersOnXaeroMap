package com.immorrtalz.bordersonxaeromap.events;

import com.immorrtalz.bordersonxaeromap.events.EventBus.IEvent;

public final class S2CErrorEvent implements IEvent
{
	private final String message;

	public S2CErrorEvent(String message)
	{
		this.message = message;
	}

	public String getMessage() { return message; }
}