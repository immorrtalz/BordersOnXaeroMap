package com.immorrtalz.bordersonxaeromap;

import com.google.gson.Gson;
import com.immorrtalz.bordersonxaeromap.events.data.S2CZonesSyncEventData;
import com.immorrtalz.bordersonxaeromap.server.ServerManager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class BordersOnXaeroMapServer
{
	private static Gson gson;

	public BordersOnXaeroMapServer() {}
	
	public static void init(IEventBus modEventBus)
	{
		gson = new Gson();
		modEventBus.addListener(BordersOnXaeroMapServer::onServerSetup);
	}

	static void onServerSetup(FMLDedicatedServerSetupEvent event)
	{
		NeoForge.EVENT_BUS.addListener(BordersOnXaeroMapServer::onClientConnectedToServer);

		BordersOnXaeroMap.LOGGER.info("BordersOnXaeroMap server initialized.");
	}

	static void onClientConnectedToServer(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer serverPlayer = (ServerPlayer)event.getEntity();

		String zonesJson = gson.toJson(ServerManager.getAllZones());
		PacketDistributor.sendToPlayer(serverPlayer, new S2CZonesSyncEventData(false, zonesJson));

		BordersOnXaeroMap.LOGGER.info("Player {} joined a server, sent all zones sync.", serverPlayer.getUUID().toString());
	}
}