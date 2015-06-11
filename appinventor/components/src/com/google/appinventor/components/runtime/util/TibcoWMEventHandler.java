package com.google.appinventor.components.runtime.util;

import javax.jms.MessageListener;

import android.app.Activity;

/**
 * This is the singleton for connecting with WebMessaging Gateway. 
 * 
 * Last update: May 2, 2015
 * @author makj@esquel.com (Jeffrey Mak EEL/IT)
 */

public interface TibcoWMEventHandler extends MessageListener
{
   public abstract void onConnected();
   //public abstract void onSubscribed();
   //public abstract void onDisconnected();
   //public abstract void onUnSubscribed();
   public abstract Activity getContext();
}