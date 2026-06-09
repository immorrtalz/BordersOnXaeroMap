package com.immorrtalz.bordersonxaeromap.server;

import java.util.Set;

import com.immorrtalz.bordersonxaeromap.common.Zone;

public class ServerManager
{
	private static Set<Zone> allZones;

	public ServerManager(Set<Zone> allZones)
	{
		ServerManager.allZones = allZones;
	}

	public static Set<Zone> getAllZones() { return allZones; }
	public static void setAllZones(Set<Zone> allZones) { ServerManager.allZones = allZones; }
}