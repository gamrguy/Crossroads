package com.Da_Technomancer.crossroads.blocks.magic;

import com.Da_Technomancer.crossroads.blocks.ModBlocks;
import com.Da_Technomancer.crossroads.items.ModItems;
import com.Da_Technomancer.crossroads.tileentities.magic.CrystalMasterAxisTileEntity;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrystalMasterAxis extends BlockContainer{
	
	public CrystalMasterAxis(){
		super(Material.ROCK);
		String name = "master_axis_crystal";
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(ModItems.TAB_CROSSROADS);
		setHardness(3);
		setSoundType(SoundType.STONE);
		ModBlocks.toRegister.add(this);
		ModBlocks.blockAddQue(this);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.getDirectionFromEntityLiving(pos, placer);
		return getDefaultState().withProperty(EssentialsProperties.FACING, enumfacing);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState blockstate){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof CrystalMasterAxisTileEntity){
			((CrystalMasterAxisTileEntity) te).disconnect();
		}
		super.breakBlock(world, pos, blockstate);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand), worldIn.isRemote)){
			if(!worldIn.isRemote){
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof CrystalMasterAxisTileEntity){
					((CrystalMasterAxisTileEntity) te).disconnect();
				}
				worldIn.setBlockState(pos, state.cycleProperty(EssentialsProperties.FACING));
			}
			return true;
		}
		return false;
	}
	
	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {EssentialsProperties.FACING});
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		EnumFacing facing = EnumFacing.getFront(meta);
		return this.getDefaultState().withProperty(EssentialsProperties.FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex();
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new CrystalMasterAxisTileEntity(EnumFacing.getFront(meta));

	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(EssentialsProperties.FACING, rot.rotate(state.getValue(EssentialsProperties.FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(EssentialsProperties.FACING)));
	}

}
