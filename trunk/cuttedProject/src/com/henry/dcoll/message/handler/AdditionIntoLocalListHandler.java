package com.henry.dcoll.message.handler;

import com.henry.dcoll.dlist.DListCore;
import com.henry.dcoll.dspace.DSpace;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.data.AbstractData;
import com.henry.dcoll.message.data.AdditionIntoLocalListData;
import com.henry.dcoll.message.receive.IMessageReceiver.Reply;
import com.henry.dcoll.message.send.MessageSender;

public class AdditionIntoLocalListHandler extends AbstractMessageHandler {

	public AdditionIntoLocalListHandler(String owner,
			DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		super(owner, dSpaceContainer, messageSender);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Reply handleMessage(AbstractData abstractData) {
		AdditionIntoLocalListData data = (AdditionIntoLocalListData) abstractData;
		String message = data.getMessage();
		String[] messageArray = message.split(":");
		String space = messageArray[0];
		String listName = messageArray[1];
		Integer index = Integer.valueOf(messageArray[2]);
		Object object = data.getObject();
		if (dSpaceContainer.contains(space)) {
			DSpace dSpace = dSpaceContainer.get(space);
			if (dSpace.contains(listName)) {
				DListCore<?> dListCore = dSpace.getByListName(listName);
				dListCore.addObject(index, object);
			}
		}
		return new Reply();
	}

}
