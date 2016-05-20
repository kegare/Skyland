package skyland.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import skyland.core.Skyland;

public class BlockSkyrite extends Block
{
	public BlockSkyrite()
	{
		super(Material.IRON);
		this.setUnlocalizedName("blockSkyrite");
		this.setHardness(5.5F);
		this.setResistance(10.0F);
		this.setSoundType(SoundType.METAL);
		this.setCreativeTab(Skyland.tabSkyland);
	}
}