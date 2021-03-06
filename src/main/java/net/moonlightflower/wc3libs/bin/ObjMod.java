package net.moonlightflower.wc3libs.bin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import net.moonlightflower.wc3libs.bin.app.objMod.W3B;
import net.moonlightflower.wc3libs.bin.app.objMod.W3D;
import net.moonlightflower.wc3libs.bin.app.objMod.W3H;
import net.moonlightflower.wc3libs.bin.app.objMod.W3Q;
import net.moonlightflower.wc3libs.bin.app.objMod.W3T;
import net.moonlightflower.wc3libs.bin.app.objMod.W3U;
import net.moonlightflower.wc3libs.bin.app.objMod.W3A;
import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.app.War3Int;
import net.moonlightflower.wc3libs.dataTypes.app.War3Real;
import net.moonlightflower.wc3libs.dataTypes.app.War3String;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.Id;
import net.moonlightflower.wc3libs.misc.MetaFieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import net.moonlightflower.wc3libs.slk.MetaSLK;
import net.moonlightflower.wc3libs.slk.RawSLK;
import net.moonlightflower.wc3libs.slk.SLK;
import net.moonlightflower.wc3libs.slk.app.meta.CommonMetaSLK;
import net.moonlightflower.wc3libs.txt.Profile;
import net.moonlightflower.wc3libs.txt.TXTSectionId;

;import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * base class for object modification files
 */
public abstract class ObjMod {
	public static class Obj {
		public static class Field {
			public enum ValType {
				INT(0),
				REAL(1),
				UNREAL(2),
				STRING(3);
				
				private int _val;
				
				public int getVal() {
					return _val;
				}
				
				private final static Map<Integer, ValType> _map = new LinkedHashMap<>();
				
				static public ValType valueOf(int val) {
					return _map.get(val);
				}
				
				ValType(int val) {
					_val = val;
				}
				
				static {
					for (ValType varType : ValType.values()) {
						_map.put(varType.getVal(), varType);
					}
				}
			}
			
			public static class Val {
				private DataType _val;
				private ValType _valType;
				
				public DataType getVal() {
					return _val;
				}
				
				@Override
				public String toString() {
					return getVal().toString();
				}
				
				public ValType getType() {
					return _valType;
				}
				
				private Val(DataType val, ValType valType) {
					_val = val;
					_valType = valType;
				}
				
				public static Val valueOf(int val) {
					return new Val(War3Int.valueOf(val), ValType.INT);
				}
				
				public static Val valueOf(float val, boolean unreal) {
					if (unreal) return new Val(War3Real.valueOf(val), ValType.UNREAL);
					
					return new Val(War3Real.valueOf(val), ValType.REAL);
				}
				
				public static Val valueOf(String val) {
					return new Val(War3String.valueOf(val), ValType.STRING);
				}
			}
			
			private final Map<Integer, Val> _vals = new LinkedHashMap<>();
			
			public Map<Integer, Val> getVals() {
				return _vals;
			}
			
			public Val getVal(int level) {
				return _vals.get(level);
			}
			
			public Val getVal() {
				return getVal(0);
			}
			
			public void setVal(Val val, int level, int dataPt) {
				_vals.put(level, val);
				setDataPt(dataPt);
			}
			
			public void setVal(@Nullable Val val, int level) {
				setVal(val, level, 0);
			}
			
			public void setVal(@Nullable Val val) {
				setVal(val, 0);
			}
			
			public void addVal(@Nullable Val val) {
				int level = 1;

				while (getVal(level) == null) {
					level++;
				}
				
				setVal(val, level);
			}
			
			public void deleteVal(int level) {
				_vals.remove(level);
			}
			
			private int _dataPt;
			
			public int getDataPt() {
				return _dataPt;
			}
			
			public void setDataPt(int val) {
				_dataPt = val;
			}
			
			public void merge(@Nonnull Field otherField) {
				setDataPt(otherField.getDataPt());
				
				for (Entry<Integer, Val> valEntry : otherField.getVals().entrySet()) {
					Integer level = valEntry.getKey();
					Val val = valEntry.getValue();
					
					setVal(val, level);
				}
			}
			
