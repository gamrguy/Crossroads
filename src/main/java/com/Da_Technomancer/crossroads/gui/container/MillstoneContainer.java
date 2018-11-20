package com.Da_Technomancer.crossroads.gui.container;

import com.Da_Technomancer.crossroads.API.templates.MachineContainer;
import com.Da_Technomancer.crossroads.tileentities.rotary.MillstoneTileEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class MillstoneContainer extends MachineContainer{

	public MillstoneContainer(IInventory playerInv, MillstoneTileEntity te){
		super(playerInv, te);
	}

	@Override
	protected void addSlots(){
		// input 0
		addSlotToContainer(new StrictSlot(te, 0, 80, 17));

		// output 1-3
		for(int x = 0; x < 3; x++){
			addSlotToContainer(new OutputSlot(te, 1 + x, 62 + (x * 18), 53));
		}
	}
}