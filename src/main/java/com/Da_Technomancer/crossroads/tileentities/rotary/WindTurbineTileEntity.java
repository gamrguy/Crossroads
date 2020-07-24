package com.Da_Technomancer.crossroads.tileentities.rotary;

import com.Da_Technomancer.crossroads.API.CRProperties;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.templates.ModuleTE;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@ObjectHolder(Crossroads.MODID)
public class WindTurbineTileEntity extends ModuleTE{

	@ObjectHolder("wind_turbine")
	public static TileEntityType<WindTurbineTileEntity> type = null;

	public static final double MAX_SPEED = 2D;
	public static final double INERTIA = 200;
	public static final double POWER_PER_LEVEL = 10D;

	//Undocumented 'easter egg'. This person takes way more damage from windmills
	//Don't ask.
	private static final String murderEasterEgg = "dinidini";

	private boolean newlyPlaced = true;
	private int level = 1;
	private boolean running = false;

	private AxisAlignedBB targetBB = null;

	public WindTurbineTileEntity(){
		super(type);
	}

	public WindTurbineTileEntity(boolean newlyPlaced){
		this();
		this.newlyPlaced = newlyPlaced;
	}

	protected Direction getFacing(){
		BlockState state = getBlockState();
		if(state.getBlock() != CRBlocks.windTurbine){
			remove();
			return Direction.NORTH;
		}
		return state.get(CRProperties.HORIZ_FACING);
	}

	private AxisAlignedBB getTargetBB(){
		if(targetBB == null){
			Direction dir = getFacing();
			Direction planeDir = dir.rotateY();
			if(planeDir.getAxisDirection() == Direction.AxisDirection.NEGATIVE){
				planeDir = planeDir.getOpposite();
			}
			BlockPos center = pos.offset(dir);
			if(dir.getAxisDirection() == Direction.AxisDirection.POSITIVE){
				targetBB = new AxisAlignedBB(center.offset(planeDir, -2).offset(Direction.DOWN, 2), center.offset(planeDir, 3).offset(Direction.UP, 3).offset(dir));
			}else{
				targetBB = new AxisAlignedBB(center.offset(planeDir, -2).offset(Direction.DOWN, 2), center.offset(planeDir, 3).offset(Direction.UP, 3).offset(dir, -1));
			}
		}

		return targetBB;
	}

	@Override
	public void updateContainingBlockInfo(){
		super.updateContainingBlockInfo();
		axleOpt.invalidate();
		axleOpt = LazyOptional.of(() -> axleHandler);
		newlyPlaced = true;
		targetBB = null;
	}

	@Override
	protected boolean useRotary(){
		return true;
	}

	@Override
	protected AxleHandler createAxleHandler(){
		return new AngleAxleHandler();
	}

	@Override
	public void addInfo(ArrayList<ITextComponent> chat, PlayerEntity player, BlockRayTraceResult hit){
		chat.add(new TranslationTextComponent("tt.crossroads.wind_turbine.weather", POWER_PER_LEVEL * (double) level));
		super.addInfo(chat, player, hit);
	}

	public float getRedstoneOutput(){
		return (float) (2 * POWER_PER_LEVEL + level * POWER_PER_LEVEL);
	}

	@Override
	public void tick(){
		super.tick();

		if(!world.isRemote){
			//Every 30 seconds check whether the placement requirements are valid, and cache the result
			if(newlyPlaced || world.getGameTime() % 600 == 0){
				newlyPlaced = false;
				running = false;
				Direction facing = getFacing();
				BlockPos offsetPos = pos.offset(facing);
				if(world.canBlockSeeSky(offsetPos)){
					running = true;
					outer:
					for(int i = -2; i <= 2; i++){
						for(int j = -2; j <= 2; j++){
							BlockPos checkPos = offsetPos.add(facing.getZOffset() * i, j, facing.getXOffset() * i);
							BlockState checkState = world.getBlockState(checkPos);
							if(!checkState.getBlock().isAir(checkState, world, checkPos)){
								running = false;
								break outer;
							}
						}
					}
				}

				markDirty();
			}

			//Damage entities in the blades while spinning at high speed
			if(Math.abs(motData[0]) >= 1.5D){
				List<LivingEntity> ents = world.getEntitiesWithinAABB(LivingEntity.class, getTargetBB(), EntityPredicates.IS_LIVING_ALIVE);
				for(LivingEntity ent : ents){
					if(ent instanceof PlayerEntity && murderEasterEgg.equals(((PlayerEntity) ent).getGameProfile().getName())){
						ent.attackEntityFrom(DamageSource.FLY_INTO_WALL, 100);//This seems fair
					}else{
						ent.attackEntityFrom(DamageSource.FLY_INTO_WALL, 1);
					}
				}
			}

			if(running && axleHandler.axis != null){
				if(world.getGameTime() % 10 == 0 && world.rand.nextInt(240) == 0){
					//Randomize output
					level = (world.rand.nextInt(2) + 1) * (world.rand.nextBoolean() ? -1 : 1);//Gen a random number from -2 to 2, other than 0
				}

				if(motData[0] * Math.signum(level) < MAX_SPEED){
					motData[1] += (double) level * POWER_PER_LEVEL;
				}

				markDirty();
			}
		}
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		level = nbt.getInt("level");
		running = nbt.getBoolean("running");
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putInt("level", level);
		nbt.putBoolean("running", running);
		return nbt;
	}

	private static final AxisAlignedBB RENDER_BOX = new AxisAlignedBB(-1, -1, -1, 2, 2, 2);

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return RENDER_BOX.offset(pos);
	}

	@Override
	public double getMoInertia(){
		return INERTIA;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == Capabilities.AXLE_CAPABILITY && (facing == null || facing == getFacing().getOpposite())){
			return (LazyOptional<T>) axleOpt;
		}
		return super.getCapability(capability, facing);
	}
}
