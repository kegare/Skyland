package skyland.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.block.SkyBlocks;
import skyland.client.audio.MovingSoundSkyJump;
import skyland.core.Skyland;

public class ItemSkyFeather extends Item
{
	public ItemSkyFeather()
	{
		this.setUnlocalizedName("skyFeather");
		this.setCreativeTab(Skyland.TAB_SKYLAND);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return Item.getItemFromBlock(SkyBlocks.SKY_PORTAL).onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player, EnumHand hand)
	{
		if (world.provider.getDimension() == 0)
		{
			if (!world.canSeeSky(new BlockPos(player.posX, player.getEntityBoundingBox().minY, player.posZ)))
			{
				return ActionResult.newResult(EnumActionResult.FAIL, itemstack);
			}

			if (!player.capabilities.isCreativeMode)
			{
				--itemstack.stackSize;
			}

			if (!world.isRemote)
			{
				player.getEntityData().setBoolean("Skyland:SkyJump", true);
			}
			else
			{
				playSkyJumpSound();
			}

			player.addVelocity(0.0D, 15.0D, 0.0D);

			return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
		}

		return super.onItemRightClick(itemstack, world, player, hand);
	}

	@SideOnly(Side.CLIENT)
	public void playSkyJumpSound()
	{
		FMLClientHandler.instance().getClient().getSoundHandler().playSound(new MovingSoundSkyJump());
	}
}