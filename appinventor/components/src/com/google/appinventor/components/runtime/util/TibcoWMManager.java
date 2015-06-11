package com.google.appinventor.components.runtime.util;

import java.net.PasswordAuthentication;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.HashMap;

import com.kaazing.gateway.jms.client.ConnectionDisconnectedException;
import com.kaazing.gateway.jms.client.JmsConnectionFactory;
import com.kaazing.net.auth.BasicChallengeHandler;
import com.kaazing.net.auth.ChallengeHandler;
import com.kaazing.net.auth.LoginHandler;
import com.kaazing.net.ws.WebSocketFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * This is the singleton for connecting with WebMessaging Gateway. 
 * 
 * Last update: May 2, 2015
 * @author makj@esquel.com (Jeffrey Mak EEL/IT)
 */

public class TibcoWMManager {
  
  private JmsConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private Boolean isConnected;
  
  public DispatchQueue dispatchQueue;
  private String locationText;
  private String destinationText;
  private String username;
  private char[] password;
  private MessageListener listener;
  private String internalMsg;
  private int internalMsgPolarity; //1=positive; 0=neutral; -1=negative used for UI
  
  private HashMap<String, ArrayDeque<MessageConsumer>> consumers = new HashMap<String, ArrayDeque<MessageConsumer>>();
  
  public TibcoWMManager(MessageListener _listener) {
    isConnected = false;
    listener = _listener;
    internalMsg = "";
    internalMsgPolarity = 0; 
    onCreate();
  }
  
  public void setLocationText(String location) {
    locationText = location;
  }
  
  public void setDestinationText(String destination) {
    destinationText = destination;
  }
  
  public void setCredential(String _username, char[] _password) {
    username = _username;
    password = _password;
  }
  
  public String getInternalMsg() {
    return internalMsg;
  }
  
  public int getInternalMsgPolarity() {
    return internalMsgPolarity;
  }
  
  public Boolean isConnected() {
    return isConnected;
  }
  
  /**
   * Called when the activity is first created.
   * @param savedInstanceState If the activity is being re-initialized after 
   * previously being shut down then this Bundle contains the data it most 
   * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
   */

  public void onCreate() {
      
      if (connectionFactory == null) {
        try {
          connectionFactory = JmsConnectionFactory.createConnectionFactory();
          WebSocketFactory webSocketFactory = connectionFactory.getWebSocketFactory();
          webSocketFactory.setDefaultChallengeHandler(createChallengehandler());
        } catch (JMSException e) {
          //e.printStackTrace();
          logMessage("Fail to start. Reason: " + e.getMessage(), -1);
          
        }
      }
  }
  
  private ChallengeHandler createChallengehandler() {
    final LoginHandler loginHandler = new LoginHandler() {
          @Override
          public PasswordAuthentication getCredentials() {
            return new PasswordAuthentication(username, password);
          }
    };
    BasicChallengeHandler challengeHandler = BasicChallengeHandler.create();
    challengeHandler.setLoginHandler(loginHandler);
    return challengeHandler;
  }
  
  public void logMessage(final String message, final int polarity) {
    internalMsg = message;
    internalMsgPolarity = polarity;
  }
  
  public void appendLogMessage(final String message) {
    internalMsg += message;
  }
  
  public void handleConnect() {
    if(!isConnected) {
      dispatchQueue = new DispatchQueue("DispatchQueue");
      dispatchQueue.start();
      dispatchQueue.waitUntilReady();
      connect();
    }
  }
  
  public void handleDisconnect() {
    disconnect();
  }
  