			public void print() {
				System.out.println(String.format("\t%s", getId()));
				
				for (Entry<Integer, Val> valEntry : getVals().entrySet()) {
					int level = valEntry.getKey();
					Val val = valEntry.getValue();
					
					System.out.println(String.format("\t\t%d -> val:%s valType:%s dataPt:%s", level, getVal(level), val.getType(), getDataPt()));
				}
			}
			
			private MetaFieldId _id;
			
			public MetaFieldId getId() {
				return _id;
			}
			
			@Override
			public String toString() {
				return getId().toString();
			}
			
			public Field(@Nonnull MetaFieldId id) {
				_id = id;
			}
		}

		private Map<MetaFieldId, Field> _fieldsMap = new LinkedHashMap<>();
		private List<Field> _fieldsList = new ArrayList<>();
		
		public Map<MetaFieldId, Field> getFields() {
			return new LinkedHashMap<>(_fieldsMap);
		}
		
		public List<Field> getFieldsList() {
			return new ArrayList<>(_fieldsList);
		}
		
		public Field getField(@Nonnull MetaFieldId id) {
			return _fieldsMap.get(id);
		}
		
		public Field addField(@Nonnull MetaFieldId id) {
			if (getFields().containsKey(id)) return getFields().get(id);
			
			Field field = new Field(id);
			
			_fieldsMap.put(id, field);
			_fieldsList.add(field);
			
			return field;
		}
		
		public void removeField(@Nonnull MetaFieldId id) {
			if (!_fieldsMap.containsKey(id)) return;
			
			Field field = _fieldsMap.get(id);
			
			_fieldsMap.remove(id);
			_fieldsList.remove(field);
		}
		
		public void remove() {
			/*objs:remove(obj)

			objsById[id] = nil

			if custom then
				customObjs:remove(obj)
			else
				origObjs:remove(obj)
			end*/
		}
		
		public void merge(@Nonnull Obj otherObj) {
			for (Field otherField : otherObj.getFields().values()) {
				MetaFieldId fieldId = otherField.getId();

				Field field = addField(fieldId);
				
				field.merge(otherField);
			}
		}
		
		private ObjId _id;

		@Nonnull
		public ObjId getId() {
			return _id;
		}
		
		private ObjId _baseId;

		@Nullable
		public ObjId getBaseId() {
			return _baseId;
		}

		private ObjId _newId;

		@Nullable
		public ObjId getNewId() {
			return _newId;
		}
		
		@Override
		public String toString() {
			if (getBaseId() == null) return String.format("%s", getId().toString());
			
			return String.format("%s (%s)", getId().toString(), getBaseId().toString());
		}

		public void print() {
			System.out.println(String.format("%s %s", getId(), getBaseId()));
			
			for (Field field : getFields().values()) {
				field.print();
			}
		}
		
		private void read_0x1(@Nonnull Wc3BinInputStream stream, boolean extended) throws BinInputStream.StreamException {
			_baseId = ObjId.valueOf(stream.readId("baseId"));
			_newId = ((Function<Id, ObjId>) id -> {
				if (id.equals(Id.valueOf("\0\0\0\0"))) return null;

				return ObjId.valueOf(id);
			}).apply(stream.readId("newId"));

			_id = (_newId == null) ? _baseId : _newId;

			int modsAmount = stream.readInt32("modsAmount");
			
			for (int i = 0; i < modsAmount; i++) {
				Field field = addField(MetaFieldId.valueOf(stream.readId("fieldId")));
				
				int varTypeI = stream.readInt32("varType");

				Field.ValType varType = Field.ValType.valueOf(varTypeI);
				
				int level = 0;
				int dataPt = 0;

				if (extended) {
					level = stream.readInt32("level/variation");
					dataPt = stream.readInt32("dataPt");
				}

				Field.Val val;

				switch (varType) {
				case INT: {
					val = Field.Val.valueOf(stream.readInt32("val (int)"));
					
					break;
				}
				case REAL: {
					val = Field.Val.valueOf(stream.readFloat32("val (real)"), false);
					
					break;
				}
				case UNREAL: {
					val = Field.Val.valueOf(stream.readFloat32("val (unreal)"), true);
					
					break;
				}
				case STRING: {
					val = Field.Val.valueOf(stream.readString("val (string) "));
					
					break;
				}
				default: {
					val = Field.Val.valueOf(stream.readString("val (string default)"));
				}
				}
				
				field.setVal(val, level, dataPt);
				
				stream.readId("endToken");
			}
		}

