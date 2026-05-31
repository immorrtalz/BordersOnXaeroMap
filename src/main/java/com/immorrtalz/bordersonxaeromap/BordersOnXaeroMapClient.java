package com.immorrtalz.bordersonxaeromap;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = BordersOnXaeroMap.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = BordersOnXaeroMap.MODID, value = Dist.CLIENT)
public class BordersOnXaeroMapClient
{
	public BordersOnXaeroMapClient() { BordersOnXaeroMap.initializeRepository(); }

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) { BordersOnXaeroMap.LOGGER.info("Xaero local zones client initialized."); }
}