package com.henry.dcoll.peer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.alljoyn.bus.ProxyBusObject;

import com.henry.dcoll.message.receive.IMessageReceiver;

public class PeerContainer {
	private ConcurrentHashMap<PeerInfo, ProxyBusObject> peers = new ConcurrentHashMap<PeerInfo, ProxyBusObject>();

	public void addPeer(PeerInfo peerInfo, ProxyBusObject peerAccessObject) {
		peers.put(peerInfo, peerAccessObject);
	}
	
	public void removePeer(Integer sessionId) {
		List<PeerInfo> peerInfoList = new LinkedList<PeerInfo>();
		for (Map.Entry<PeerInfo, ProxyBusObject> entry : peers.entrySet()) {
			if (entry.getKey().getSessionId().equals(sessionId)) {
				peerInfoList.add(entry.getKey());
			}
		}
		for (PeerInfo peerInfo : peerInfoList) {
			peers.remove(peerInfo);
		}
	}
	
	public void clear() {
		peers.clear();
	}
	
	public IMessageReceiver getMessageReceiverByPeerName(String peerName) {
		for (Map.Entry<PeerInfo, ProxyBusObject> entry : peers.entrySet()) {
			if (entry.getKey().getPeerName().equals(peerName)) {
				return entry.getValue().getInterface(IMessageReceiver.class);
			}
		}
		return null;
	}
	
	public ProxyBusObject getBySessionId(Integer sessionId) {
		for (Map.Entry<PeerInfo, ProxyBusObject> entry : peers.entrySet()) {
			if (entry.getKey().getSessionId().equals(sessionId)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public ProxyBusObject getByPeerName(String peerName) {
		for (Map.Entry<PeerInfo, ProxyBusObject> entry : peers.entrySet()) {
			if (entry.getKey().getPeerName().equals(peerName)) {
				return entry.getValue();
			}
		}
		return null;
	}
	
	public String getPeerNameBySessionId(Integer sessionId) {
		for (PeerInfo peerInfo : peers.keySet()) {
			if (peerInfo.getSessionId().equals(sessionId)) {
				return peerInfo.getPeerName();
			}
		}
		return null;
	}
	
	public List<IMessageReceiver> getAllMessageReceivers() {
		List<IMessageReceiver> messageReceivers = new LinkedList<IMessageReceiver>();
		for (ProxyBusObject peer : peers.values()) {
			messageReceivers.add(peer.getInterface(IMessageReceiver.class));
		}
		return messageReceivers;
	}
	
	public List<IMessageReceiver> getMessageReceivers(List<String> peerNames) {
		List<IMessageReceiver> messageReceivers = new LinkedList<IMessageReceiver>();
		for (Map.Entry<PeerInfo, ProxyBusObject> entry : peers.entrySet()) {
			if (peerNames.contains(entry.getKey().getPeerName())) {
				messageReceivers.add(entry.getValue().getInterface(IMessageReceiver.class));
			}
		}
		return messageReceivers;
	}
	
	public Map<PeerInfo, ProxyBusObject> getPeers() {
		return Collections.unmodifiableMap(peers);
	}
}
