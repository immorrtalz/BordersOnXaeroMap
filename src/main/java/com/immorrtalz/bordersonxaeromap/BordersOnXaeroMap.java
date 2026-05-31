package com.immorrtalz.bordersonxaeromap;

import com.immorrtalz.bordersonxaeromap.zone.ZoneRepository;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;

@Mod(BordersOnXaeroMap.MODID)
public class BordersOnXaeroMap
{
	public static final String MODID = "bordersonxaeromap";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static ZoneRepository repository;

	public BordersOnXaeroMap(IEventBus modEventBus)
	{
		// No common setup needed for this client-focused mod.
	}

	public static void initializeRepository()
	{
		if (repository != null) return;
		Path savePath = FMLPaths.CONFIGDIR.get().resolve("xaero-local-zones.json");
		repository = new ZoneRepository(savePath);
		LOGGER.info("Initialized local zones storage at {}", savePath);
	}

	public static ZoneRepository getRepository() { return repository; }
}