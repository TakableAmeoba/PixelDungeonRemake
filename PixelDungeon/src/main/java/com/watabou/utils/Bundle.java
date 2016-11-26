/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.utils;

import com.coner.android.util.TrackedRuntimeException;
import com.coner.android.util.Util;
import com.coner.pixeldungeon.remake.EventCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Bundle {

	private static final String CLASS_NAME = "__className";
	
	private static Map<String,String> aliases = new HashMap<>();
	
	private JSONObject data;
	
	public Bundle() {
		this( new JSONObject() );
	}
	
	public Bundle( String data ) throws JSONException {
		this.data = new JSONObject(data);
	}
	
	private Bundle( JSONObject data ) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
	
	public boolean isNull() {
		return data == null;
	}
	
	public boolean contains( String key ) {
		return !data.isNull( key );
	}
	
	public boolean getBoolean( String key ) {
		return data.optBoolean(key);
	}
	
	public int getInt( String key ) {
		return data.optInt(key);
	}

	public int optInt( String key, int val ) {
		return data.optInt( key, val );
	}
	
	public float getFloat( String key ) {
		return (float)data.optDouble( key );
	}
	
	public String optString( String key, String defVal ) {
		return data.optString(key, defVal);
	}
	
	public String getString( String key ) {
		return data.optString(key);
	}
	
	public Bundle getBundle( String key ) {
		return new Bundle( data.optJSONObject( key ) );
	}
	
	private Bundlable get() {
		String clName="no class";
		try {
			clName = getString( CLASS_NAME );
			if (aliases.containsKey( clName )) {
				clName = aliases.get( clName );
			}
			
			Class<?> cl = Class.forName( clName );
			if (cl != null) {
				Bundlable object = (Bundlable)cl.newInstance();
				object.restoreFromBundle( this );
				return object;
			} else {
				Util.storeEventInAcra("Bundable.get unknown class ",clName);
				return null;
			}
		} catch (Exception e) {
			EventCollector.logException(e,clName);
			Util.storeEventInAcra("Bundable.get",e);
			return null;
		}	
	}
	
	public Bundlable get( String key ) {
		JSONObject obj = data.optJSONObject( key );
		if(obj!=null) {
			return new Bundle(obj).get();
		}
		return null;
	}
	
	public <E extends Enum<E>> E getEnum( String key, Class<E> enumClass ) {
		try {
			return Enum.valueOf( enumClass, data.getString( key ) );
		} catch (JSONException e) {
			EventCollector.logException(e);
			return enumClass.getEnumConstants()[0];
		}
	}

	public int[] getIntArray(String key) {
		JSONArray array = data.optJSONArray(key);
		if (array == null) {
			return null;
		}
		int length = array.length();
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			result[i] = array.optInt(i);
		}
		return result;
	}
	
	public boolean[] getBooleanArray( String key ) {
		try {
			JSONArray array = data.getJSONArray( key );
			int length = array.length();
			boolean[] result = new boolean[length];
			for (int i=0; i < length; i++) {
				result[i] = array.getBoolean(i);
			}
			return result;
		} catch (JSONException e) {
			Util.storeEventInAcra("Bundable.getBooleanArray",e);
			return null;
		}
	}
	
	public String[] getStringArray( String key ) {
		if(!data.has(key)) {
			return null;
		}

		try {
			JSONArray array = data.getJSONArray( key );
			int length = array.length();
			String[] result = new String[length];
			for (int i=0; i < length; i++) {
				result[i] = array.getString( i );
			}
			return result;
		} catch (JSONException e) {
			Util.storeEventInAcra("Bundable.getStringArray", e);
			return null;
		}
	}
	
	public <T extends Bundlable> Collection<T> getCollection( String key, Class<T> type ) {

		if(!data.has(key)) {
			return new Vector<T>();
		}

		List<T> list = new ArrayList<>();
		
		try {
			JSONArray array = data.getJSONArray( key );
			for (int i=0; i < array.length(); i++) {
				Object storedObject = new Bundle(array.getJSONObject(i)).get();
				if(storedObject!=null) {
					list.add(type.cast(storedObject));
				}
			}
		} catch (JSONException e) {
			Util.storeEventInAcra("Bundable.getCollection", e);
			return new Vector<T>();
		}
		
		return list;
	}
	
	public void put( String key, boolean value ) {
		try {
			data.put( key, value );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, int value ) {
		try {
			data.put( key, value );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, float value ) {
		try {
			data.put( key, value );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, String value ) {
		try {
			data.put( key, value );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, Bundle bundle ) {
		try {
			data.put( key, bundle.data );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, Bundlable object ) {
		if (object != null && !object.dontPack()) {
			try {
				Bundle bundle = new Bundle();
				bundle.put( CLASS_NAME, object.getClass().getName() );
				object.storeInBundle( bundle );
				data.put( key, bundle.data );
			} catch (JSONException e) {
				throw new TrackedRuntimeException("key:"+key,e);
			}
		}
	}
	
	public void put( String key, Enum<?> value ) {
		if (value != null) {
			try {
				data.put( key, value.name() );
			} catch (JSONException e) {
				throw new TrackedRuntimeException("key:"+key,e);
			}
		}
	}
	
	public void put( String key, int[] array ) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i=0; i < array.length; i++) {
				jsonArray.put( i, array[i] );
			}
			data.put( key, jsonArray );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, boolean[] array ) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i=0; i < array.length; i++) {
				jsonArray.put( i, array[i] );
			}
			data.put( key, jsonArray );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, String[] array ) {
		try {
			JSONArray jsonArray = new JSONArray();
			for (int i=0; i < array.length; i++) {
				jsonArray.put( i, array[i] );
			}
			data.put( key, jsonArray );
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public void put( String key, Collection<? extends Bundlable> collection ) {
		JSONArray array = new JSONArray();
		for (Bundlable object : collection) {
			if(!object.dontPack()){
				Bundle bundle = new Bundle();
				bundle.put( CLASS_NAME, object.getClass().getName() );
				object.storeInBundle( bundle );
				array.put( bundle.data );
			}
		}
		try {
			data.put(key, array);
		} catch (JSONException e) {
			throw new TrackedRuntimeException("key:"+key,e);
		}
	}
	
	public static Bundle read( InputStream stream ) {
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
			String line = reader.readLine();
			Util.storeEventInAcra("Bundle read line",line);
			JSONObject json = (JSONObject)new JSONTokener(line).nextValue();
			reader.close();
			return new Bundle( json );
		} catch (Exception e) {
			Util.storeEventInAcra("Bundle Exception",e);
			return null;
		}
	}

	public static boolean write( Bundle bundle, OutputStream stream ) {
		try {
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( stream ) );
			writer.write( bundle.data.toString() );
			writer.close();
			return true;
		} catch (IOException e) {
			throw new TrackedRuntimeException("bungle write failed: %s\n",e);
		}
	}
	
	public static void addAlias( Class<?> cl, String alias ) {
		aliases.put( alias, cl.getName() );
	}
}