		private void read_0x2(@Nonnull Wc3BinInputStream stream, boolean extended) throws BinInputStream.StreamException {
			_baseId = ObjId.valueOf(stream.readId("baseId"));
			_newId = ((Function<Id, ObjId>) id -> {
				if (id.equals(Id.valueOf("\0\0\0\0"))) return null;

				return ObjId.valueOf(id);
			}).apply(stream.readId("newId"));

			_id = (_newId == null) ? _baseId : _newId;

			int modsAmount = stream.readInt32("modsAmount");

			for (int i = 0; i < modsAmount; i++) {				
				Field field = addField(MetaFieldId.valueOf(stream.readId("fieldId")));

				Field.ValType varType = Field.ValType.valueOf(stream.readInt32("varType"));
				
				int level = 0;
				int dataPt = 0;

				if (extended) {
					level = stream.readInt32("level/variation");
					dataPt = stream.readInt32("dataPt");
				}

				Field.Val val;
				
				switch (varType) {
				case INT: {
					val = Field.Val.valueOf(stream.readInt32("val (int)"));
					
					break;
				}
				case REAL: {
					val = Field.Val.valueOf(stream.readFloat32("val (real)"), false);
					
					break;
				}
				case UNREAL: {
					val = Field.Val.valueOf(stream.readFloat32("val (unreal)"), true);
					
					break;
				}
				case STRING: {
					val = Field.Val.valueOf(stream.readString("val (string)"));
					
					break;
				}
				default: {
					val = Field.Val.valueOf(stream.readString("val (string default)"));
				}
				}

				field.setVal(val, level, dataPt);

				stream.readId("endToken");
			}
		}
		
		private void write_0x1(@Nonnull Wc3BinOutputStream stream, boolean extended) {
			stream.writeId((_baseId == null) ? _id : _baseId);
			stream.writeId((_newId == null) ? Id.valueOf("\0\0\0\0") : _newId);
			
			int modsAmount = 0;
			
			for (Field field : getFields().values()) {
				modsAmount += field.getVals().size();
			}

			stream.writeInt32(modsAmount);
			
			for (int i = 0; i < getFieldsList().size(); i++) {
				Field field = getFieldsList().get(i);
				
				MetaFieldId id = field.getId();
				int dataPt = field.getDataPt();
				
				for (Map.Entry<Integer, Field.Val> valEntry : field.getVals().entrySet()) {
					int level = valEntry.getKey();
					Field.Val val = valEntry.getValue();

					stream.writeId(id);
					
					Field.ValType valType = val.getType();
					
					stream.writeInt32(valType.getVal());

					if (extended) {
						stream.writeInt32(level);
						stream.writeInt32(dataPt);
					}

					switch (valType) {
					case INT: {
						War3Int valSpec = (War3Int) val.getVal();
						
						stream.writeInt32(valSpec.toInt());
						
						break;
					}
					case REAL: {
						War3Real valSpec = (War3Real) val.getVal();
						
						stream.writeFloat32(valSpec.toFloat());
						
						break;
					}
					case UNREAL: {
						War3Real valSpec = (War3Real) val.getVal();
						
						stream.writeFloat32(valSpec.toFloat());
						
						break;
					}
					case STRING: {
						War3String valSpec = (War3String) val.getVal();
						
						stream.writeString(valSpec.getVal());
						
						break;
					}
					default: {
						stream.writeString(val.getVal().toString());
					}
					}
					
					stream.writeId(null); //endToken
				}
			}
		}
		
