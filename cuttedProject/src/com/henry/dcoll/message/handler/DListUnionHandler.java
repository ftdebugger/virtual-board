package com.henry.dcoll.message.handler;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.DListUnionData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;

public class DListUnionHandler extends AbstractMessageHandler {
	public DListUnionHandler(String owner, DSpaceContainer dSpaceContainer,
			MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@Override
	public Reply handleMessage(AbstractData abstractData) {
		DListUnionData data = (DListUnionData) abstractData;
		String sender = data.getSender();
		String message = data.getMessage();
		String[] messageArray = message.split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		Integer size = Integer.valueOf(messageArray[2]);
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				dListCore.addRemoteList(sender, size);
				dListCore.getListener().dListPartFound(sender);
				return new Reply(owner, space + ":" + listName + ":" + dListCore.getLocalList().size());
			}
		}
		return new Reply();
	}
}
