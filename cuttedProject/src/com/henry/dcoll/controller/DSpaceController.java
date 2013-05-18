package com.henry.dcoll.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.Status;

import android.util.Log;

import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dlist.IDListListener;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.main.Runner;
import com.henry.dcoll.message.receive.IMessageReceiver;
import com.henry.dcoll.message.receive.MessageReceiver;
import com.henry.dcoll.message.send.MessageSender;
import com.henry.dcoll.peer.PeerContainer;
import com.henry.dcoll.peer.PeerInfo;

public class DSpaceController {
	private static final String group = "com.henry.dcoll.controller";
	private static final short CONTACT_PORT = 42;
	private static boolean isConnected = false;
	private static String owner;
	private static BusAttachment mBus;
	private static PeerContainer peerContainer = new PeerContainer();
	private static DSpaceContainer dSpaceContainer = new DSpaceContainer();
	private static MessageSender messageSender;
	private static MessageReceiver messageReceiver;
	
	static { 
		System.loadLibrary("alljoyn_java");
	}
	
	private static class MyBusListener extends BusListener {
		@Override
		public void foundAdvertisedName(String name, short transport, String namePrefix) {
			if (!name.equals(group + "." + owner)) {
				Log.i(Runner.TAG,String.format("BusListener.foundAdvertisedName(%s, %d, %s)", name, transport, namePrefix));
				short contactPort = CONTACT_PORT;
				SessionOpts sessionOpts = new SessionOpts();
				sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
				sessionOpts.isMultipoint = false;
				sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
				sessionOpts.transports = SessionOpts.TRANSPORT_ANY;
				
				Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
				
				mBus.enableConcurrentCallbacks();
				
				Status status = mBus.joinSession(name, contactPort, sessionId, sessionOpts,	new MySessionListener());
				if (status != Status.OK) {
					System.exit(0);
				}
				Log.i(Runner.TAG,String.format("BusAttachement.joinSession successful sessionId = %d", sessionId.value));
				
				ProxyBusObject peerAccessPoint =  mBus.getProxyBusObject(name,
													"/messageReciever",
													sessionId.value,
													new Class<?>[] {IMessageReceiver.class});
				
				String peerName = name.replace(group + ".", "");
				peerContainer.addPeer(new PeerInfo(peerName, sessionId.value),
						peerAccessPoint);
				for (Map.Entry<String, DSpace> spaceEntry : dSpaceContainer
						.getSpaces().entrySet()) {
					for (Map.Entry<String, DListCore<?>> listEntry : spaceEntry
							.getValue().getLists().entrySet()) {
						messageSender.sendDListUnionMessage(peerName,
								spaceEntry.getKey(), listEntry.getKey(),
								listEntry.getValue().getLocalList().size());
					}
				}
				
			}
		}
		
		@Override
		public void lostAdvertisedName(String name, short transport, String namePrefix) {
			Log.i(Runner.TAG,String.format("BusListener.lostAdvertisedName(%s, %d, %s)", name, transport, namePrefix));
			super.lostAdvertisedName(name, transport, namePrefix);
		}
		
		@Override
		public void nameOwnerChanged(String busName, String previousOwner,
				String newOwner) {
			Log.i(Runner.TAG,String.format("BusListener.nameOwnerChanged(%s, %s, %s)", busName, previousOwner, newOwner));
			super.nameOwnerChanged(busName, previousOwner, newOwner);
		}
		
		
	}
	
	private static class MySessionListener extends SessionListener {
		@Override
		public void sessionLost(int sessionId) {
			Log.i(Runner.TAG,String.format("SessionListener.sessionLost(%d)", sessionId));
			String peerName = peerContainer.getPeerNameBySessionId(sessionId);
			dSpaceContainer.removeAllDListParts(peerName);
			peerContainer.removePeer(sessionId);
			super.sessionLost(sessionId);
		}
		
		@Override
		public void sessionMemberAdded(int sessionId, String uniqueName) {
			Log.i(Runner.TAG,String.format("SessionListener.sessionMemberAdded(%d, %s)", sessionId, uniqueName));
			super.sessionMemberAdded(sessionId, uniqueName);
		}
		
		@Override
		public void sessionMemberRemoved(int sessionId, String uniqueName) {
			Log.i(Runner.TAG,String.format("SessionListener.sessionMemberRemoved(%d, %s)", sessionId, uniqueName));
			super.sessionMemberRemoved(sessionId, uniqueName);
		}
	}
	
