// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.webkit.JavascriptInterface;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The GridView is used to display a JSon array in table format.
 * Last update: May 14, 2015
 * @author makj@esquel.com (Jeffrey Mak EEL/IT)
 */

@DesignerComponent(version = YaVersion.WEBVIEWER_COMPONENT_VERSION,
    category = ComponentCategory.DATADOTESQUEL,
    description = "GridView is a custom-made easy-to-use view to show a list of data "
        + "from Data.Esquel in table format.")


@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
public final class GridView extends AndroidViewComponent {

  private final WebView webview;

  // URL for the WebViewer to load initially
//  private String homeUrl;

  // whether or not to follow links when they are tapped
  private boolean followLinks = true;

  // Whether or not to prompt for permission in the WebViewer
  private boolean prompt = false;

  // ignore SSL Errors (mostly certificate errors. When set
  // self signed certificates should work.

  private boolean ignoreSslErrors = false;
  private String tableHead = "<div ng-app=\"myApp\"> <div ng-controller=\"EntityCtrl\" class=\"datagrid\"><table> <thead><tr>";
  
  // allows passing strings to javascript
  WebViewInterface wvInterface;

  private LinkedList<String> titleList = new LinkedList<String>();
  private LinkedList<String> fieldList = new LinkedList<String>();
  private String dataPath = "";
//  private String htmlString = "";
  
  /**
   * Creates a new WebViewer component.
   *
   * @param container  container the component will be placed in
   */
  public GridView(ComponentContainer container) {
    super(container);

    webview = new WebView(container.$context());
    resetWebViewClient();       // Set up the web view client
    webview.getSettings().setJavaScriptEnabled(true);
    webview.getSettings().setDefaultTextEncodingName("utf-8");
    webview.setFocusable(true);
    // adds a way to send strings to the javascript
    wvInterface = new WebViewInterface(webview.getContext());
    webview.addJavascriptInterface(wvInterface, "AppInventor");
    // enable pinch zooming and zoom controls
    webview.getSettings().setBuiltInZoomControls(true);
    webview.getSettings().setDisplayZoomControls(false);
    webview.setBackgroundColor(Color.TRANSPARENT);
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
      EclairUtil.setupWebViewGeoLoc(this, webview, container.$context());

    container.$add(this);

    webview.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
          case MotionEvent.ACTION_UP:
            if (!v.hasFocus()) {
              v.requestFocus();
            }
            break;
        }
        return false;
      }
    });

    // set the initial default properties.  Height and Width
    // will be fill-parent, which will be the default for the web viewer.

//    HomeUrl("");
    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
  }


  
//  @SimpleProperty(
//      description = "The HTML String representing the table.",
//      category = PropertyCategory.BEHAVIOR)
//  public String HtmlString() {
//    return htmlString;
//  }
  
  /**
   * Gets the web view string
   *
   * @return string
   */
//  @SimpleProperty(description = "Gets the WebView's String, which is viewable through " +
//      "Javascript in the WebView as the window.AppInventor object",
//      category = PropertyCategory.BEHAVIOR)
  public String WebViewString() {
    return wvInterface.getWebViewString();
  }

  /**
   * Sets the web view string
   */
//  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void WebViewString(String newString) {
    wvInterface.setWebViewString(newString);
  }

  @Override
  public View getView() {
    return webview;
  }

  // Create a class so we can override the default link following behavior.
  // The handler doesn't do anything on its own.  But returning true means that
  // this do nothing will override the default WebVew behavior.  Returning
  // false means to let the WebView handle the Url.  In other words, returning
  // true will not follow the link, and returning false will follow the link.
  private class WebViewerClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return !followLinks;
    }
  }

  // Components don't normally override Width and Height, but we do it here so that
  // the automatic width and height will be fill parent.
  @Override
  @SimpleProperty()
  public void Width(int width) {
    if (width == LENGTH_PREFERRED) {
      width = LENGTH_FILL_PARENT;
    }
    super.Width(width);
  }

  @Override
  @SimpleProperty()
  public void Height(int height) {
    if (height == LENGTH_PREFERRED) {
      height = LENGTH_FILL_PARENT;
    }
    super.Height(height);
  }


  /**
   * Returns the URL of the page the WebVewier should load
   *
   * @return URL of the page the WebVewier should load
   */
