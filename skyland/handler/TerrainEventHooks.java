package skyland.handler;

import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skyland.util.SkyUtils;

public class TerrainEventHooks
{
	@SubscribeEvent
	public void onDecorateBiome(DecorateBiomeEvent.Decorate event)
	{
		World world = event.getWorld();

		if (SkyUtils.isSkyland(world) && event.getType() == DecorateBiomeEvent.Decorate.EventType.FOSSIL)
		{
			event.setResult(Result.DENY);
		}
	}
}