package com.google.appinventor.components.runtime;

import java.util.LinkedList;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ChartUtil;
import com.google.appinventor.components.runtime.util.ConvertDataToChartData;
import com.google.appinventor.components.runtime.util.EclairUtil;
import com.google.appinventor.components.runtime.util.FroyoUtil;
import com.google.appinventor.components.runtime.util.SdkLevel;

/**
 * The ChartView is used to display a JSon array in chart format. Last update:
 * June 4, 2015
 * 
 * @author Angusyang@apjcorp.com (Angus Yang EEL/APJ)
 */

@DesignerComponent(version = YaVersion.WEBVIEWER_COMPONENT_VERSION, category = ComponentCategory.DATADOTESQUEL, description = "ChartView is a custom-made easy-to-use view to show a chart of data from Data.Esquel .")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "json.jar")
public final class ChartView extends AndroidViewComponent {

  private final WebView webview;

  // whether or not to follow links when they are tapped
  private boolean followLinks = true;

  // Whether or not to prompt for permission in the WebViewer
  private boolean prompt = false;

  // ignore SSL Errors (mostly certificate errors. When set
  // self signed certificates should work.
  private boolean ignoreSslErrors = false;

  // allows passing strings to javascript
  WebViewInterface wvInterface;

  private String jsonData = "";

  private String chartType = "";

  private String businessCode = "";

  // "#010101","#222222"
  private String colorStr = "";

  private int chartTop = -1;

  private int chartLeft = -1;

  private int chartRight = -1;

  private int chartBottom = -1;

  /**
   * Creates a new WebViewer component.
   *
   * @param container
   *          container the component will be placed in
   */
  public ChartView(ComponentContainer container) {
    super(container);

    webview = new WebView(container.$context());
    resetWebViewClient(); // Set up the web view client
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

    // set the initial default properties. Height and Width
    // will be fill-parent, which will be the default for the web viewer.
    Width(LENGTH_FILL_PARENT);
    Height(LENGTH_FILL_PARENT);
  }

  /**
   * Gets the web view string
   *
   * @return string
   */
  public String WebViewString() {
    return wvInterface.getWebViewString();
  }

  /**
   * Sets the web view string
   */
  public void WebViewString(String newString) {
    wvInterface.setWebViewString(newString);
  }

  @Override
  public View getView() {
    return webview;
  }

  // Create a class so we can override the default link following behavior.
  // The handler doesn't do anything on its own. But returning true means that
  // this do nothing will override the default WebVew behavior. Returning
  // false means to let the WebView handle the Url. In other words, returning
  // true will not follow the link, and returning false will follow the link.
  private class WebViewerClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return !followLinks;
    }
  }

  // Components don't normally override Width and Height, but we do it here so
  // that
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
   * Returns the Top point of the Chart in the WebVewier
   *
   * @return Top point of the Chart in the WebVewier
   */
  @SimpleProperty(description = "Top point of the Chart in the WebVewier.  ", category = PropertyCategory.BEHAVIOR)
  public int ChartTop() {
    return chartTop;
  }

  /**
   * set the Top point of the Chart in the WebVewier
   *
   * @param top
   *          the Top point of the Chart in the WebVewier
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "-1")
  @SimpleProperty()
  public void ChartTop(int top) {
    chartTop = top;
  }

  /**
   * Returns the Left point of the Chart in the WebVewier
   *
   * @return Left point of the Chart in the WebVewier
   */
  @SimpleProperty(description = "Left point of the Chart in the WebVewier.  ", category = PropertyCategory.BEHAVIOR)
  public int ChartLeft() {
    return chartLeft;
  }

  /**
   * set the Left point of the Chart in the WebVewier
   *
   * @param top
   *          the Left point of the Chart in the WebVewier
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "-1")
  @SimpleProperty()
  public void ChartLeft(int left) {
    chartLeft = left;
  }

  /**
   * Returns the Right point of the Chart in the WebVewier
   *
   * @return Right point of the Chart in the WebVewier
   */
  @SimpleProperty(description = "Right point of the Chart in the WebVewier.  ", category = PropertyCategory.BEHAVIOR)
  public int ChartRight() {
    return chartRight;
  }

  /**
   * set the Right point of the Chart in the WebVewier
   *
   * @param right
   *          the Right point of the Chart in the WebVewier
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "-1")
  @SimpleProperty()
  public void ChartRight(int right) {
    chartRight = right;
  }

  /**
   * Returns the Bottom point of the Chart in the WebVewier
   *
   * @return Bottom point of the Chart in the WebVewier
   */
  @SimpleProperty(description = "Bottom point of the Chart in the WebVewier.  ", category = PropertyCategory.BEHAVIOR)
  public int ChartBottom() {
    return chartBottom;
  }

  /**
   * set the Bottom point of the Chart in the WebVewier
   *
   * @param bottom
   *          the Bottom point of the Chart in the WebVewier
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "-1")
  @SimpleProperty()
  public void ChartBottom(int bottom) {
    chartBottom = bottom;
  }

  /**
   * Determine if the user should be prompted for permission to use the
   * geolocation API while in the webviewer.
   *
   * @return true if prompting is required. False assumes permission is granted.
   */

  @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. "
      + "If False, then assume permission is granted.")
  public boolean PromptforPermission() {
    return prompt;
  }

  /**
   * 
   */
  private void resetWebViewClient() {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_FROYO) {
      webview.setWebViewClient(FroyoUtil.getWebViewClient(ignoreSslErrors, followLinks,
          container.$form(), this));
    } else {
      webview.setWebViewClient(new WebViewerClient());
    }
  }

  /**
   * Refresh data to the ChartView in the interface This function generates an
   * HTML with the data given according to the fieldMapping. The HTML is
   * generated as a Text and will refresh the interface with the new HTML Text.
   * 
   * @param entitySet
   */
  @SimpleFunction(description = "Refresh the data in the Chart.")
  public void Refresh(final String data, final String chartType, String businessCode, int chartWidth, int chartHeight) {
    webview.getSettings().setDefaultTextEncodingName("UTF-8");
    this.chartType = chartType;
    this.businessCode = businessCode;
    for (int i = 0; i < 3; i++) {
      if (i == 0) {
        colorStr = "\"" + ChartUtil.getRandomColor() + "\"";
      } else {
        colorStr = colorStr + ",\"" + ChartUtil.getRandomColor() + "\"";
      }
    }
    this.jsonData = ConvertDataToChartData.getInstance().convertData(data, chartType, this.businessCode);
    webview.loadData(ChartUtil.getHtmlStringForChart(jsonData, chartType, chartWidth, chartHeight, chartTop, chartLeft, chartRight, chartBottom, colorStr), "text/html; charset=utf-8", "utf-8");
    webview.reload();
    return;
  }

  /**
   * Refresh the ChartView Size
   * 
   * @param entitySet
   */
  @SimpleFunction(description = "Refresh the ChartView Size.")
  public void ReSize(int chartWidth, int chartHeight) {
    webview.getSettings().setDefaultTextEncodingName("UTF-8");
    webview.loadData(ChartUtil.getHtmlStringForChart(jsonData, chartType, chartWidth, chartHeight, chartTop, chartLeft, chartRight, chartBottom, colorStr), "text/html; charset=utf-8", "utf-8");
    webview.reload();
    return;
  }

  /**
   * Allows the setting of properties to be monitored from the javascript in the
   * WebView
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