//  @SimpleProperty(
//      description = "URL of the page the WebViewer should initially open to.  " +
//          "Setting this will load the page.",
//      category = PropertyCategory.BEHAVIOR)
//  public String HomeUrl() {
//    return homeUrl;
//  }

  /**
   * Specifies the URL of the page the WebVewier should load
   *
   * @param url URL of the page the WebVewier should load
   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
//      defaultValue = "")
//  @SimpleProperty()
//  public void HomeUrl(String url) {
//    homeUrl = url;
//    // clear the history, since changing Home is a kind of reset
//    webview.clearHistory();
//    webview.loadUrl(homeUrl);
//  }

  /**
   * Returns the URL currently being viewed
   *
   * @return URL of the page being viewed
   */
//  @SimpleProperty(
//      description = "URL of the page currently viewed.   This could be different from the " +
//          "Home URL if new pages were visited by following links.",
//      category = PropertyCategory.BEHAVIOR)
//  public String CurrentUrl() {
//    return (webview.getUrl() == null) ? "" : webview.getUrl();
//  }

  /**
   * Returns the title of the page currently being viewed
   *
   * @return title of the page being viewed
   */
//  @SimpleProperty(
//      description = "Title of the page currently viewed",
//      category = PropertyCategory.BEHAVIOR)
//  public String CurrentPageTitle() {
//    return (webview.getTitle() == null) ? "" : webview.getTitle();
//  }


  /** Indicates whether to follow links when they are tapped in the WebViewer
   * @return true or false
   */
//  @SimpleProperty(
//      description = "Determines whether to follow links when they are tapped in the WebViewer.  " +
//          "If you follow links, you can use GoBack and GoForward to navigate the browser history. ",
//      category = PropertyCategory.BEHAVIOR)
//  public boolean FollowLinks() {
//    return followLinks;
//  }


  /** Determines whether to follow links when they are tapped
   *
   * @param follow
   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
//      defaultValue = "True")
//  @SimpleProperty()
//  public void FollowLinks(boolean follow) {
//    followLinks = follow;
//    resetWebViewClient();
//  }

  /**
   * Determines whether SSL Errors are ignored. Set to true to use self signed certificates
   *
   * @return true or false
   *
   */
//  @SimpleProperty(
//      description = "Determine whether or not to ignore SSL errors. Set to true to ignore " +
//          "errors. Use this to accept self signed certificates from websites.",
//      category = PropertyCategory.BEHAVIOR)
//  public boolean IgnoreSslErrors() {
//    return ignoreSslErrors;
//  }

  /**
   * Determines whether or not to ignore SSL Errors
   *
   * @param ignoreErrors set to true to ignore SSL errors
   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
//      defaultValue = "False")
//  @SimpleProperty()
//  public void IgnoreSslErrors(boolean ignoreSslErrors) {
//    this.ignoreSslErrors = ignoreSslErrors;
//    resetWebViewClient();
//  }


  
  
  /**
   * Loads the  page from the home URL.  This happens automatically when
   * home URL is changed.
   */
//  @SimpleFunction(
//      description = "Loads the home URL page.  This happens automatically when " +
//          "the home URL is changed.")
//  public void GoHome() {
//    webview.loadUrl(homeUrl);
//  }

  /**
   *  Go back to the previously viewed page.
   */
//  @SimpleFunction(
//      description = "Go back to the previous page in the history list.  " +
//          "Does nothing if there is no previous page.")
//  public void GoBack() {
//    if (webview.canGoBack()) {
//      webview.goBack();
//    }
//  }

  /**
   *  Go forward in the history list
   */
//  @SimpleFunction(
//      description = "Go forward to the next page in the history list.   " +
//          "Does nothing if there is no next page.")
//  public void GoForward() {
//    if (webview.canGoForward()) {
//      webview.goForward();
//    }
//  }

  /**
   *  @return true if the WebViewer can go forward in the history list
   */
//  @SimpleFunction(
//      description = "Returns true if the WebViewer can go forward in the history list.")
//  public boolean CanGoForward() {
//    return webview.canGoForward();
//  }


  /**
   *  @return true if the WebViewer can go back in the history list
   */
