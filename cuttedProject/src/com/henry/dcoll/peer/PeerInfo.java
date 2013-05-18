package com.henry.dcoll.peer;

public class PeerInfo {
	private String peerName;
	private Integer sessionId;
	
	public PeerInfo(String peerName, Integer sessionId) {
		super();
		this.peerName = peerName;
		this.sessionId = sessionId;
	}

	public String getPeerName() {
		return peerName;
	}

	public void setPeerName(String peerName) {
		this.peerName = peerName;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public void setSessionId(Integer sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return "PeerInfo [peerName=" + peerName + ", sessionId=" + sessionId
				+ "]";
	}
}
