package skyland.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.block.SkyBlocks;
import skyland.client.audio.MovingSoundSkyJump;
import skyland.client.handler.ClientEventHooks;
import skyland.core.Skyland;
import skyland.handler.SkyEventHooks;
import skyland.util.SkyUtils;

public class ItemSkyFeather extends Item
{
	public ItemSkyFeather()
	{
		this.setUnlocalizedName("skyFeather");
		this.setCreativeTab(Skyland.TAB_SKYLAND);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return Item.getItemFromBlock(SkyBlocks.SKY_PORTAL).onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		ActionResult<ItemStack> result;

		if (SkyUtils.isSkyland(world))
		{
			result = onItemUseInSkyland(world, player, hand, stack);
		}
		else if (Skyland.DIM_SKYLAND != null && world.provider.getDimensionType() == DimensionType.OVERWORLD)
		{
			result = onItemUseInOverworld(world, player, hand, stack);
		}
		else
		{
			result = ActionResult.newResult(EnumActionResult.FAIL, stack);
		}

		if (result.getType() == EnumActionResult.SUCCESS && !player.capabilities.isCreativeMode)
		{
			stack.shrink(1);
		}

		return result;
	}

	public ActionResult<ItemStack> onItemUseInSkyland(World world, EntityPlayer player, EnumHand hand, ItemStack stack)
	{
		player.motionY = 2.75D;

		if (!player.onGround)
		{
			float f = player.rotationYaw * 0.017453292F;

			player.motionX -= MathHelper.sin(f);
			player.motionZ += MathHelper.cos(f);
		}

		player.isAirBorne = true;

		if (world.isRemote)
		{
			playSkyJump(player);
		}
		else if (!player.capabilities.isCreativeMode)
		{
			SkyEventHooks.FALL_CANCELABLE_PLAYERS.add(player.getCachedUniqueIdString());
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	public ActionResult<ItemStack> onItemUseInOverworld(World world, EntityPlayer player, EnumHand hand, ItemStack stack)
	{
		if (!world.canSeeSky(player.getPosition()))
		{
			return ActionResult.newResult(EnumActionResult.FAIL, stack);
		}

		player.motionY = 15.0D;
		player.isAirBorne = true;

		if (world.isRemote)
		{
			playSkyJump(player);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@SideOnly(Side.CLIENT)
	protected void playSkyJump(EntityPlayer player)
	{
		if (!player.capabilities.isCreativeMode)
		{
			ClientEventHooks.fallCancelable = true;
		}

		if (MovingSoundSkyJump.prevSound == null || MovingSoundSkyJump.prevSound.isDonePlaying())
		{
			MovingSoundSkyJump sound = new MovingSoundSkyJump();

			FMLClientHandler.instance().getClient().getSoundHandler().playSound(sound);

			MovingSoundSkyJump.prevSound = sound;
		}
	}
}