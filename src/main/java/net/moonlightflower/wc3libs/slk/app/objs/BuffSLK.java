package net.moonlightflower.wc3libs.slk.app.objs;

import net.moonlightflower.wc3libs.dataTypes.DataType;
import net.moonlightflower.wc3libs.dataTypes.DataTypeInfo;
import net.moonlightflower.wc3libs.dataTypes.app.*;
import net.moonlightflower.wc3libs.misc.FieldId;
import net.moonlightflower.wc3libs.misc.ObjId;
import net.moonlightflower.wc3libs.slk.ObjSLK;
import net.moonlightflower.wc3libs.slk.SLK;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BuffSLK extends ObjSLK<BuffSLK, BuffId, BuffSLK.Obj> {
	public final static File GAME_PATH = new File("Units\\AbilityBuffData.slk");

	public static class State<T extends DataType> extends ObjSLK.State<T> {
		public final static State<BuffId> OBJ_ID = new State<>("alias", BuffId.class);
		public final static State<BuffCode> DATA_CODE = new State<>("code", BuffCode.class);
		public final static State<War3String> EDITOR_COMMENTS = new State<>("comments", War3String.class);
		public final static State<War3Bool> DATA_IS_EFFECT = new State<>("isEffect", War3Bool.class);
		public final static State<War3Int> EDITOR_VERSION = new State<>("version", War3Int.class);
		public final static State<War3Bool> EDITOR_IN_EDITOR = new State<>("useInEditor", War3Bool.class);
		public final static State<War3String> EDITOR_SORT = new State<>("sort", War3String.class);
		public final static State<UnitRace> DATA_RACE = new State<>("race", UnitRace.class);
		public final static State<War3Bool> EDITOR_IN_BETA = new State<>("InBeta", War3Bool.class);

		public State(@Nonnull String idString, @Nonnull DataTypeInfo typeInfo, @Nullable T defVal) {
			super(idString, typeInfo, defVal);
		}

		public State(@Nonnull String idString, @Nonnull DataTypeInfo typeInfo) {
			super(idString, typeInfo);
		}

		public State(@Nonnull String idString, @Nonnull Class<T> type) {
			super(idString, type);
		}

		public State(@Nonnull String idString, @Nonnull Class<T> type, @Nullable T defVal) {
			super(idString, type, defVal);
		}
	}
	
	public static class Obj extends SLK.Obj<BuffId> {
		private final Map<State, DataType> _stateVals = new LinkedHashMap<>();

		@Override
		public Map<State, DataType> getStateVals() {
			return new LinkedHashMap<>(_stateVals);
		}

		@Override
		protected void on_set(@Nonnull FieldId fieldId, @Nullable DataType val) {
			State state = (State) State.valueByField(State.class, fieldId);

			if (state != null) _stateVals.put(state, val);
		}

		@Override
		protected void on_remove(@Nonnull FieldId fieldId) {
			State state = (State) State.valueByField(State.class, fieldId);

			if (state != null) _stateVals.remove(state);
		}

		@Override
		protected void on_clear() {
			_stateVals.clear();
		}

		public <T extends DataType> T get(State<T> state) throws DataTypeInfo.CastException {
			if (!super.contains(state.getFieldId())) return state.getDefVal();

			return state.tryCastVal(super.get(state));
		}
		
		public <T extends DataType> void set(State<T> state, T val) {
			super.set(state, val);
		}
		
		private void read(SLK.Obj<? extends ObjId> slkObj) {
			merge(slkObj, true);
		}
		
		public Obj(SLK.Obj<? extends ObjId> slkObj) {
			super(BuffId.valueOf(slkObj.getId()));
			
			read(slkObj);
		}
		
		public Obj(BuffId id) {
			super(id);

			/*for (State<?> state : State.values(State.class)) {
				set(state, state.getDefVal());
			}*/
		}

		@Override
		public void reduce() {
			// TODO Auto-generated method stub
			
		}
	}
	
	//private Map<BuffId, Camera> _objs = new LinkedHashMap<>();
	
	@Nonnull
	@Override
	public Map<BuffId, Obj> getObjs() {
		return _objs;
	}
	
	@Override
	public void addObj(@Nonnull Obj val) {
		_objs.put(val.getId(), val);
	}
	
	@Nonnull
	@Override
	public Obj addObj(@Nonnull BuffId id) {
		if (_objs.containsKey(id)) return _objs.get(id);
		
		Obj obj = new Obj(id);
		
		addObj(obj);
		
		return obj;
	}
	
	@Override
	protected void read(@Nonnull SLK<?, ? extends ObjId, ? extends SLK.Obj<? extends ObjId>> slk) {
		for (Entry<? extends ObjId, ? extends SLK.Obj<? extends ObjId>> slkEntry : slk.getObjs().entrySet()) {
			ObjId id = slkEntry.getKey();
			SLK.Obj<? extends ObjId> slkObj = slkEntry.getValue();
			
			Obj obj = new Obj(slkObj);

			addObj(obj);
		}
	}
	
	@Override
	public void read(@Nonnull File file) throws IOException {
		super.read(file);
	}
	
	@Override
	public void write(@Nonnull File file) throws IOException {
		super.write(file);
	}
	
	public BuffSLK(SLK slk) {
		read(slk);
	}
	
	public BuffSLK() {
		super();
		
		addField(State.OBJ_ID);
		
		for (State state : State.values(State.class)) {
			addField(state);
		}
	}
	
	public BuffSLK(File file) throws IOException {
		this();
		
		read(file);
	}

	@Nonnull
	@Override
	public Obj createObj(@Nonnull ObjId id) {
		return new Obj(BuffId.valueOf(id));
	}

	@Override
	public void merge(@Nonnull BuffSLK other, boolean overwrite) {
		for (Map.Entry<BuffId, Obj> objEntry : other.getObjs().entrySet()) {
			BuffId objId = objEntry.getKey();
			Obj otherObj = objEntry.getValue();
			
			Obj obj = addObj(objId);
			
			obj.merge(otherObj);
		}
	}
}
