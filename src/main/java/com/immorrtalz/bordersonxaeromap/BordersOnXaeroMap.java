package com.immorrtalz.bordersonxaeromap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.immorrtalz.bordersonxaeromap.common.Zone;
import com.immorrtalz.bordersonxaeromap.events.data.S2CErrorEventData;
import com.immorrtalz.bordersonxaeromap.events.data.S2CZonesSyncEventData;
import com.mojang.logging.LogUtils;

import java.util.List;

import org.slf4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(BordersOnXaeroMap.MODID)
public class BordersOnXaeroMap
{
	public static final String MODID = "bordersonxaeromap";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static Gson gson;

	// private static ZoneRepository repository;

	public BordersOnXaeroMap(IEventBus modEventBus, Dist dist)
	{
		gson = new Gson();

		if (dist.isClient()) BordersOnXaeroMapClient.init(modEventBus);
		else BordersOnXaeroMapServer.init(modEventBus);

		modEventBus.addListener(BordersOnXaeroMap::register);
	}

	/* public static void initializeRepository()
	{
		if (repository != null) return;
		Path savePath = FMLPaths.CONFIGDIR.get().resolve("bordersonxaeromap.json");
		// repository = new ZoneRepository(savePath);
		LOGGER.info("Initialized local zones storage at {}", savePath);
	} */

	/* public static ZoneRepository getRepository() { return repository; } */

	public static void register(final RegisterPayloadHandlersEvent event)
	{
		final PayloadRegistrar registrar = event.registrar("1");

		registrar.playToClient(
			S2CErrorEventData.TYPE,
			S2CErrorEventData.STREAM_CODEC,
			new MainThreadPayloadHandler<>(ClientPayloadHandler::handleS2CErrorEvent));

		registrar.playToClient(
			S2CZonesSyncEventData.TYPE,
			S2CZonesSyncEventData.STREAM_CODEC,
			new MainThreadPayloadHandler<>(ClientPayloadHandler::handleS2CZonesSyncEvent));

		/* registrar.playBidirectional(
			S2CErrorEventData.TYPE,
			S2CErrorEventData.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				ClientPayloadHandler::handleS2CErrorEvent,
				ServerPayloadHandler::handleS2CErrorEvent)); */
	}

	public class ClientPayloadHandler
	{
		public static void handleS2CErrorEvent(final S2CErrorEventData data, final IPayloadContext context)
		{
			String message = data.message();

			BordersOnXaeroMap.LOGGER.info("Received an error from the server: \"{}`\"", message);
		}

		public static void handleS2CZonesSyncEvent(final S2CZonesSyncEventData data, final IPayloadContext context)
		{
			boolean isPartial = data.isPartial();
			String zonesJson = data.zonesJson();
			List<Zone> zones = gson.fromJson(zonesJson, new TypeToken<List<Zone>>() {}.getType());

			if (!isPartial) BordersOnXaeroMapClient.setAllZones(zones);
			else BordersOnXaeroMapClient.processZonesSync(zones);

			BordersOnXaeroMap.LOGGER.info("Received zones sync from the server: partial={}, json={}", isPartial, zonesJson);
		}
	}

	public class ServerPayloadHandler {}
}