//  @SimpleFunction(
//      description = "Returns true if the WebViewer can go back in the history list.")
//  public boolean CanGoBack() {
//    return webview.canGoBack();
//  }


  /**
   *  Load the given URL
   */
//  @SimpleFunction(
//      description = "Load the page at the given URL.")
//  public void GoToUrl(String url) {
//    webview.loadUrl(url);
//  }

  /**
   * Specifies whether or not this WebViewer can access the JavaScript
   * Location API.
   *
   * @param uses -- Whether or not the API is available
   */
//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
//      defaultValue = "False")
//  @SimpleProperty(userVisible = false,
//      description = "Whether or not to give the application permission to use the Javascript geolocation API. " +
//          "This property is available only in the designer.")
//  public void UsesLocation(boolean uses) {
//    // We don't actually do anything here (the work is in the MockWebViewer)
//  }

  /**
   * Determine if the user should be prompted for permission to use the geolocation API while in
   * the webviewer.
   *
   * @return true if prompting is  required.  False assumes permission is granted.
   */

  @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. " +
      "If False, then assume permission is granted.")
  public boolean PromptforPermission() {
    return prompt;
  }

  /**
   * Determine if the user should be prompted for permission to use the geolocation API while in
   * the webviewer.
   *
   * @param prompt set to true to require prompting. False assumes permission is granted.
   */

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
//      defaultValue = "True")
//  @SimpleProperty(userVisible = true)
//  public void PromptforPermission(boolean prompt) {
//    this.prompt = prompt;
//  }

  /**
   * Returns the data path of the record list
   *
   * @return the data path of the record list
   */
  @SimpleProperty(
      description = "the data path of the record list",
      category = PropertyCategory.BEHAVIOR)
  public String DataPath() {
    return dataPath;
  }

  /**
   * Specifies the data path of the record list
   *
   * @param the data path of the record list
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty()
  public void DataPath(String path) {
    dataPath = path;
  }
  
  
  
  
  /**
   * Clear Stored Location permissions. When the geolocation API is used in
   * the WebViewer, the end user is prompted on a per URL basis for whether
   * or not permission should be granted to access their location. This
   * function clears this information for all locations.
   *
   * As the permissions interface is not available on phones older then
   * Eclair, this function is a no-op on older phones.
   */

