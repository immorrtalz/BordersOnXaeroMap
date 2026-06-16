package com.immorrtalz.bordersonxaeromap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.immorrtalz.bordersonxaeromap.common.Zone;
import com.immorrtalz.bordersonxaeromap.common.ZoneType;
import com.immorrtalz.bordersonxaeromap.events.data.S2CZonesSyncEventData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class BordersOnXaeroMapServer
{
	private static Gson gson;
	private static List<Zone> allZones = new ArrayList<>();

	public BordersOnXaeroMapServer() {}
	
	public static void init(IEventBus modEventBus)
	{
		gson = new Gson();
		modEventBus.addListener(BordersOnXaeroMapServer::onServerSetup);

		allZones.add(new Zone(2, "minecraft:overworld", "Some test zone", "63643417-01ac-44e0-9fbd-7032213c2eb3", ZoneType.PRIVATE,
			0xFFDD74F2, 0xFFDD74F2, Set.of(ChunkPos.asLong(0, 0), ChunkPos.asLong(1, 0), ChunkPos.asLong(0, 1))));
	}

	static void onServerSetup(FMLDedicatedServerSetupEvent event)
	{
		NeoForge.EVENT_BUS.addListener(BordersOnXaeroMapServer::onClientConnectedToServer);

		BordersOnXaeroMap.LOGGER.info("BordersOnXaeroMap server initialized.");
	}

	static void onClientConnectedToServer(PlayerEvent.PlayerLoggedInEvent event)
	{
		ServerPlayer serverPlayer = (ServerPlayer)event.getEntity();

		String zonesJson = gson.toJson(getAllZones());
		PacketDistributor.sendToPlayer(serverPlayer, new S2CZonesSyncEventData(false, zonesJson));

		BordersOnXaeroMap.LOGGER.info("Player {} joined a server, sent all zones sync.", serverPlayer.getUUID().toString());
	}

	public static List<Zone> getAllZones() { return allZones; }
	public static void setAllZones(List<Zone> allZones) { BordersOnXaeroMapServer.allZones = allZones; }
}