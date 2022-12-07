//package com.example.expensecalculator;
//
//import android.widget.Toast;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.List;
//
//public class GetStatementsUtil {
//
//    public List<Statement> generateList(String expenseData, String fullDate, String monthName){
//        // Parsing JSON
//        try {
//
//            String year = "";
//            JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
//            JSONObject yearData = jObject.getJSONObject(year.toString()); // Get Current Year Data
//            JSONArray monthData = yearData.getJSONArray(monthName); // Get Current Month Data
//
//            // Looping over all current months data
//            for (int i = 0; i < monthData.length(); i++) {
//                String paymentType, category, money;
//
//                JSONObject daysData = monthData.getJSONObject(i);
//
//                // getting current Days data
//                JSONObject todaysData = daysData.optJSONObject(fullDate);
//
//                // Add to list if todays data is not null
//                if(todaysData != null){
//                    Double amount;
//
//                    paymentType = todaysData.getString("PaymentType");
//                    category = todaysData.getString("Category");
//                    money = todaysData.getString("money");
//
//                    // Calculate Total Income & Expense Amount
//                    amount = Double.parseDouble(money.replace("$", "").trim());
//
////                    if(category.trim().toLowerCase().equals("expense")) totalExpenseAmount += amount;
////                    else totalIncomeAmount += amount;
////
////                    // Add data into Statements List
////                    Statement stmt = new Statement(fullDate, paymentType, category, money);
////                    allStatements.add(stmt);
//                }
//
//            }
//
//        }catch (JSONException e){
//            e.printStackTrace();
////            Toast.makeText(this, "Error while parsing json", Toast.LENGTH_SHORT).show();
//        }
////    }
////}
