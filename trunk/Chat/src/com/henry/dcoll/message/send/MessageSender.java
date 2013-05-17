package com.henry.dcoll.message.send;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.alljoyn.bus.BusException;

import android.util.Log;

import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dlist.ObjectLocation;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.main.Runner;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.AdditionIntoRemoteListData;
import com.henry.dcoll.message.data.AdditionIntoLocalListData;
import com.henry.dcoll.message.data.DListSeparationData;
import com.henry.dcoll.message.data.DListUnionData;
import com.henry.dcoll.message.data.MethodInvocationData;
import com.henry.dcoll.message.data.RemovalFromLocalListByIndexData;
import com.henry.dcoll.message.data.RemovalFromLocalListByObjectData;
import com.henry.dcoll.message.data.RemovalFromRemoteListData;
import com.henry.dcoll.message.receive.IMessageReceiver;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.peer.PeerContainer;
import com.henry.dcoll.util.Serializer;

import static com.henry.dcoll.message.MessageType.*;

public class MessageSender {
	private DSpaceContainer dSpaceContainer;
	private PeerContainer peerContainer;
	
	public MessageSender(DSpaceContainer dSpaceContainer,
			PeerContainer peerContainer) {
		super();
		this.dSpaceContainer = dSpaceContainer;
		this.peerContainer = peerContainer;
	}
	
	public void sendDListUnionMessage(String receiver, String space,
			String listName, int size) {
		DListUnionData data = new DListUnionData(DSpaceController.getOwner(),
				DLIST_UNION, space + ":" + listName + ":" + size);
		sendMessage(receiver, data);
	}

	public void sendDListUnionBroadcastMessage(String space, String listName,
			int size) {
		DListUnionData data = new DListUnionData(DSpaceController.getOwner(),
				DLIST_UNION, space + ":" + listName + ":" + size);
		List<Reply> replies = sendBroadcastMessage(data);
		for (Reply reply : replies) {
			if (reply.getSender().length() != 0
					&& reply.getMessage().length() != 0) {
				String sender = reply.getSender();
				Log.i(Runner.TAG,"Reply has been received from " + sender
						+ ". Reply {" + reply.getMessage() + "}");
				String[] messageArray = reply.getMessage().split(":");
				String replySpace = messageArray[0];
				String replyListName = messageArray[1];
				Integer replySize = Integer.valueOf(messageArray[2]);
				if (dSpaceContainer.contains(replySpace)) {
					DSpace dSpace = dSpaceContainer.get(replySpace);
					if (dSpace.contains(replyListName)) {
						DListCore<?> dListCore = dSpace
								.getByListName(replyListName);
						dListCore.addRemoteList(sender, replySize);
						dListCore.getListener().dListPartFound(sender);
					}
				}
			}
		}
	}

	public void sendDListSeparationBroadcastMessage(String space,
			String listName) {
		DListSeparationData data = new DListSeparationData(DSpaceController.getOwner(),
				DLIST_SEPARATION, space + ":" + listName);
		List<Reply> replies = sendBroadcastMessage(data);
		for (Reply reply : replies) {
			if (reply.getSender().length() != 0
					&& reply.getMessage().length() != 0) {
				String sender = reply.getSender();
				Log.i(Runner.TAG,"Reply has been received from " + sender
						+ ". Reply {" + reply.getMessage() + "}");
				String[] messageArray = reply.getMessage().split(":");
				String replySpace = messageArray[0];
				String replyListName = messageArray[1];
				if (dSpaceContainer.contains(replySpace)) {
					DSpace dSpace = dSpaceContainer.get(replySpace);
					if (dSpace.contains(replyListName)) {
						DListCore<?> dListCore = dSpace
								.getByListName(replyListName);
						dListCore.removeRemoteList(sender);
						dListCore.getListener().dListPartLost(sender);
					}
				}
			}
		}
	}
	
