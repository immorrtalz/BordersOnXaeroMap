package com.immorrtalz.bordersonxaeromap.client.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

import org.lwjgl.glfw.GLFW;

public class DeleteZoneScreen extends Screen
{
	private final Screen parent;

	private final Runnable onDelete;

	public DeleteZoneScreen(Screen parent, Runnable onDelete)
	{
		super(Component.translatable("borders_on_xaero_map.delete_zone_question"));
		this.parent = parent;
		this.onDelete = onDelete;
	}

	@Override
	protected void init()
	{
		int centerX = width / 2;
		int centerY = height / 2;

		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
			.pos(centerX - 110, centerY + 20)
			.size(108, 20)
			.build());

		addRenderableWidget(Button.builder(Component.translatable("borders_on_xaero_map.delete"), button -> confirm())
			.pos(centerX + 2, centerY + 20)
			.size(108, 20)
			.build());
	}

	@Override
	public void onClose()
	{
		if (minecraft != null)
			minecraft.setScreen(parent);
	}

	private void confirm()
	{
		if (onDelete != null)
			onDelete.run();
		onClose();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
		{
			confirm();
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_ESCAPE)
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
		drawContext.drawCenteredString(font, title, centerX, centerY - 20, 16777215);
		drawContext.drawCenteredString(font, Component.translatable("borders_on_xaero_map.this_action_cannot_be_undone"), centerX, centerY, 13421772);
	}
}