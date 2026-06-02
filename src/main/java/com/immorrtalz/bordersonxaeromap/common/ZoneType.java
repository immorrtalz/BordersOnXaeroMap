package com.immorrtalz.bordersonxaeromap.common;

public enum ZoneType
{
	PUBLIC(0),
	PRIVATE(1);

	private final int id;

	ZoneType(int id) { this.id = id; }

	public int getId() { return id; }
}