		private void write_0x2(@Nonnull Wc3BinOutputStream stream, boolean extended) {
			stream.writeId((_baseId == null) ? _id : _baseId);
			stream.writeId((_newId == null) ? Id.valueOf("\0\0\0\0") : _newId);

			int modsAmount = 0;

			for (Field field : getFields().values()) {
				modsAmount += field.getVals().size();
			}

			stream.writeInt32(modsAmount);
			
			for (int i = 0; i < getFieldsList().size(); i++) {
				Field field = getFieldsList().get(i);
				
				MetaFieldId id = field.getId();
				int dataPt = field.getDataPt();

				for (Map.Entry<Integer, Field.Val> valEntry : field.getVals().entrySet()) {
					int level = valEntry.getKey();
					Field.Val val = valEntry.getValue();
					
					stream.writeId(id);
					
					Field.ValType valType = val.getType();
					
					stream.writeInt32(valType.getVal());

					if (extended) {
						stream.writeInt32(level);
						stream.writeInt32(dataPt);
					}

					switch (valType) {
					case INT: {
						War3Int valSpec = (War3Int) val.getVal();
						
						stream.writeInt32(valSpec.toInt());
						
						break;
					}
					case REAL: {						
						War3Real valSpec = (War3Real) val.getVal();

						stream.writeFloat32(valSpec.toFloat());
						
						break;
					}
					case UNREAL: {
						War3Real valSpec = (War3Real) val.getVal();
						
						stream.writeFloat32(valSpec.toFloat());
						
						break;
					}
					case STRING: {
						War3String valSpec = (War3String) val.getVal();
						
						stream.writeString(valSpec.getVal());
						
						break;
					}
					default: {
						stream.writeString(val.getVal().toString());
					}
					}
					
					stream.writeId(null); //endToken
				}
			}
		}
		
		public void read(@Nonnull Wc3BinInputStream stream, @Nonnull EncodingFormat format, boolean extended) throws BinInputStream.StreamException {
			try {
				switch (format.toEnum()) {
				case OBJ_0x1: {
					read_0x1(stream, extended);
					
					break;
				}
				case OBJ_0x2: {
					read_0x2(stream, extended);
					
					break;
				}
				}
			} catch (RuntimeException e) {
				throw new BinInputStream.StreamException(stream);
			}
		}
		
		public void write(@Nonnull Wc3BinOutputStream stream, @Nonnull EncodingFormat format, boolean extended) {
			switch (format.toEnum()) {
			case AUTO:
			case OBJ_0x2: {
				write_0x2(stream, extended);
				
				break;
			}
			case OBJ_0x1: {
				write_0x1(stream, extended);
				
				break;
			}
			}
		}

		private Obj copy() {
			Obj ret = new Obj(_id, _baseId);

			ret._newId = _newId;

			ret.merge(this);

			return ret;
		}
		
		public Obj(@Nonnull Wc3BinInputStream stream, @Nonnull EncodingFormat format, boolean extended) throws BinInputStream.StreamException {
			read(stream, format, extended);
		}
		
		public Obj(@Nonnull ObjId id, @Nullable ObjId baseId) {
			_id = id;
			_baseId = baseId;
		}
	}
	
	protected final Map<ObjId, Obj> _objs = new LinkedHashMap<>();
	private final List<Obj> _objsList = new ArrayList<>();

	@Nonnull
	public Map<ObjId, ? extends Obj> getObjs() {
		return _objs;
	}

	@Nonnull
	public List<Obj> getObjsList() {
		return _objsList;
	}

	@Nonnull
	public List<Obj> getOrigObjs() {
		List<Obj> ret = new ArrayList<>();
		
		for (int i = 0; i < getObjsList().size(); i++) {
			Obj obj = getObjsList().get(i);
			
			if (obj.getNewId() == null) {
				ret.add(obj);
			}
		}
		
		return ret;
	}

	@Nonnull
	public List<Obj> getCustomObjs() {
		List<Obj> ret = new ArrayList<>();

		for (int i = 0; i < getObjsList().size(); i++) {
			Obj obj = getObjsList().get(i);

			if (obj.getNewId() != null) {
				ret.add(obj);
			}
		}

		return ret;
	}

