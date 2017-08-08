package skyland.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.HoverChecker;
import skyland.network.SkyNetworkRegistry;
import skyland.network.client.RegenerationGuiMessage.EnumType;
import skyland.network.server.RegenerationMessage;

public class GuiRegeneration extends GuiScreen
{
	private static boolean backup = true;

	protected GuiButton regenButton;
	protected GuiButton cancelButton;
	protected GuiCheckBox backupCheckBox;

	private HoverChecker backupHoverChecker;

	@Override
	public void initGui()
	{
		if (regenButton == null)
		{
			regenButton = new GuiButtonExt(0, 0, 0, I18n.format("skyland.regeneration.gui.regenerate"));
		}

		regenButton.x = width / 2 - 100;
		regenButton.y = height / 4 + regenButton.height + 65;

		if (cancelButton == null)
		{
			cancelButton = new GuiButtonExt(1, 0, 0, I18n.format("gui.cancel"));
		}

		cancelButton.x = regenButton.x;
		cancelButton.y = regenButton.y + regenButton.height + 5;

		if (backupCheckBox == null)
		{
			backupCheckBox = new GuiCheckBox(2, 10, 0, I18n.format("skyland.regeneration.gui.backup"), backup);
		}

		backupCheckBox.y = height - 20;

		buttonList.clear();
		buttonList.add(regenButton);
		buttonList.add(cancelButton);
		buttonList.add(backupCheckBox);

		if (backupHoverChecker == null)
		{
			backupHoverChecker = new HoverChecker(backupCheckBox, 800);
		}
	}

	@Override
	protected void keyTyped(char c, int code) throws IOException
	{
		if (code == Keyboard.KEY_ESCAPE)
		{
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		if (button.enabled)
		{
			switch (button.id)
			{
				case 0:
					SkyNetworkRegistry.sendToServer(new RegenerationMessage(backupCheckBox.isChecked()));

					regenButton.enabled = false;
					cancelButton.visible = false;
					break;
				case 1:
					mc.displayGuiScreen(null);
					mc.setIngameFocus();
					break;
				case 2:
					backup = backupCheckBox.isChecked();
					break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks)
	{
		drawGradientRect(0, 0, width, height, 0, Integer.MAX_VALUE);

		GlStateManager.pushMatrix();
		GlStateManager.scale(2.0F, 2.0F, 2.0F);
		drawCenteredString(fontRenderer, I18n.format("skyland.regeneration.gui.title"), width / 4, 30, 0xFFFFFF);
		GlStateManager.popMatrix();

		drawCenteredString(fontRenderer, I18n.format("skyland.regeneration.gui.info"), width / 2, 100, 0xEEEEEE);

		super.drawScreen(mouseX, mouseY, ticks);

		if (backupHoverChecker.checkHover(mouseX, mouseY))
		{
			drawHoveringText(fontRenderer.listFormattedStringToWidth(I18n.format("skyland.regeneration.gui.backup.tooltip"), 300), mouseX, mouseY);
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public void updateProgress(EnumType type)
	{
		regenButton.enabled = false;
		cancelButton.visible = false;

		if (type == null)
		{
			regenButton.visible = false;
			cancelButton.visible = true;
		}
		else switch (type)
		{
			case START:
				regenButton.displayString = I18n.format("skyland.regeneration.gui.progress.start");
				break;
			case BACKUP:
				regenButton.displayString = I18n.format("skyland.regeneration.gui.progress.backup");
				break;
			case SUCCESS:
				regenButton.displayString = I18n.format("skyland.regeneration.gui.progress.regenerated");
				cancelButton.displayString = I18n.format("gui.done");
				cancelButton.visible = true;
				break;
			case FAILED:
				regenButton.displayString = I18n.format("skyland.regeneration.gui.progress.failed");
				cancelButton.visible = true;
				break;
		}
	}
}