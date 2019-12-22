package com.Da_Technomancer.crossroads.API.heat;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatUtil{

	/**
	 * Absolute zero in degrees C
	 */
	public static final double ABSOLUTE_ZERO = -273D;

	public static double toKelvin(double celcius){
		return celcius - ABSOLUTE_ZERO;
	}

	public static double toCelcius(double kelvin){
		return kelvin + ABSOLUTE_ZERO;
	}

	/**
	 * When provided with an array of temperatures in ascending order, finds the index of the highest lower bound of the temperature, or -1 if no such bound exists.
	 * Used to find the operating speed/heat usage of many heat machines based on temperature
	 * @param temp The temperature
	 * @param tempTiers The array of temperatures in ascending order. Must be in the same units (celcius or kelvin) as temp
	 * @return The index of the highest lower bound, or -1 if no such bound is in tempTiers
	 */
	public static int getHeatTier(double temp, int[] tempTiers){
		for(int i = tempTiers.length - 1; i >= 0; i--){
			if(temp >= tempTiers[i]){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Calculates the biome temperature at a location
	 * @param world The world (client or server)
	 * @param pos The position to find the temperature at
	 * @return The biome temperature, in degrees C
	 */
	public static double convertBiomeTemp(World world, BlockPos pos){
		double rawTemp = world.getBiome(pos).getTemperature(pos);
		//This formula was derived with the power of wikipedia and excel spreadsheets to compare biome temperatures to actual real world temperatures.
		//Most people probably wouldn't care if I'd just pulled it out of my *rse, but I made an effort and I want someone to know this. Appreciate it. Please?
		return MiscUtil.betterRound(rawTemp * 17.5D - 2.5D, 3);
	}
}