	public boolean containsObj(@Nonnull ObjId id) {
		return _objs.containsKey(id);
	}

	@Nullable
	public Obj getObj(@Nonnull ObjId id) {
		return getObjs().get(id);
	}
	
	private void addObj(@Nonnull Obj val) {
		_objs.put(val.getId(), val);
		_objsList.add(val);
	}
	
	public void removeObj(@Nonnull Obj val) {
		_objs.remove(val.getId());
		_objsList.remove(val);
	}

	public void removeObj(@Nonnull ObjId id) {
		Obj obj = getObj(id);
		
		if (obj != null) {
			removeObj(obj);
		}
	}
	
	public void clearObjs() {
		_objs.clear();
		_objsList.clear();
	}
	
	public Obj addObj(@Nonnull ObjId id, @Nullable ObjId baseId) {
		if (getObjs().containsKey(id)) return getObjs().get(id);
		
		Obj obj = new Obj(id, baseId);
	
		addObj(obj);
		
		return obj;
	}
	
	public void merge(@Nonnull ObjMod other) {
		for (Obj otherObj : other.getObjs().values()) {
			Obj obj = otherObj.copy();

			addObj(obj);
		}
	}

	@Nonnull
	public abstract ObjMod copy();
	
	public void print() {
		for (Obj obj : getObjs().values()) {
			obj.print();
		}
	}
	
	public static class ObjPack {
		private Map<ObjId, ObjId> _baseObjIds = new LinkedHashMap<>();
		
		public Map<ObjId, ObjId> getBaseObjIds() {
			return _baseObjIds;
		}
		
		private Map<File, SLK> _slks = new LinkedHashMap<>();
		
		public Map<File, SLK> getSlks() {
			return _slks;
		}
		
		private Profile _profile = new Profile();
		
		public Profile getProfile() {
			return _profile;
		}
		
		private ObjMod _objMod;
		
		public ObjMod getObjMod() {
			return _objMod;
		}
		
		private ObjPack(@Nonnull ObjMod orig) {
			_objMod = orig.copy();

			for (Obj obj : _objMod.getCustomObjs()) {
				_baseObjIds.put(obj.getId(), obj.getBaseId());
			}
		}
	}

	public abstract Collection<File> getSLKs();
	public abstract Collection<File> getNecessarySLKs();