//  @SimpleFunction(description = "Clear stored location permissions.")
//  public void ClearLocations() {
//    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR)
//      EclairUtil.clearWebViewGeoLoc();
//  }

  private void resetWebViewClient() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      webview.setWebViewClient(FroyoUtil.getWebViewClient(ignoreSslErrors, followLinks, container.$form(), this));
    } else {
      webview.setWebViewClient(new WebViewerClient());
    }
  }
  
  /**
   * Mapping the Entity field to a specific column
   *
   * @param columnName, the name of the column in the table header row
   * @param field, the entity field mapping to the column
   */
  @SimpleFunction(description = "Mapping the Data to grid columns: <br> ColumnName is the name of the column in the table header. DataName is the data mapping to the column.")
  public void MapDataToColumn(final String columnName, final String dataName) {
    titleList.addLast(columnName);
    fieldList.addLast(dataName);
    return;
  }
  
  /**
   * Reset the Column Mapping defined previously
   *
   */
  @SimpleFunction(description = "Reset the Column Mapping")
  public void ResetDataColumnMapping() {
    titleList.clear();
    fieldList.clear();
    return;
  }
  
  
  /**
   * Refresh data to the GridView in the interface
   * This function generates an HTML table with the data given according to the fieldMapping.
   * The HTML table is generated as a Text and will refresh the interface with the new HTML Text.
   * 
   * @param entitySet
   */
  @SimpleFunction(description = "Refresh the data in the Grid.")
  public void Refresh(final String data) {
    
    String resultHTML = "";
    String tablePart = tableHead;
    webview.getSettings().setDefaultTextEncodingName("UTF-8");
    
    //Set the table style 
    String stylePart = ".datagrid table { border-collapse: collapse; text-align: left; width: 100%; } .datagrid {font: normal 12px/150% Arial, Helvetica, sans-serif; background: #fff; overflow: hidden; border: 1px solid #991821; -webkit-border-radius: 3px; -moz-border-radius: 3px; border-radius: 3px; }.datagrid table td, .datagrid table th { padding: 3px 10px; }.datagrid table thead th {background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #991821), color-stop(1, #80141C) );background:-moz-linear-gradient( center top, #991821 5%, #80141C 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#991821', endColorstr='#80141C');background-color:#991821; color:#FFFFFF; font-size: 15px; font-weight: bold; border-left: 1px solid #B01C26; } .datagrid table thead th:first-child { border: none; }.datagrid table tbody td { color: #80141C; border-left: 1px solid #F7CDCD;font-size: 12px;font-weight: normal; }.datagrid table tbody .alt td { background: #F7CDCD; color: #80141C; }.datagrid table tbody td:first-child { border-left: none; }.datagrid table tbody tr:last-child td { border-bottom: none; }.datagrid table tfoot td div { border-top: 1px solid #991821;background: #F7CDCD;} .datagrid table tfoot td { padding: 0; font-size: 12px } .datagrid table tfoot td div{ padding: 2px; }.datagrid table tfoot td ul { margin: 0; padding:0; list-style: none; text-align: right; }.datagrid table tfoot  li { display: inline; }.datagrid table tfoot li a { text-decoration: none; display: inline-block;  padding: 2px 8px; margin: 1px;color: #FFFFFF;border: 1px solid #991821;-webkit-border-radius: 3px; -moz-border-radius: 3px; border-radius: 3px; background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #991821), color-stop(1, #80141C) );background:-moz-linear-gradient( center top, #991821 5%, #80141C 100% );filter:progid:DXImageTransform.Microsoft.gradient(startColorstr='#991821', endColorstr='#80141C');background-color:#991821; }.datagrid table tfoot ul.active, .datagrid table tfoot ul a:hover { text-decoration: none;border-color: #80141C; color: #FFFFFF; background: none; background-color:#991821;}div.dhtmlx_window_active, div.dhx_modal_cover_dv { position: fixed !important; }";

   //Generate the parsing script using AngularJS
    String jscriptPart = "var app = angular.module('myApp', []); function EntityCtrl($scope, $http) { $scope.entityJson = JSON.parse('"+ data + "');}";
    
    //Generate the html table with AngularJS binding
     Iterator<String> iterator = titleList.iterator();
     while (iterator.hasNext()) {
       tablePart += "<th>" + iterator.next() + "</th>";
     }
     
     tablePart +="</tr> </thead> <tbody><tr ng-repeat=\"entity in entityJson" + (dataPath.isEmpty()? "":"."+dataPath) + "\">";
     iterator = fieldList.iterator();
     int index = 0;
     while (iterator.hasNext()) {
       index++;
       if(index==2) {
         tablePart += "<td><img src=\"{{entity."+ iterator.next() + "}}\"> </img></td>";
       } else {
         tablePart += "<td>{{entity."+ iterator.next() + "}}</td>";
       }
     }
     tablePart +="</tr> </tbody></table> </div> </div>";
    
     //Construct a complete HTML with both JScript and HTML
     resultHTML = "<!DOCTYPE html> <html>  <head> <meta charset=\"utf-8\"> <script src=\"http://ajax.googleapis.com/ajax/libs/angularjs/1.2.26/angular.min.js\"></script> <script>" 
                   +jscriptPart +"</script> <style type=\"text/css\"> "+ stylePart +" </style> </head>  <body>" 
                   +tablePart +" </body> </html>";
     
     //Load the HTML in webview
     webview.loadData(resultHTML, "text/html; charset=utf-8", "utf-8");
     //htmlString = resultHTML;
     return;
  }

  /**
   * Allows the setting of properties to be monitored from the javascript
   * in the WebView
   */
  public class WebViewInterface {
    Context mContext;
    String webViewString;

    /** Instantiate the interface and set the context */
    WebViewInterface(Context c) {
      mContext = c;
      webViewString = " ";
    }

    /**
     * Gets the web view string
     *
     * @return string
     */
    @JavascriptInterface
    public String getWebViewString() {
      return webViewString;
    }

    /**
     * Sets the web view string
     */
    public void setWebViewString(String newString) {
      webViewString = newString;
    }

  }
}

