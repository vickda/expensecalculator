package com.example.expensecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;

public class AddExpenseActivity extends AppCompatActivity implements View.OnClickListener{

    // View Variables
    Spinner selectTypeDropdwn;
    DatePickerDialog datePickerDialog;
    EditText editTextExpenseTitle, editTextDate, editTextExpenseDescription;
    RadioGroup radioGroupPaidWith;
    RadioButton selectedRadioBtn;
    TextView amountTextView;
    Button closeBtn, addBtn;

    // Firebase Variables
    FirebaseAuth mauth;

    // Other Variables
    String dateForJson;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Get User Email
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
//        userEmail = "vdas@hawk.iit.edu";

        // Setup Dropdown
        selectTypeDropdwn = findViewById(R.id.selectTypeDropdwn);
        String[] category = {"Expense", "Income"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,  category);
        selectTypeDropdwn.setAdapter(categoryAdapter);

        // Setup Expense Title
        editTextExpenseTitle = findViewById(R.id.editTextExpenseTitle);

        // Setup Date
        initDatePicker();
        editTextDate = findViewById(R.id.editTextDate);
        editTextDate.setFocusable(false);
        editTextDate.setText(getTodaysDate());
        editTextDate.setOnClickListener(this);

        // Setup Expense Description
        editTextExpenseDescription = findViewById(R.id.editTextExpenseDescription);

        // Setup Radio Button
        radioGroupPaidWith = findViewById(R.id.radioGroupPaidWith);

        // Setup Expense Amount
        amountTextView = findViewById(R.id.amountTextView);

        // Close Button
        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(this);

        // Add Button
        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(this);


    }

    // Event Listener
    @Override
    public void onClick(View view) {

        switch(view.getId()){
            // For Close Button
            case R.id.closeBtn:
                onClickCloseBtn();
                break;

            case R.id.addBtn:
                onClickAddBtn();
                break;

            case R.id.editTextDate:
                openDatePicker();
                break;
        }
    }

    /*
    Close Button code
     */

    private void onClickCloseBtn(){
        startActivity(new Intent(this, MainActivity.class));
    }


    /*
    Add Button code
     */

    private void onClickAddBtn(){
        String finalJsonString, category, expenseTitle, description, paidWith, money;
        String [] values;
        Boolean isValueEmpty = false;

        category = selectTypeDropdwn.getSelectedItem().toString();
        expenseTitle = editTextExpenseTitle.getText().toString();
        description = editTextExpenseDescription.getText().toString();
        money = amountTextView.getText().toString();

        selectedRadioBtn = findViewById(radioGroupPaidWith.getCheckedRadioButtonId());
        paidWith = selectedRadioBtn.getText().toString();

        values = new String[]{category, expenseTitle, description, money, paidWith};

        // Check if any input is null
        for (int i = 0; i < values.length; i++) {
            String value = values[i];

            if(value.isEmpty()){
                isValueEmpty = true;
            }
        }

        if(isValueEmpty){
            Toast.makeText(this, "Values Cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        finalJsonString = String.format("{\"%s\":{\"Expense Title\": \"%s\", \"Expense Description\": \"%s\", \"PaymentType\": \"%s\",\"Category\": \"%s\",\"money\": \"$%s\"}}",
                dateForJson, expenseTitle, description, paidWith, category, money);

        addToDatabase(finalJsonString);
    }

    private void addToDatabase(String expenseJson) {

        String expenseData, fullDate, monthName, finalJsonString;
        Integer month, day, year;

        day =  java.time.LocalDateTime.now().getDayOfMonth();
        month = java.time.LocalDateTime.now().getMonthValue();
        year = java.time.LocalDateTime.now().getYear();
        monthName = java.time.LocalDateTime.now().getMonth().toString();

        // Get All data
        DatabaseOperations db = new DatabaseOperations(this);

        expenseData = db.getExpenseData(userEmail);

        if(expenseData.equals("{}")){
            finalJsonString = String.format("{\"%s\":{\"%s\":[%s]}}", year, monthName, expenseJson);
            db.updateUserData(userEmail, finalJsonString);

            db.getAllData();
        }
        // Parse JSON if expense data is not null
        else{
            // Parse Json and add the expense data into users data
            try {
                JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
                JSONObject yearData = jObject.getJSONObject(year.toString()); // Get Current Year Data
                JSONArray monthData = yearData.getJSONArray(monthName); // Get Current Month Data

                monthData.put(expenseJson);

                db.updateUserData(userEmail, jObject.toString());

                db.getAllData();

            }
            catch (JSONException e){
                e.printStackTrace();
            }
        }

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

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
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