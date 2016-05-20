package skyland.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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
		this.setCreativeTab(Skyland.tabSkyland);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		BlockPos pos1 = pos.offset(side);

		if (SkyBlocks.sky_portal.trySpawnPortal(world, pos1))
		{
			world.playSound(null, pos1.getX() + 0.5D, pos1.getY() + 0.5D, pos1.getZ() + 0.5D, SkyBlocks.sky_portal.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 2.0F);

			if (!player.capabilities.isCreativeMode && --stack.stackSize <= 0)
			{
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}

			return EnumActionResult.SUCCESS;
		}

		return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemstack, World world, EntityPlayer player, EnumHand hand)
	{
		if (world.provider.getDimension() == 0)
		{
			BlockPos pos = player.getPosition();

			while (pos.getY() < world.getHeight() && world.isAirBlock(pos.up()))
			{
				pos = pos.up();
			}

			if (!world.isAirBlock(pos))
			{
				return ActionResult.newResult(EnumActionResult.FAIL, itemstack);
			}

			if (!world.isRemote)
			{
				if (!player.capabilities.isCreativeMode && --itemstack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				player.getEntityData().setBoolean("Skyland:SkyJump", true);
			}
			else
			{
				playSkyJumpSound();
			}

			player.addVelocity(0.0D, 15.0D, 0.0D);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, itemstack);
	}

	@SideOnly(Side.CLIENT)
	public void playSkyJumpSound()
	{
		FMLClientHandler.instance().getClient().getSoundHandler().playSound(new MovingSoundSkyJump());
	}
}