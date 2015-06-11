// -*- mode: java; c-basic-offset: 2; -*-
// Copyright Esquel, All Rights reserved



package com.google.appinventor.components.runtime;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.TibcoWMManager;


/**
 * The PublishSubscribe component is the connector to the Esquel ESB to subscribe Enterprise Events.
 * It carries the capability to provide notification when new message is received.
 * It carries the capability to do deduplication if the message received is the same as the last one.
 * Last update: May 10, 2015
 * @author makj@esquel.com (Jeffrey Mak EEL/IT)
 */

@DesignerComponent(version = YaVersion.WEB_COMPONENT_VERSION,
description = "Non-visible component that subscribe Esquel ESB to receive Enterprise Events." +
               "It carries capability to provide notifications and deduplication",
category = ComponentCategory.DATADOTESQUEL,
nonVisible = true,
iconName = "images/publish.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "json.jar, wm-client.jar, wm-jms-client.jar, wm-geronimo-jms.jar, wm-support.jar")


public class PublishSubscribe extends AndroidNonvisibleComponent implements Component, MessageListener {
  
  private static final String LOG_TAG = "PublishSubscribe";
  
  //===== Jeffrey Extend ===========
  private String url = "";
  private String topic = "";
  private boolean deduplication;
  
  PendingIntent pending;
  private TibcoWMManager wmManager= null;
  private String lastContent = "";
  private final Component selfCom;
  private final Activity activity;
  //===== End Jeffrey Extend ===========
  
  /**
   * Creates a new Web component.
   *
   * @param container the Form that this component is contained in.
   */
  public PublishSubscribe(ComponentContainer container) {
    super(container.$form());
    url = "ws://<host>:<port>"; //Default web messaging host
    topic = "/topic/sample";
    deduplication = false;
    
    //Init TibcoWMManager
    wmManager = new TibcoWMManager(this);
    wmManager.setDestinationText(topic);
    selfCom = this;
    activity = container.$context();
    
  }
  
  //=========Jeffrey Extend ==============
  
  /**
   * Returns the web socket host for sales summary
   *
   * @return the host:port of the web socket for sales summary
   */
  @SimpleProperty(
      description = "the host:port of the web service for pye sales summary",
      category = PropertyCategory.BEHAVIOR)
  public String Url() {
    return url;
  }

  /**
   * Specifies the web socket host for sales summary
   *
   * @param host:port of the web socket for sales summary
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "ws://<host>:<port>")
  @SimpleProperty()
  public void Url(String host) {
    url = host;
  }
  
  /**
   * Returns the name of the subscribing topic: /topic/<name>
   *
   * @return the name of the subscribing topic: /topic/<name>
   */
  @SimpleProperty(
      description = "the name of the subscribing topic: /topic/<name>",
      category = PropertyCategory.BEHAVIOR)
  public String Topic() {
    return topic;
  }

  /**
   * Specifies the name of the subscribing topic: /topic/<name>
   *
   * @param the name of the subscribing topic: /topic/<name>
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "/topic/sample")
  @SimpleProperty()
  public void Topic(String name) {
    topic = name;
    wmManager.setDestinationText(topic);
  }

  /**
   * Returns the deduplication setting. If true, repeating message will be discarded silently.
   *
   * @return the deduplication setting. If true, repeating message will be discarded silently.
   */
  @SimpleProperty(
      description = "the deduplication setting. If true, repeating message will be discarded silently.",
      category = PropertyCategory.BEHAVIOR)
  public boolean Deduplication() {
    return deduplication;
  }

  /**
   * Specifies the deduplication setting. If true, repeating message will be discarded silently.
   *
   * @param the deduplication setting. If true, repeating message will be discarded silently.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "false")
  @SimpleProperty()
  public void Deduplication(boolean setting) {
    deduplication = setting;
  }
  
  /**
   * Get Web Messaging Connection Status
   */
  @SimpleFunction
  public String GetStatus() {
    // Capture property values in local variables before running asynchronously.
    return wmManager.getInternalMsg();
  }
  
  /**
   * Get Web Messaging Connection Status Polarity
   */
  @SimpleFunction
  public int GetStatusPolarity() {
    // Capture property values in local variables before running asynchronously.
    return wmManager.getInternalMsgPolarity();
  }
  
  /**
   * Initiate the connection to the Web Messaging Gateway
   */
  @SimpleFunction
  public void Connect() {
    // Capture property values in local variables before running asynchronously.
    wmManager.setLocationText(getWSURL());
    //Connect
    try {
      wmManager.handleConnect();
      wmManager.handleSubscribe();
    } catch(Exception e) {
      wmManager.logMessage("Disconnected. Reason: " + e.getMessage(), -1);
    }
  }
  
  /**
   * Initiate the disconnection to the Web Messaging Gateway
   */
  @SimpleFunction
  public void Disconnect() {
    // Capture property values in local variables before running asynchronously.
    try {
      if(wmManager.isConnected()) {
        wmManager.handleUnsubscribe();
        wmManager.handleDisconnect();
      }
    } catch(Exception e) {
      wmManager.logMessage("Disconnected. Reason: " + e.getMessage(), -1);
    }
  }

  //========End Jeffrey Extend =================
  
  /**
   * Event indicating that an event has arrived.
   *
   * @param url the URL used for the request
   * @param responseContent the response content from the server
   */
  @SimpleEvent
  public void GotEvent(String url, String responseContent) {
    // Received the response in XML or JSon. 
    // dispatch the event if: (1) No de-duplication is needed; OR (2) A different message is received;
    if(!deduplication || responseContent.compareTo(lastContent) != 0) {
      //Fetch the event back to "Blocks" to run user defined behavior.
      lastContent = responseContent;
      activity.runOnUiThread(new Runnable()    
      {    
          public void run()    
          {    
            EventDispatcher.dispatchEvent(selfCom, "GotEvent", getWSURL(), lastContent);    
          }
      });
      
    }
  }
  
  private String getWSURL() {
    return url + "/jms";
  }
  
  @Override
  public void onMessage(Message message) {
    
      if (message instanceof TextMessage) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        wmManager.logMessage("Updated at " + sdf.format(Calendar.getInstance().getTime()), 0);wmManager.logMessage("Updated at " + sdf.format(Calendar.getInstance().getTime()), 0);
              try {
                GotEvent(getWSURL(), ((TextMessage) message).getText());
              }
              catch (Exception ex) {
                //ex.printStackTrace();
                wmManager.logMessage("Messaging failure. Reason:" + ex.getMessage(), -1);
              }
      }
      else {
        wmManager.logMessage("Unknown message type is received: "+message.getClass().getSimpleName(), -1);
      }

  }
}
