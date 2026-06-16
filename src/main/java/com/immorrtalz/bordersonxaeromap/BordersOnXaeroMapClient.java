package com.immorrtalz.bordersonxaeromap;

import java.util.List;

import com.immorrtalz.bordersonxaeromap.common.Zone;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class BordersOnXaeroMapClient
{
	private static List<Zone> allZones;

	public BordersOnXaeroMapClient() {}

	public static void init(IEventBus modEventBus)
	{
		modEventBus.addListener(BordersOnXaeroMapClient::onClientSetup);
	}

	static void onClientSetup(FMLClientSetupEvent event)
	{
		NeoForge.EVENT_BUS.addListener(BordersOnXaeroMapClient::onClientLoggedIn);

		BordersOnXaeroMap.LOGGER.info("BordersOnXaeroMap client initialized.");
	}

	static void onClientLoggedIn(LoggingIn event)
	{
		BordersOnXaeroMap.LOGGER.info("Client (self) joined a server.");
		// PacketDistributor.sendToServer(new C2SJoinedWorldEventData("Hello from the client!"));
	}

	public static List<Zone> getAllZones() { return allZones; }
	public static void setAllZones(List<Zone> allZones) { BordersOnXaeroMapClient.allZones = allZones; }

	public static void processZonesSync(List<Zone> receivedZones)
	{
		receivedZones.forEach(receivedZone ->
		{
			allZones.removeIf(existingZone -> existingZone.getId() == receivedZone.getId());
			allZones.add(receivedZone);
		});
	}
}