  public void handleSubscribe() {
    final String destinationName = destinationText;
    logMessage("Try to Subscribe...", 0);
    dispatchQueue.dispatchAsync(new Runnable() {
      public void run() {
        logMessage("In DispatchAsync Subscribe", 0);
        try {
          if (session == null) {
            //This will happened if connection to gateway is failed.
            logMessage("Unsubscribed. Reason: Session is not obtained.", -1);
            return;
          }
          
          Destination destination = getDestination(destinationName);
          if (destination == null) {
            logMessage("Unsubscribed. Reason: Cannot find destination name:" + destinationName, -1);
            return;
          }

          MessageConsumer consumer = session.createConsumer(destination);
          ArrayDeque<MessageConsumer> consumersToDestination = consumers.get(destinationName);
          if (consumersToDestination == null) {
            consumersToDestination = new ArrayDeque<MessageConsumer>();
            consumers.put(destinationName, consumersToDestination);
          }
          consumersToDestination.add(consumer);
          consumer.setMessageListener(listener);
          logMessage("Listening to updates...", 0);
        } catch (JMSException e) {
          //e.printStackTrace();
          logMessage("Unsubscribed. Reason: " + e.getMessage(), -1);
        }
      }
    });
  }
  
  public void handleUnsubscribe() {
    String destinationName = destinationText;
    logMessage("Try to unsubscribe...", 0);
    ArrayDeque<MessageConsumer> consumersToDestination = consumers.get(destinationName);
    if (consumersToDestination == null) {
      logMessage("Unsubscribed", 0);
      return;
    }
    final MessageConsumer consumer = consumersToDestination.poll();
    if (consumer == null) {
      logMessage("Unsubscribed", 0);
      return;
    }
    dispatchQueue.dispatchAsync(new Runnable() {  
      public void run() {
        try {
          consumer.close();
          logMessage("Unsubscribed", 0);
        } catch (JMSException e) {
          //e.printStackTrace();
          logMessage("Unsubscription failure. Reason: " + e.getMessage(), -1);
        }
      }
    });
  }
  
  
  private Destination getDestination(String destinationName) throws JMSException {
    Destination destination;
    if (destinationName.startsWith("/topic/")) {
      destination = session.createTopic(destinationName);
    }
    else if (destinationName.startsWith("/queue/")) {
      destination = session.createQueue(destinationName);
    }
    else {
      logMessage("Invalid destination name: " + destinationName, -1);
      return null;
    }
    return destination;
  }
  
  private void connect() {
    
    logMessage("Trying to connect...", 0);
    
    // Since createConnection() is a blocking method which will not return until 
    // the connection is established or connection fails, it is a good practice to 
    // establish connection on a separate thread so that UI is not blocked.
    dispatchQueue.dispatchAsync(new Runnable() {
      public void run() {
        try {
          appendLogMessage("In dispatchAsync of Connect");
          String location = locationText;
          connectionFactory.setGatewayLocation(URI.create(location));
          connection = connectionFactory.createConnection();
          connection.start();
          session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
          connection.setExceptionListener(new ConnectionExceptionListener()); 
          isConnected = true;
          logMessage("Connected", 1);
          logMessage("End DispatchAsync of Connect", 0);
        } catch (Exception e) {
          isConnected = false;
          //e.printStackTrace();
          logMessage("Disconnected. Reason: " + e.getMessage(), -1);
        }
      }
    }); 
  }
  
  private void disconnect() {
    logMessage("Try to disconnect...", 0);
    
    dispatchQueue.removePendingJobs();
    dispatchQueue.quit();
    new Thread(new Runnable() {
      public void run() {
        try {
          connection.close();
          logMessage("Disconnected at 1", 0);
        } catch (JMSException e) {
          //e.printStackTrace();
          logMessage("Disconnection failed. Reason: " + e.getMessage(), -1);
        }
        finally {
          connection = null;
          isConnected = false;
        }
      }
    }).start();
  }
  
  private class ConnectionExceptionListener implements ExceptionListener {
    public void onException(final JMSException exception) {
      logMessage(exception.getMessage(), -1);
      if (exception instanceof ConnectionDisconnectedException) {
        isConnected = false;
      }
    }
  }
}