	@Nonnull
	public ObjPack reduce(@Nonnull MetaSLK reduceMetaSlk, Collection<Class<? extends ObjMod>> excludedClasses) throws Exception {
		ObjPack pack = new ObjPack(this);

		if (excludedClasses.contains(getClass())) return pack;
		
		Map<File, SLK> outSlks = pack.getSlks();
		Profile outProfile = pack.getProfile();
		ObjMod outObjMod = pack.getObjMod();
		
		for (Obj obj : getObjs().values()) {
			ObjId objId = obj.getId();
			/*if(objId.toString().equals("n01M")) {
				System.out.println("yay");
			}*/
			//
			//outObjMod.getObj(objId).removeField(MetaFieldId.valueOf("wurs"));

			//if (!objId.toString().matches("[0-9A-za-z]{4}")) continue;

			for (Obj.Field field : obj.getFields().values()) {
				MetaFieldId fieldId = field.getId();

				ObjId metaObjId = ObjId.valueOf(fieldId);
				MetaSLK.Obj metaObj = reduceMetaSlk.getObj(metaObjId);

				if (metaObj != null) {
					String slkName = metaObj.getS(FieldId.valueOf("slk"));

					String slkFieldName = metaObj.getS(FieldId.valueOf("field"));

					if (slkName.equals("Profile")) {
						for (Entry<Integer, Obj.Field.Val> valEntry : field.getVals().entrySet()) {
							int level = valEntry.getKey();
							Obj.Field.Val val = valEntry.getValue();

							int index = (level == 0) ? 0 : (level - 1);
							int metaIndex = War3Int.valueOf(metaObj.get(FieldId.valueOf("index"), War3Int.valueOf(0))).getVal();

							if (metaIndex > 0) {
								index += metaIndex;
							}

							Profile.Obj profileObj = outProfile.addObj(TXTSectionId.valueOf(objId.toString()));

							for (File necessarySlkFile : getNecessarySLKs()) {
								SLK necessarySlk = outSlks.computeIfAbsent(necessarySlkFile, k -> new RawSLK());

								necessarySlk.addObj(objId);
							}
							
							FieldId profileFieldId = FieldId.valueOf(slkFieldName);

							Profile.Obj.Field profileField = profileObj.addField(profileFieldId);
							DataType profileVal = null;

							if (metaObj.getS(FieldId.valueOf("type")).endsWith("List")) {
								//split up list types like stringList
								profileVal = val.getVal();

								if (profileVal != null) {
									String[] vals = profileVal.toString().split(",");

									for (int i = 0; i < vals.length; i++) {
										profileField.set(War3String.valueOf(vals[i]), index + i);
									}
								}
							} else {
								if (val.getType().equals(Obj.Field.ValType.INT)) {
									profileVal = War3Int.valueOf(val.getVal());
								} else if (val.getType().equals(Obj.Field.ValType.REAL)) {
									profileVal = War3Real.valueOf(val.getVal());
								} else if (val.getType().equals(Obj.Field.ValType.UNREAL)) {
									profileVal = War3Real.valueOf(val.getVal());
								} else {
									profileVal = War3String.valueOf(val.getVal());
								}

								profileField.set(profileVal, index);
							}
						}

						outObjMod.getObj(objId).removeField(fieldId);
					} else {
						File slkFile = CommonMetaSLK.convertSLKName(slkName);

						if (slkFile == null) throw new RuntimeException("no slkFile for name " + slkName);

						SLK outSlk = outSlks.computeIfAbsent(slkFile, k -> new RawSLK());

						for (Entry<Integer, Obj.Field.Val> valEntry : field.getVals().entrySet()) {
							int level = valEntry.getKey();
							Obj.Field.Val val = valEntry.getValue();

							FieldId slkFieldIdAdjusted = CommonMetaSLK.getSLKField(slkFile, metaObj, level);

							outSlk.addField(slkFieldIdAdjusted);

							SLK.Obj slkObj = outSlk.addObj(objId);

							for (File necessarySlkFile : getNecessarySLKs()) {
								SLK necessarySlk = outSlks.computeIfAbsent(necessarySlkFile, k -> new RawSLK());

								necessarySlk.addObj(objId);
							}
							
							/*if (slkFile.equals(UnitBalanceSLK.GAME_PATH) || slkFile.equals(UnitAbilsSLK.GAME_PATH) || slkFile.equals(UnitUISLK.GAME_PATH) || slkFile.equals(UnitWeaponsSLK.GAME_PATH)) {
								File unitBaseSLKFile = UnitDataSLK.GAME_PATH;

								SLK unitBaseSLK = outSlks.computeIfAbsent(unitBaseSLKFile, k -> new RawSLK());

								unitBaseSLK.addCamera(objId);

								//

								File unitAbilSLKFile = UnitAbilsSLK.GAME_PATH;

								SLK unitAbilSLK = outSlks.computeIfAbsent(unitAbilSLKFile, k -> new RawSLK());

								unitAbilSLK.addCamera(objId);
							}*/

							slkObj.set(slkFieldIdAdjusted, War3String.valueOf(val));

							outObjMod.getObj(objId).removeField(fieldId);
						}
					}
				}
			}

			Obj outObj = outObjMod.getObj(objId);

			if (outObj != null) {
				outObj.removeField(MetaFieldId.valueOf("wurs"));

				if (outObj.getFields().isEmpty()) {
					outObjMod.removeObj(objId);
				} else {
					outObjMod.removeObj(objId);

					outObjMod.addObj(objId, null).merge(outObj);
				}
			}
		}

		for (Map.Entry<File, SLK> slkEntry : outSlks.entrySet()) {
			SLK convSlk = SLK.createFromInFile(slkEntry.getKey(), slkEntry.getValue());

			outSlks.put(slkEntry.getKey(), convSlk);
		}

		return pack;
	}
	
