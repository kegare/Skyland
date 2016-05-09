package skyland.block;

import java.util.Random;

import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import skyland.core.Skyland;
import skyland.item.SkyItems;

public class BlockSkyriteOre extends BlockOre
{
	public BlockSkyriteOre()
	{
		super();
		this.setUnlocalizedName("oreSkyrite");
		this.setHardness(3.0F);
		this.setResistance(5.0F);
		this.setStepSound(SoundType.STONE);
		this.setCreativeTab(Skyland.tabSkyland);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return SkyItems.skyrite;
	}

	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune)
	{
		Random rand = world instanceof World ? ((World)world).rand : new Random();

		return MathHelper.getRandomIntegerInRange(rand, 5, 8);
	}
}