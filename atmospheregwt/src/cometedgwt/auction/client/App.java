/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 */
package cometedgwt.auction.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import cometedgwt.auction.entity.AuctionItem;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class App implements EntryPoint {

	private final String streamingServlet = GWT.getModuleBaseURL() + "streamingServlet";
	private final String streamingService = GWT.getModuleBaseURL() + "streamingService";

	public static final String TOPIC = "bids";
	private Map<Object, Label> mapOfItemPrices = new HashMap<Object, Label>();
	private Map<Object, Label> mapOfNumberOfBids = new HashMap<Object, Label>();

	private int watchDogTimerTime = 100000;
	Map<Object, AsyncCallback> callbacks = new HashMap<Object, AsyncCallback>();
	private boolean keepAlive = false;
	private final StreamingServiceAsync service = (StreamingServiceAsync) GWT.create(StreamingService.class);
	private final Map waitingSubscriber = new HashMap();

	public void onModuleLoad() {

		List itens = getAuctionItens();
		Grid table = new Grid(itens.size() + 1, 6);
		table.setStylePrimaryName("corpo");

		table.setText(0, 0, "Item Name");
		table.setText(0, 1, "# of bids");
		table.setText(0, 2, "Price");
		table.setText(0, 3, "My bid");

		for (int i = 0; i < itens.size(); i++) {

			final AuctionItem item = (AuctionItem) itens.get(i);

			final int itemId = item.getId();
			final Label labelNumberOfBids = new Label(String.valueOf(item.getNumberOfBids()));
			final Label labelPrice = new Label("$ " + String.valueOf(item.getPrice()));
			final TextBox txtBoxMyBid = new TextBox();
			final Button bidButton = new Button("Bid!");
			final Label labelMessage = new Label("");

			bidButton.setStylePrimaryName("principal");

			// Save itemPrice Label to be used when new bids are processed.
			mapOfItemPrices.put(new Integer(itemId), labelPrice);
			// Save numberOfBids Label to be used when new bids are processed.
			mapOfNumberOfBids.put(new Integer(itemId), labelNumberOfBids);

			// Handle ENTER key
			txtBoxMyBid.addKeyUpHandler(new KeyUpHandler() {

				@Override
				public void onKeyUp(KeyUpEvent event) {
					if (event.getNativeKeyCode() == '\r') {
						sendNewBid(item, txtBoxMyBid, labelMessage);
					}

				}
			});

			// Handle button click
			bidButton.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					sendNewBid(item, txtBoxMyBid, labelMessage);
				}
			});

			table.setText(i + 1, 0, item.getName());
			table.setWidget(i + 1, 1, labelNumberOfBids);
			table.setWidget(i + 1, 2, labelPrice);
			table.setWidget(i + 1, 3, txtBoxMyBid);
			table.setWidget(i + 1, 4, bidButton);
			table.setWidget(i + 1, 5, labelMessage);

		}

		RootPanel.get("slot1").add(table);

		callbacks.put("keepAliveInternal", internalKeepAliveCallback);
		callbacks.put("restartStreamingInternal", restartStreamingCallback);

		((ServiceDefTarget) service).setServiceEntryPoint(streamingService);

		setUpNativeCode(this);

		restartStreamingFromIFrame();

		createWatchDogTimer();

		subScribeToEvent(TOPIC, new BidCallback());

	}

	private final static AsyncCallback<Void> voidCallback = new AsyncCallback<Void>() {

		public void onFailure(Throwable caught) {
		}

		public void onSuccess(Void result) {
		}
	};
	private final AsyncCallback<String> restartStreamingCallback = new AsyncCallback<String>() {

		public void onFailure(Throwable caught) {
		}

		public void onSuccess(String result) {
			restartStreamingFromIFrame();
			callback("restartStreaming", result);
		}
	};
	/**
	 * Receive hearthbeats from the streaming server. It sets keepAlive flag to
	 * true to prevent the Timer to restart the streaming and then call the
	 * client callback (maybe the client has to do something with the heartbeat
	 * itself).
	 */
	private final AsyncCallback<String> internalKeepAliveCallback = new AsyncCallback<String>() {

		public void onFailure(Throwable caught) {
		}

		public void onSuccess(String result) {

			alert("keepAlive");
			keepAlive = true;
			watchDogTimerTime = 10 * Integer.parseInt(result);

			for (Iterator iter = waitingSubscriber.entrySet().iterator(); iter.hasNext();) {
				Entry callback = (Entry) iter.next();

				/*
				 * Take care, the Map implementation can be different from his
				 * Java counterpart. I think it uses object identity instead of
				 * object equality, so for instance two equals String done in
				 * different way can lend to two different keys in the map. Try
				 * this
				 * 
				 * void testMap(Object key, Object value) { Map test = new
				 * HashMap(); test.put(key.toString(),value);
				 * 
				 * if(!test.containsKey("testKey")) {
				 * GWT.log("bug in map found !!!",null); } }
				 * 
				 * If called this way:
				 * 
				 * testMap("testKey", new Object());
				 * 
				 * that code will print the warning row !!!!! Should be fixed.
				 */
				subScribeToEvent((String) callback.getKey(), (AsyncCallback) callback.getValue());

				iter.remove();
			}

			callback("keepAlive", "");
		}
	};

	/**
	 * If we have a callback for the "event" (and we should if we subscribed)
	 * then let's call it.
	 * 
	 * @param event
	 *            : contains the event we subscribed
	 * @param data
	 *            : datas that come from the server
	 */
	private void callback(String topicName, String data) {
		keepAlive = true;

		alert("received callback for (" + topicName + "," + data + ")");

		if (callbacks.containsKey(topicName)) {
			AsyncCallback callback = (AsyncCallback) callbacks.get(topicName);

			try {
				Object dataToSend = data;

				if (data.startsWith("$JSONSTART$") && data.endsWith("$JSONEND$")) {
					dataToSend = JSONParser.parse(data.substring("$JSONSTART$".length(), data.length() - "$JSONEND$".length()));
				}

				callback.onSuccess(dataToSend);
			} catch (JSONException e) {
				callback.onFailure(e);
			}
		} else {
			alert("received event for a not subscribed topic: '" + topicName + "'");
			alert("current topics are: " + callbacks.keySet());
		}
	}

	/**
	 * Setup the two Javascript method used for the callback from the server
	 * 
	 * @param thisInstance
	 *            : a trick, because I was unable to use this !!!
	 */
	private native void setUpNativeCode(App app) /*-{
		$wnd.callback = function(topicName, data)
		{
		app.@cometedgwt.auction.client.App::callback(Ljava/lang/String;Ljava/lang/String;)(topicName,data);
		}
	}-*/;

	/**
	 * A Timer that every 20s check if everything is working.
	 * 
	 */
	private void createWatchDogTimer() {
		Timer t = new Timer() {

			public void run() {
				if (!keepAlive) {
					alert("the dog is angry !!! Awake streaming !!!");
					restartStreamingFromIFrame();
				}

				keepAlive = false;
			}
		};
		t.scheduleRepeating(watchDogTimerTime);
	}

	/**
	 * Uses DOM to create, if necessary, the iframe, then sets the src attribute
	 * to start the streaming. It's important to clear the "old" iframe when
	 * restarting, or spurios Javascript can send old event to the callback
	 * method
	 */
	private void restartStreamingFromIFrame() {
		Element iframe = DOM.getElementById("__gwt_streamingFrame");

		if (iframe != null) {
			DOM.removeChild(RootPanel.getBodyElement(), iframe);
		}

		iframe = DOM.createIFrame();
		DOM.setAttribute(iframe, "id", "__gwt_streamingFrame");
		DOM.setStyleAttribute(iframe, "width", "0");
		DOM.setStyleAttribute(iframe, "height", "0");
		DOM.setStyleAttribute(iframe, "border", "0");

		DOM.appendChild(RootPanel.getBodyElement(), iframe);

		DOM.setAttribute(iframe, "src", streamingServlet);
	}

	public void sendMessage(String topicName, String data) {
		service.sendMessage(topicName, data, voidCallback);
	}

	public void sendMessage(String topicName, JSONValue object) {
		sendMessage(topicName, "$JSONSTART$" + object.toString() + "$JSONEND$");
	}

	public void subScribeToEvent(String topicName, AsyncCallback callback) {
		if (keepAlive) {
			alert("Streaming is alive, subscribing to '" + topicName + "' with callback " + callback);
			service.subscribeToTopic(topicName, voidCallback);
			callbacks.put(topicName, callback);

			alert(callbacks.toString());
		} else {
			alert("Streaming is not alive, subscriber '" + topicName + "' is cached with callback " + callback + " until online");

			waitingSubscriber.put(topicName, callback);
		}
	}

	private final TextArea textArea = new TextArea();

	private void alert(String message) {
		if (GWT.isScript()) {
			RootPanel debugDiv = RootPanel.get("debug");
			if (debugDiv != null) {
				if (debugDiv.getWidgetIndex(textArea) == -1) {
					textArea.setVisibleLines(30);
					textArea.setWidth("100%");
					textArea.setText("");
					debugDiv.add(textArea);
				}

				textArea.setText(textArea.getText() + "\n" + new Date() + "(" + System.currentTimeMillis() + "):" + message);
			}
		} else {
			GWT.log(message, null);
		}
	}

	//
	// Ajax Push happens here!
	//
	private class BidCallback implements AsyncCallback {

		public void onFailure(Throwable throwable) {
			Window.alert(throwable.getMessage());
		}

		public void onSuccess(Object result) {

			JSONObject resultObject = (JSONObject) result;
			JSONArray resultArray = (JSONArray) resultObject.get("value");
			JSONNumber itemId = (JSONNumber) resultArray.get(0);
			JSONNumber itemPrice = (JSONNumber) resultArray.get(1);
			JSONNumber numberOfBids = (JSONNumber) resultArray.get(2);

			Integer itemKey = new Integer((int) itemId.getValue());
			((Label) mapOfItemPrices.get(itemKey)).setText("$ " + itemPrice.toString());
			((Label) mapOfNumberOfBids.get(itemKey)).setText("" + Double.valueOf(numberOfBids.toString()).intValue());
		}
	}

	/**
	 * This is the entry point method.
	 */

	private List getAuctionItens() {

		// TODO Get them from server side.

		AuctionItem item1 = new AuctionItem(0, "Nokia N80", 100.0f);
		AuctionItem item2 = new AuctionItem(1, "Laptop Apple PowerBook G4 17''", 1050.0f);
		AuctionItem item3 = new AuctionItem(2, "Canon Rebel XT", 800.0f);

		List itens = new ArrayList();
		itens.add(item1);
		itens.add(item2);
		itens.add(item3);

		return itens;
	}

	private void sendNewBid(AuctionItem item, TextBox myBid, Label message) {

		int itemId = item.getId();
		double lastBid = item.getPrice();

		String newBid = myBid.getText();
		double newBidValue = 0.0;

		try {
			newBidValue = Double.parseDouble(newBid);
		} catch (NumberFormatException e) {
			message.setText("Not a valid bid");
			return;
		}

		if (newBidValue < lastBid) {
			message.setText("Not a valid bid");
			return;
		}

		message.setText("");

		item.setPrice(newBidValue);
		int numberOfBids = item.getNumberOfBids();

		JSONArray array = new JSONArray();
		array.set(0, new JSONNumber(itemId));
		array.set(1, new JSONNumber(newBidValue));
		array.set(2, new JSONNumber(numberOfBids));

		JSONObject container = new JSONObject();
		container.put("value", array);

		sendMessage(TOPIC, container);
		myBid.setText("");
		myBid.setFocus(true);
	}
}
