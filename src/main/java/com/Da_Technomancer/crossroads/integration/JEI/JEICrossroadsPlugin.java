package com.Da_Technomancer.crossroads.integration.JEI;

import com.Da_Technomancer.crossroads.API.alchemy.ReagentManager;
import com.Da_Technomancer.crossroads.Crossroads;
import com.Da_Technomancer.crossroads.blocks.CRBlocks;
import com.Da_Technomancer.crossroads.crafting.CRRecipes;
import com.Da_Technomancer.crossroads.crafting.recipes.IOptionalRecipe;
import com.Da_Technomancer.crossroads.gui.container.DetailedCrafterContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.stream.Collectors;

@JeiPlugin
@SuppressWarnings("unused")
public class JEICrossroadsPlugin implements IModPlugin{

	private static final ResourceLocation PLUGIN_ID = new ResourceLocation(Crossroads.MODID, "jei_plugin");

	@Override
	public ResourceLocation getPluginUid(){
		return PLUGIN_ID;
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry){
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.millstone, 1), MillstoneCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.stampMill, 1), StampMillCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.fluidCoolingChamber, 1), FluidCoolingCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.heatingCrucible, 1), HeatingCrucibleCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.waterCentrifuge, 1), CentrifugeCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.blastFurnace, 1), BlastFurnaceCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.oreCleanser, 1), OreCleanserCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.beamExtractor, 1), BeamExtractorCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.beamReflector, 1), BeamTransmuteCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.detailedCrafter, 1), DetailedCrafterCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.reactionChamberGlass, 1), AlchemyCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.reactionChamberCrystal, 1), AlchemyCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.glasswareHolder, 1), AlchemyCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.copshowiumCreationChamber, 1), CopshowiumCategory.ID);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.lensFrame, 1), BeamLensCategory.ID);

		//Add relevant crossroads machines to vanilla recipe types
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.smelter, 1), VanillaRecipeCategoryUid.FURNACE);
		registry.addRecipeCatalyst(new ItemStack(CRBlocks.steamer, 1), VanillaRecipeCategoryUid.SMOKING);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
		registration.addRecipeTransferHandler(DetailedCrafterContainer.class, DetailedCrafterCategory.ID, 1, 9, 10, 36);
		registration.addRecipeTransferHandler(DetailedCrafterContainer.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration){
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

		registration.addRecipes(getRecipes(recipeManager, CRRecipes.MILL_TYPE), MillstoneCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.STAMP_MILL_TYPE), StampMillCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.FLUID_COOLING_TYPE), FluidCoolingCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.CRUCIBLE_TYPE), HeatingCrucibleCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.CENTRIFUGE_TYPE), CentrifugeCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.BLAST_FURNACE_TYPE), BlastFurnaceCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.ORE_CLEANSER_TYPE), OreCleanserCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.BEAM_EXTRACT_TYPE), BeamExtractorCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.BEAM_TRANSMUTE_TYPE), BeamTransmuteCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.DETAILED_TYPE), DetailedCrafterCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.ALCHEMY_TYPE), AlchemyCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.COPSHOWIUM_TYPE), CopshowiumCategory.ID);
		registration.addRecipes(getRecipes(recipeManager, CRRecipes.BEAM_LENS_TYPE), BeamLensCategory.ID);
		registration.addRecipes(ReagentManager.getRegisteredReags(), ReagInfoCategory.ID);
	}

	private static Collection<?> getRecipes(RecipeManager manage, IRecipeType<?> type){
		//Filter to recipes of the passed type, and check that they're enabled if they're IOptionalRecipe
		return manage.getRecipes().parallelStream().filter(rec -> rec.getType() == type && (!(rec instanceof IOptionalRecipe) || ((IOptionalRecipe<?>) rec).isEnabled())).collect(Collectors.toList());
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registry){
		ReagIngr.populate();
		registry.register(ReagIngr.REAG, ReagIngr.REAG_TYPES, new ReagentIngredientHelper(), ReagentIngredientRenderer.RENDERER);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry){
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(
				new MillstoneCategory(guiHelper),
				new FluidCoolingCategory(guiHelper),
				new HeatingCrucibleCategory(guiHelper),
				new DetailedCrafterCategory(guiHelper),
				new BeamExtractorCategory(guiHelper),
				new AlchemyCategory(guiHelper),
				new ReagInfoCategory(guiHelper),
				new StampMillCategory(guiHelper),
				new OreCleanserCategory(guiHelper),
				new BlastFurnaceCategory(guiHelper),
				new BeamTransmuteCategory(guiHelper),
				new CentrifugeCategory(guiHelper),
				new CopshowiumCategory(guiHelper),
				new BeamLensCategory(guiHelper));
	}

	protected static IDrawableStatic createFluidOverlay(IGuiHelper helper){
//		guiHelper.createDrawable(new ResourceLocation(Crossroads.MODID, "textures/gui/rectangle_fluid_overlay.png"), 0, 0, 16, 64)
		return helper.drawableBuilder(new ResourceLocation(Crossroads.MODID, "textures/gui/rectangle_fluid_overlay.png"), 0, 0, 16, 64).setTextureSize(64, 64).build();
	}
}
