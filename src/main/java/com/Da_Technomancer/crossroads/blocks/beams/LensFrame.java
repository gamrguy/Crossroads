package com.Da_Technomancer.crossroads.blocks.beams;

import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.items.CRItems;
import com.Da_Technomancer.crossroads.tileentities.beams.LensFrameTileEntity;
import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.IReadable;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class LensFrame extends ContainerBlock implements IReadable{

	private static final VoxelShape[] SHAPE = new VoxelShape[3];

	static{
		SHAPE[0] = box(6, 0, 0, 10, 16, 16);
		SHAPE[1] = box(0, 6, 0, 16, 10, 16);
		SHAPE[2] = box(0, 0, 6, 16, 16, 10);
	}

	public LensFrame(){
		super(CRBlocks.getRockProperty());
		String name = "lens_frame";
		setRegistryName(name);
		CRBlocks.toRegister.add(this);
		CRBlocks.blockAddQue(this);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return SHAPE[state.getValue(ESProperties.AXIS).ordinal()];
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new LensFrameTileEntity();
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state){
		return BlockRenderType.MODEL;
	}

//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT;
//	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return defaultBlockState().setValue(ESProperties.AXIS, context.getNearestLookingDirection().getAxis());
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.AXIS);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		ItemStack stack = playerIn.getItemInHand(hand);

		if(ESConfig.isWrench(stack)){
			// Wrenches rotate the block instead
			if(!worldIn.isClientSide) worldIn.setBlockAndUpdate(pos, state.cycle(ESProperties.AXIS));
			return ActionResultType.SUCCESS;
		}else if(stack.sameItem(CRItems.omnimeter.getDefaultInstance())){
			// Omnimeter performs its function instead
			return ActionResultType.PASS;
		}else{
			TileEntity te = worldIn.getBlockEntity(pos);
			if(!(te instanceof LensFrameTileEntity)){
				return ActionResultType.PASS;
			}
			LensFrameTileEntity lens = (LensFrameTileEntity)te;
			ItemStack inLens = lens.getLensItem();
			if(!inLens.isEmpty()){
				if(!worldIn.isClientSide) {
					if(!playerIn.inventory.add(inLens)){
						ItemEntity dropped = playerIn.drop(inLens, false);
						if(dropped != null){
							dropped.setNoPickUpDelay();
							dropped.setOwner(playerIn.getUUID());
						}
					}
					lens.setLensItem(ItemStack.EMPTY);
				}
				return ActionResultType.SUCCESS;
			}else if(!stack.isEmpty()){
				if(worldIn.getRecipeManager().getRecipeFor(CRRecipes.BEAM_LENS_TYPE, new Inventory(stack), worldIn).isPresent()){
					if(!worldIn.isClientSide){
						lens.setLensItem(stack.split(1));
					}
					return ActionResultType.SUCCESS;
				}
			}
		}

		return ActionResultType.PASS;
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving){
		TileEntity te = world.getBlockEntity(pos);
		if(newState.getBlock() != this && te instanceof LensFrameTileEntity){
			InventoryHelper.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), ((LensFrameTileEntity) te).getLensItem());
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state){
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World world, BlockPos pos){
		return RedstoneUtil.clampToVanilla(read(world, pos, blockState));
	}

	@Override
	public float read(World world, BlockPos pos, BlockState blockState){
		TileEntity te = world.getBlockEntity(pos);
		if(te instanceof LensFrameTileEntity){
			return ((LensFrameTileEntity) te).getRedstone();
		}
		return 0;
	}
}
