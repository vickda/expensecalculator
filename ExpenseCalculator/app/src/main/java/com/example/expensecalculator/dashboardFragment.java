package com.example.expensecalculator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link dashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class dashboardFragment extends Fragment implements View.OnClickListener {

    String dateForJson;
    Double totalIncomeAmount = 0.00, totalExpenseAmount = 0.00;
    DatePickerDialog datePickerDialog;
    EditText editTextDate;
    Button searchBtn;
    TextView incomeAmountTV, expenseAmountTV;
    ListView expenseListview;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public dashboardFragment() {
        // Required empty public constructor
    }
    
    // TODO: Rename and change types and number of parameters
    public static dashboardFragment newInstance(String param1, String param2) {
        dashboardFragment fragment = new dashboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        // Setup date listener
        initDatePicker();
        editTextDate = getView().findViewById(R.id.selectDataET);
        editTextDate.setText(getTodaysDate());
        editTextDate.setOnClickListener(this);

        // Setup Search button
        searchBtn = getView().findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(this);

        // Income & Expense amount
        incomeAmountTV = getView().findViewById(R.id.incomeAmountTV);
        expenseAmountTV = getView().findViewById(R.id.expenseAmountTV);

        // Listview
        expenseListview = getView().findViewById(R.id.expenseListview);
    }

    // On Click listener
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.selectDataET:
                openDatePicker();
                break;

            case R.id.searchBtn:
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
                List<Statement> statements = searchItems(email);

                ExpenseCustomAdapter expenseCustomAdapter = new ExpenseCustomAdapter(getContext(), statements);
                expenseListview.setAdapter(expenseCustomAdapter);

                // Set Total Income &  Expense Amount
                incomeAmountTV = getView().findViewById(R.id.incomeAmountTV);
                incomeAmountTV.setText("$" + totalIncomeAmount);

                expenseAmountTV = getView().findViewById(R.id.expenseAmountTV);
                expenseAmountTV.setText("$" + totalExpenseAmount);
                break;
        }
    }


    /*
    Search Item Code
     */

    private List<Statement> searchItems(String userEmail) {
        List<Statement> allStatements = new ArrayList<Statement>();
        totalExpenseAmount = 0.0;
        totalIncomeAmount = 0.0;

        // Fetch Data from Database
        DatabaseOperations db = new DatabaseOperations(getContext());

        // Get Data from database
        String expenseData = db.getExpenseData(userEmail);

        db.getAllData();

        // Get Current Date & Time
        Integer month, day, year;

//        String monthName = Month.of();
        String dateArr [] = dateForJson.split("/");

        day =  Integer.parseInt(dateArr[1]);
        month = Integer.parseInt(dateArr[0]);
        year = Integer.parseInt(dateArr[2]);

        Month getmonthName = Month.of(month);
        String monthName = getmonthName.getDisplayName( TextStyle.FULL , Locale.US ).toUpperCase();

//        String expenseData = "{\"2022\":{\"DECEMBER\":[{\"12/3/2022\":{\"PaymentType\": \"Bank Account\",\"Category\": \"Expense\",\"money\": \"$800\"}},{\"12/4/2022\":{\"PaymentType\": \"Bank Account\",\"Category\": \"Income\",\"money\": \"$1000\"}}]}}";

        // Parsing JSON
        try {

            JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
            JSONObject yearData = jObject.getJSONObject(year.toString()); // Get Current Year Data
            JSONObject monthData = yearData.getJSONObject(monthName); // Get Current Month Data
            JSONArray daysData = monthData.optJSONArray(dateForJson); // Get Current Days Data

            if(daysData != null){
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

                        if(category.trim().toLowerCase().equals("expense")) totalExpenseAmount += amount;
                        else totalIncomeAmount += amount;

                        // Add data into Statements List
                        Statement stmt = new Statement(dateForJson, paymentType, category, money);
                        allStatements.add(stmt);
                    }

                }

            }else Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();


        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "No Data Found", Toast.LENGTH_SHORT).show();
        }
        return allStatements;
    }

    /*
    DatePicker code
     */

    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        dateForJson = month + "/" + day + "/" + year;
        return makeDateString(day, month, year);
    }

    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                dateForJson = month + "/" + day + "/" + year;
                editTextDate.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(getContext(), style, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

    }

    private String makeDateString(int day, int month, int year)
    {
        return getMonthFormat(month) + " " + day + " " + year;
    }

    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        //default should never happen
        return "JAN";
    }

    public void openDatePicker()
    {
        Log.i("openDatePicker: ", "Btn clicked");
        datePickerDialog.show();
    }
}