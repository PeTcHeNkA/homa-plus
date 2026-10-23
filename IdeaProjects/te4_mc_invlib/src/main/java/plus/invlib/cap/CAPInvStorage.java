package plus.invlib.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;


public class CAPInvStorage implements IStorage<ICAPInventory> {

	@Override
	public NBTBase writeNBT(Capability<ICAPInventory> capability, ICAPInventory instance, EnumFacing side) {
		NBTTagCompound properties = new NBTTagCompound();
		instance.getInventory().writeToNBT(properties);
		return properties;
	}

	@Override
	public void readNBT(Capability<ICAPInventory> capability, ICAPInventory instance, EnumFacing side, NBTBase nbt) {
		NBTTagCompound properties = (NBTTagCompound)nbt;
		instance.getInventory().readFromNBT(properties);
	}
}