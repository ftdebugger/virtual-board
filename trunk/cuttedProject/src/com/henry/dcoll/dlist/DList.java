package com.henry.dcoll.dlist;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.message.send.MessageSender;

public class DList<T> {
	// private String dListOwner;
	private InvocationHandler handler = new InvocationHandler() {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			ObjectLocation objectLocation = dListCore.getObjectLocation(System
					.identityHashCode(proxy));
			return messageSender.sendMethodInvocationMessage(objectLocation,
					method, args);
		}
	};
	private DListCore<T> dListCore;
	private MessageSender messageSender;

	public DList(DListCore<T> dListCore, MessageSender messageSender) {
		super();
		// this.dListOwner = dListOwner;
		this.dListCore = dListCore;
		dListCore.setHandler(handler);
		this.messageSender = messageSender;
	}

	public String getSpace() {
		return dListCore.getSpace();
	}

	public String getListName() {
		return dListCore.getListName();
	}

	public IDListListener getListener() {
		return dListCore.getListener();
	}

	public List<T> get() {
		return Collections.unmodifiableList(dListCore.getLocalList());
	}

	public T get(int index) {
		return dListCore.getLocalList().get(index);
	}

	public List<T> get(String owner) {
		return Collections.unmodifiableList(dListCore.getRemoteList(owner));
	}

	public T get(String owner, int index) {
		return dListCore.getRemoteList(owner).get(index);
	}

	public void add(T object) {
		dListCore.getLocalList().add(object);
		messageSender.sendAdditionIntoRemoteListMessage(
				DSpaceController.getOwner(), dListCore.getRemoteListOwners(),
				dListCore.getSpace(), dListCore.getListName(), -1);
	}

	public void add(int index, T object) {
		dListCore.getLocalList().add(index, object);
		messageSender.sendAdditionIntoRemoteListMessage(
				DSpaceController.getOwner(), dListCore.getRemoteListOwners(),
				dListCore.getSpace(), dListCore.getListName(), index);
	}

	public void add(String owner, T object) {
		add(owner, -1, object);
	}

	public void add(String owner, int index, T object) {
		messageSender.sendAdditionIntoLocalListMessage(owner,
				dListCore.getSpace(), dListCore.getListName(), index, object);
		dListCore.addProxyObject(owner, index);
		List<String> receivers = dListCore.getRemoteListOwners();
		receivers.remove(owner);
		messageSender.sendAdditionIntoRemoteListMessage(owner, receivers,
				dListCore.getSpace(), dListCore.getListName(), index);
	}

	public T remove(int index) { // better use local
		T object = dListCore.removeObject(index);
		messageSender.sendRemovalFromRemoteListMessage(
				DSpaceController.getOwner(), dListCore.getRemoteListOwners(),
				dListCore.getSpace(), dListCore.getListName(), index);
		return object;
	}

	public boolean remove(Object object) {// local
		int index = dListCore.indexOfObject(object);
		boolean wasDeleted = dListCore.removeObject(object);
		if (wasDeleted) {
			messageSender.sendRemovalFromRemoteListMessage(
					DSpaceController.getOwner(),
					dListCore.getRemoteListOwners(), dListCore.getSpace(),
					dListCore.getListName(), index);
		}
		return wasDeleted;
	}

	public T remove(String owner, int index) {
		T object = messageSender.sendRemovalFromLocalListByIndexMessage(owner,
				dListCore.getSpace(), dListCore.getListName(), index);
		dListCore.removeProxyObject(owner, index);
		List<String> receivers = dListCore.getRemoteListOwners();
		receivers.remove(owner);
		messageSender.sendRemovalFromRemoteListMessage(owner, receivers,
				dListCore.getSpace(), dListCore.getListName(), index);
		return object;
	}

	public int size() {
		return dListCore.getLocalList().size();
	}

	public int size(String owner) {
		return dListCore.getRemoteList(owner).size();
	}

	public boolean contains(T object) {
		return dListCore.getLocalList().contains(object);
	}

	public int indexOf(T object) {
		return dListCore.getLocalList().indexOf(object);
	}

	@Override
	protected void finalize() throws Throwable {
		DSpaceController.destroyDList(this);
		super.finalize();
	}

	/*
	 * public boolean remove(String owner, Object object) { int index =
	 * messageSender .sendRemovalFromLocalListByObjectMessage(owner,
	 * dListCore.getSpace(), dListCore.getListName(), object); if (index >= 0) {
	 * dListCore.removeProxyObject(owner, index); List<String> receivers =
	 * dListCore.getRemoteListOwners(); receivers.remove(owner);
	 * messageSender.sendRemovalFromRemoteListMessage(owner, receivers,
	 * dListCore.getSpace(), dListCore.getListName(), index); return true; }
	 * else { return false; } }
	 */
}
