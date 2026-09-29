package com.immorrtalz.bordersonxaeromap.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.immorrtalz.bordersonxaeromap.client.ZoneMapRenderer;

import net.minecraft.client.gui.GuiGraphics;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.world.MapDimension;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin
{
	@Shadow private double cameraX;
	@Shadow private double cameraZ;
	@Shadow private double scale;
	@Shadow private MapProcessor mapProcessor;
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lxaero/map/element/MapElementRenderHandler;render"))
	private void bordersonxaeromap$renderZones(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		MapDimension mapDim = mapProcessor.getMapWorld().getCurrentDimension();
		if (mapDim == null) return;

		ZoneMapRenderer.render(graphics, cameraX, cameraZ, scale, mapDim.getDimId().location().toString());
	}
}