	public Object sendMethodInvocationMessage(ObjectLocation objectLocation,
			Method method, Object[] args) {
		String receiver = objectLocation.getOwner();
		String space = objectLocation.getSpace();
		String list = objectLocation.getList();
		Integer index = objectLocation.getIndex();
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		MethodInvocationData data = new MethodInvocationData(DSpaceController.getOwner(),
				METHOD_INVOCATION, space + ":" + list + ":" + index + ":"
						+ methodName, parameterTypes, args);
		Reply reply = sendMessage(receiver, data);
		return Serializer.deserializeObject(reply.getData());
	}
	
	public void sendAdditionIntoRemoteListMessage(String listOwner, List<String> receivers,
			String space, String listName, int index) {
		AdditionIntoRemoteListData data = new AdditionIntoRemoteListData(listOwner,
				ADDITION_INTO_REMOTE_LIST, space + ":" + listName + ":" + index);
		sendMulticastMessage(receivers, data);
	}

	public <T> void sendAdditionIntoLocalListMessage(String listOwner, String space, String listName, int index,
			T object) {
		AdditionIntoLocalListData<T> secondData = new AdditionIntoLocalListData<T>(
				DSpaceController.getOwner(), ADDITION_INTO_LOCAL_LIST, space + ":" + listName + ":"
						+ index, object);
		sendMessage(listOwner, secondData);
	}

	public void sendRemovalFromRemoteListMessage(String listOwner,
			List<String> receivers, String space, String listName, int index) {
		RemovalFromRemoteListData data = new RemovalFromRemoteListData(
				listOwner, REMOVAL_FROM_REMOTE_LIST, space + ":" + listName + ":" + index);
		sendMulticastMessage(receivers, data);
	}	
	
	public <T> T sendRemovalFromLocalListByIndexMessage(String listOwner,
			String space, String listName, int index) {
		RemovalFromLocalListByIndexData data = new RemovalFromLocalListByIndexData(
				DSpaceController.getOwner(), REMOVAL_FROM_LOCAL_LIST_BY_INDEX, space
						+ ":" + listName + ":" + index);
		Reply reply = sendMessage(listOwner, data);
		return Serializer.deserializeObject(reply.getData());
	}

	public <T> int sendRemovalFromLocalListByObjectMessage(
			String listOwner, String space, String listName, T object) {
		RemovalFromLocalListByObjectData<T> data = new RemovalFromLocalListByObjectData<T>(
				DSpaceController.getOwner(), REMOVAL_FROM_LOCAL_LIST_BY_OBJECT, space + ":"
						+ listName, object);
		Reply reply = sendMessage(listOwner, data);
		return Serializer.deserializeObject(reply.getData());
	}
	
	private Reply sendMessage(String receiver, AbstractData abstractData) {
		Reply reply = null;
		IMessageReceiver messageReceiver = peerContainer.getMessageReceiverByPeerName(receiver);
		try {
			reply = messageReceiver.message(Serializer.serializeObject(abstractData));
		} catch (BusException e) {
			e.printStackTrace();
		}
		return reply;
	}
	
	private List<Reply> sendMulticastMessage(List<String> receivers, AbstractData abstractData) {
 		List<Reply> replies = new LinkedList<Reply>();
		for (IMessageReceiver messageReceiver : peerContainer.getMessageReceivers(receivers)) {
			try {
				replies.add(messageReceiver.message(Serializer.serializeObject(abstractData)));
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
		return replies;
	}
	
	private List<Reply> sendBroadcastMessage(AbstractData abstractData) {
 		List<Reply> replies = new LinkedList<Reply>();
		for (IMessageReceiver messageReceiver : peerContainer.getAllMessageReceivers()) {
			try {
				replies.add(messageReceiver.message(Serializer.serializeObject(abstractData)));
			} catch (BusException e) {
				e.printStackTrace();
			}
		}
		return replies;
	}
}
