/*
 * Copyright 2014 Gurtam
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 */

package com.wialon.java.test;

import com.wialon.core.Errors;
import com.wialon.core.EventHandler;
import com.wialon.core.EventProvider;
import com.wialon.core.Session;
import com.wialon.extra.UpdateSpec;
import com.wialon.item.Item;
import com.wialon.item.Unit;
import com.wialon.remote.handlers.ResponseHandler;

import java.io.IOException;
import java.util.Collection;

public class EventsManagementExample implements Runnable {
	private Session session;

	// Login to server
	private void login(){
		// initialize Wialon session
		session.initSession("https://local3.overseer.ua");
		// trying login
		session.loginToken("bbe8d60e3d5c69ec30d0519750b0c8707459F3DAB607BC434346CC128EB4E115BE565F16", new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// login succeed
				System.out.println(String.format("Logged successfully. User name is %s", session.getCurrUser().getName()));
				//call update data flags
				updateDataFlags();
			}

			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// login failed, print error
				System.out.println(Errors.getErrorText(errorCode));
			}
		});
	}

	private void updateDataFlags(){
		//Create new update specification
		UpdateSpec updateSpec=new UpdateSpec();
		//Set update mode to add specified flags
		updateSpec.setMode(1);
		//Set update type by type
		updateSpec.setType("type");
		//Set update data to units
		updateSpec.setData(Item.ItemType.avl_unit);
		//Set update data flags
		updateSpec.setFlags(Item.dataFlag.base.getValue()| Unit.dataFlag.lastMessage.getValue());
		//Send update by created update specification
		session.updateDataFlags(new UpdateSpec[]{updateSpec}, new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// Update succeed
				System.out.println("Update data flags is successful");
				bindEventsHandler();
			}
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// update data flags failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				logout();
			}
		});
	}

	private void bindEventsHandler(){
		//Get items from session
		Collection<Item> sessionItems=session.getItems();
		//Create events handler
		EventsHandlerExample eventsHandlerExample=new EventsHandlerExample();
		//Add handler to session
		session.addListener(eventsHandlerExample, EventProvider.events.All);
		if (sessionItems!=null && sessionItems.size()>0) {
			System.out.println(String.format("%d items added to Session\r\nStart binding event listeners to items", sessionItems.size()));
			//Add handler to items
			for (Item item : sessionItems)
				item.addListener(eventsHandlerExample, EventProvider.events.All);
			System.out.println("Event listeners successfully bound to items\r\nPress enter to logout and exit");
		}
		//Wait for press enter key
		try {
			System.in.read();
			logout();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Logout
	private void logout(){
		session.logout(new ResponseHandler() {
			@Override
			public void onSuccess(String response) {
				super.onSuccess(response);
				// logout succeed
				System.out.println("Logout successfully");
				System.exit(0);
			}
			@Override
			public void onFailure(int errorCode, Throwable throwableError) {
				super.onFailure(errorCode, throwableError);
				// logout failed, print error
				System.out.println(Errors.getErrorText(errorCode));
				System.exit(0);
			}
		});
	}
	//EventHandler witch listen bound event types
	private class EventsHandlerExample implements EventHandler{
		@Override
		public void onEvent(Enum event, Object object, Object oldData, Object newData) {
			String objectValue= object==null ? "null" : object.toString();
			String oldDataValue= oldData==null ? "null" : oldData.toString();
			String newDataValue= newData==null ? "null" : newData.toString();
			System.out.println(String.format("Fired event with type %s, object is %s, oldData is %s, newData is %s", event, objectValue, oldDataValue, newDataValue));
		}
	}

	@Override
	public void run() {
		// get instance of current Session
		session=Session.getInstance();
		login();
	}

	public static void main(String[] args){
		new Thread(new EventsManagementExample()).start();
	}
}
