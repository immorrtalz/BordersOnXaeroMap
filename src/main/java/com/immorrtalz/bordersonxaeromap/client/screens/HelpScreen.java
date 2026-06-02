package com.immorrtalz.bordersonxaeromap.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

public class HelpScreen extends Screen
{
	private final Screen parent;

	public HelpScreen(Screen parent)
	{
		super(Component.translatable("borders_on_xaero_map.help.title"));
		this.parent = parent;
	}

	@Override
	protected void init()
	{
		int centerX = width / 2;
		int centerY = height / 2;

		addRenderableWidget(Button.builder(Component.translatable("borders_on_xaero_map.OK"), button -> onClose())
			.pos(centerX - 110, centerY + 54)
			.size(220, 20)
			.build());
	}

	@Override
	public void onClose()
	{
		if (minecraft != null)
			minecraft.setScreen(parent);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE)
		{
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(@Nonnull GuiGraphics drawContext, int mouseX, int mouseY, float delta)
	{
		drawContext.fill(0, 0, width, height, -1442840576);
		super.render(drawContext, mouseX, mouseY, delta);

		int centerX = width / 2;
		int centerY = height / 2;
		drawContext.drawCenteredString(font, title, centerX, centerY - 30, 16777215);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.help.line1"), centerX, centerY - 10, 13421772);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.help.line2"), centerX, centerY, 13421772);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.help.line3"), centerX, centerY + 10, 13421772);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.help.line4"), centerX, centerY + 20, 13421772);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.help.line5"), centerX, centerY + 30, 13421772);
	}
}