package skyland.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;
import skyland.item.ItemSkyPortal;
import skyland.item.SkyItems;
import skyland.util.SkyUtils;

public class SkyBlocks
{
	public static final BlockSkyPortal sky_portal = new BlockSkyPortal();
	public static final BlockSkyriteOre skyrite_ore = new BlockSkyriteOre();
	public static final Block skyrite_block = new BlockSkyrite();

	public static void registerBlocks()
	{
		sky_portal.setRegistryName("sky_portal");
		skyrite_ore.setRegistryName("skyrite_ore");
		skyrite_block.setRegistryName("skyrite_block");

		GameRegistry.register(sky_portal);
		GameRegistry.register(new ItemSkyPortal(sky_portal), sky_portal.getRegistryName());

		GameRegistry.register(skyrite_ore);
		GameRegistry.register(new ItemBlock(skyrite_ore), skyrite_ore.getRegistryName());

		GameRegistry.register(skyrite_block);
		GameRegistry.register(new ItemBlock(skyrite_block), skyrite_block.getRegistryName());

		SkyUtils.registerOreDict(skyrite_ore, "oreSkyrite");
		SkyUtils.registerOreDict(skyrite_block, "blockSkyrite");
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		registerModel(sky_portal, "sky_portal");
		registerModel(skyrite_ore, "skyrite_ore");
		registerModel(skyrite_block, "skyrite_block");
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Block block, String modelName)
	{
		SkyItems.registerModel(Item.getItemFromBlock(block), modelName);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModelWithMeta(Block block, String... modelName)
	{
		SkyItems.registerModelWithMeta(Item.getItemFromBlock(block), modelName);
	}

	public static void registerRecipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(SkyBlocks.skyrite_block,
			"XXX", "XXX", "XXX",
			'X', "skyrite"
		));

		GameRegistry.addSmelting(SkyBlocks.skyrite_ore, new ItemStack(SkyItems.skyrite), 1.0F);
	}
}