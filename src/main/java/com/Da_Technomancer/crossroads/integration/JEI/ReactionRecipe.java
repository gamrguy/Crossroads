package com.Da_Technomancer.crossroads.integration.JEI;

import com.Da_Technomancer.crossroads.API.alchemy.IReagent;
import com.Da_Technomancer.crossroads.API.alchemy.ITransparentReaction;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.elements.DrawableResource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactionRecipe implements IRecipeWrapper{

	//Has to be kept as an Object to prevent crashes without JEI
	private Object bolt;
	private Object blast;
	
	private final ArrayList<ReagIngr> ingr;
	private final ArrayList<ReagIngr> prod;
	private final boolean charged;
	private final boolean dangerous;
	private final IReagent catalyst;
	private final double minTemp;
	private final double maxTemp;
	private final double deltaHeat;

	public ReactionRecipe(ITransparentReaction reaction){
		ingr = new ArrayList<ReagIngr>(reaction.getReagents().length);
		for(int i = 0; i < reaction.getReagents().length; i++){
			ingr.add(new ReagIngr(reaction.getReagents()[i].getType(), reaction.getReagents()[i].getAmount()));
		}
		prod = new ArrayList<ReagIngr>(reaction.getProducts().length);
		for(int i = 0; i < reaction.getProducts().length; i++){
			prod.add(new ReagIngr(reaction.getProducts()[i].getType(), reaction.getProducts()[i].getAmount()));
		}
		charged = reaction.charged();
		dangerous = reaction.isDestructive();
		catalyst = reaction.getCatalyst();
		minTemp = reaction.minTemp();
		maxTemp = reaction.maxTemp();
		deltaHeat = reaction.deltaHeatPer();
	}

	@Override
	public List<String> getTooltipStrings(int mouseX, int mouseY){
		if(catalyst != null && mouseX >= 82 && mouseX <= 98 && mouseY >= 2 && mouseY <= 18){
			return ImmutableList.of(catalyst.getName());
		}
		return Collections.emptyList();
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY){
		FontRenderer fontRenderer = minecraft.fontRenderer;
		String line = (minTemp <= -273 ? "-273" : minTemp) + "" + (maxTemp >= Short.MAX_VALUE - 100 ? "°C and up" : " to " + maxTemp + "°C");
		fontRenderer.drawString(line, 90 - fontRenderer.getStringWidth(line) / 2, 42, 4210752);
		line = deltaHeat > 0 ? "Endothermic" : deltaHeat < 0 ? "Exothermic" : "Isothermic";
		fontRenderer.drawString(line, 90 - fontRenderer.getStringWidth(line) / 2, 62, 4210752);

		if(charged){
			if(bolt == null){
				bolt = new DrawableResource(ReactionCategory.ICONS, 16, 0, 16, 16, 0, 0, 0, 0, 256, 256);
			}
			GlStateManager.color(1, 1, 1);
			((IDrawable) bolt).draw(minecraft, 66, 2);
		}

		if(dangerous){
			if(blast == null){
				blast = new DrawableResource(ReactionCategory.ICONS, 64, 0, 16, 16, 0, 0, 0, 0, 256, 256);
			}
			GlStateManager.color(1, 1, 1);
			((IDrawable) blast).draw(minecraft, 98, 2);
		}

		if(catalyst != null){
			GlStateManager.color(1, 1, 1);
			ReagentIngredientRenderer.RENDERER.render(minecraft, 82, 2, new ReagIngr(catalyst, 0));
		}
	}

	@Override
	public void getIngredients(IIngredients ingredients){
		ingredients.setInputs(ReagIngr.class, ingr);
		ingredients.setOutputs(ReagIngr.class, prod);
	}
}
