package skyland.client.handler;

import com.google.common.base.Strings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.core.Config;
import skyland.core.SkySounds;
import skyland.core.Skyland;
import skyland.util.SkyUtils;
import skyland.util.Version;

@SideOnly(Side.CLIENT)
public class ClientEventHooks
{
	public static boolean fallCancelable;

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event)
	{
		if (event.getModID().equals(Skyland.MODID))
		{
			Config.syncConfig();
		}
	}

	@SubscribeEvent
	public void onClientConnected(ClientConnectedToServerEvent event)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (Config.versionNotify && Version.isOutdated())
		{
			ITextComponent name = new TextComponentString(Skyland.metadata.name);
			name.getStyle().setColor(TextFormatting.AQUA);
			ITextComponent latest = new TextComponentString(Version.getLatest().toString());
			latest.getStyle().setColor(TextFormatting.YELLOW);

			ITextComponent message;

			message = new TextComponentTranslation("skyland.version.message", name);
			message.appendText(" : ").appendSibling(latest);
			message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, Skyland.metadata.url));

			mc.ingameGUI.getChatGUI().printChatMessage(message);
			message = null;

			if (Version.isBeta())
			{
				message = new TextComponentTranslation("skyland.version.message.beta", name);
			}
			else if (Version.isAlpha())
			{
				message = new TextComponentTranslation("skyland.version.message.alpha", name);
			}

			if (message != null)
			{
				mc.ingameGUI.getChatGUI().printChatMessage(message);
			}
		}
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if (event.phase != Phase.END || Skyland.SKYLAND == null)
		{
			return;
		}

		World world = FMLClientHandler.instance().getWorldClient();

		if (world == null)
		{
			return;
		}

		if (world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldInfo().getTerrainType() == Skyland.SKYLAND)
		{
			world.prevRainingStrength = 0.0F;
			world.rainingStrength = 0.0F;
			world.prevThunderingStrength = 0.0F;
			world.thunderingStrength = 0.0F;
		}
	}

	@SubscribeEvent
	public void onRenderGameTextOverlay(RenderGameOverlayEvent.Text event)
	{
		if (Skyland.DIM_SKYLAND == null)
		{
			return;
		}

		Minecraft mc = FMLClientHandler.instance().getClient();

		if (SkyUtils.isEntityInSkyland(mc.player))
		{
			if (mc.gameSettings.showDebugInfo)
			{
				event.getLeft().add("Dim: " + SkyUtils.getDimensionName(Skyland.DIM_SKYLAND));
			}
		}
	}

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event)
	{
		GuiScreen gui = event.getGui();

		if (gui != null && GuiModList.class == gui.getClass())
		{
			String desc = I18n.format("skyland.description");

			if (!Strings.isNullOrEmpty(desc))
			{
				Skyland.metadata.description = desc;
			}
		}
	}

	@SubscribeEvent
	public void onPlaySound(PlaySoundEvent event)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();
		ISound sound = event.getSound();

		if (sound.getCategory() == SoundCategory.MUSIC && SkyUtils.isEntityInSkyland(mc.player))
		{
			ISound newMusic = null;

			if (mc.world.isDaytime() && Math.random() < 0.45D)
			{
				newMusic = PositionedSoundRecord.getMusicRecord(SkySounds.SKYLAND);
			}

			if (newMusic != null)
			{
				event.setResultSound(newMusic);
			}
		}
	}

	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (mc.player == null)
		{
			return;
		}

		if (fallCancelable && event.getEntity() == mc.player)
		{
			fallCancelable = false;

			event.setCanceled(true);
		}
	}
}