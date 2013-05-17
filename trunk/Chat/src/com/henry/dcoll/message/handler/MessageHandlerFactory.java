package com.henry.dcoll.message.handler;

import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dspace.DSpaceContainer;
import com.henry.dcoll.message.MessageType;
import com.henry.dcoll.message.send.MessageSender;

public class MessageHandlerFactory {
	private DSpaceContainer dSpaceContainer;
	private MessageSender messageSender;
	
	public MessageHandlerFactory(DSpaceContainer dSpaceContainer, MessageSender messageSender) {
		this.dSpaceContainer = dSpaceContainer;
		this.messageSender = messageSender;
	}

	public AbstractMessageHandler getMessageHandler(MessageType messageType) {
		switch (messageType) {
		case DLIST_UNION: {
			return new DListUnionHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case DLIST_SEPARATION: {
			return new DListSeparationHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case METHOD_INVOCATION: {
			return new MethodInvocationHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case ADDITION_INTO_REMOTE_LIST: {
			return new AdditionIntoRemoteListHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case ADDITION_INTO_LOCAL_LIST: {
			return new AdditionIntoLocalListHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case REMOVAL_FROM_REMOTE_LIST: {
			return new RemovalFromRemoteListHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case REMOVAL_FROM_LOCAL_LIST_BY_INDEX: {
			return new RemovalFromLocalListByIndexHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		case REMOVAL_FROM_LOCAL_LIST_BY_OBJECT: {
			return new RemovalFromLocalListByObjectHandler(DSpaceController.getOwner(), dSpaceContainer, messageSender);
		}
		default: {
			throw new IllegalArgumentException("There is no message handler for message type " + messageType);
		}
		}
	}

}
