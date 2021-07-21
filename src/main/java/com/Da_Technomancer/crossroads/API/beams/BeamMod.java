package com.Da_Technomancer.crossroads.API.beams;

import com.Da_Technomancer.crossroads.API.MiscUtil;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Arrays;

/**
 * An immutable class that represents one beam pulse lasting one cycle. It stores the energy, potential, stability, and void values and has several helper methods
 * For a mutable version, see BeamUnitStorage
 */
public class BeamMod {

	public static final BeamMod EMPTY = new BeamMod(1, 1, 1, 1, 0);

	private final float[] multipliers = new float[5];//0: Energy, 1: Potential, 2: stability, 3: Void, 4: Void Convert

	public BeamMod(float[] mults){
		this(mults[0], mults[1], mults[2], mults[3], mults[4]);
	}

	public BeamMod(float energy, float potential, float stability, float voi, float voiConv){
		multipliers[0] = energy;
		multipliers[1] = potential;
		multipliers[2] = stability;
		multipliers[3] = voi;
		multipliers[4] = voiConv;

		if(energy < 0 || potential < 0 || stability < 0 || voi < 0 || voiConv < 0){
			throw new IllegalArgumentException("Negative BeamMod input! EN: " + energy + "; PO: " + potential + "; ST: " + stability + "; VO: " + voi + "; VO-CONV: " + voiConv);
		}
	}

	public float getEnergyMult(){
		return multipliers[0];
	}

	public float getPotentialMult(){
		return multipliers[1];
	}

	public float getStabilityMult(){
		return multipliers[2];
	}

	public float getVoidMult(){
		return multipliers[3];
	}

	public float getVoidConvert(){
		return multipliers[4];
	}

	public boolean isEmpty(){
		return this == BeamMod.EMPTY ||
				multipliers[0] == 1
				&& multipliers[1] == 1
				&& multipliers[2] == 1
				&& multipliers[3] == 1
				&& multipliers[4] == 0;
	}

	/**
	 * @return A size five array containing energy, potential, stability, void, and void conversion in that order.
	 * Changes to the array will not write back to the BeamMod
	 */
	public float[] getValues(){
		return Arrays.copyOf(multipliers, 5);
	}

	/**
	 * @param u
	 * @return A BeamUnit modified by this set of multipliers and the void conversion factor.
	 */
	public BeamUnit mult(BeamUnit u){
		float energy = u.getEnergy() * getEnergyMult();
		float potential = u.getPotential() * getPotentialMult();
		float stability = u.getStability() * getStabilityMult();
		float voi = u.getVoid() * getVoidMult();

		// Convert a percentage of non-Void colors to Void
		float voiConv = u.getVoid() + (energy + potential + stability) * getVoidConvert();
		energy *= 1 - getVoidConvert();
		potential *= 1 - getVoidConvert();
		stability *= 1 - getVoidConvert();

		voi += voiConv;

		// Numbers are truncated in order to prevent possible positive feedback loops
		// This is necessary since lenses can't simply redirect the excess elsewhere
		return new BeamUnit((int)energy, (int)potential, (int)stability, (int)voi);
	}

	@Override
	public boolean equals(Object other){
		if(other instanceof BeamMod){
			BeamMod o = (BeamMod)other;
			return o == this ||
					o.multipliers[0] == multipliers[0]
					&& o.multipliers[1] == multipliers[1]
					&& o.multipliers[2] == multipliers[2]
					&& o.multipliers[3] == multipliers[3]
					&& o.multipliers[4] == multipliers[4];
		}
		return false;
	}

	public void writeToNBT(@Nonnull String key, CompoundNBT nbt){
		CompoundNBT newNBT = new CompoundNBT();
		newNBT.putFloat("energy", multipliers[0]);
		newNBT.putFloat("potential", multipliers[1]);
		newNBT.putFloat("stability", multipliers[2]);
		newNBT.putFloat("void", multipliers[3]);
		newNBT.putFloat("voidConvert", multipliers[4]);
		nbt.put(key, newNBT);
	}

	public static BeamMod readFromNBT(@Nonnull String key, CompoundNBT nbt){
		if(nbt.contains(key)){
			CompoundNBT compound = nbt.getCompound(key);
			return new BeamMod(
					compound.getFloat("energy"),
					compound.getFloat("potential"),
					compound.getFloat("stability"),
					compound.getFloat("void"),
					compound.getFloat("voidConvert")
			);
		}
		return BeamMod.EMPTY;
	}
}