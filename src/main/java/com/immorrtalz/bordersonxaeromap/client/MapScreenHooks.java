package com.immorrtalz.bordersonxaeromap.client;

import com.immorrtalz.bordersonxaeromap.BordersOnXaeroMap;
import com.immorrtalz.bordersonxaeromap.client.screen.EditZoneScreen;
import com.immorrtalz.bordersonxaeromap.client.screen.DeleteZoneScreen;
import com.immorrtalz.bordersonxaeromap.client.screen.HelpScreen;
import com.immorrtalz.bordersonxaeromap.zone.ZoneArea;
import com.immorrtalz.bordersonxaeromap.zone.ZoneDimension;
import com.immorrtalz.bordersonxaeromap.zone.ZoneRepository;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = BordersOnXaeroMap.MODID, value = Dist.CLIENT)
public final class MapScreenHooks
{
	private static final int MAX_RENDER_CHUNKS = 12000;
	private static final int TOOLBAR_MARGIN = 8;
	private static final int TOOLBAR_GAP = 2;
	private static final int TOOLBAR_TOP = 40;
	private static final int STATUS_TOP_PADDING = 23;
	private static final int STATUS_BOTTOM_PADDING = 13;

	private static final int BUTTON_SIZE = 20;
	private static final int ICON_SIZE = 16;

	private static final int ICON_VISIBILITY_ON_U = 16;
	private static final int ICON_VISIBILITY_ON_V = 16;
	private static final int ICON_VISIBILITY_OFF_U = 16;
	private static final int ICON_VISIBILITY_OFF_V = 0;

	private static final int ICON_DRAW_ON_U = 32;
	private static final int ICON_DRAW_ON_V = 16;
	private static final int ICON_DRAW_OFF_U = 32;
	private static final int ICON_DRAW_OFF_V = 0;

	private static final int ICON_EDIT_U = 0;
	private static final int ICON_EDIT_V = 0;

	private static final int ICON_DELETE_U = 48;
	private static final int ICON_DELETE_V = 0;

	private static final int ICON_HELP_U = 0;
	private static final int ICON_HELP_V = 16;

	private static final int GUI_TEXTURE_SIZE = 64;

	private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath("bordersonxaeromap", "gui/gui.png");

	private static final int PREVIEW_PAINT_FILL = 1715052005;
	private static final int PREVIEW_PAINT_BORDER = -11881473;
	private static final int PREVIEW_ERASE_FILL = 1726434364;
	private static final int PREVIEW_ERASE_BORDER = -39338;
	private static final long DOUBLE_CLICK_WINDOW_MS = 300L;

	private static final Map<Screen, ScreenSession> SESSIONS = new WeakHashMap<>();

