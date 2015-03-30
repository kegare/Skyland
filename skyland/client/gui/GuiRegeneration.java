/*
 * Skyland
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package skyland.client.gui;

import java.awt.Desktop;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.HoverChecker;

import org.lwjgl.input.Keyboard;

import skyland.core.Skyland;
import skyland.network.RegenerateMessage;
import skyland.world.WorldProviderSkyland;

public class GuiRegeneration extends GuiScreen
{
	private boolean backup;

	protected GuiButton regenButton;
	protected GuiButton openButton;
	protected GuiButton cancelButton;
	protected GuiCheckBox backupCheckBox;

	private HoverChecker backupHoverChecker;

	public GuiRegeneration() {}

	public GuiRegeneration(boolean backup)
	{
		this.backup = backup;
	}

	@Override
	public void initGui()
	{
		if (regenButton == null)
		{
			regenButton = new GuiButtonExt(0, 0, 0, I18n.format("skyland.regenerate.gui.regenerate"));
		}

		regenButton.xPosition = width / 2 - 100;
		regenButton.yPosition = height / 4 + regenButton.height + 65;

		if (openButton == null)
		{
			openButton = new GuiButtonExt(1, 0, 0, I18n.format("skyland.regenerate.gui.backup.open"));
			openButton.visible = false;
		}

		openButton.xPosition = regenButton.xPosition;
		openButton.yPosition = regenButton.yPosition;

		if (cancelButton == null)
		{
			cancelButton = new GuiButtonExt(2, 0, 0, I18n.format("gui.cancel"));
		}

		cancelButton.xPosition = regenButton.xPosition;
		cancelButton.yPosition = regenButton.yPosition + regenButton.height + 5;

		if (backupCheckBox == null)
		{
			backupCheckBox = new GuiCheckBox(3, 10, 0, I18n.format("skyland.regenerate.gui.backup"), backup);
		}

		backupCheckBox.yPosition = height - 20;

		buttonList.clear();
		buttonList.add(regenButton);
		buttonList.add(openButton);
		buttonList.add(cancelButton);
		buttonList.add(backupCheckBox);

		if (backupHoverChecker == null)
		{
			backupHoverChecker = new HoverChecker(backupCheckBox, 800);
		}
	}

	@Override
	public void handleKeyboardInput() throws IOException
	{
		super.handleKeyboardInput();

		if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
		{
			openButton.visible = Keyboard.getEventKeyState();
			regenButton.visible = !openButton.visible;
		}
	}

	@Override
	protected void keyTyped(char c, int code)
	{
		if (code == Keyboard.KEY_ESCAPE)
		{
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button.enabled)
		{
			switch (button.id)
			{
				case 0:
					Skyland.network.sendToServer(new RegenerateMessage(backupCheckBox.isChecked()));

					regenButton.enabled = false;
					cancelButton.visible = false;
					break;
				case 1:
					try
					{
						Desktop.getDesktop().open(WorldProviderSkyland.getDimDir().getParentFile());
					}
					catch (Exception e) {}

					break;
				case 2:
					mc.displayGuiScreen(null);
					mc.setIngameFocus();
					break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks)
	{
		drawGradientRect(0, 0, width, height, 0, Integer.MAX_VALUE);

		GlStateManager.pushMatrix();
		GlStateManager.scale(1.5F, 1.5F, 1.0F);
		drawCenteredString(fontRendererObj, I18n.format("skyland.regenerate.gui.title"), width / 3, 30, 0xFFFFFF);
		GlStateManager.popMatrix();

		drawCenteredString(fontRendererObj, I18n.format("skyland.regenerate.gui.info"), width / 2, 90, 0xEEEEEE);

		super.drawScreen(mouseX, mouseY, ticks);

		if (backupHoverChecker.checkHover(mouseX, mouseY))
		{
			drawHoveringText(fontRendererObj.listFormattedStringToWidth(I18n.format("skyland.regenerate.gui.backup.tooltip"), 300), mouseX, mouseY);
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public void updateProgress(int task)
	{
		regenButton.enabled = false;
		cancelButton.visible = false;

		if (task < 0)
		{
			regenButton.visible = false;
			cancelButton.visible = true;
		}
		else switch (task)
		{
			case 0:
				regenButton.displayString = I18n.format("skyland.regenerate.gui.progress.regenerating");
				break;
			case 1:
				regenButton.displayString = I18n.format("skyland.regenerate.gui.progress.backingup");
				break;
			case 2:
				regenButton.displayString = I18n.format("skyland.regenerate.gui.progress.regenerated");
				cancelButton.displayString = I18n.format("gui.done");
				cancelButton.visible = true;
				break;
			case 3:
				regenButton.displayString = I18n.format("skyland.regenerate.gui.progress.failed");
				cancelButton.visible = true;
				break;
		}
	}
}