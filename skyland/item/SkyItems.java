package skyland.item;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;
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

	public static final ArmorMaterial ARMOR_SKYRITE = EnumHelper.addArmorMaterial("SKYRITE", "skyrite", 60, new int[]{3, 6, 8, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F).setRepairItem(new ItemStack(SKYRITE));

	public static final ItemArmor SKYRITE_HELMET = new ItemArmorSkyrite(ARMOR_SKYRITE, "helmetSkyrite", "skyrite", EntityEquipmentSlot.HEAD);
	public static final ItemArmor SKYRITE_CHESTPLATE = new ItemArmorSkyrite(ARMOR_SKYRITE, "chestplateSkyrite", "skyrite", EntityEquipmentSlot.CHEST);
	public static final ItemArmor SKYRITE_LEGGINGS = new ItemArmorSkyrite(ARMOR_SKYRITE, "leggingsSkyrite", "skyrite", EntityEquipmentSlot.LEGS);
	public static final ItemArmor SKYRITE_BOOTS = new ItemArmorSkyrite(ARMOR_SKYRITE, "bootsSkyrite", "skyrite", EntityEquipmentSlot.FEET);

	public static void registerItems(IForgeRegistry<Item> registry)
	{
		registry.register(SKY_FEATHER.setRegistryName("sky_feather"));
		registry.register(SKYRITE.setRegistryName("skyrite"));
		registry.register(SKYRITE_SWORD.setRegistryName("skyrite_sword"));
		registry.register(SKYRITE_SHOVEL.setRegistryName("skyrite_shovel"));
		registry.register(SKYRITE_PICKAXE.setRegistryName("skyrite_pickaxe"));
		registry.register(SKYRITE_AXE.setRegistryName("skyrite_axe"));
		registry.register(SKYRITE_HOE.setRegistryName("skyrite_hoe"));
		registry.register(SKYRITE_HELMET.setRegistryName("skyrite_helmet"));
		registry.register(SKYRITE_CHESTPLATE.setRegistryName("skyrite_chestplate"));
		registry.register(SKYRITE_LEGGINGS.setRegistryName("skyrite_leggings"));
		registry.register(SKYRITE_BOOTS.setRegistryName("skyrite_boots"));
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
		registerModel(SKYRITE_HELMET, "skyrite_helmet");
		registerModel(SKYRITE_CHESTPLATE, "skyrite_chestplate");
		registerModel(SKYRITE_LEGGINGS, "skyrite_leggings");
		registerModel(SKYRITE_BOOTS, "skyrite_boots");
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
}