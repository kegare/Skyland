package skyland.block;

import java.util.Random;

import com.google.common.cache.LoadingCache;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockPattern.PatternHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skyland.client.gui.GuiRegeneration;
import skyland.core.Skyland;
import skyland.stats.PortalCache;
import skyland.world.TeleporterSkyPortal;

public class BlockSkyPortal extends BlockPortal
{
	public BlockSkyPortal()
	{
		super();
		this.setUnlocalizedName("skyPortal");
		this.setSoundType(SoundType.GLASS);
		this.setTickRandomly(false);
		this.setBlockUnbreakable();
		this.disableStats();
		this.setCreativeTab(Skyland.TAB_SKYLAND);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {}

	@Override
	public boolean trySpawnPortal(World world, BlockPos pos)
	{
		Size size = new Size(world, pos, EnumFacing.Axis.X);

		if (size.isValid() && size.portalBlockCount == 0)
		{
			size.placePortalBlocks();

			return true;
		}
		else
		{
			Size size1 = new Size(world, pos, EnumFacing.Axis.Z);

			if (size1.isValid() && size1.portalBlockCount == 0)
			{
				size1.placePortalBlocks();

				return true;
			}
			else return false;
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		EnumFacing.Axis axis = state.getValue(AXIS);
		Size size;

		if (axis == EnumFacing.Axis.X)
		{
			size = new Size(world, pos, EnumFacing.Axis.X);

			if (!size.isValid() || size.portalBlockCount < size.width * size.height)
			{
				world.setBlockToAir(pos);
			}
		}
		else if (axis == EnumFacing.Axis.Z)
		{
			size = new Size(world, pos, EnumFacing.Axis.Z);

			if (!size.isValid() || size.portalBlockCount < size.width * size.height)
			{
				world.setBlockToAir(pos);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
		{
			displayGui(world, pos, state, player, hand, side);
		}

		return true;
	}

	@SideOnly(Side.CLIENT)
	public void displayGui(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side)
	{
		FMLClientHandler.instance().showGuiScreen(new GuiRegeneration());
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
	{
		if (world.isRemote || Skyland.DIM_SKYLAND == null)
		{
			return;
		}

		if (entity.isDead || entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss() || entity instanceof IProjectile)
		{
			return;
		}

		if (entity.timeUntilPortal <= 0)
		{
			ResourceLocation key = TeleporterSkyPortal.KEY;
			PortalCache cache = PortalCache.get(entity);
			MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
			DimensionType dimOld = world.provider.getDimensionType();
			DimensionType dimNew = dimOld == Skyland.DIM_SKYLAND ? cache.getLastDim(key) : Skyland.DIM_SKYLAND;
			WorldServer worldOld = server.getWorld(dimOld.getId());
			WorldServer worldNew = server.getWorld(dimNew.getId());

			if (worldOld == null || worldNew == null)
			{
				return;
			}

			ITeleporter teleporter = new TeleporterSkyPortal();
			BlockPos prevPos = entity.getPosition();

			entity.timeUntilPortal = entity.getPortalCooldown();

			cache.setLastDim(key, dimOld);
			cache.setLastPos(key, dimOld, prevPos);

			PatternHelper pattern = createPatternHelper(world, pos);
			double d0 = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? (double)pattern.getFrontTopLeft().getZ() : (double)pattern.getFrontTopLeft().getX();
			double d1 = pattern.getForwards().getAxis() == EnumFacing.Axis.X ? entity.posZ : entity.posX;
			d1 = Math.abs(MathHelper.pct(d1 - (pattern.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE ? 1 : 0), d0, d0 - pattern.getWidth()));
			double d2 = MathHelper.pct(entity.posY - 1.0D, pattern.getFrontTopLeft().getY(), pattern.getFrontTopLeft().getY() - pattern.getHeight());

			cache.setLastPortalVec(new Vec3d(d1, d2, 0.0D));
			cache.setTeleportDirection(pattern.getForwards());

			entity.changeDimension(dimNew.getId(), teleporter);
		}
		else
		{
			entity.timeUntilPortal = entity.getPortalCooldown();
		}
	}

	@Override
	public PatternHelper createPatternHelper(World world, BlockPos pos)
	{
		EnumFacing.Axis axis = EnumFacing.Axis.Z;
		Size size = new Size(world, pos, EnumFacing.Axis.X);
		LoadingCache<BlockPos, BlockWorldState> cache = BlockPattern.createLoadingCache(world, true);

		if (!size.isValid())
		{
			axis = EnumFacing.Axis.X;
			size = new Size(world, pos, EnumFacing.Axis.Z);
		}

		if (!size.isValid())
		{
			return new PatternHelper(pos, EnumFacing.NORTH, EnumFacing.UP, cache, 1, 1, 1);
		}
		else
		{
			int[] values = new int[EnumFacing.AxisDirection.values().length];
			EnumFacing facing = size.rightDir.rotateYCCW();
			BlockPos blockpos = size.buttonLeft.up(size.getHeight() - 1);

			for (EnumFacing.AxisDirection direction : EnumFacing.AxisDirection.values())
			{
				PatternHelper pattern = new PatternHelper(facing.getAxisDirection() == direction ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1), EnumFacing.getFacingFromAxis(direction, axis), EnumFacing.UP, cache, size.getWidth(), size.getHeight(), 1);

				for (int i = 0; i < size.getWidth(); ++i)
				{
					for (int j = 0; j < size.getHeight(); ++j)
					{
						BlockWorldState state = pattern.translateOffset(i, j, 1);

						if (state.getBlockState() != null && state.getBlockState().getMaterial() != Material.AIR)
						{
							++values[direction.ordinal()];
						}
					}
				}
			}

			EnumFacing.AxisDirection axis1 = EnumFacing.AxisDirection.POSITIVE;

			for (EnumFacing.AxisDirection direction : EnumFacing.AxisDirection.values())
			{
				if (values[direction.ordinal()] < values[axis1.ordinal()])
				{
					axis1 = direction;
				}
			}

			return new PatternHelper(facing.getAxisDirection() == axis1 ? blockpos : blockpos.offset(size.rightDir, size.getWidth() - 1), EnumFacing.getFacingFromAxis(axis1, axis), EnumFacing.UP, cache, size.getWidth(), size.getHeight(), 1);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		return new ItemStack(this);
	}

	public class Size
	{
		private final World world;
		private final EnumFacing.Axis axis;
		private final EnumFacing rightDir;
		private final EnumFacing leftDir;
		private int portalBlockCount = 0;
		private BlockPos buttonLeft;
		private int height;
		private int width;

		public Size(World world, BlockPos pos, EnumFacing.Axis axis)
		{
			this.world = world;
			this.axis = axis;

			if (axis == EnumFacing.Axis.X)
			{
				this.leftDir = EnumFacing.EAST;
				this.rightDir = EnumFacing.WEST;
			}
			else
			{
				this.leftDir = EnumFacing.NORTH;
				this.rightDir = EnumFacing.SOUTH;
			}

			for (BlockPos blockpos = pos; pos.getY() > blockpos.getY() - 21 && pos.getY() > 0 && isEmptyBlock(world.getBlockState(pos.down())); pos = pos.down())
			{
				;
			}

			int i = getDistanceUntilEdge(pos, leftDir) - 1;

			if (i >= 0)
			{
				this.buttonLeft = pos.offset(leftDir, i);
				this.width = getDistanceUntilEdge(buttonLeft, rightDir);

				if (width < 2 || width > 21)
				{
					this.buttonLeft = null;
					this.width = 0;
				}
			}

			if (buttonLeft != null)
			{
				this.height = calculatePortalHeight();
			}
		}

		protected int getDistanceUntilEdge(BlockPos pos, EnumFacing face)
		{
			int i;

			for (i = 0; i < 22; ++i)
			{
				BlockPos pos1 = pos.offset(face, i);

				if (!isEmptyBlock(world.getBlockState(pos1)) || world.getBlockState(pos1.down()).getBlock() != Blocks.QUARTZ_BLOCK)
				{
					break;
				}
			}

			Block block = world.getBlockState(pos.offset(face, i)).getBlock();

			return block == Blocks.QUARTZ_BLOCK ? i : 0;
		}

		public int getHeight()
		{
			return height;
		}

		public int getWidth()
		{
			return width;
		}

		protected int calculatePortalHeight()
		{
			int i;

			outside: for (height = 0; height < 21; ++height)
			{
				for (i = 0; i < width; ++i)
				{
					BlockPos pos = buttonLeft.offset(rightDir, i).up(height);
					IBlockState state = world.getBlockState(pos);
					Block block = state.getBlock();

					if (!isEmptyBlock(state))
					{
						break outside;
					}

					if (block == BlockSkyPortal.this)
					{
						++portalBlockCount;
					}

					if (i == 0)
					{
						block = world.getBlockState(pos.offset(leftDir)).getBlock();

						if (block != Blocks.QUARTZ_BLOCK)
						{
							break outside;
						}
					}
					else if (i == width - 1)
					{
						block = world.getBlockState(pos.offset(rightDir)).getBlock();

						if (block != Blocks.QUARTZ_BLOCK)
						{
							break outside;
						}
					}
				}
			}

			for (i = 0; i < width; ++i)
			{
				if (world.getBlockState(buttonLeft.offset(rightDir, i).up(height)).getBlock() != Blocks.QUARTZ_BLOCK)
				{
					height = 0;
					break;
				}
			}

			if (height <= 21 && height >= 3)
			{
				return height;
			}
			else
			{
				buttonLeft = null;
				width = 0;
				height = 0;

				return 0;
			}
		}

		protected boolean isEmptyBlock(IBlockState state)
		{
			return state.getMaterial() == Material.AIR || state.getBlock() == BlockSkyPortal.this;
		}

		public boolean isValid()
		{
			return buttonLeft != null && width >= 2 && width <= 21 && height >= 3 && height <= 21;
		}

		public void placePortalBlocks()
		{
			for (int i = 0; i < width; ++i)
			{
				BlockPos pos = buttonLeft.offset(rightDir, i);

				for (int j = 0; j < height; ++j)
				{
					world.setBlockState(pos.up(j), getDefaultState().withProperty(AXIS, axis), 2);
				}
			}
		}
	}
}