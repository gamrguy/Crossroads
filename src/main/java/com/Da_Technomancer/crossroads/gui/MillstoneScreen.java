package com.Da_Technomancer.crossroads.gui;

import com.Da_Technomancer.crossroads.API.templates.MachineGUI;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.gui.container.MillstoneContainer;
import com.Da_Technomancer.crossroads.tileentities.rotary.MillstoneTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class MillstoneScreen extends MachineGUI<MillstoneContainer, MillstoneTileEntity>{

	private static final ResourceLocation BACKGROUND = new ResourceLocation(Crossroads.MODID, "textures/gui/container/millstone_gui.png");

	public MillstoneScreen(MillstoneContainer cont, PlayerInventory playerInv, ITextComponent text){
		super(cont, playerInv, text);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		GlStateManager.color3f(1, 1, 1);
		Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND);
		blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		blit(guiLeft + 66, guiTop + 35, 176, 0, 44, (int) Math.ceil(te.progRef.get() * 17 / MillstoneTileEntity.REQUIRED));
	}
}