package skyland.stats;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IPortalCache
{
	public int getLastDim(int type);

	public void setLastDim(int type, int dim);

	public BlockPos getLastPos(int type, int dim);

	public BlockPos getLastPos(int type, int dim, BlockPos pos);

	public boolean hasLastPos(int type, int dim);

	public void setLastPos(int type, int dim, BlockPos pos);

	public void writeToNBT(NBTTagCompound nbt);

	public void readFromNBT(NBTTagCompound nbt);
}