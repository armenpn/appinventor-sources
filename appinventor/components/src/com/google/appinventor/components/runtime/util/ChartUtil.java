package com.google.appinventor.components.runtime.util;

import java.util.Random;

/**
 * This util product the chart html string
 * 
 * @author Angus
 *
 */
public class ChartUtil {

  /**
   * Prevent instantiation.
   */
  private ChartUtil() {
  }

  /**
   * Returns a String of Html
   * 
   * @param data
   * @param chartType
   * @return
   * @throws Exception
   */
  public static String getHtmlStringForChart(String jsonData, String chartType, int width,
      int height, int top, int left, int right, int bottom, String colorstr) {
    // generate chart html
    if (chartType != null && !"".equals(chartType.trim()) && jsonData != null
        && !"".equals(jsonData)) {
      // bar chart
      if ("BAR".equals(chartType.trim().toUpperCase())) {
        // data formate
        // {"data":[{"name":"***","value:111"},{"name":"***","value:111"}]}
        return getBarChartHtml(jsonData, width, height, top, left, right, bottom);
      } else if ("GROUPBAR".equals(chartType.trim().toUpperCase())) {
        // data formate
        // {"groupNames":["zhangsan","lisi"],"childNames":["A","B","C"],"ytitle":"Amount","data":[{"groupName":"zhangsan","child":[{"name":"A","value":0.1},{"name":"B","value":0.2},{"name":"C","value":0.3}]},{"groupName":"lisi","child":[{"name":"A","value":0.4},{"name":"B","value":0.5},{"name":"C","value":0.6}]}]}
        return getGroupBarChartHtml(jsonData, width, height, top, left, right, bottom, colorstr);
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  /**
   * generate bar chart html
   * 
   * @param data
   *          data formate
   *          {"data":[{"name":"***","value:111"},{"name":"***","value:111"}]}
   * @return
   * @throws Exception
   */
  private static String getBarChartHtml(String data, int width, int height, int top, int left,
      int right, int bottom) {
    if (top < 0) {
      top = 20;
    }
    if (left < 0) {
      left = 40;
    }
    if (right < 0) {
      right = 20;
    }
    if (bottom < 0) {
      bottom = 30;
    }

    StringBuffer returnHtml = new StringBuffer(256);

    // Set the chart style
    StringBuffer stylePart = new StringBuffer(64);
    stylePart.append(".bar {fill: steelblue; } ");
    stylePart.append(".bar:hover {fill: brown; } ");
    stylePart.append(".axis {font: 10px sans-serif; } ");
    stylePart
        .append(".axis path,.axis line {fill: none;stroke: #000;shape-rendering: crispEdges;} ");
    stylePart.append(".x.axis path {display: none;} ");

    // Generate the parsing script using d3JS
    StringBuffer jscriptPart = new StringBuffer(128);
    jscriptPart.append("var jsondata = ").append(data).append("; ");

    jscriptPart.append("var margin = {top: ").append(top).append(", right: ").append(right).append(", bottom: ").append(bottom).append(", left: ").append(left).append("}, ");
    jscriptPart.append("    width = ").append(width).append(" - margin.left - margin.right, ");
    jscriptPart.append("    height = ").append(height).append(" - margin.top - margin.bottom; ");
    jscriptPart.append("var x = d3.scale.ordinal().rangeRoundBands([0, width], .1); ");
    jscriptPart.append("var y = d3.scale.linear().range([height, 0]); ");
    jscriptPart.append("var xAxis = d3.svg.axis().scale(x).orient(\"bottom\"); ");
    jscriptPart.append("var yAxis = d3.svg.axis().scale(y).orient(\"left\").ticks(10, \"%\"); ");
    jscriptPart.append("var svg = d3.select(\"body\").append(\"svg\").attr(\"width\", width + margin.left + margin.right).attr(\"height\", height + margin.top + margin.bottom).append(\"g\").attr(\"transform\", \"translate(\" + margin.left + \",\" + margin.top + \")\"); ");
    jscriptPart.append("x.domain(jsondata.data.map(function(d) { return d.name; })); ");
    jscriptPart.append("y.domain([0, d3.max(jsondata.data, function(d) { return d.value; })]); ");
    jscriptPart.append("svg.append(\"g\").attr(\"class\", \"x axis\").attr(\"transform\", \"translate(0,\" + height + \")\").call(xAxis); ");
    jscriptPart.append("svg.append(\"g\").attr(\"class\", \"y axis\").call(yAxis).append(\"text\").attr(\"transform\", \"rotate(-90)\").attr(\"y\", 6).attr(\"dy\", \".71em\").style(\"text-anchor\", \"end\").text(\"Frequency\"); ");
    jscriptPart.append("svg.selectAll(\".bar\").data(jsondata.data).enter().append(\"rect\").attr(\"class\", \"bar\").attr(\"x\", function(d) { return x(d.name); }).attr(\"width\", x.rangeBand()).attr(\"y\", function(d) { return y(d.value); }).attr(\"height\", function(d) { return height - y(d.value); }); ");

    returnHtml.append("<!DOCTYPE html> ");
    returnHtml.append("<html> ");
    returnHtml.append("  <head> ");
    returnHtml.append("    <meta charset=\"utf-8\"> ");
    returnHtml.append("    <style type=\"text/css\">").append(stylePart).append("</style> ");
    returnHtml.append("    <script src=\"http://d3js.org/d3.v3.min.js\"></script> ");
    returnHtml.append("  </head> ");
    returnHtml.append("  <body> ");
    returnHtml.append("    <script>").append(jscriptPart).append("</script>");
    returnHtml.append("  </body> ");
    returnHtml.append("</html> ");
    return returnHtml.toString();
  }

  /**
   * generate group bar chart html
   * 
   * @param data
   *          data formate
   *          {"groupNames":["zhangsan","lisi"],"childNames":["A","B","C"],"ytitle":"Amount",
   *          "data":[{"groupName":"zhangsan","child":[{"name":"A","value":0.1},{"name":"B","value":0.2},{"name":"C","value":0.3}]},
   *          {"groupName":"lisi","child":[{"name":"A","value":0.4},{"name":"B","value":0.5},{"name":"C","value":0.6}]}]}
   * @param width
   * @param height
   * @param top
   * @param left
   * @param right
   * @param bottom
   * @param colorstr
   * @return
   */
  private static String getGroupBarChartHtml(String data, int width, int height, int top, int left,
      int right, int bottom, String colorstr) {
    if (top < 0) {
      top = 20;
    }
    if (left < 0) {
      left = 40;
    }
    if (right < 0) {
      right = 20;
    }
    if (bottom < 0) {
      bottom = 30;
    }

    StringBuffer returnHtml = new StringBuffer(256);

    // Set the chart style
    StringBuffer stylePart = new StringBuffer(64);
    stylePart.append("body {font: 10px sans-serif;} ");
    stylePart
        .append(".axis path,.axis line {fill: none;stroke: #000;shape-rendering: crispEdges;} ");
    stylePart.append(".bar {fill: steelblue;} ");
    stylePart.append(".x.axis path {display: none;} ");

    // Generate the parsing script using d3JS
    StringBuffer jscriptPart = new StringBuffer(128);
    
    jscriptPart.append("var jsondata = ").append(data).append("; ");
    jscriptPart.append("var margin = {top: ").append(top).append(", right: ").append(right).append(", bottom: ").append(bottom).append(", left: ").append(left).append("}, ");
    jscriptPart.append("    width = ").append(width).append(" - margin.left - margin.right, ");
    jscriptPart.append("    height = ").append(height).append(" - margin.top - margin.bottom; ");

    jscriptPart.append("var x0 = d3.scale.ordinal().rangeRoundBands([0, width], .1); ");
    jscriptPart.append("var x1 = d3.scale.ordinal(); ");
    jscriptPart.append("var y = d3.scale.linear().range([height, 0]); ");
    // generate color
    jscriptPart.append("var color = d3.scale.ordinal().range([ ");
    if(colorstr != null && !"".equals(colorstr)){
      jscriptPart.append(colorstr);
    } else {
      for (int i = 0; i < 10; i++) {
        if (i == 0) {
          jscriptPart.append("\"").append(getRandomColor()).append("\"");
        } else {
          jscriptPart.append(",\"").append(getRandomColor()).append("\"");
        }
      }
    }
    jscriptPart.append("]); ");

    jscriptPart.append("var xAxis = d3.svg.axis().scale(x0).orient(\"bottom\"); ");
    jscriptPart.append("var yAxis = d3.svg.axis().scale(y).orient(\"left\"); ");
    jscriptPart.append("var svg = d3.select(\"body\").append(\"svg\").attr(\"width\", width + margin.left + margin.right).attr(\"height\", height + margin.top + margin.bottom).append(\"g\").attr(\"transform\", \"translate(\" + margin.left + \",\" + margin.top + \")\"); ");
    jscriptPart.append("x0.domain(jsondata.groupNames); ");
    // jscriptPart.append("x0.domain(jsondata.data.map(function(d) { return d.groupName; })); ");
    jscriptPart.append("x1.domain(jsondata.childNames).rangeRoundBands([0, x0.rangeBand()]); ");
    // jscriptPart.append("x1.domain(jsondata.data[0].child.map(function(d) { return d.name;})).rangeRoundBands([0, x0.rangeBand()]); ");
    jscriptPart.append("y.domain([0, d3.max(jsondata.data.map(function(d){ return d3.max(d.child, function(d1){return d1.value;});}))]); ");
    jscriptPart.append("svg.append(\"g\").attr(\"class\", \"x axis\").attr(\"transform\", \"translate(0,\" + height + \")\").call(xAxis); ");
    jscriptPart.append("svg.append(\"g\").attr(\"class\", \"y axis\").call(yAxis).append(\"text\").attr(\"transform\", \"rotate(-90)\").attr(\"y\", 6).attr(\"dy\", \".71em\").style(\"text-anchor\", \"end\").text(jsondata.ytitle); ");
    jscriptPart.append("var state = svg.selectAll(\".state\").data(jsondata.data).enter().append(\"g\").attr(\"class\", \"g\").attr(\"transform\", function(d) { return \"translate(\" + x0(d.groupName) + \",0)\"; }); ");
    jscriptPart.append("state.selectAll(\"rect\").data(function(d) { return d.child; }).enter().append(\"rect\").attr(\"width\", x1.rangeBand()).attr(\"x\", function(d) { return x1(d.name); }).attr(\"y\", function(d) { return y(d.value); }).attr(\"height\", function(d) { return height - y(d.value)}).style(\"fill\", function(d) { return color(d.name); }); ");
    jscriptPart.append("var legend = svg.selectAll(\".legend\").data(jsondata.childNames.slice().reverse()).enter().append(\"g\").attr(\"class\", \"legend\").attr(\"transform\", function(d, i) { return \"translate(0,\" + i * 20 + \")\"; }); ");
    jscriptPart.append("legend.append(\"rect\").attr(\"x\", width - 18).attr(\"width\", 18).attr(\"height\", 18).style(\"fill\", color); ");
    jscriptPart.append("legend.append(\"text\").attr(\"x\", width - 24).attr(\"y\", 9).attr(\"dy\", \".35em\").style(\"text-anchor\", \"end\").text(function(d) { return d; }); ");
    jscriptPart.append(" ");

    returnHtml.append("<!DOCTYPE html> ");
    returnHtml.append("<html> ");
    returnHtml.append("  <head> ");
    returnHtml.append("    <meta charset=\"utf-8\"> ");
    returnHtml.append("    <style type=\"text/css\">").append(stylePart).append("</style> ");
    returnHtml.append("    <script src=\"http://d3js.org/d3.v3.min.js\"></script> ");
    returnHtml.append("  </head> ");
    returnHtml.append("  <body> ");
    returnHtml.append("    <script>").append(jscriptPart).append("</script>");
    returnHtml.append("  </body> ");
    returnHtml.append("</html> ");
    return returnHtml.toString();
  }

  public static String getRandomColor() {
    String r, g, b;
    Random random = new Random();
    r = Integer.toHexString(random.nextInt(256)).toUpperCase();
    g = Integer.toHexString(random.nextInt(256)).toUpperCase();
    b = Integer.toHexString(random.nextInt(256)).toUpperCase();

    r = r.length() == 1 ? "0" + r : r;
    g = g.length() == 1 ? "0" + g : g;
    b = b.length() == 1 ? "0" + b : b;

    return ("#" + r + g + b);
  }
}
