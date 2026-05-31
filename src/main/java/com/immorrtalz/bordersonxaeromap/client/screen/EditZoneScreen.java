package com.immorrtalz.bordersonxaeromap.client.screen;

import com.immorrtalz.bordersonxaeromap.zone.ZonePalette;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class EditZoneScreen extends Screen
{
	private final Screen parent;

	private final String initialName;
	private final int initialBorderColor;
	private final int initialFillColor;

	private final Consumer<String> onRename;
	private final Consumer<Integer> onChangeBorderColor;
	private final Consumer<Integer> onChangeFillColor;

	private EditBox nameField;
	private Button borderColorButton;
	private Button fillColorButton;

	private int newBorderColor;
	private int newFillColor;

	public EditZoneScreen(Screen parent, String initialName, int initialBorderColor, int initialFillColor,
		Consumer<String> onRename, Consumer<Integer> onChangeBorderColor, Consumer<Integer> onChangeFillColor)
	{
		super(Component.translatable("borders_on_xaero_map.edit_zone"));
		this.parent = parent;
		this.initialName = initialName;
		this.initialBorderColor = initialBorderColor;
		this.initialFillColor = initialFillColor;
		this.newBorderColor = initialBorderColor;
		this.newFillColor = initialFillColor;
		this.onRename = onRename;
		this.onChangeBorderColor = onChangeBorderColor;
		this.onChangeFillColor = onChangeFillColor;
	}

	@Override
	protected void init()
	{
		int centerX = width / 2;
		int centerY = height / 2;

		nameField = new EditBox(font, centerX - 110, centerY - 31, 220, 20, Component.translatable("borders_on_xaero_map.zone_name"));
		nameField.setValue(initialName);
		nameField.setMaxLength(64);
		nameField.setFocused(true);
		addRenderableWidget(nameField);

		borderColorButton = Button.builder(ZonePalette.colorName(initialBorderColor), button -> changeBorderColor())
			.pos(centerX - 110, centerY + 13)
			.size(108, 20)
			.build();

		borderColorButton.setFGColor(initialBorderColor);

		addRenderableWidget(borderColorButton);

		fillColorButton = Button.builder(ZonePalette.colorName(initialFillColor), button -> changeFillColor())
			.pos(centerX + 2, centerY + 13)
			.size(108, 20)
			.build();

		fillColorButton.setFGColor(initialFillColor);

		addRenderableWidget(fillColorButton);

		addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
			.pos(centerX - 110, centerY + 49)
			.size(108, 20)
			.build());

		addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> confirm())
			.pos(centerX + 2, centerY + 49)
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
		String value = nameField.getValue().trim();

		if (!value.isEmpty()) onRename.accept(value);
		onChangeBorderColor.accept(newBorderColor);
		onChangeFillColor.accept(newFillColor);

		onClose();
	}

	private void changeBorderColor()
	{
		newBorderColor = ZonePalette.nextBorderColor(newBorderColor);
		borderColorButton.setMessage(ZonePalette.colorName(newBorderColor));
		borderColorButton.setFGColor(newBorderColor);
	}

	private void changeFillColor()
	{
		newFillColor = ZonePalette.nextFillColor(newFillColor);
		fillColorButton.setMessage(ZonePalette.colorName(newFillColor));
		fillColorButton.setFGColor(newFillColor);
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
	public void render(GuiGraphics drawContext, int mouseX, int mouseY, float delta)
	{
		drawContext.fill(0, 0, width, height, -1442840576);
		super.render(drawContext, mouseX, mouseY, delta);

		int centerX = width / 2;
		int centerY = height / 2;
		drawContext.drawCenteredString(font, title, centerX, centerY - 65, 16777215);
		drawContext.drawString(font, Component.translatable("borders_on_xaero_map.zone_name"), centerX - 110, centerY - 44, 13421772, false);
		drawContext.drawString(font, Component.translatable("borders_on_xaero_map.border_color"), centerX - 110, centerY, 13421772, false);
		drawContext.drawString(font, Component.translatable("borders_on_xaero_map.fill_color"), centerX + 2, centerY, 13421772, false);
	}
}