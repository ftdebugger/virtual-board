package com.henry.dcoll.dspace;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DSpaceContainer {
	private ConcurrentHashMap<String, DSpace> spaces = new ConcurrentHashMap<String, DSpace>();
	
	public boolean contains(String spaceName) {
		return spaces.containsKey(spaceName);
	}
	
	public DSpace get(String spaceName) {
		return spaces.get(spaceName);
	}
	
	public void addDSpace(String spaceName, DSpace space) {
		spaces.put(spaceName, space);
	}
	
	public void removeDSpace(String spaceName) {
		spaces.remove(spaceName);
	}

	public Map<String, DSpace> getSpaces() {
		return Collections.unmodifiableMap(spaces);
	}
	
	public void removeAllDListParts(String peerName) {
		for (DSpace dSpace : spaces.values()) {
			dSpace.removeAllDListParts(peerName);
		}
	}
}
