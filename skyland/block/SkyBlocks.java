package skyland.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import skyland.item.ItemSkyPortal;
import skyland.item.SkyItems;

public class SkyBlocks
{
	public static final BlockSkyPortal SKY_PORTAL = new BlockSkyPortal();
	public static final BlockSkyriteOre SKYRITE_ORE = new BlockSkyriteOre();
	public static final Block SKYRITE_BLOCK = new BlockSkyrite();

	public static void registerBlocks(IForgeRegistry<Block> registry)
	{
		registry.register(SKY_PORTAL.setRegistryName("sky_portal"));
		registry.register(SKYRITE_ORE.setRegistryName("skyrite_ore"));
		registry.register(SKYRITE_BLOCK.setRegistryName("skyrite_block"));
	}

	public static void registerItemBlocks(IForgeRegistry<Item> registry)
	{
		registry.register(new ItemSkyPortal(SKY_PORTAL).setRegistryName(SKY_PORTAL.getRegistryName()));
		registry.register(new ItemBlock(SKYRITE_ORE).setRegistryName(SKYRITE_ORE.getRegistryName()));
		registry.register(new ItemBlock(SKYRITE_BLOCK).setRegistryName(SKYRITE_BLOCK.getRegistryName()));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		registerModel(SKY_PORTAL, "sky_portal");
		registerModel(SKYRITE_ORE, "skyrite_ore");
		registerModel(SKYRITE_BLOCK, "skyrite_block");
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

	public static void registerOreDicts()
	{
		OreDictionary.registerOre("oreSkyrite", SKYRITE_ORE);
		OreDictionary.registerOre("blockSkyrite", SKYRITE_BLOCK);
	}

	public static void registerRecipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(SkyBlocks.SKYRITE_BLOCK,
			"XXX", "XXX", "XXX",
			'X', "gemSkyrite"
		));

		GameRegistry.addSmelting(SkyBlocks.SKYRITE_ORE, new ItemStack(SkyItems.SKYRITE), 1.0F);
	}
}