package skyland.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import skyland.core.Skyland;

public class BlockSkyrite extends Block
{
	public BlockSkyrite()
	{
		super(Material.iron);
		this.setUnlocalizedName("blockSkyrite");
		this.setHardness(5.5F);
		this.setResistance(10.0F);
		this.setStepSound(SoundType.METAL);
		this.setCreativeTab(Skyland.tabSkyland);
	}
}