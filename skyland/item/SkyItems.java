package skyland.item;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import skyland.core.Skyland;

public class SkyItems
{
	public static final ItemSkyFeather SKY_FEATHER = new ItemSkyFeather();
	public static final Item SKYRITE = new Item().setUnlocalizedName("skyrite").setCreativeTab(Skyland.TAB_SKYLAND);

	public static final ToolMaterial TOOL_SKYRITE = EnumHelper.addToolMaterial("SKYRITE", 3, 3000, 6.0F, 2.0F, 12).setRepairItem(new ItemStack(SKYRITE));

	public static final ItemSword SKYRITE_SWORD = (ItemSword)new ItemSword(TOOL_SKYRITE).setUnlocalizedName("swordSkyrite").setCreativeTab(Skyland.TAB_SKYLAND);
	public static final ItemSpade SKYRITE_SHOVEL = (ItemSpade)new ItemSpade(TOOL_SKYRITE).setUnlocalizedName("shovelSkyrite").setCreativeTab(Skyland.TAB_SKYLAND);
	public static final ItemPickaxe SKYRITE_PICKAXE = (ItemPickaxe)new ItemPickaxeSkyland(TOOL_SKYRITE).setUnlocalizedName("pickaxeSkyrite").setCreativeTab(Skyland.TAB_SKYLAND);
	public static final ItemAxe SKYRITE_AXE = (ItemAxe)new ItemAxeSkyland(TOOL_SKYRITE, 6.0F, -3.0F).setUnlocalizedName("axeSkyrite").setCreativeTab(Skyland.TAB_SKYLAND);
	public static final ItemHoe SKYRITE_HOE = (ItemHoe)new ItemHoe(TOOL_SKYRITE).setUnlocalizedName("hoeSkyrite").setCreativeTab(Skyland.TAB_SKYLAND);

	public static void registerItems(IForgeRegistry<Item> registry)
	{
		registry.register(SKY_FEATHER.setRegistryName("sky_feather"));
		registry.register(SKYRITE.setRegistryName("skyrite"));
		registry.register(SKYRITE_SWORD.setRegistryName("skyrite_sword"));
		registry.register(SKYRITE_SHOVEL.setRegistryName("skyrite_shovel"));
		registry.register(SKYRITE_PICKAXE.setRegistryName("skyrite_pickaxe"));
		registry.register(SKYRITE_AXE.setRegistryName("skyrite_axe"));
		registry.register(SKYRITE_HOE.setRegistryName("skyrite_hoe"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		registerModel(SKY_FEATHER, "sky_feather");
		registerModel(SKYRITE, "skyrite");
		registerModel(SKYRITE_SWORD, "skyrite_sword");
		registerModel(SKYRITE_SHOVEL, "skyrite_shovel");
		registerModel(SKYRITE_PICKAXE, "skyrite_pickaxe");
		registerModel(SKYRITE_AXE, "skyrite_axe");
		registerModel(SKYRITE_HOE, "skyrite_hoe");
	}

	@SideOnly(Side.CLIENT)
	public static void registerModel(Item item, String modelName)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Skyland.MODID + ":" + modelName, "inventory"));
	}

	@SideOnly(Side.CLIENT)
	public static void registerModelWithMeta(Item item, String... modelName)
	{
		List<ModelResourceLocation> models = Lists.newArrayList();

		for (String model : modelName)
		{
			models.add(new ModelResourceLocation(Skyland.MODID + ":" + model, "inventory"));
		}

		ModelBakery.registerItemVariants(item, models.toArray(new ResourceLocation[models.size()]));

		for (int i = 0; i < models.size(); ++i)
		{
			ModelLoader.setCustomModelResourceLocation(item, i, models.get(i));
		}
	}

	public static void registerOreDicts()
	{
		OreDictionary.registerOre("gemSkyrite", SKYRITE);
	}

	public static void registerRecipes()
	{
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(SKYRITE, 9), "blockSkyrite"));

		GameRegistry.addRecipe(new ShapedOreRecipe(SKYRITE_SWORD,
			"X", "X", "Y",
			'X', SKYRITE,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(SKYRITE_SHOVEL,
			"X", "Y", "Y",
			'X', SKYRITE,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(SKYRITE_PICKAXE,
			"XXX", " Y ", " Y ",
			'X', SKYRITE,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(SKYRITE_AXE,
			"XX", "XY", " Y",
			'X', SKYRITE,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(SKYRITE_HOE,
			"XX", " Y", " Y",
			'X', SKYRITE,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(Items.ARROW,
			"X", "#", "Y",
			'X', Items.FLINT,
			'#', "stickWood",
			'Y', SKY_FEATHER
		));
	}
}