	public static void connect(String ownerName) {
		if (!isConnected) {
			owner = ownerName;
			mBus = new BusAttachment("AppName", BusAttachment.RemoteMessage.Receive);
			messageSender = new MessageSender(dSpaceContainer, peerContainer);
			messageReceiver = new MessageReceiver(dSpaceContainer, messageSender);
			
			
			Status status = mBus.registerBusObject(messageReceiver, "/messageReciever");
			if (status != Status.OK) {
			    System.exit(0);
			    return;
			}
			
			mBus.registerBusListener(new MyBusListener());
			
			status = mBus.connect();
			if (status != Status.OK) {
			    System.exit(0);
			    return;
			}
			
			Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);
			SessionOpts sessionOpts = new SessionOpts();
			sessionOpts.traffic = SessionOpts.TRAFFIC_MESSAGES;
			sessionOpts.isMultipoint = false;
			sessionOpts.proximity = SessionOpts.PROXIMITY_ANY;
			sessionOpts.transports = SessionOpts.TRANSPORT_ANY;
			
			status = mBus.bindSessionPort(contactPort, sessionOpts,
					new SessionPortListener() {
						@Override
						public boolean acceptSessionJoiner(short sessionPort,
								String joiner, SessionOpts sessionOpts) {
							Log.i(Runner.TAG,String.format("SessionPortListener.acceptSessionJoiner(%d, %s)", sessionPort, joiner));
							if (sessionPort == CONTACT_PORT) {
								return true;
							} else {
								return false;
							}
						}
						
						@Override
			            public void sessionJoined(short sessionPort, int id, String joiner) {
							Log.i(Runner.TAG,String.format("SessionPortListener.sessionJoined(%d, %d, %s)", sessionPort, id, joiner));
			            }
					});
			if (status != Status.OK) {
				System.exit(0);
				return;
			}
			
			
			
			int flags = 0;
			status = mBus.requestName(group + "." + owner, flags);
			if (status != Status.OK) {
				System.exit(0);
				return;
			}
	
			status = mBus.advertiseName(group + "." + owner,
					SessionOpts.TRANSPORT_ANY);
			if (status != Status.OK) {
				mBus.releaseName(group + "." + owner);
				System.exit(0);
				return;
			}
			
			status = mBus.findAdvertisedName(group);
			if (status != Status.OK) {
			    System.exit(0);
			    return;
			}
			
			isConnected = true;
		}
	}
	
	public static String getOwner() {
		return owner;
	}
	
	public static <T> DList<T> createNewDList(String space, String listName, IDListListener listener, Collection<T> collection, Class<?> objectInterface) {
		if (listener == null) {
			listener = new IDListListener() {
				@Override
				public void dListPartFound(String owner) {
				}	
				
				@Override
				public void dListPartLost(String owner) {
				}
			};
		}
		if (!dSpaceContainer.contains(space)) {
			dSpaceContainer.addDSpace(space, new DSpace());
		}
		DSpace dSpace = dSpaceContainer.get(space);
		DListCore<T> dListCore = dSpace.addList(space, listName, new ArrayList<T>(collection), listener, objectInterface);
		DList<T> dList = new DList<T>(dListCore, messageSender);
		if (messageSender != null) {
			messageSender.sendDListUnionBroadcastMessage(space, listName, collection.size());
		}
		return dList;
	}
	
	public static <T> DList<T> createNewDList(String space, String listName, IDListListener listener, Class<?> objectInterface) {
		return DSpaceController.createNewDList(space, listName, listener, new ArrayList<T>(), objectInterface);
	}
	
	public static <T> DList<T> createNewDList(String space, String listName, Collection<T> collection, Class<?> objectInterface) {
		return DSpaceController.createNewDList(space, listName, null, collection, objectInterface);
	}
	
	public static void destroyDList(DList<?> dList) {
		String spaceName = dList.getSpace();
		if (messageSender != null) {
			messageSender.sendDListSeparationBroadcastMessage(spaceName, dList.getListName());
		}
		DSpace dSpace = dSpaceContainer.get(spaceName);
		dSpace.removeList(dList.getListName());
		if (dSpace.size() == 0) {
			dSpaceContainer.removeDSpace(spaceName);
		}
	}
	
	public static DSpaceContainer getDSpaceContainer() {
		return dSpaceContainer;
	}
	
	public static PeerContainer getPeerContainer() {
		return peerContainer;
	}
	
	public static void disconnect() {
		if (isConnected) {
			peerContainer.clear();
			messageReceiver = null;
			messageSender = null;
			mBus.cancelAdvertiseName(group + "." + owner, SessionOpts.TRANSPORT_ANY);
			mBus.cancelFindAdvertisedName(group);
			mBus.unbindSessionPort(CONTACT_PORT);
			mBus.release();
			isConnected = false;
		}
	}

}
