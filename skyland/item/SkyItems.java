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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import skyland.core.Skyland;

public class SkyItems
{
	public static final ItemSkyFeather sky_feather = new ItemSkyFeather();
	public static final Item skyrite = new Item().setUnlocalizedName("skyrite").setCreativeTab(Skyland.tabSkyland);

	public static final ToolMaterial SKYRITE = EnumHelper.addToolMaterial("SKYRITE", 3, 3000, 6.0F, 2.0F, 12).setRepairItem(new ItemStack(skyrite));

	public static final ItemSword skyrite_sword = (ItemSword)new ItemSword(SKYRITE).setUnlocalizedName("swordSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemSpade skyrite_shovel = (ItemSpade)new ItemSpade(SKYRITE).setUnlocalizedName("shovelSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemPickaxe skyrite_pickaxe = (ItemPickaxe)new ItemPickaxeSkyland(SKYRITE).setUnlocalizedName("pickaxeSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemAxe skyrite_axe = (ItemAxe)new ItemAxeSkyland(SKYRITE, 6.0F, -3.0F).setUnlocalizedName("axeSkyrite").setCreativeTab(Skyland.tabSkyland);
	public static final ItemHoe skyrite_hoe = (ItemHoe)new ItemHoe(SKYRITE).setUnlocalizedName("hoeSkyrite").setCreativeTab(Skyland.tabSkyland);

	public static void registerItems()
	{
		sky_feather.setRegistryName("sky_feather");
		skyrite.setRegistryName("skyrite");
		skyrite_sword.setRegistryName("skyrite_sword");
		skyrite_shovel.setRegistryName("skyrite_shovel");
		skyrite_pickaxe.setRegistryName("skyrite_pickaxe");
		skyrite_axe.setRegistryName("skyrite_axe");
		skyrite_hoe.setRegistryName("skyrite_hoe");

		GameRegistry.register(sky_feather);
		GameRegistry.register(skyrite);
		GameRegistry.register(skyrite_sword);
		GameRegistry.register(skyrite_shovel);
		GameRegistry.register(skyrite_pickaxe);
		GameRegistry.register(skyrite_axe);
		GameRegistry.register(skyrite_hoe);

		OreDictionary.registerOre("gemSkyrite", skyrite);
	}

	@SideOnly(Side.CLIENT)
	public static void registerModels()
	{
		registerModel(sky_feather, "sky_feather");
		registerModel(skyrite, "skyrite");
		registerModel(skyrite_sword, "skyrite_sword");
		registerModel(skyrite_shovel, "skyrite_shovel");
		registerModel(skyrite_pickaxe, "skyrite_pickaxe");
		registerModel(skyrite_axe, "skyrite_axe");
		registerModel(skyrite_hoe, "skyrite_hoe");
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

	public static void registerRecipes()
	{
		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(skyrite, 9), "blockSkyrite"));

		GameRegistry.addRecipe(new ShapedOreRecipe(skyrite_sword,
			"X", "X", "Y",
			'X', skyrite,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(skyrite_shovel,
			"X", "Y", "Y",
			'X', skyrite,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(skyrite_pickaxe,
			"XXX", " Y ", " Y ",
			'X', skyrite,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(skyrite_axe,
			"XX", "XY", " Y",
			'X', skyrite,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(skyrite_hoe,
			"XX", " Y", " Y",
			'X', skyrite,
			'Y', "stickWood"
		));
		GameRegistry.addRecipe(new ShapedOreRecipe(Items.ARROW,
			"X", "#", "Y",
			'X', Items.FLINT,
			'#', "stickWood",
			'Y', sky_feather
		));
	}
}