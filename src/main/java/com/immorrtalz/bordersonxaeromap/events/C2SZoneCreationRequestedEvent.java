package com.immorrtalz.bordersonxaeromap.events;

import net.neoforged.bus.api.Event;

public final class C2SZoneCreationRequestedEvent extends Event
{
	private final String name;
	private final int borderColor;
	private final int fillColor;
	private final boolean isPublic;

	public C2SZoneCreationRequestedEvent(String name, int borderColor, int fillColor, boolean isPublic)
	{
		this.name = name;
		this.borderColor = borderColor;
		this.fillColor = fillColor;
		this.isPublic = isPublic;
	}

	public String getName() { return name; }
	public int getBorderColor() { return borderColor; }
	public int getFillColor() { return fillColor; }
	public boolean isPublic() { return isPublic; }
}