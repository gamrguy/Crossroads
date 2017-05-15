package com.Da_Technomancer.crossroads.tileentities.technomancy;

import com.Da_Technomancer.crossroads.ModConfig;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.MiscOp;
import com.Da_Technomancer.crossroads.API.packets.IDoubleReceiver;
import com.Da_Technomancer.crossroads.API.packets.ModPackets;
import com.Da_Technomancer.crossroads.API.packets.SendDoubleToClient;
import com.Da_Technomancer.crossroads.API.rotary.IAxisHandler;
import com.Da_Technomancer.crossroads.API.rotary.IAxleHandler;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class MechanicalArmTileEntity extends TileEntity implements ITickable, IDoubleReceiver{

	private static final double LOWER_ARM_LENGTH = 3;
	private static final double UPPER_ARM_LENGTH = 5;
	private static final double MAXIMUM_LOWER_ANGLE = 17D * Math.PI / 36D;//In radians, from horizontal.
	private static final double MINIMUM_LOWER_ANGLE = Math.PI / 12D;//In radians, from horizontal.
	private static final double MAXIMUM_UPPER_ANGLE = .75D * Math.PI;//In radians, from straight down.
	private static final double MINIMUM_UPPER_ANGLE = .25D * Math.PI;//In radians, from straight down.

	private static final int TIERS = ModConfig.speedTiers.getInt();

	private double[][] motionData = new double[3][3];
	/** In radians. */
	private double[] angle = {0, MAXIMUM_LOWER_ANGLE, MINIMUM_UPPER_ANGLE};
	/** A record of the last speeds sent to the client.*/
	private double[] speedRecord = new double[3];
	private static final double[] PHYS_DATA = new double[2];
	/**
	 * ((redstone - 1) % 7) + 1 corresponds to action type, which are:
	 * 0: None, 1: Pickup entity, 2: Pickup block, 3: Pickup from inventory, 4: Use, 5: Deposit into inventory, 6: Drop entity, 7: Drop entity with momentum.
	 * (redstone - 1) / 7 corresponds to an EnumFacing. Only some action types (3, 4, 5) vary based on EnumFacing. 
	 */
	private int redstone = 0;

	@Override
	public void update(){
		for(int i = 0; i < 3; i++){
			angle[i] += motionData[i][0] / 20D;
			if(i == 1){
				angle[1] = Math.min(MAXIMUM_LOWER_ANGLE, Math.max(MINIMUM_LOWER_ANGLE, angle[1]));
			}else if(i == 2){
				angle[2] = Math.min(MAXIMUM_UPPER_ANGLE, Math.max(MINIMUM_UPPER_ANGLE, angle[2]));
			}
			if(!world.isRemote){
				if(MiscOp.tiersRound(motionData[i][0], TIERS) != speedRecord[i]){
					speedRecord[i] = MiscOp.tiersRound(motionData[i][0], TIERS);
					ModPackets.network.sendToAllAround(new SendDoubleToClient("w" + i, speedRecord[i], pos), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
					//The exact angle is of such importance to this device that it is synced in full each time the speed is synced. 
					ModPackets.network.sendToAllAround(new SendDoubleToClient("a" + i, angle[i], pos), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
				}
			}
		}


		int actionType = ((redstone - 1) % 7) + 1;
		if(actionType == 0){
			return;
		}
		EnumFacing side = EnumFacing.getFront((redstone - 1) / 7);
		
		double lengthCross = Math.sqrt(Math.pow(LOWER_ARM_LENGTH, 2) + Math.pow(UPPER_ARM_LENGTH, 2) - (2D * LOWER_ARM_LENGTH * UPPER_ARM_LENGTH * Math.cos(angle[2])));
		double thetaD = angle[1] + angle[2] + Math.asin(Math.sin(angle[2] * LOWER_ARM_LENGTH / lengthCross));
		double holder = -Math.cos(thetaD) * lengthCross;
		
		double posX = holder * Math.cos(angle[0]);
		double posY = -Math.sin(thetaD) * lengthCross;
		double posZ = holder * Math.sin(angle[0]);
		BlockPos endPos = new BlockPos(Math.round(posX), Math.round(posY), Math.round(posZ));
		//TODO
	}

	@Override
	public void receiveDouble(String context, double message){
		char char0 = context.charAt(0);
		char char1 = context.charAt(1);
		if(char0 == 'w'){
			int i = char1 == '0' ? 0 : char1 == '1' ? 1 : 2;
			motionData[i][0] = message;
		}else if(char0 == 'a'){
			int i = char1 == '0' ? 0 : char1 == '1' ? 1 : 2;
			angle[i] = message;
		}
	}

	public void setRedstone(double redstoneIn){
		redstone = Math.min((int) Math.round(redstoneIn), 42);
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		return writeToNBT(super.getUpdateTag());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		nbt.setDouble("speed0", motionData[0][0]);
		nbt.setDouble("speed1", motionData[1][0]);
		nbt.setDouble("speed2", motionData[2][0]);
		nbt.setDouble("angle0", angle[0]);
		nbt.setDouble("angle1", angle[1]);
		nbt.setDouble("angle2", angle[2]);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		motionData[0][0] = nbt.getDouble("speed0");
		motionData[1][0] = nbt.getDouble("speed1");
		motionData[2][0] = nbt.getDouble("speed2");
		angle[0] = nbt.getDouble("angle0");
		angle[1] = nbt.getDouble("angle1");
		angle[2] = nbt.getDouble("angle2");
		if(!world.isRemote){
			speedRecord[0] = motionData[0][0];
			speedRecord[1] = motionData[1][0];
			speedRecord[2] = motionData[2][0];
		}
	}

	//Down: Rotation about y-axis, East: Base bar angle, West: Upper bar angle.
	private final AxleHandler[] axles = {new AxleHandler(0), new AxleHandler(1), new AxleHandler(2)};

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(cap == Capabilities.AXLE_HANDLER_CAPABILITY){
			if(side == EnumFacing.DOWN || side == EnumFacing.EAST || side == EnumFacing.WEST){
				return true;
			}
		}
		return super.hasCapability(cap, side);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(cap == Capabilities.AXLE_HANDLER_CAPABILITY){
			if(side == EnumFacing.DOWN){
				return (T) axles[0];
			}else if(side == EnumFacing.EAST){
				return (T) axles[1];
			}else if(side == EnumFacing.WEST){
				return (T) axles[2];
			}
		}
		return super.getCapability(cap, side);
	}

	private class AxleHandler implements IAxleHandler{

		private final int index;

		public AxleHandler(int index){
			this.index = index;
		}

		@Override
		public double[] getMotionData(){
			return motionData[index];
		}

		private double rotRatio;
		private byte updateKey;

		@Override
		public void propogate(IAxisHandler masterIn, byte key, double rotRatioIn, double lastRadius){
			//If true, this has already been checked.
			if(key == updateKey || masterIn.addToList(this)){
				return;
			}

			rotRatio = rotRatioIn == 0 ? 1 : rotRatioIn;
			updateKey = key;
		}

		@Override
		public double[] getPhysData(){
			return PHYS_DATA;
		}

		@Override
		public double getRotationRatio(){
			return rotRatio;
		}

		@Override
		public void resetAngle(){
			//No effect, as in this the angle is used for non-rendering purposes.
		}

		@Override
		public double getAngle(){
			return angle[index];
		}

		@Override
		public void addEnergy(double energy, boolean allowInvert, boolean absolute){
			if(allowInvert && absolute){
				motionData[index][1] += energy;
			}else if(allowInvert){
				motionData[index][1] += energy * MiscOp.posOrNeg(motionData[index][1]);
			}else if(absolute){
				int sign = (int) MiscOp.posOrNeg(motionData[index][1]);
				motionData[index][1] += energy;
				if(sign != 0 && MiscOp.posOrNeg(motionData[index][1]) != sign){
					motionData[index][1] = 0;
				}
			}else{
				int sign = (int) MiscOp.posOrNeg(motionData[index][1]);
				motionData[index][1] += energy * ((double) sign);
				if(MiscOp.posOrNeg(motionData[index][1]) != sign){
					motionData[index][1] = 0;
				}
			}
			markDirty();
		}

		@Override
		public void markChanged(){
			markDirty();
		}
	}
}
