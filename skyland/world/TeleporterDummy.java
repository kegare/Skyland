package skyland.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
			EntityPlayerMP player = (EntityPlayerMP)entity;

			if (!player.capabilities.isCreativeMode)
			{
				ObfuscationReflectionHelper.setPrivateValue(EntityPlayerMP.class, player, true, "invulnerableDimensionChange", "field_184851_cj");
			}
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