	public static class EncodingFormat extends Format<EncodingFormat.Enum> {
		public enum Enum {
			AUTO,
			OBJ_0x1,
			OBJ_0x2
		}

		private final static Map<Integer, EncodingFormat> _map = new LinkedHashMap<>();

		public final static EncodingFormat AUTO = new EncodingFormat(Enum.AUTO, -1);
		public final static EncodingFormat OBJ_0x1 = new EncodingFormat(Enum.OBJ_0x1, 0x1);
		public final static EncodingFormat OBJ_0x2 = new EncodingFormat(Enum.OBJ_0x2, 0x2);

		@Nullable
		public static EncodingFormat valueOf(int version) {
			return _map.get(version);
		}

		private EncodingFormat(@Nonnull Enum enumVal, int version) {
			super(enumVal, version);

			_map.put(version, this);
		}
	}

	private void read_0x1(@Nonnull Wc3BinInputStream stream, boolean extended) throws BinInputStream.StreamException {
		int version = stream.readInt32("version");

		stream.checkFormatVersion(EncodingFormat.OBJ_0x1.getVersion(), version);

		int origObjsAmount = stream.readInt32("origObjsAmount");
		
		for (int i = 0; i < origObjsAmount; i++) {
			ObjId baseId = ObjId.valueOf(stream.readId("baseId"));
			ObjId id = ObjId.valueOf(stream.readId("objId"));
			
			Obj obj = new Obj(id, null);
			
			obj.read(stream, EncodingFormat.OBJ_0x1, extended);
			
			addObj(obj);
		}
		
		int customObjsAmount = stream.readInt32("customObjsAmount");

		for (int i = 0; i < customObjsAmount; i++) {
			ObjId baseId = ObjId.valueOf(stream.readId("baseId"));
			ObjId id = ObjId.valueOf(stream.readId("objId"));
			
			Obj obj = new Obj(baseId, baseId);
			
			obj.read(stream, EncodingFormat.OBJ_0x1, extended);
			
			addObj(obj);
		}
	}

	private void read_0x2(@Nonnull Wc3BinInputStream stream, boolean extended) throws BinInputStream.StreamException {
		int version = stream.readInt32("version");

		stream.checkFormatVersion(EncodingFormat.OBJ_0x2.getVersion(), version);

		int origObjsAmount = stream.readInt32("origObjsAmount");

		for (int i = 0; i < origObjsAmount; i++) {
			Obj obj = new Obj(stream, EncodingFormat.OBJ_0x2, extended);
			
			addObj(obj);
		}
		
		int customObjsAmount = stream.readInt32("customObjsAmount");

		for (int i = 0; i < customObjsAmount; i++) {
			Obj obj = new Obj(stream, EncodingFormat.OBJ_0x2, extended);
		
			addObj(obj);
		}
	}
	
	private void write_0x1(@Nonnull Wc3BinOutputStream stream, boolean extended) {
		stream.writeInt32(EncodingFormat.OBJ_0x1.getVersion());
		
		stream.writeInt32(getOrigObjs().size());

		for (Obj obj : getOrigObjs()) {
			obj.write(stream, EncodingFormat.OBJ_0x1, extended);
		}
		
		stream.writeInt32(getCustomObjs().size());
		
		for (Obj obj : getCustomObjs()) {
			obj.write(stream, EncodingFormat.OBJ_0x1, extended);
		}
	}
	
	private void write_0x2(@Nonnull Wc3BinOutputStream stream, boolean extended) {
		stream.writeInt32(EncodingFormat.OBJ_0x2.getVersion());

		stream.writeInt32(getOrigObjs().size());
		
		for (int i = 0; i < getOrigObjs().size(); i++) {
			Obj obj = getOrigObjs().get(i);
			
			obj.write(stream, EncodingFormat.OBJ_0x2, extended);
		}
		
		stream.writeInt32(getCustomObjs().size());
		
		for (int i = 0; i < getCustomObjs().size(); i++) {
			Obj obj = getCustomObjs().get(i);
			
			obj.write(stream, EncodingFormat.OBJ_0x2, extended);
		}
	}
	
