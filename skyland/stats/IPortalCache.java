package skyland.stats;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;

public interface IPortalCache
{
	public DimensionType getLastDim(ResourceLocation key);

	public DimensionType getLastDim(ResourceLocation key, DimensionType nullDefault);

	public void setLastDim(ResourceLocation key, DimensionType type);

	public BlockPos getLastPos(ResourceLocation key, DimensionType type);

	public BlockPos getLastPos(ResourceLocation key, DimensionType type, BlockPos pos);

	public boolean hasLastPos(ResourceLocation key, DimensionType type);

	public void setLastPos(ResourceLocation key, DimensionType type, BlockPos pos);

	public void clearLastPos(ResourceLocation key, DimensionType type);

	public void writeToNBT(NBTTagCompound nbt);

	public void readFromNBT(NBTTagCompound nbt);
}