package com.Da_Technomancer.crossroads.render.TESR;

import com.Da_Technomancer.crossroads.tileentities.technomancy.FluxSinkTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class FluxSinkRenderer extends TileEntityRenderer<FluxSinkTileEntity>{

	@Override
	public void render(FluxSinkTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te == null || !te.isRunningForRender()){
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);


		GlStateManager.popMatrix();
		//TODO
	}
}