	private void read_auto(@Nonnull Wc3BinInputStream stream, boolean extended) throws BinInputStream.StreamException {
		int version = stream.readInt32("version");
		
		stream.rewind();

		EncodingFormat format = EncodingFormat.valueOf(version);

		if (format == null) throw new IllegalArgumentException("unknown format " + version);

		read(stream, format, extended);
	}
	
	public void read(@Nonnull Wc3BinInputStream stream, @Nonnull EncodingFormat format, boolean extended) throws BinInputStream.StreamException {
		switch (format.toEnum()) {
		case AUTO: {
			read_auto(stream, extended);
			
			break;
		}
		case OBJ_0x1: {
			read_0x1(stream, extended);
			
			break;
		}
		case OBJ_0x2: {
			read_0x2(stream, extended);
			
			break;
		}
		}
	}

	public void read(@Nonnull Wc3BinInputStream stream, boolean extended) throws IOException {
		read(stream, EncodingFormat.AUTO, extended);
	}
	
	public void read(@Nonnull InputStream inStream, boolean extended) throws IOException {
		read(new Wc3BinInputStream(inStream), EncodingFormat.AUTO, extended);
	}
	
	public void write(@Nonnull Wc3BinOutputStream stream, @Nonnull EncodingFormat format, boolean extended) {
		switch (format.toEnum()) {
		case AUTO:
		case OBJ_0x2: {
			write_0x2(stream, extended);
			
			break;
		}
		case OBJ_0x1: {
			write_0x1(stream, extended);
			
			break;
		}
		}
	}

	public void write(@Nonnull Wc3BinOutputStream stream, boolean extended) {
		write(stream, EncodingFormat.AUTO, extended);
	}

	public ObjMod(@Nonnull Wc3BinInputStream stream, boolean extended) throws IOException {
		read(stream, extended);
	}

	public ObjMod(@Nonnull File file, boolean extended) throws IOException {
		Wc3BinInputStream inStream = new Wc3BinInputStream(file);

		read(inStream, extended);

		inStream.close();
	}
	
	public ObjMod() {
	}

	@Nullable
	public static ObjMod createFromInFile(@Nonnull File inFile, @Nonnull File outFile) throws Exception {
		ObjMod ret = null;

		if (inFile.equals(W3A.GAME_PATH)) {
			ret = new W3A(outFile);
		}
		if (inFile.equals(W3B.GAME_PATH)) {
			ret = new W3B(outFile);
		}
		if (inFile.equals(W3D.GAME_PATH)) {
			ret = new W3D(outFile);
		}
		if (inFile.equals(W3H.GAME_PATH)) {
			ret = new W3H(outFile);
		}
		if (inFile.equals(W3Q.GAME_PATH)) {
			ret = new W3Q(outFile);
		}
		if (inFile.equals(W3T.GAME_PATH)) {
			ret = new W3T(outFile);
		}
		if (inFile.equals(W3U.GAME_PATH)) {
			ret = new W3U(outFile);
		}
		
		return ret;
	}

	@Nullable
	public static ObjMod createFromInFile(@Nonnull File inFile) {
		ObjMod ret = null;

		if (inFile.equals(W3A.GAME_PATH)) {
			ret = new W3A();
		}
		if (inFile.equals(W3B.GAME_PATH)) {
			ret = new W3B();
		}
		if (inFile.equals(W3D.GAME_PATH)) {
			ret = new W3D();
		}
		if (inFile.equals(W3H.GAME_PATH)) {
			ret = new W3H();
		}
		if (inFile.equals(W3Q.GAME_PATH)) {
			ret = new W3Q();
		}
		if (inFile.equals(W3T.GAME_PATH)) {
			ret = new W3T();
		}
		if (inFile.equals(W3U.GAME_PATH)) {
			ret = new W3U();
		}
		
		return ret;
	}
}