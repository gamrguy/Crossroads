package com.Da_Technomancer.crossroads.items.crafting;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.state.IBlockState;

public class BlockRecipePredicate implements RecipePredicate<IBlockState>{

	private final IBlockState state;
	private final boolean ignoreMeta;
	
	/**
	 * 
	 * @param state
	 * @param ignoreMeta If true, ignore all properties and only focus on blocktype
	 */
	public BlockRecipePredicate(IBlockState state, boolean ignoreMeta){
		this.state = state;
		this.ignoreMeta = ignoreMeta;
	}

	@Override
	public boolean test(IBlockState state){
		if(ignoreMeta){
			return state.getBlock() == this.state.getBlock();
		}
		return state == this.state;
	}

	@Override
	public List<IBlockState> getMatchingList(){
		if(ignoreMeta){
			return state.getBlock().getBlockState().getValidStates();
		}
		return ImmutableList.of(state);
	}

	@Override
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof BlockRecipePredicate){
			BlockRecipePredicate otherStack = (BlockRecipePredicate) other;
			return state == otherStack.state && ignoreMeta == otherStack.ignoreMeta;
		}

		return false;
	}
	
	@Override
	public int hashCode(){
		return (state.hashCode() << 1) + (ignoreMeta ? 1 : 0);
	}

	@Override
	public String toString(){
		return "BlockCraftingStack[State: " + state.toString() + "]";
	}
}