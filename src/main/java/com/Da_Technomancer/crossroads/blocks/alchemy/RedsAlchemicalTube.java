package com.Da_Technomancer.crossroads.blocks.alchemy;

import com.Da_Technomancer.crossroads.tileentities.alchemy.RedsAlchemicalTubeTileEntity;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class RedsAlchemicalTube extends AlchemicalTube{

	public RedsAlchemicalTube(boolean crystal){
		super(crystal, (crystal ? "crystal_" : "") + "reds_alch_tube");
		setDefaultState(getDefaultState().with(EssentialsProperties.REDSTONE_BOOL, false));
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new RedsAlchemicalTubeTileEntity(!crystal);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(state.get(EssentialsProperties.REDSTONE_BOOL) && EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			return super.onBlockActivated(state, worldIn, pos, playerIn, hand, hit);
		}
		return false;
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		if(worldIn.isBlockPowered(pos)){
			if(!state.get(EssentialsProperties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, state.with(EssentialsProperties.REDSTONE_BOOL, true));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof RedsAlchemicalTubeTileEntity){
					((RedsAlchemicalTubeTileEntity) te).wipeCache();
				}
			}
		}else{
			if(state.get(EssentialsProperties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, state.with(EssentialsProperties.REDSTONE_BOOL, false));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof RedsAlchemicalTubeTileEntity){
					((RedsAlchemicalTubeTileEntity) te).wipeCache();
				}
			}
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		super.fillStateContainer(builder);
		builder.add(EssentialsProperties.REDSTONE_BOOL);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		boolean hasReds = context.getWorld().isBlockPowered(context.getPos());
		if(hasReds){
			return super.getStateForPlacement(context).with(EssentialsProperties.REDSTONE_BOOL, true);
		}else{
			return getDefaultState().with(EssentialsProperties.REDSTONE_BOOL, false);
		}
	}
}
