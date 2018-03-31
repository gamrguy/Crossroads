package com.Da_Technomancer.crossroads.tileentities.alchemy;

import java.util.ArrayList;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.EnergyConverters;
import com.Da_Technomancer.crossroads.API.IInfoDevice;
import com.Da_Technomancer.crossroads.API.IInfoTE;
import com.Da_Technomancer.crossroads.API.MiscOp;
import com.Da_Technomancer.crossroads.API.heat.IHeatHandler;
import com.Da_Technomancer.crossroads.API.technomancy.EnumGoggleLenses;
import com.Da_Technomancer.crossroads.items.OmniMeter;
import com.Da_Technomancer.crossroads.items.Thermometer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;

public class MaxwellDemonTileEntity extends TileEntity implements ITickable, IInfoTE{

	private double tempUp = 0;
	private double tempDown = 0;
	private boolean init = false;

	@Override
	public void addInfo(ArrayList<String> chat, IInfoDevice device, EntityPlayer player, EnumFacing side){
		if(device instanceof OmniMeter || device == EnumGoggleLenses.RUBY || device instanceof Thermometer){
			chat.add("Upper Temp: " + MiscOp.betterRound(tempUp, 3) + "°C");
			chat.add("Lower Temp: " + MiscOp.betterRound(tempDown, 3) + "°C");
			if(!(device instanceof Thermometer)){
				chat.add("Biome Temp: " + EnergyConverters.convertBiomeTemp(world.getBiomeForCoordsBody(pos).getTemperature(pos)) + "°C");
			}
		}
	}

	@Override
	public void update(){
		if(world.isRemote){
			return;
		}

		if(!init){
			heatHandlerUp.init();
		}

		if(tempUp < 2500D){
			tempUp = Math.min(2500D, tempUp + 15D);
			markDirty();
		}
		if(tempDown > -250D){
			tempDown = Math.max(-250D, tempDown - 15D);
			markDirty();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		nbt.setBoolean("init", init);
		nbt.setDouble("tempU", tempUp);
		nbt.setDouble("tempD", tempDown);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		init = nbt.getBoolean("init");
		tempUp = nbt.getDouble("tempU");
		tempDown = nbt.getDouble("tempD");
	}

	private final HeatHandler heatHandlerUp = new HeatHandler(true);
	private final HeatHandler heatHandlerDown = new HeatHandler(false);

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(cap == Capabilities.HEAT_HANDLER_CAPABILITY && (side == null || side.getAxis() == Axis.Y)){
			return true;
		}
		return super.hasCapability(cap, side);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(cap == Capabilities.HEAT_HANDLER_CAPABILITY){
			if(side == null || side == EnumFacing.UP){
				return (T) heatHandlerUp;
			}else if(side == EnumFacing.DOWN){
				return (T) heatHandlerDown;
			}
		}
		return super.getCapability(cap, side);
	}

	private class HeatHandler implements IHeatHandler{

		private final boolean up;

		private HeatHandler(boolean up){
			this.up = up;
		}

		private void init(){
			if(!init){
				tempUp = EnergyConverters.convertBiomeTemp(world.getBiomeForCoordsBody(pos).getTemperature(pos));
				tempDown = tempUp;
				init = true;
			}
		}

		@Override
		public double getTemp(){
			init();
			return up ? tempUp : tempDown;
		}

		@Override
		public void setTemp(double tempIn){
			init = true;
			if(up){
				tempUp = tempIn;
			}else{
				tempDown = tempIn;
			}
			markDirty();
		}

		@Override
		public void addHeat(double heat){
			init();
			if(up){
				tempUp += heat;
			}else{
				tempDown += heat;
			}
			markDirty();
		}
	}
}
