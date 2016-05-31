package skyland.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import skyland.util.SkyUtils;

public class TeleporterDummy extends Teleporter
{
	public TeleporterDummy(WorldServer world)
	{
		super(world);
	}

	@Override
	public void placeInPortal(Entity entity, float rotationYaw)
	{
		if (entity instanceof EntityPlayerMP)
		{
			SkyUtils.setDimensionChange((EntityPlayerMP)entity);
		}
	}

	@Override
	public boolean placeInExistingPortal(Entity entity, float rotationYaw)
	{
		return true;
	}

	@Override
	public boolean makePortal(Entity entity)
	{
		return true;
	}

	@Override
	public void removeStalePortalLocations(long time) {}
}