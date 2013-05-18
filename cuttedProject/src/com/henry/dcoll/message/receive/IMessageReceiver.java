package com.henry.dcoll.message.receive;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.Position;

@BusInterface (name = "com.chat.IMessageReciever")
public interface IMessageReceiver {
	public class Reply {
		@Position(0)
		public String sender;
		@Position(1)
		public String message;
		@Position(2)
		public byte[] data;
		
		public Reply() {
			super();
			this.sender = new String();
			this.message = new String();
			this.data = new byte[0];
		}
		
		public Reply(byte[] data) {
			super();
			this.sender = new String();
			this.message = new String();
			this.data = data;
		}
				
		public Reply(String sender, String message, byte[] data) {
			super();
			this.sender = sender;
			this.message = message;
			this.data = data;
		}

		public Reply(String sender, String message) {
			super();
			this.sender = sender;
			this.message = message;
			this.data = new byte[0];
		}

		public String getSender() {
			return sender;
		}

		public void setSender(String sender) {
			this.sender = sender;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	}
	
	@BusMethod(signature = "ay", replySignature="ssay")
	public Reply message(byte[] byteData) throws BusException;
}