	private MapScreenHooks() {}

	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Pre event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		ScreenSession session = session(screen);
		session.stopSelection();
		session.toolbarAttached = false;
		session.visibilityToggleButton = null;
		session.drawToggleButton = null;
		session.editZoneButton = null;
		session.deleteZoneButton = null;
		session.helpButton = null;
	}

	@SubscribeEvent
	public static void onScreenInitPost(ScreenEvent.Init.Post event)
	{
		Screen screen = event.getScreen();
		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;
		attachToolbar(event, screen);
	}

	@SubscribeEvent
	public static void onScreenRender(ScreenEvent.Render.Post event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		onAfterRender(screen, event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
	}

	@SubscribeEvent
	public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		if (!onAllowKeyPress(screen, event.getKeyCode(), event.getScanCode(), event.getModifiers()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		if (!onAllowMouseClick(screen, event.getMouseX(), event.getMouseY(), event.getButton()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		if (!onAllowMouseDrag(screen, event.getMouseX(), event.getMouseY()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event)
	{
		Screen screen = event.getScreen();

		if (!XaeroMapReflection.isXaeroMapScreen(screen)) return;

		if (!onAllowMouseRelease(screen, event.getMouseX(), event.getMouseY(), event.getButton()))
			event.setCanceled(true);
	}

	private static void attachToolbar(ScreenEvent.Init.Post event, Screen screen)
	{
		ScreenSession session = session(screen);
		if (session.toolbarAttached) return;
		session.toolbarAttached = true;

		int columnX = screen.width - BUTTON_SIZE - TOOLBAR_MARGIN;
		int row1Y = TOOLBAR_TOP;
		int row2Y = row1Y + BUTTON_SIZE + TOOLBAR_GAP;
		int row3Y = row2Y + BUTTON_SIZE + TOOLBAR_GAP;
		int row4Y = row3Y + BUTTON_SIZE + TOOLBAR_GAP;
		int row5Y = row4Y + BUTTON_SIZE + TOOLBAR_GAP;

		session.toolbarLeft = columnX;
		session.toolbarTop = row1Y;
		session.toolbarRight = columnX + BUTTON_SIZE;
		session.toolbarBottom = row5Y + BUTTON_SIZE;

		session.visibilityToggleButton = addToolbarButton(event, new XaeroIconButton(columnX, row1Y, ICON_VISIBILITY_ON_U, ICON_VISIBILITY_ON_V,
			Component.translatable("borders_on_xaero_map.toolbar_button.zones_visibility_on"),
			button -> toggleZonesVisibility(screen)));

		session.drawToggleButton = addToolbarButton(event, new XaeroIconButton(columnX, row2Y, ICON_DRAW_OFF_U, ICON_DRAW_OFF_V,
			Component.translatable("borders_on_xaero_map.toolbar_button.draw_mode_off"),
			button -> toggleDrawMode(screen)));

		session.editZoneButton = addToolbarButton(event, new XaeroIconButton(columnX, row3Y, ICON_EDIT_U, ICON_EDIT_V,
			Component.translatable("borders_on_xaero_map.edit_zone"),
			button -> editFocusedZone(screen)));

		session.deleteZoneButton = addToolbarButton(event, new XaeroIconButton(columnX, row4Y, ICON_DELETE_U, ICON_DELETE_V,
			Component.translatable("borders_on_xaero_map.delete_zone"),
			button -> deleteFocusedZone(screen)));

		session.helpButton = addToolbarButton(event, new XaeroIconButton(columnX, row5Y, ICON_HELP_U, ICON_HELP_V,
			Component.translatable("borders_on_xaero_map.help.title"),
			button -> openHelpScreen(screen)));
	}

	private static void onAfterRender(Screen screen, GuiGraphics drawContext, double mouseX, double mouseY, float delta)
	{
		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);
		if (state == null || state.scale() <= 0.0D) return;

		ZoneRepository repository = BordersOnXaeroMap.getRepository();
		if (repository == null) return;

		ScreenSession session = session(screen);
		ZoneDimension dimension = repository.getDimension(state.worldId(), state.dimensionId());
		ZoneArea activeZone = resolveActiveZone(session, state, dimension);
		String activeZoneId = activeZone == null ? null : activeZone.getId();

		if (dimension != null)
			renderZones(screen, drawContext, state, dimension, activeZoneId);

		renderSelectionPreview(drawContext, state, session);
		renderStatus(drawContext, state, session, dimension, activeZone);
		updateToolbarState(session, state, dimension, activeZone);
	}

	private static boolean onAllowKeyPress(Screen screen, int keyCode, int scanCode, int modifiers)
	{
		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);
		if (state == null) return true;

		ZoneRepository repository = BordersOnXaeroMap.getRepository();
		if (repository == null) return true;

		// Don't change the active zone for Shift key presses (prevents changing while holding Shift)
		if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT)
			return true;

		ScreenSession session = session(screen);
		ZoneDimension dimension = repository.getOrCreateDimension(state.worldId(), state.dimensionId());
		ZoneArea hoveredZone = dimension.getZoneAtChunk(state.cursorChunkX(), state.cursorChunkZ());

		if (hoveredZone != null)
			setActiveZone(session, state, hoveredZone.getId());

		return true;
	}

	private static boolean onAllowMouseClick(Screen screen, double mouseX, double mouseY, int button)
	{
		ScreenSession session = session(screen);
		if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;
		if (isInsideToolbar(session, mouseX, mouseY)) return true; 

		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);
		if (state == null || state.scale() <= 0.0D) return true;

		if (!session.drawModeEnabled)
		{
			ZoneArea clickedZone = null;
			ZoneRepository repository = BordersOnXaeroMap.getRepository();

			if (repository != null)
			{
				ZoneDimension dimension = repository.getDimension(state.worldId(), state.dimensionId());

				if (dimension != null)
				{
					int chunkX = screenToChunkX(state, mouseX);
					int chunkZ = screenToChunkZ(state, mouseY);
					clickedZone = dimension.getZoneAtChunk(chunkX, chunkZ);
				}
			}

			long now = Util.getMillis();
			boolean isDoubleClick = clickedZone != null && isDoubleClick(session, state, clickedZone, now);
			recordClick(session, state, clickedZone, now);

			if (clickedZone != null)
			{
				setActiveZone(session, state, clickedZone.getId());

				if (isDoubleClick)
				{
					openEditZoneScreen(screen, state, clickedZone);
					return false;
				}
			}
			else clearActiveZone(session);

			return true;
		}

		beginSelection(session, state, mouseX, mouseY, isShiftDown());
		return false;
	}

	private static boolean onAllowMouseDrag(Screen screen, double mouseX, double mouseY)
	{
		ScreenSession session = session(screen);

		if (!session.selectionActive) return true;

		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);

		if (state == null || state.scale() <= 0.0D) return false;

		addSelectionPoint(session, state, mouseX, mouseY);
		return false;
	}

	private static boolean onAllowMouseRelease(Screen screen, double mouseX, double mouseY, int button)
	{
		ScreenSession session = session(screen);

		if (!session.selectionActive || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return true;

		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);

		if (state != null && state.scale() > 0.0D) addSelectionPoint(session, state, mouseX, mouseY);

		commitSelection(session, state);
		session.stopSelection();
		return false;
	}

	private static void beginSelection(ScreenSession session, XaeroMapReflection.MapState state, double mouseX, double mouseY, boolean eraseMode)
	{
		session.selectionActive = true;
		session.selectionEraseMode = eraseMode;
		session.selectionWorldId = state.worldId();
		session.selectionDimensionId = state.dimensionId();
		session.selectionChunks.clear();
		session.lastSelectionChunkSet = false;

		addSelectionPoint(session, state, mouseX, mouseY);
	}

	private static void addSelectionPoint(ScreenSession session, XaeroMapReflection.MapState state, double mouseX, double mouseY)
	{
		int chunkX = screenToChunkX(state, mouseX);
		int chunkZ = screenToChunkZ(state, mouseY);

		if (!session.lastSelectionChunkSet)
		{
			addSelectionChunk(session, chunkX, chunkZ);
			session.lastSelectionChunkX = chunkX;
			session.lastSelectionChunkZ = chunkZ;
			session.lastSelectionChunkSet = true;
			return;
		}

		if (session.lastSelectionChunkX == chunkX && session.lastSelectionChunkZ == chunkZ) return;

		addSelectionText(session, session.lastSelectionChunkX, session.lastSelectionChunkZ, chunkX, chunkZ);
		session.lastSelectionChunkX = chunkX;
		session.lastSelectionChunkZ = chunkZ;
	}

	private static void addSelectionText(ScreenSession session, int fromChunkX, int fromChunkZ, int toChunkX, int toChunkZ)
	{
		int x = fromChunkX;
		int z = fromChunkZ;
		int dx = Math.abs(toChunkX - fromChunkX);
		int dz = Math.abs(toChunkZ - fromChunkZ);
		int sx = fromChunkX <= toChunkX ? 1 : -1;
		int sz = fromChunkZ <= toChunkZ ? 1 : -1;
		int error = dx - dz;

		while (true)
		{
			addSelectionChunk(session, x, z);

			if (x == toChunkX && z == toChunkZ) break;
			int e2 = error * 2;

			if (e2 > -dz)
			{
				error -= dz;
				x += sx;
			}

			if (e2 < dx)
			{
				error += dx;
				z += sz;
			}
		}
	}

	private static void addSelectionChunk(ScreenSession session, int chunkX, int chunkZ)
	{
		if (session.selectionChunks.size() >= ZoneRepository.MAX_SELECTION_CHUNKS) return;

		session.selectionChunks.add(ZoneRepository.packChunk(chunkX, chunkZ));
	}

	private static void commitSelection(ScreenSession session, XaeroMapReflection.MapState fallbackState)
	{
		if (session.selectionChunks.isEmpty()) return;

		ZoneRepository repository = BordersOnXaeroMap.getRepository();

		if (repository == null) return;

		String worldId = session.selectionWorldId;
		String dimensionId = session.selectionDimensionId;

		if ((worldId == null || dimensionId == null) && fallbackState != null)
		{
			worldId = fallbackState.worldId();
			dimensionId = fallbackState.dimensionId();
		}

		if (worldId == null || dimensionId == null) return;

		ZoneDimension dimension = repository.getOrCreateDimension(worldId, dimensionId);
		Set<Long> selectionCopy = new HashSet<>(session.selectionChunks);

		if (session.selectionEraseMode)
		{
			ZoneArea target = resolveActiveZone(session, worldId, dimensionId, dimension);

			if (target == null && session.lastSelectionChunkSet)
			{
				target = dimension.getZoneAtChunk(session.lastSelectionChunkX, session.lastSelectionChunkZ);

				if (target != null) setActiveZone(session, worldId, dimensionId, target.getId());
			}

			if (target != null)
			{
				repository.removeChunksFromZone(worldId, dimensionId, target.getId(), selectionCopy);

				if (dimension.getZoneById(target.getId()) == null && target.getId().equals(session.activeZoneId))
					clearActiveZone(session);
			}
		}
		else
		{
			ZoneArea target = resolveActiveZone(session, worldId, dimensionId, dimension);

			if (target == null)
			{
				ZoneArea created = repository.createZone(worldId, dimensionId, selectionCopy);

				if (created != null)
				{
					setActiveZone(session, worldId, dimensionId, created.getId());
					notifyActionbar(Component.translatable("borders_on_xaero_map.actionbar.zone_created", created.getName(), created.getChunkCount()));
				}
			}
			else repository.addChunksToZone(worldId, dimensionId, target.getId(), selectionCopy);
		}
	}

	private static void toggleZonesVisibility(Screen screen)
	{
		ScreenSession session = session(screen);
		session.zonesVisible = !session.zonesVisible;

		if (!session.zonesVisible) session.drawModeEnabled = false;
		session.drawToggleButton.active = session.zonesVisible;
	}

	private static void toggleDrawMode(Screen screen)
	{
		ScreenSession session = session(screen);
		session.drawModeEnabled = !session.drawModeEnabled;

		if (!session.drawModeEnabled)
		session.stopSelection();
	}

	private static void editFocusedZone(Screen screen)
	{
		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);
		ZoneRepository repository = BordersOnXaeroMap.getRepository();

		if (state == null || repository == null) return;

		ScreenSession session = session(screen);
		ZoneDimension dimension = repository.getDimension(state.worldId(), state.dimensionId());
		if (dimension == null) return;

		ZoneArea target = resolveFocusedZone(session, state, dimension);
		if (target == null) return;

		setActiveZone(session, state, target.getId());
		openEditZoneScreen(screen, state, target);
	}

	private static void deleteFocusedZone(Screen screen)
	{
		XaeroMapReflection.MapState state = XaeroMapReflection.readState(screen);
		ZoneRepository repository = BordersOnXaeroMap.getRepository();

		if (state == null || repository == null) return;

		ScreenSession session = session(screen);
		ZoneDimension dimension = repository.getDimension(state.worldId(), state.dimensionId());
		if (dimension == null) return;

		ZoneArea target = resolveFocusedZone(session, state, dimension);
		if (target == null) return;

		setActiveZone(session, state, target.getId());
		openDeleteZoneScreen(screen, state, target);
	}

	private static ZoneArea resolveFocusedZone(ScreenSession session, XaeroMapReflection.MapState state, ZoneDimension dimension)
	{
		ZoneArea active = resolveActiveZone(session, state, dimension);
		if (active != null) return active;
		ZoneArea hovered = dimension.getZoneAtChunk(state.cursorChunkX(), state.cursorChunkZ());

		if (hovered != null) setActiveZone(session, state, hovered.getId());

		return hovered;
	}

	private static void openEditZoneScreen(Screen parent, XaeroMapReflection.MapState state, ZoneArea targetZone)
	{
		ZoneRepository repository = BordersOnXaeroMap.getRepository();
		if (repository == null) return;

		Minecraft client = Minecraft.getInstance();

		client.setScreen(new EditZoneScreen(parent, targetZone.getName(), targetZone.getBorderColor(), targetZone.getFillColor(),
			newName ->
			{
				repository.renameZone(state.worldId(), state.dimensionId(), targetZone.getId(), newName);
			},
			newBorderColor ->
			{
				repository.setBorderColor(state.worldId(), state.dimensionId(), targetZone.getId(), newBorderColor);
			},
			newFillColor ->
			{
				repository.setFillColor(state.worldId(), state.dimensionId(), targetZone.getId(), newFillColor);
			}));
	}

	private static void openDeleteZoneScreen(Screen parent, XaeroMapReflection.MapState state, ZoneArea targetZone)
	{
		ZoneRepository repository = BordersOnXaeroMap.getRepository();
		if (repository == null) return;

		Minecraft client = Minecraft.getInstance();

		client.setScreen(new DeleteZoneScreen(parent,
			() ->
			{
				if (repository.deleteZone(state.worldId(), state.dimensionId(), targetZone.getId()))
					notifyActionbar(Component.translatable("borders_on_xaero_map.actionbar.zone_deleted", targetZone.getName()));
			}));
	}

	private static void openHelpScreen(Screen parent)
	{
		ZoneRepository repository = BordersOnXaeroMap.getRepository();
		if (repository == null) return;

		Minecraft client = Minecraft.getInstance();
		client.setScreen(new HelpScreen(parent));
	}

	private static void renderZones(Screen screen, GuiGraphics drawContext, XaeroMapReflection.MapState state, ZoneDimension dimension, String activeZoneId)
	{
		ScreenSession session = session(screen);
		if (!session.zonesVisible) return;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		double halfWorldWidth = state.framebufferWidth() / 2.0D / state.scale();
		double halfWorldHeight = state.framebufferHeight() / 2.0D / state.scale();

		int minChunkX = (int) Math.floor((state.cameraX() - halfWorldWidth) / 16.0D) - 1;
		int maxChunkX = (int) Math.floor((state.cameraX() + halfWorldWidth) / 16.0D) + 1;
		int minChunkZ = (int) Math.floor((state.cameraZ() - halfWorldHeight) / 16.0D) - 1;
		int maxChunkZ = (int) Math.floor((state.cameraZ() + halfWorldHeight) / 16.0D) + 1;

		long totalVisible = (long) (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
		long maxVisible = MAX_RENDER_CHUNKS;

		if (state.scale() < 1.0D)
		{
			double scale = Math.max(state.scale(), 0.35D);
			maxVisible = (long) Math.ceil(MAX_RENDER_CHUNKS / (scale * scale));
			maxVisible = Math.min(maxVisible, MAX_RENDER_CHUNKS * 8L);
		}

		if (totalVisible > maxVisible) return;

		int baseBorderWidth = Math.max(1, (int) Math.round(state.scale() * 0.04D / state.scaleFactor()));
		Set<String> labeledZones = new HashSet<>();
		Minecraft client = Minecraft.getInstance();
		Font textRenderer = client.font;

		for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++)
		{
			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++)
			{
				ZoneArea zone = dimension.getZoneAtChunk(chunkX, chunkZ);

				if (zone == null) continue;

				boolean isActive = zone.getId().equals(activeZoneId);
				int borderWidth = isActive ? baseBorderWidth + 1 : baseBorderWidth;
				int borderColor = isActive ? brighten(zone.getBorderColor()) : zone.getBorderColor();

				int x1 = worldToScreenX(state, chunkX * 16.0D);
				int y1 = worldToScreenY(state, chunkZ * 16.0D);
				int x2 = worldToScreenX(state, (chunkX + 1) * 16.0D);
				int y2 = worldToScreenY(state, (chunkZ + 1) * 16.0D);

				if (x1 < state.scaledWidth() && y1 < state.scaledHeight() && x2 > 0 && y2 > 0)
				{
					int fillColor = softenFill(zone.getFillColor());
					drawContext.fill(x1, y1, x2, y2, fillColor);

					String currentId = zone.getId();

					if (!currentId.equals(dimension.getZoneIdAtChunk(chunkX - 1, chunkZ)))
						drawContext.fill(x1, y1, x1 + borderWidth, y2, borderColor);

					if (!currentId.equals(dimension.getZoneIdAtChunk(chunkX + 1, chunkZ)))
						drawContext.fill(x2 - borderWidth, y1, x2, y2, borderColor);

					if (!currentId.equals(dimension.getZoneIdAtChunk(chunkX, chunkZ - 1)))
						drawContext.fill(x1, y1, x2, y1 + borderWidth, borderColor);

					if (!currentId.equals(dimension.getZoneIdAtChunk(chunkX, chunkZ + 1)))
						drawContext.fill(x1, y2 - borderWidth, x2, y2, borderColor);

					if (labeledZones.add(currentId))
					{
						long labelChunkKey = resolveLabelChunkKey(zone);

						if (labelChunkKey != Long.MIN_VALUE)
						{
							int labelChunkX = ZoneRepository.unpackChunkX(labelChunkKey);
							int labelChunkZ = ZoneRepository.unpackChunkZ(labelChunkKey);
							double centerX = (labelChunkX + 0.5D) * 16.0D;
							double centerZ = (labelChunkZ + 0.5D) * 16.0D;
							int labelX = worldToScreenX(state, centerX);
							int labelY = worldToScreenY(state, centerZ);

							if (labelX > 0 && labelY > 0 && labelX < state.scaledWidth() && labelY < state.scaledHeight())
							{
								MutableComponent label = Component.literal(zone.getName());
								int textWidth = textRenderer.width(label);
								drawContext.fill(labelX - textWidth / 2 - 2, labelY - 6, labelX + textWidth / 2 + 2, labelY + 4, 1996488704);
								drawContext.drawString(textRenderer, label, labelX - textWidth / 2, labelY - 5, -1, true);
							}
						}
					}
				}
			}
		}
	}

	private static boolean isDoubleClick(ScreenSession session, XaeroMapReflection.MapState state, ZoneArea zone, long now)
	{
		return zone.getId().equals(session.lastClickZoneId)
			&& state.worldId().equals(session.lastClickWorldId)
			&& state.dimensionId().equals(session.lastClickDimensionId)
			&& now - session.lastClickTime <= DOUBLE_CLICK_WINDOW_MS;
	}

	private static void recordClick(ScreenSession session, XaeroMapReflection.MapState state, ZoneArea zone, long now)
	{
		session.lastClickTime = now;
		session.lastClickZoneId = zone == null ? null : zone.getId();
		session.lastClickWorldId = state.worldId();
		session.lastClickDimensionId = state.dimensionId();
	}

	private static long resolveLabelChunkKey(ZoneArea zone)
	{
		Set<Long> chunkKeys = zone.getChunkKeys();

		if (chunkKeys.isEmpty())
			return Long.MIN_VALUE;

		double sumX = 0.0D;
		double sumZ = 0.0D;

		for (long chunkKey : chunkKeys)
		{
			sumX += ZoneRepository.unpackChunkX(chunkKey) + 0.5D;
			sumZ += ZoneRepository.unpackChunkZ(chunkKey) + 0.5D;
		}

		double centerX = sumX / chunkKeys.size();
		double centerZ = sumZ / chunkKeys.size();

		long bestKey = Long.MIN_VALUE;
		double bestDistanceSq = Double.MAX_VALUE;

		for (long chunkKey : chunkKeys)
		{
			double dx = ZoneRepository.unpackChunkX(chunkKey) + 0.5D - centerX;
			double dz = ZoneRepository.unpackChunkZ(chunkKey) + 0.5D - centerZ;
			double distanceSq = dx * dx + dz * dz;

			if (distanceSq < bestDistanceSq)
			{
				bestDistanceSq = distanceSq;
				bestKey = chunkKey;
			}
		}

		return bestKey;
	}

	private static void renderSelectionPreview(GuiGraphics drawContext, XaeroMapReflection.MapState state, ScreenSession session)
	{
		if (!session.selectionActive || session.selectionChunks.isEmpty()) return;
		if (!state.worldId().equals(session.selectionWorldId) || !state.dimensionId().equals(session.selectionDimensionId)) return;

		int fillColor = session.selectionEraseMode ? PREVIEW_ERASE_FILL : PREVIEW_PAINT_FILL;
		int borderColor = session.selectionEraseMode ? PREVIEW_ERASE_BORDER : PREVIEW_PAINT_BORDER;
		int borderWidth = Math.max(1, (int) Math.round(state.scale() * 0.04D / state.scaleFactor()));

		for (long chunkKey : session.selectionChunks)
		{
			int chunkX = ZoneRepository.unpackChunkX(chunkKey);
			int chunkZ = ZoneRepository.unpackChunkZ(chunkKey);

			int x1 = worldToScreenX(state, chunkX * 16.0D);
			int y1 = worldToScreenY(state, chunkZ * 16.0D);
			int x2 = worldToScreenX(state, (chunkX + 1) * 16.0D);
			int y2 = worldToScreenY(state, (chunkZ + 1) * 16.0D);

			if (x1 >= state.scaledWidth() || y1 >= state.scaledHeight() || x2 <= 0 || y2 <= 0) continue;

			drawContext.fill(x1, y1, x2, y2, fillColor);

			long left = ZoneRepository.packChunk(chunkX - 1, chunkZ);
			long right = ZoneRepository.packChunk(chunkX + 1, chunkZ);
			long top = ZoneRepository.packChunk(chunkX, chunkZ - 1);
			long bottom = ZoneRepository.packChunk(chunkX, chunkZ + 1);

			if (!session.selectionChunks.contains(left))
				drawContext.fill(x1, y1, x1 + borderWidth, y2, borderColor);

			if (!session.selectionChunks.contains(right))
				drawContext.fill(x2 - borderWidth, y1, x2, y2, borderColor);

			if (!session.selectionChunks.contains(top))
				drawContext.fill(x1, y1, x2, y1 + borderWidth, borderColor);

			if (!session.selectionChunks.contains(bottom))
				drawContext.fill(x1, y2 - borderWidth, x2, y2, borderColor);
		}
	}

	private static void renderStatus(GuiGraphics drawContext, XaeroMapReflection.MapState state, ScreenSession session, ZoneDimension dimension, ZoneArea activeZone)
	{
		Minecraft client = Minecraft.getInstance();
		Font textRenderer = client.font;

		int centerX = state.scaledWidth() / 2;
		int bottomY = state.scaledHeight();

		if (dimension != null)
		{
			ZoneArea hoveredZone = dimension.getZoneAtChunk(state.cursorChunkX(), state.cursorChunkZ());

			if (hoveredZone != null)
			{
				String hoveredZoneText = hoveredZone.getName() + " (" + hoveredZone.getChunkCount() + " " + Component.translatable("borders_on_xaero_map.chunks").getString() + ")";
				int hoveredZoneTextWidth = textRenderer.width(hoveredZoneText);

				int localX = centerX - hoveredZoneTextWidth / 2;

				drawContext.fill(localX - 2, STATUS_TOP_PADDING + 10, localX + hoveredZoneTextWidth + 2, STATUS_TOP_PADDING - 2, 1996488704);
				drawContext.drawString(textRenderer, hoveredZoneText, localX, STATUS_TOP_PADDING, hoveredZone.getBorderColor(), true);
			}
		}

		String selectionText = session.selectionActive
			? session.selectionChunks.size() > ZoneRepository.MAX_SELECTION_CHUNKS
				? Component.translatable("borders_on_xaero_map.map_gui.selection_limit_reached", session.selectionChunks.size(), ZoneRepository.MAX_SELECTION_CHUNKS).getString()
				: Component.translatable("borders_on_xaero_map.map_gui.selection_chunks", session.selectionChunks.size(), ZoneRepository.MAX_SELECTION_CHUNKS).getString()
			: "";

		String activeZoneText = activeZone != null
			? Component.translatable("borders_on_xaero_map.map_gui.active_zone", activeZone.getName(), activeZone.getChunkCount()).getString()
			: Component.translatable("borders_on_xaero_map.map_gui.active_zone_none").getString();

		if (!selectionText.isEmpty())
		{
			int selectionTextWidth = textRenderer.width(selectionText);
			int localX = centerX - selectionTextWidth / 2;
			int textColor = session.selectionEraseMode ? PREVIEW_ERASE_BORDER : PREVIEW_PAINT_BORDER;

			drawContext.fill(localX - 2, bottomY - STATUS_BOTTOM_PADDING - 12, localX + selectionTextWidth + 2, bottomY - STATUS_BOTTOM_PADDING - 24, 1996488704);
			drawContext.drawString(textRenderer, selectionText, localX, bottomY - STATUS_BOTTOM_PADDING - 22, textColor, true);
		}

		if (!activeZoneText.isEmpty())
		{
			int activeZoneTextWidth = textRenderer.width(activeZoneText);
			int localX = centerX - activeZoneTextWidth / 2;

			drawContext.fill(localX - 2, bottomY - STATUS_BOTTOM_PADDING, localX + activeZoneTextWidth + 2, bottomY - STATUS_BOTTOM_PADDING - 12, 1996488704);
			drawContext.drawString(textRenderer, activeZoneText, localX, bottomY - STATUS_BOTTOM_PADDING - 10, 16777215, true);
		}
	}

	private static void updateToolbarState(ScreenSession session, XaeroMapReflection.MapState state, ZoneDimension dimension, ZoneArea activeZone)
	{
		if (session.zonesVisible)
		{
			session.visibilityToggleButton.setIcon(ICON_VISIBILITY_ON_U, ICON_VISIBILITY_ON_V);
			session.visibilityToggleButton.setTooltipText(Component.translatable("borders_on_xaero_map.toolbar_button.zones_visibility_on"));
		}
		else
		{
			session.visibilityToggleButton.setIcon(ICON_VISIBILITY_OFF_U, ICON_VISIBILITY_OFF_V);
			session.visibilityToggleButton.setTooltipText(Component.translatable("borders_on_xaero_map.toolbar_button.zones_visibility_off"));
		}

		if (session.drawToggleButton == null) return;

		if (session.drawModeEnabled)
		{
			session.drawToggleButton.setIcon(ICON_DRAW_ON_U, ICON_DRAW_ON_V);
			session.drawToggleButton.setTooltipText(Component.translatable("borders_on_xaero_map.toolbar_button.draw_mode_on"));
		}
		else
		{
			session.drawToggleButton.setIcon(ICON_DRAW_OFF_U, ICON_DRAW_OFF_V);
			session.drawToggleButton.setTooltipText(Component.translatable("borders_on_xaero_map.toolbar_button.draw_mode_off"));
		}

		ZoneArea focused = null;

		if (activeZone != null) focused = activeZone;
		else if (state != null && dimension != null)
			focused = dimension.getZoneAtChunk(state.cursorChunkX(), state.cursorChunkZ());

		boolean hasFocusedZone = focused != null;

		if (session.editZoneButton != null)
			session.editZoneButton.active = hasFocusedZone;

		if (session.deleteZoneButton != null)
			session.deleteZoneButton.active = hasFocusedZone;
	}

	private static ZoneArea resolveActiveZone(ScreenSession session, XaeroMapReflection.MapState state, ZoneDimension dimension)
	{
		if (state == null || dimension == null) return null;

		return resolveActiveZone(session, state.worldId(), state.dimensionId(), dimension);
	}

	private static ZoneArea resolveActiveZone(ScreenSession session, String worldId, String dimensionId, ZoneDimension dimension)
	{
		if (session.activeZoneId == null) return null;

		if (!worldId.equals(session.activeWorldId) || !dimensionId.equals(session.activeDimensionId))
		{
			clearActiveZone(session);
			return null;
		}

		ZoneArea active = dimension.getZoneById(session.activeZoneId);

		if (active == null)
			clearActiveZone(session);

		return active;
	}

	private static void setActiveZone(ScreenSession session, XaeroMapReflection.MapState state, String zoneId)
	{
		setActiveZone(session, state.worldId(), state.dimensionId(), zoneId);
	}

	private static void setActiveZone(ScreenSession session, String worldId, String dimensionId, String zoneId)
	{
		session.activeWorldId = worldId;
		session.activeDimensionId = dimensionId;
		session.activeZoneId = zoneId;
	}

	private static void clearActiveZone(ScreenSession session)
	{
		session.activeWorldId = null;
		session.activeDimensionId = null;
		session.activeZoneId = null;
	}

	private static boolean isInsideToolbar(ScreenSession session, double mouseX, double mouseY)
	{
		return mouseX >= session.toolbarLeft && mouseX <= session.toolbarRight && mouseY >= session.toolbarTop && mouseY <= session.toolbarBottom;
	}

	private static <T extends AbstractWidget> T addToolbarButton(ScreenEvent.Init.Post event, T button)
	{
		event.addListener(button);
		return button;
	}

	private static boolean isShiftDown()
	{
		Minecraft client = Minecraft.getInstance();
		long window = client.getWindow().getWindow();
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	private static int brighten(int color)
	{
		int alpha = color >>> 24 & 0xFF;
		int red = color >>> 16 & 0xFF;
		int green = color >>> 8 & 0xFF;
		int blue = color & 0xFF;

		red = Math.min(255, red + 28);
		green = Math.min(255, green + 28);
		blue = Math.min(255, blue + 28);

		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static int softenFill(int color)
	{
		int alpha = color >>> 24 & 0xFF;
		if (alpha == 0) alpha = 255;
		int clamped = Math.min(alpha, 80);
		return clamped << 24 | color & 0xFFFFFF;
	}

	private static int screenToChunkX(XaeroMapReflection.MapState state, double screenX)
	{
		double rawX = state.guiToRawX(screenX);
		double worldX = (rawX - state.framebufferWidth() / 2.0D) / state.scale() + state.cameraX();
		return (int) Math.floor(worldX / 16.0D);
	}

	private static int screenToChunkZ(XaeroMapReflection.MapState state, double screenY)
	{
		double rawY = state.guiToRawY(screenY);
		double worldZ = (rawY - state.framebufferHeight() / 2.0D) / state.scale() + state.cameraZ();
		return (int) Math.floor(worldZ / 16.0D);
	}

	private static int worldToScreenX(XaeroMapReflection.MapState state, double worldX)
	{
		double rawX = (worldX - state.cameraX()) * state.scale() + state.framebufferWidth() / 2.0D;
		return state.rawToGuiX(rawX);
	}

	private static int worldToScreenY(XaeroMapReflection.MapState state, double worldZ)
	{
		double rawY = (worldZ - state.cameraZ()) * state.scale() + state.framebufferHeight() / 2.0D;
		return state.rawToGuiY(rawY);
	}

	private static void notifyActionbar(Component message)
	{
		Minecraft client = Minecraft.getInstance();

		if (client.player != null)
			client.player.displayClientMessage(message, true);
	}

	private static ScreenSession session(Screen screen) { return SESSIONS.computeIfAbsent(screen, ignored -> new ScreenSession()); }

	private static final class XaeroIconButton extends Button
	{
		private int textureX;
		private int textureY;

		private XaeroIconButton(int x, int y, int textureX, int textureY, Component tooltipText, OnPress onPress)
		{
			super(x, y, BUTTON_SIZE, BUTTON_SIZE, Component.empty(), onPress, DEFAULT_NARRATION);
			this.textureX = textureX;
			this.textureY = textureY;
			setTooltipText(tooltipText);
		}

		private void setIcon(int textureX, int textureY)
		{
			this.textureX = textureX;
			this.textureY = textureY;
		}

		private void setTooltipText(Component tooltipText) { setTooltip(Tooltip.create(tooltipText)); }

		@Override
		protected void renderWidget(@Nonnull GuiGraphics context, int mouseX, int mouseY, float deltaTicks)
		{
			int iconX = getX() + getWidth() / 2 - ICON_SIZE / 2;
			int iconY = getY() + getHeight() / 2 - ICON_SIZE / 2;
			int color = -12566464;

			if (active)
			{
				if (isHoveredOrFocused())
				{
					iconY--;
					color = -1644826;
				}
				else color = -197380;
			}

			float alpha = (color >>> 24) / 255.0F;
			float red = ((color >>> 16) & 0xFF) / 255.0F;
			float green = ((color >>> 8) & 0xFF) / 255.0F;
			float blue = (color & 0xFF) / 255.0F;

			RenderSystem.setShaderColor(red, green, blue, alpha);
			context.blit(GUI_TEXTURE, iconX, iconY, textureX, textureY, ICON_SIZE, ICON_SIZE, GUI_TEXTURE_SIZE, GUI_TEXTURE_SIZE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private static final class ScreenSession
	{
		private boolean zonesVisible = true;
		private boolean drawModeEnabled;
		private String activeWorldId;
		private String activeDimensionId;
		private String activeZoneId;
		private boolean selectionActive;
		private boolean selectionEraseMode;
		private String selectionWorldId;
		private String selectionDimensionId;
		private long lastClickTime;
		private String lastClickZoneId;
		private String lastClickWorldId;
		private String lastClickDimensionId;
		private final Set<Long> selectionChunks = new HashSet<>();
		private boolean lastSelectionChunkSet;
		private int lastSelectionChunkX;
		private int lastSelectionChunkZ;
		private int toolbarLeft;
		private int toolbarTop;
		private int toolbarRight;
		private int toolbarBottom;
		private boolean toolbarAttached;

		private XaeroIconButton visibilityToggleButton;
		private XaeroIconButton drawToggleButton;
		private XaeroIconButton editZoneButton;
		private XaeroIconButton deleteZoneButton;
		private XaeroIconButton helpButton;

		private void stopSelection()
		{
			selectionActive = false;
			selectionEraseMode = false;
			selectionWorldId = null;
			selectionDimensionId = null;
			selectionChunks.clear();
			lastSelectionChunkSet = false;
		}
	}
}