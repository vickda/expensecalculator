package com.example.expensecalculator;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link homeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class homeFragment extends Fragment implements View.OnClickListener{

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

    AlertDialog.Builder dialogBox; // Dialog Box


    public homeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get Email
        fireBaseUserInstance = FirebaseAuth.getInstance().getCurrentUser();
        userEmail = fireBaseUserInstance.getEmail();

        fullDisplayName = fireBaseUserInstance.getDisplayName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        // Set Listener for logout button
        logoutBtn = (Button) getView().findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);

        // Set UserName info in the view
        userNameTV = getView().findViewById(R.id.welcomeUserTV);
        if(fullDisplayName == null) userName = "There";
        else userName = fullDisplayName.split(" ")[0];

        userNameTV.setText("Hi " + userName);

        /* Set HomeScreen Data */

        // Get & Set Expense
        listView = getView().findViewById(R.id.expenseListview);
        List<Statement> statements = getExpenseData(userEmail);

        // Set Total Income & total income &  Expense Amount
        totalAmountTV = getView().findViewById(R.id.totalAmountTV);
        totalAmountTV.setText("$" + (totalIncomeAmount - totalExpenseAmount));

        incomeAmountTV = getView().findViewById(R.id.incomeAmountTV);
        incomeAmountTV.setText("$" + totalIncomeAmount);

        expenseAmountTV = getView().findViewById(R.id.expenseAmountTV);
        expenseAmountTV.setText("$" + totalExpenseAmount);

        // Inflate Listview Items
        ExpenseCustomAdapter expenseCustomAdapter = new ExpenseCustomAdapter(getContext(), statements);
        listView.setAdapter(expenseCustomAdapter);

        // Show ads every 10 minutes
        Handler handler = new Handler();
       try {
           handler.postDelayed(new Runnable() {
               public void run() {
                   dialogBox = new AlertDialog.Builder(getContext());
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
//                handler.postDelayed(this, 100000);
               }
           }, 3000);
       }
       catch (Exception e){
           Log.i("Dialog Box Crash: ", e.getMessage());
       }



//        Timer myTimer = new Timer();
//        myTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                AlertDialog.Builder dialogBox = new AlertDialog.Builder(this);
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
                Toast.makeText(getContext(), "Logout btn clicked", Toast.LENGTH_SHORT).show();
                logoutUser();
                break;
            case R.id.welcomeUserTV:
                // Create User into DB
                DatabaseOperations db = new DatabaseOperations(getContext());
                db.getAllData();

                break;
        }
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        getActivity().finish();
        startActivity(new Intent(getContext(), LoginActivity.class));
    }

    private List<Statement> getExpenseData(String userEmail) {
        List<Statement> allStatements = new ArrayList<Statement>();
        totalExpenseAmount = 0.0;
        totalIncomeAmount = 0.0;

        // Fetch Data from Database
        DatabaseOperations db = new DatabaseOperations(getContext());

        // Get Data from database
        String expenseData = db.getExpenseData(userEmail);


        // Get Current Date & Time
        String fullDate, monthName;
        Integer month, day, year;

        day =  java.time.LocalDateTime.now().getDayOfMonth();
        month = java.time.LocalDateTime.now().getMonthValue();
        year = java.time.LocalDateTime.now().getYear();
        monthName = java.time.LocalDateTime.now().getMonth().toString();

        fullDate = month + "/" + day + "/" + year;

        // Parsing JSON
        try {

            JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
            JSONObject yearData = jObject.getJSONObject(year.toString()); // Get Current Year Data
            JSONObject monthData = yearData.getJSONObject(monthName); // Get Current Month Data
            JSONArray daysData = monthData.getJSONArray(fullDate); // Get Current Days Data

            // Looping over all current months data
            for (int i = 0; i < daysData.length(); i++) {
                String paymentType, category, money;

                // getting current Days data
                JSONObject todaysData = daysData.getJSONObject(i);

                // Add to list if todays data is not null
                if(todaysData != null){
                    Double amount;

                    paymentType = todaysData.getString("PaymentType");
                    category = todaysData.getString("Category");
                    money = todaysData.getString("money");

                    // Calculate Total Income & Expense Amount
                    amount = Double.parseDouble(money.replace("$", "").trim());

                    if(category.trim().equals("Expense")) totalExpenseAmount += amount;
                    else totalIncomeAmount += amount;

                    Log.i("getExpenseData: ", totalExpenseAmount + " " + totalIncomeAmount);

                    // Add data into Statements List
                    Statement stmt = new Statement(fullDate, paymentType, category, money);
                    allStatements.add(stmt);
                }

            }

        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        return allStatements;
    }
}