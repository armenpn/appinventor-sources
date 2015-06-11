package com.google.appinventor.components.runtime.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This convert data by chartType and businessCode
 * 
 * @author Angus
 *
 */
public class ConvertDataToChartData {
  static ConvertDataToChartData convertDataToChartData = null;

  private ConvertDataToChartData() {
  }

  public static ConvertDataToChartData getInstance() {
    if (convertDataToChartData == null) {
      convertDataToChartData = new ConvertDataToChartData();
    }
    return convertDataToChartData;
  }

  /**
   * convert data by chartType and businessCode
   * 
   * @param data
   * @param chartType
   * @param businessCode
   * @return
   */
  public String convertData(String data, String chartType, String businessCode) {
    if (businessCode != null && "GENDERSALES".equals(businessCode.toUpperCase())) {
      // convert data by chartType for GENDERSALES
      return convertDataForGenderSales(data, chartType);
    } else {
      return data;
    }
  }

  /**
   * convert data by chartType for GENDERSALES
   * 
   * @param data
   * @param chartType
   * @return
   */
  private String convertDataForGenderSales(String data, String chartType) {
    try {
      // convert data by groupbar for GENDERSALES
      if (chartType != null && "GROUPBAR".equals(chartType.toUpperCase())) {
        JSONObject jsonObj = new JSONObject(data);
        StringBuffer jsondata = new StringBuffer(128);
        StringBuffer groupNames = new StringBuffer(32);
        String childNames = "\"LADIES'\", \"MEN'S\", \"UNISEX\"";
        String[] childNamesArray = new String[] { "LADIES'", "MEN'S", "UNISEX" };
        jsondata.append("{\"data\":[");
        JSONObject offlineStoreSalesReportOutputObj = jsonObj
            .getJSONObject("OfflineStoreSalesReportOutput");
        if (offlineStoreSalesReportOutputObj == null) {
          return data;
        }
        JSONArray monthlySummaryArray = offlineStoreSalesReportOutputObj
            .getJSONArray("MonthlySummary");
        if (monthlySummaryArray == null || monthlySummaryArray.length() < 1) {
          return data;
        }
        for (int i = 0; i < monthlySummaryArray.length(); i++) {
          if (i > 0) {
            jsondata.append(",");
            groupNames.append(",");
          }
          jsondata.append("{\"groupName\":");
          JSONObject jo = (JSONObject) monthlySummaryArray.get(i);
          jsondata.append("\"")
              .append(jo.getString("StoreId") == null ? "" : jo.getString("StoreId")).append("\",");
          groupNames.append("\"")
              .append(jo.getString("StoreId") == null ? "" : jo.getString("StoreId")).append("\"");
          jsondata.append("\"child\":[");
          JSONObject genderSalesObj = null;
          try {
            genderSalesObj = jo.getJSONObject("GenderSales");
          } catch (JSONException e1) {

          }
          if (genderSalesObj == null) {
            jsondata
                .append("{\"name\":\"LADIES'\",\"value\":0.00},{\"name\":\"MEN'S\",\"value\":0.00},{\"name\":\"UNISEX\",\"value\":0.00}]");
          } else {
            JSONArray genderSaleArray = genderSalesObj.getJSONArray("GenderSale");
            if (genderSaleArray == null || genderSaleArray.length() < 1) {
              jsondata
                  .append("{\"name\":\"LADIES'\",\"value\":0.00},{\"name\":\"MEN'S\",\"value\":0.00},{\"name\":\"UNISEX\",\"value\":0.00}]");
            } else {
              for (int n = 0; n < childNamesArray.length; n++) {
                if (n > 0) {
                  jsondata.append(",");
                }
                jsondata.append("{\"name\":");
                jsondata.append("\"").append(childNamesArray[n]).append("\",");
                jsondata.append("\"value\":");
                double value = 0.0;
                for (int j = 0; j < genderSaleArray.length(); j++) {
                  JSONObject jochild = (JSONObject) genderSaleArray.get(j);
                  if (childNamesArray[n].equals(jochild.getString("gender") == null ? "" : jochild
                      .getString("gender"))) {
                    value = jochild.getDouble("Amount");
                    break;
                  }
                }
                jsondata.append(value).append("}");
              }
              jsondata.append("]");
            }
          }
          jsondata.append("}");
        }
        jsondata.append("]");
        jsondata.append(",\"groupNames\":[").append(groupNames).append("]");
        jsondata.append(",\"childNames\":[").append(childNames).append("]");
        jsondata.append(",\"ytitle\":\"Amount\"");
        jsondata.append("}");
        return jsondata.toString();
      } else if (chartType != null && "BAR".equals(chartType.toUpperCase())) {
        return data;
      } else {
        return data;
      }
    } catch (JSONException e) {
      e.printStackTrace();
      return data;
    }
  }
}
