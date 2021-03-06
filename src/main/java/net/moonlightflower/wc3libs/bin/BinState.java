package net.moonlightflower.wc3libs.bin;

import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.DataTypeInfo;
import net.moonlightflower.wc3libs.misc.State;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BinState<T extends DataType> extends State<T> {
	public BinState(@Nonnull String fieldIdS, @Nonnull DataTypeInfo typeInfo, @Nullable T defVal) {
		super(fieldIdS, typeInfo, defVal);
	}

	public BinState(@Nonnull String fieldIdS, @Nonnull DataTypeInfo typeInfo) {
		this(fieldIdS, typeInfo, (T) typeInfo.getDefVal());
	}

	public BinState(@Nonnull String idString, @Nonnull Class<T> type) {
		this(idString, new DataTypeInfo(type));
	}

	public BinState(@Nonnull String idString, @Nonnull Class<T> type, @Nullable T defVal) {
		this(idString, new DataTypeInfo(type), defVal);
	}
}
