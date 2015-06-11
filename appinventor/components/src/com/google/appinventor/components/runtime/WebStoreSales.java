// -*- mode: java; c-basic-offset: 2; -*-
// Copyright Esquel, All Rights reserved



package com.google.appinventor.components.runtime;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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
import com.google.appinventor.components.runtime.util.TibcoWMEventHandler;
import com.google.appinventor.components.runtime.util.TibcoWMManager;


/**
 * The Online Transaction Summary component tap into the CSP Retail Channel 
 * to receive latest summary on daily sales update of online channel (Magento).
 * Last update: Jan 30, 2015
 * @author makj@esquel.com (Jeffrey Mak EEL/IT)
 */

@DesignerComponent(version = YaVersion.WEB_COMPONENT_VERSION,
description = "Non-visible component that subscribe CSP Retail Channel to receive daily sales update of online Retail Channel",
category = ComponentCategory.DATADOTESQUEL,
nonVisible = true,
iconName = "images/onlineTransSummary.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "json.jar, wm-client.jar, wm-jms-client.jar, wm-geronimo-jms.jar, wm-support.jar")


public class WebStoreSales extends EsqEntityList implements Component, TibcoWMEventHandler {
  
  private static final String LOG_TAG = "WebStoreSales";
  
  //===== Jeffrey Extend ===========
  private String queryDate = "";
  private String todayDate = "";
  private String queryHost = "";
  private String wsHost = "";
  private NotificationManager nm;
  PendingIntent pending;
  private ComponentContainer _container;
  private String lastContent = "";
  private TibcoWMManager wmManager= null;
  //===== End Jeffrey Extend ===========
  

  /**
   * Creates a new Web component.
   *
   * @param container the Form that this component is contained in.
   */
  public WebStoreSales(ComponentContainer container) {
    super(container.$form());
    _container = container;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//2015-04-10
    todayDate = sdf.format(Calendar.getInstance().getTime());
    queryDate = todayDate;
    queryHost = "192.168.1.106:8224"; //Default service host
    wsHost = "192.168.1.106:8001"; //Default web messaging host
    
    //Init TibcoWMManager
    wmManager = new TibcoWMManager(this);
    wmManager.setDestinationText("/topic/stock");
    
  }
  
  /**
   * This constructor is for testing purposes only.
   */
  protected WebStoreSales() {
    super(null);
  }
  
  //=========Jeffrey Extend ==============
  /**
   * Returns the web service host for sales summary
   *
   * @return the host:port of the web service for sales summary
   */
  @SimpleProperty(
      description = "the host:port of the web service for pye sales summary",
      category = PropertyCategory.BEHAVIOR)
  public String QueryHost() {
    return queryHost;
  }

  /**
   * Specifies the web service host for sales summary
   *
   * @param host:port of the web service for sales summary
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "192.168.1.106:8224")
  @SimpleProperty()
  public void QueryHost(String host) {
    queryHost = host;
  }
  
  /**
   * Returns the web socket host for sales summary
   *
   * @return the host:port of the web socket for sales summary
   */
  @SimpleProperty(
      description = "the host:port of the web service for pye sales summary",
      category = PropertyCategory.BEHAVIOR)
  public String WSHost() {
    return wsHost;
  }

  /**
   * Specifies the web socket host for sales summary
   *
   * @param host:port of the web socket for sales summary
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "192.168.1.106:8001")
  @SimpleProperty()
  public void WSHost(String host) {
    wsHost = host;
  }
  
  /**
   * Returns the date of the query
   *
   * @return the date of the query for sales summary
   */
  @SimpleProperty(
      description = "the date of the pye sales summary",
      category = PropertyCategory.BEHAVIOR)
  public String QueryDate() {
    return queryDate;
  }

  /**
   * Specifies the date of the query for sales summary
   *
   * @param the date of the query for sales summary (YYYY-MM-DD)
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "2015-01-30")
  @SimpleProperty()
  public void QueryDate(String dateString) {
    queryDate = dateString;
  }
  
  
  @SimpleFunction(description = "Returns the name of the storeId field for EntityGrid Mapping.")
  public String getStoreIdFieldName() {
    return "storeId";
  }
  
  /**
   * Returns the name of the sku field
   */
  @SimpleFunction(description = "Returns the Picese Sold this month")
  public String getMonthlyPiecesFieldName() {
    return "monthly_pieces";
  }
  
  /**
   * Returns the name of the quantity field
   */
  @SimpleFunction(description = "Returns the Amount Sold this month")
  public String getMonthlyAmountFieldName() {
    return "monthly_amount";
  }
  
  /**
   * Returns the name of the sku field
   */
  @SimpleFunction(description = "Returns the Picese Sold today")
  public String getDailyPiecesFieldName() {
    return "daily_pieces";
  }
  
  /**
   * Returns the name of the quantity field
   */
  @SimpleFunction(description = "Returns the Amount Sold today")
  public String getDailyAmountFieldName() {
    return "daily_amount";
  }

  @SimpleFunction(description = "Get the Json Representation of the Entity")
  public String getJsonText() {
    return super.getJsonText();
  }
  
  //========End Jeffrey Extend =================
  
  /**
   * Performs an HTTP GET request using the Url property and retrieves the
   * response.<br>
   * If the SaveResponse property is true, the response will be saved in a file
   * and the GotFile event will be triggered. The ResponseFileName property
   * can be used to specify the name of the file.<br>
   * If the SaveResponse property is false, the GotText event will be
   * triggered.
   */
  @SimpleFunction
  public void Get() {
    // Capture property values in local variables before running asynchronously.
    super.Get();
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
      if(wmManager.isConnected()) {
        wmManager.handleSubscribe();
      }
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
  
  /**
   * Get Web Messaging Connection Log
   */
  @SimpleFunction
  public String GetLog() {
    // Capture property values in local variables before running asynchronously.
    return wmManager.getInternalMsg();
  }
  
  /**
   * Get Web Messaging Connection Log Polarity
   */
  @SimpleFunction
  public int GetLogPolarity() {
    // Capture property values in local variables before running asynchronously.
    return wmManager.getInternalMsgPolarity();
  }
  
  /**
   * Event indicating that a request has finished.
   *
   * @param url the URL used for the request
   * @param responseCode the response code from the server
   * @param responseType the mime type of the response
   * @param responseContent the response content from the server
   */
  @SimpleEvent
  @Override
  public void GotResult(String url, int responseCode, String responseType, String responseContent) {
    // Received the response in XML or JSon. 
    // This function should parse the XML/JSon and set all attributes with values.
    /*
    responseContent = "["
    +"{ \"storeId\" : \"Hong Kong - Central\",\"quantity\" : \"1\",\"amount\" : \"HKD-989\",\"sku\" : \"fw3154-1\"},"
    +"{ \"storeId\" : \"Hong Kong - Admiralty\",\"quantity\" : \"1\",\"amount\" : \"HKD-1089\",\"sku\" : \"fw3151-1\"},"
    +"{ \"storeId\" : \"Hong Kong - Central\",\"quantity\" : \"1\",\"amount\" : \"HKD-980\",\"sku\" : \"fw3157-1\"},"
    +"{ \"storeId\" : \"Shanghai\",\"quantity\" : \"1\",\"amount\" : \"RMB-1030\",\"sku\" : \"fw3121-1\"}]";
    */
    
    this.setJsonText(responseContent); //Store up the json Response.
    
    //Fetch the event back to "Blocks" to run user defined behavior.
    EventDispatcher.dispatchEvent(this, "GotResult", url, responseCode, responseType,
        responseContent);
  }
  
  /**
   * Event indicating that a request has finished.
   *
   * @param url the URL used for the request
   * @param responseCode the response code from the server
   * @param responseType the mime type of the response
   * @param responseContent the response content from the server
   */
  @SimpleEvent
  public void GotMessage(String url, int responseCode, String responseType, String responseContent) {
    // Received the response in XML or JSon. 
    // This function should parse the XML/JSon and set all attributes with values.
    //Send Notification if updated
    if(responseContent.compareTo(lastContent) != 0) {
      this.setJsonText(responseContent); //Store up the json Response.
    
      //Fetch the event back to "Blocks" to run user defined behavior.
      EventDispatcher.dispatchEvent(this, "GotMessage", url, responseCode, responseType,
        responseContent);
    

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      sendNotification("Pye Store Sales Notification", "New update at " + sdf.format(Calendar.getInstance().getTime()));
      lastContent = responseContent;
    }
    
  }
  
  @Override
  protected String getURL() {
    // TODO Auto-generated method stub
    return "http://" + queryHost + "/OfflineStoreDailySales?date=" + queryDate;
  }
  
  private String getWSURL() {
    return "ws://" + wsHost + "/jms";
  }
  
  private void sendNotification(String title, String msg) {
    
    nm = (NotificationManager) _container.$context().getSystemService(Context.NOTIFICATION_SERVICE);
    pending =PendingIntent.getActivity(_container.$context().getApplicationContext(), 0, new Intent(),0);
    Notification notify=new Notification(android.R.drawable.stat_notify_more,title,System.currentTimeMillis());
    //Notification notify=new Notification(android.R.drawable.pye,title,System.currentTimeMillis());
    
    notify.setLatestEventInfo(_container.$context().getApplicationContext(), title, msg,pending);
    nm.notify(0, notify);
    
  }
  
  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof TextMessage) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                wmManager.logMessage("Updated at " + sdf.format(Calendar.getInstance().getTime()), 0);
                GotMessage(getWSURL(), 0, "application/json", ((TextMessage) message).getText());
      }
      else {
        wmManager.logMessage("Unknown message type is received: "+message.getClass().getSimpleName(), -1);
      }
    }
    catch (Exception ex) {
      //ex.printStackTrace();
      wmManager.logMessage("Messaging failure. Reason:" + ex.getMessage(), -1);
    }
  }

  @Override
  public void onConnected() {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public Activity getContext() {
    // TODO Auto-generated method stub
    return this.form.$context();
  }
}
