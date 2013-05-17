package com.henry.dcoll.dspace;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dlist.IDListListener;

public class DSpace {
	private Map<String, DListCore<?>> lists = new HashMap<String, DListCore<?>>();
	
	public boolean contains(String listName) {
		return lists.containsKey(listName);
	}
	
	public DListCore<?> getByListName(String listName) {
		return lists.get(listName);
	}
	
	public <T> DListCore<T> addList(String space,
			String name, List<T> list, IDListListener listener,
			Class<?> objectInterface) {
		DListCore<T> dListCore = new DListCore<T>(space, name, list,
				listener, objectInterface);
		lists.put(name, dListCore);
		return dListCore;
	}
	
	public void removeList(String name) {
		lists.remove(name);
	}
	
	public Map<String, DListCore<?>> getLists() {
		return Collections.unmodifiableMap(lists);
	}
	
	public void removeAllDListParts(String peerName) {
		for (DListCore<?> dListCore : lists.values()) {
			if (dListCore.removeRemoteList(peerName)) {
				dListCore.getListener().dListPartLost(peerName);
			}
		}
	}
	
	public int size() {
		return lists.size();
	}

	@Override
	public String toString() {
		return "DSpace [lists=" + lists + "]";
	}
}
