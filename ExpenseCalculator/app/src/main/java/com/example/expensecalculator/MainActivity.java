package com.example.expensecalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlarmManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Firebase variable
    private FirebaseUser fireBaseUserInstance;
    private DatabaseReference reference;

    // View Variables
    TextView userNameTV, incomeAmountTV, expenseAmountTV, totalAmountTV;
    Button logoutBtn;
    ListView listView;

    // Other variables
    private String userEmail, userName, fullDisplayName;
    private Double totalIncomeAmount = 0.0, totalExpenseAmount = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Listener for logout button
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);

        // Get Email
        fireBaseUserInstance = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = fireBaseUserInstance.getEmail();

        // Set Username
        userNameTV = findViewById(R.id.welcomeUserTV);
        fullDisplayName = fireBaseUserInstance.getDisplayName();

        Log.i("onCreate: ", fullDisplayName + "");

        if(fullDisplayName == null) userName = "There";
        else userName = fullDisplayName;

        userNameTV.setText("Hi " + userName);

        /* Set HomeScreen Data */

        // Get & Set Expense
        listView = findViewById(R.id.expenseListview);
        List<Statement> statements = getExpenseData(userEmail);

        // Set Total Income & total income &  Expense Amount
        totalAmountTV = findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("$" + (totalIncomeAmount - totalExpenseAmount));

        incomeAmountTV = findViewById(R.id.incomeAmountTV);
        incomeAmountTV.setText("$" + totalIncomeAmount);

        expenseAmountTV = findViewById(R.id.expenseAmountTV);
        expenseAmountTV.setText("$" + totalExpenseAmount);

        // Inflate Listview Items
        ExpenseCustomAdapter expenseCustomAdapter = new ExpenseCustomAdapter(this, statements);
        listView.setAdapter(expenseCustomAdapter);

        // Show ads every 10 minutes
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                AlertDialog.Builder dialogBox = new AlertDialog.Builder(MainActivity.this);
                dialogBox.setTitle("Annonying Ad here")
                        .setMessage("This is an ad!! To stop this purchase our pro pack for just $2 per month")
                        .setCancelable(true)
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
                handler.postDelayed(this, 100000);
            }
        }, 100000);



//        Timer myTimer = new Timer();
//        myTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                AlertDialog.Builder dialogBox = new AlertDialog.Builder(MainActivity.this);
//                dialogBox.setTitle("Annonying Ad here")
//                        .setMessage("This is an ad!! To stop this purchase our pro pack for just $2 per month")
//                        .setCancelable(true)
//                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                dialogInterface.cancel();
//                            }
//                        })
//                        .show();
//            }
//        }, 0, 300000);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.logoutBtn:
                Toast.makeText(this, "Logout btn clicked", Toast.LENGTH_SHORT).show();
                logoutUser();
                break;
            case R.id.welcomeUserTV:
                // Create User into DB
                DatabaseOperations db = new DatabaseOperations(this);
                db.getAllData();

                break;
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    private List<Statement> getExpenseData(String userEmail) {
        List<Statement> allStatements = new ArrayList<Statement>();

        // Fetch Data from Database
        DatabaseOperations db = new DatabaseOperations(this);

        // Get Data from database
//        String expenseData = db.getExpenseData(userEmail);


        // Get Current Date & Time
        String fullDate, monthName;
        Integer month, day, year;

        day =  java.time.LocalDateTime.now().getDayOfMonth();
        month = java.time.LocalDateTime.now().getMonthValue();
        year = java.time.LocalDateTime.now().getYear();
        monthName = java.time.LocalDateTime.now().getMonth().toString();

        fullDate = month + "/" + day + "/" + year;

        String expenseData = "{\"2022\":{\"DECEMBER\":[{\"12/4/2022\":{\"PaymentType\": \"Bank Account\",\"Category\": \"Expense\",\"money\": \"$800\"}},{\"12/4/2022\":{\"PaymentType\": \"Bank Account\",\"Category\": \"Income\",\"money\": \"$1000\"}}]}}";

        // Parsing JSON
        try {

            JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
            JSONObject yearData = jObject.getJSONObject(year.toString()); // Get Current Year Data
            JSONArray monthData = yearData.getJSONArray(monthName); // Get Current Month Data
            
            // Looping over all current months data
            for (int i = 0; i < monthData.length(); i++) {
                String paymentType, category, money;

                JSONObject daysData = monthData.getJSONObject(i);

                // getting current Days data
                JSONObject todaysData = daysData.optJSONObject(fullDate);

                // Add to list if todays data is not null
                if(todaysData != null){
                    Double amount;

                    paymentType = todaysData.getString("PaymentType");
                    category = todaysData.getString("Category");
                    money = todaysData.getString("money");

                    // Calculate Total Income & Expense Amount
                    amount = Double.parseDouble(money.replace("$", "").trim());

                    if(category.trim().toLowerCase().equals("expense")) totalExpenseAmount += amount;
                    else totalIncomeAmount += amount;

                    // Add data into Statements List
                    Statement stmt = new Statement(fullDate, paymentType, category, money);
                    allStatements.add(stmt);
                }

            }
        
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, "Error while parsing json", Toast.LENGTH_SHORT).show();
        }
        return allStatements;
    }

    public void showAd(){
        AlertDialog.Builder dialogBox = new AlertDialog.Builder(this);
        dialogBox.setTitle("Annonying Ad here")
                .setMessage("This is an ad!! To stop this purchase our pro pack for just $2 per month")
                .setCancelable(true)
                .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

}


/*
Parsing JSON using nested loops
 */

/*
try {
            JSONObject jObject = new JSONObject(expenseData);

            // Looping over all years
            for (int i = 0; i < jObject.names().length(); i++) {

                String year = jObject.names().getString(i);

                if (year.equals("2022")){
                    JSONObject monthsData = jObject.getJSONObject(year);

                    // Looping over all months
                    for (int j = 0; j < monthsData.length(); j++) {

                        String currMonth = monthsData.names().getString(j);
                        Log.i("setExpenseData: ", "Count " + currMonth);

                        JSONArray daysData = monthsData.getJSONArray(currMonth);

                        // Looping over dates
                        for (int k = 0; k < daysData.length(); k++) {

                            JSONObject currDay = daysData.getJSONObject(i);

                            Log.i("setExpenseData: ", "Day " + currDay);

                            for(Iterator it = currDay.keys(); it.hasNext(); ) {
                                String paymentType, category, money;

                                String date = (String)it.next();

                                JSONObject finalData = currDay.getJSONObject(date);

                                paymentType = finalData.getString("PaymentType");
                                category = finalData.getString("Category");
                                money = finalData.getString("money");

                                Statement stmt = new Statement(date, paymentType, category, money);

                                allStatements.add(stmt);
                                Log.i("setExpenseData: ",  paymentType + " " + category+ " " + money);
                            }
                        }
                    }
                }

            }
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this, "Error while parsing json", Toast.LENGTH_SHORT).show();
        }
 */