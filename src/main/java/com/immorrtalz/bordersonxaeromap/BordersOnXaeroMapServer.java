package com.immorrtalz.bordersonxaeromap;

import com.immorrtalz.bordersonxaeromap.events.data.C2SJoinedWorldEventData;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class BordersOnXaeroMapServer
{
	public BordersOnXaeroMapServer() {}
	
	public static void init(IEventBus modEventBus)
	{
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

		BordersOnXaeroMap.LOGGER.info("Player {} joined a server.", serverPlayer.getUUID().toString());
		PacketDistributor.sendToPlayer(serverPlayer, new C2SJoinedWorldEventData("Hello from the server!"));
	}
}