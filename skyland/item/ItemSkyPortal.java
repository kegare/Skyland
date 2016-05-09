package skyland.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import skyland.block.SkyBlocks;

public class ItemSkyPortal extends ItemBlock
{
	public ItemSkyPortal(Block block)
	{
		super(block);
		this.setMaxStackSize(1);
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if (!world.isRemote)
		{
			BlockPos pos1 = pos.offset(side);

			if (SkyBlocks.sky_portal.func_176548_d(world, pos1))
			{
				world.playSound(null, pos1.getX() + 0.5D, pos1.getY() + 0.5D, pos1.getZ() + 0.5D, SkyBlocks.sky_portal.getStepSound().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 2.0F);

				if (!player.capabilities.isCreativeMode && --stack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				return EnumActionResult.SUCCESS;
			}
		}

		return EnumActionResult.PASS;
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		return EnumActionResult.PASS;
	}
}