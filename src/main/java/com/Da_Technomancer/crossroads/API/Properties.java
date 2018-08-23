package com.Da_Technomancer.crossroads.API;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;

public class Properties{

	public static final UnlistedPropertyBooleanSixArray CONNECT = new UnlistedPropertyBooleanSixArray("connect");
	public static final UnlistedPropertyIntegerSixArray CONNECT_MODE = new UnlistedPropertyIntegerSixArray("connect_mode");
	public static final UnlistedPropertyIntegerSixArray PORT_TYPE = new UnlistedPropertyIntegerSixArray("port_type");
	public static final PropertyBool LIGHT = PropertyBool.create("light");
	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	public static final PropertyInteger FULLNESS = PropertyInteger.create("fullness", 0, 3);
	/**true means X axis, false means Z axis*/
	public static final PropertyBool ORIENT = PropertyBool.create("orient");
	/** Depending on context: 
	 * 0: copper, 1: molten copper, 2: cobblestone, 3: lava
	 * 0: copper 1: iron 2: quartz 3: diamond
	 */
	public static final PropertyInteger TEXTURE_4 = PropertyInteger.create("text", 0, 3);
	/**0 = none, 1 = ruby, 2 = emerald, 3 = diamond, 4 = pure quartz, 5 = luminescent quartz, 6 = void crystal */
	public static final PropertyInteger TEXTURE_7 = PropertyInteger.create("text_seven", 0, 6);
	public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);
	public static final PropertyBool CRYSTAL = PropertyBool.create("crystal");
	public static final PropertyDirection HORIZONTAL_FACING = PropertyDirection.create("horiz_facing", (EnumFacing side) -> side != null && side.getAxis() != EnumFacing.Axis.Y);
	public static final PropertyBool CONTAINER_TYPE = PropertyBool.create("container_type");
}
