package com.henry.dcoll.dlist;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DListCore <T> {
	private String space;
	private String listName;
	private List<T> localList;
	private IDListListener listener;
	private Map<String, List<T>> remoteLists = new HashMap<String, List<T>>();
	private Map<Integer, ObjectLocation> remoteObjectsLocations = new HashMap<Integer, ObjectLocation>();
	private InvocationHandler handler;
	private Class<?> objectInterface;

	public DListCore(String space, String listName, List<T> list,
			IDListListener listener, Class<?> objectInterface) {
		super();
		this.space = space;
		this.listName = listName;
		this.localList = list;
		this.listener = listener;
		this.objectInterface = objectInterface;
	}

	public String getSpace() {
		return space;
	}

	public String getListName() {
		return listName;
	}

	public List<T> getLocalList() {
		return localList;
	}

	public List<T> getRemoteList(String owner) {
		return remoteLists.get(owner);
	}

	public ObjectLocation getObjectLocation(Integer hashCode) {
		return remoteObjectsLocations.get(hashCode);
	}

	public IDListListener getListener() {
		return listener;
	}

	public void setListener(IDListListener listener) {
		this.listener = listener;
	}

	public void setHandler(InvocationHandler handler) {
		this.handler = handler;
	}

	@SuppressWarnings("unchecked")
	public void addRemoteList(String owner, Integer size) {
		remoteLists.put(owner, new ArrayList<T>());
		for (int index = 0; index < size; index++) {
			T object = (T) Proxy.newProxyInstance(
					objectInterface.getClassLoader(),
					new Class<?>[] { objectInterface }, handler);
			remoteLists.get(owner).add(object);
			remoteObjectsLocations.put(System.identityHashCode(object),
					new ObjectLocation(owner, space, listName, index));
		}
	}

	public boolean removeRemoteList(String owner) {
		if (remoteLists.containsKey(owner)) {
			List<T> objectList = remoteLists.get(owner);
			for (T object : objectList) {
				remoteObjectsLocations.remove(System.identityHashCode(object));
			}
			remoteLists.remove(owner);
			return true;
		} else {
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addProxyObject(String owner, int index) {
		T newProxyObject = (T) Proxy.newProxyInstance(
				objectInterface.getClassLoader(),
				new Class<?>[] { objectInterface }, handler);
		List<T> remoteList = remoteLists.get(owner);
		if (index < 0) {
			remoteObjectsLocations.put(System.identityHashCode(newProxyObject),
					new ObjectLocation(owner, space, listName, remoteList.size()));
			remoteList.add(newProxyObject);
		} else {
			for (int i = index; i < remoteList.size(); i++) {
				T proxyObject = remoteList.get(i);
				remoteObjectsLocations.get(System.identityHashCode(proxyObject))
						.increaseIndex();
			}
			remoteObjectsLocations.put(System.identityHashCode(newProxyObject),
					new ObjectLocation(owner, space, listName, index));
			remoteList.add(index, newProxyObject);
		}
	}
	
	public void removeProxyObject(String owner, int index) {
		List<T> remoteList = remoteLists.get(owner);
		for (int i = index + 1; i < remoteList.size(); i++) {
			T proxyObject = remoteList.get(i);
			remoteObjectsLocations.get(System.identityHashCode(proxyObject))
					.decreaseIndex();
		}
		T object = remoteList.remove(index);
		remoteObjectsLocations.remove(System.identityHashCode(object));
	}
	
	@SuppressWarnings("unchecked")
	public void addObject(int index, Object object) {
		if (index < 0) {
			localList.add((T) object);
		} else {
			localList.add(index, (T) object);
		}
	}
	
	public T removeObject(int index) {
		return localList.remove(index);
	}
	
	public boolean removeObject(Object object) {
		return localList.remove(object);
	}
	
	public int indexOfObject(Object object) {
		return localList.indexOf(object);
	}
	
	public List<String> getRemoteListOwners() {
		return new LinkedList<String>(remoteLists.keySet());
	}

	@Override
	public String toString() {
		return "DListInfo [space=" + space + ", listName=" + listName
				+ ", localList=" + localList + ", listener=" + listener
				+ ", remoteLists=" + remoteLists.keySet()
				+ ", remoteObjectsLocations=" + remoteObjectsLocations + "]";
	}
}
