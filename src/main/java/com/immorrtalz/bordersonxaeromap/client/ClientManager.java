package com.immorrtalz.bordersonxaeromap.client;

import java.util.List;

import com.immorrtalz.bordersonxaeromap.common.Zone;

public class ClientManager
{
	private static List<Zone> allZones;

	public ClientManager(List<Zone> allZones)
	{
		ClientManager.allZones = allZones;
	}

	public static List<Zone> getAllZones() { return allZones; }
	public static void setAllZones(List<Zone> allZones) { ClientManager.allZones = allZones; }

	public static void processZonesSync(List<Zone> receivedZones)
	{
		receivedZones.forEach(receivedZone ->
		{
			allZones.removeIf(existingZone -> existingZone.getId() == receivedZone.getId());
			allZones.add(receivedZone);
		});
	}
}