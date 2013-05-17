package com.henry.dcoll.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import android.util.Log;

import com.henry.dcoll.controller.DSpaceController;
import com.henry.dcoll.dlist.DList;
import com.henry.dcoll.dlist.IDListListener;

public class Runner {
	public static final String TAG = "chatlibrary";
	private static class MyDListListener implements IDListListener {
		@Override
		public void dListPartFound(String owner) {
			Log.i(TAG,"I found part of D-list. Owner: " + owner);
		}
		
		@Override
		public void dListPartLost(String owner) {
			Log.i(TAG,"I lost part of D-list. Owner: " + owner);
			
		}
	}
    
	public static void main() {
		Map<String, DList<IMyEntity>> map = new HashMap<String, DList<IMyEntity>>();
		
		Log.i(TAG,"Enter your nickname:");
		Scanner in = new Scanner(System.in);
		String nickname = "lol";
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				DSpaceController.disconnect();
			}
		});
		
		DSpaceController.connect(nickname);
		
		List<IMyEntity> myList = new ArrayList<IMyEntity>();
		myList.add(new MyEntity(nickname + ":line2", 54));
		myList.add(new MyEntity(nickname + ":line36", 86));
		myList.add(new MyEntity(nickname + ":line71", 43));
		myList.add(new MyEntity(nickname + ":line92", 13));
		
		List<IMyEntity> addedObjects = new ArrayList<IMyEntity>();
		
		DList<IMyEntity> dLists = DSpaceController
				.createNewDList("mySpace", "list1", //space namelist logger mylist interface.name
						new MyDListListener(), myList,
						IMyEntity.class);
		DSpaceController.connect(nickname);
		
		String line;
		while(true){
			DSpaceController.connect(String.valueOf((new Random()).nextDouble()));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DSpaceController.disconnect();
		}
//		while (true) {
//			if (in.hasNext()) {
//				line = in.nextLine();
//				String[] messageArray = line.split(" ");
//				if (messageArray.length == 1) {
//					String command = messageArray[0].toLowerCase();
//					if ("connect".equals(command)) {
//						DSpaceController.connect(nickname);
//					} else if ("disconnect".equals(command)) {
//						DSpaceController.disconnect();
//					} else {
//						Log.i(TAG,"Wrong command. Skipping.");
//					}
//				} else if (messageArray.length == 2) {
//					String command = messageArray[0].toLowerCase();
//					String parameter = messageArray[1].toLowerCase();
//					if ("create".equals(command)) {
//						
//						DList<IMyEntity> dList = DSpaceController
//								.createNewDList("mySpace", parameter, //space namelist logger mylist interface.name
//										new MyDListListener(), myList,
//										IMyEntity.class);
//						
//						map.put(parameter, dList);
//						
//					} else if ("delete".equals(command)) {
//						if (map.containsKey(parameter)) {
//							DSpaceController.destroyDList(map.get(parameter));
//							map.remove(parameter);
//						}
//					} else if ("print".equals(command)) {
//						if ("lists".equals(parameter)) {
//							Log.i(TAG,DSpaceController
//									.getDSpaceContainer().getSpaces().toString());
//						} else if ("peers".equals(parameter)) {
//							Log.i(TAG,DSpaceController
//									.getPeerContainer().getPeers().toString());
//						} else {
//							Log.i(TAG,"Wrong command. Skipping.");
//						}
//					} else {
//						Log.i(TAG,"Wrong command. Skipping.");
//					}
//				} else if (messageArray.length == 3) {
//					String command = messageArray[0].toLowerCase();
//					if ("print".equals(command)) {
//						String owner = messageArray[1].toLowerCase();
//						String listName = messageArray[2].toLowerCase();
//						DList<IMyEntity> dList = map.get(listName);
//						if (owner.equals(nickname)) {
//							for (IMyEntity myEntity : dList.get()) {
//								Log.i(TAG,System.identityHashCode(myEntity) + "  " + myEntity);
//							}
//						} else {
//							for (IMyEntity myEntity : dList.get(owner)) {
//								Log.i(TAG,System.identityHashCode(myEntity) + "  " + myEntity);
//							}
//						}
//					}
//				} else if (messageArray.length == 5) {
//					String command = messageArray[0].toLowerCase();
//					if ("remove".equals(command)) {
//						String owner = messageArray[1].toLowerCase();
//						String listName = messageArray[2].toLowerCase();
//						int index = Integer.valueOf(messageArray[3]);
//						Boolean byObject = Boolean.valueOf(messageArray[4].toLowerCase());
//						DList<IMyEntity> dList = map.get(listName);
//						if (byObject) {
//							if (owner.equals(nickname)) {
//								IMyEntity entity = addedObjects.get(index);
//								dList.remove(entity);
//							}
//						} else {
//							if (owner.equals(nickname)) {
//								dList.remove(index);
//							} else {
//								dList.remove(owner, index);
//							}
//						}
//						
//					}
//				} else if (messageArray.length == 6) {
//					String command = messageArray[0].toLowerCase();
//					if ("add".equals(command)) {
//						String owner = messageArray[1].toLowerCase();
//						String listName = messageArray[2].toLowerCase();
//						Integer index = Integer.valueOf(messageArray[3]);
//						String entLine = messageArray[4].toLowerCase();
//						Integer entInt = Integer.valueOf(messageArray[5]);
//						IMyEntity entity = new MyEntity(entLine, entInt);
//						DList<IMyEntity> dList = map.get(listName);
//						if (index < 0) {
//							if (owner.equals(nickname)) {
//								dList.add(entity);
//							} else {
//								dList.add(owner, entity);
//							}
//						} else {
//							if (owner.equals(nickname)) {
//								dList.add(index, entity);
//							} else {
//								dList.add(owner, index, entity);
//							}
//						}
//						addedObjects.add(entity);
//					}
//				}
//			}
//		}
	}
}
