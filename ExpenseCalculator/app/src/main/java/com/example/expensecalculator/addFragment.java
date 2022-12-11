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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Month;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link addFragment#newInstance} factory method to
 * create an instance of getContext() fragment.
 */
public class addFragment extends Fragment implements View.OnClickListener{

    // View Variables
    Spinner selectTypeDropdwn;
    DatePickerDialog datePickerDialog;
    EditText editTextExpenseTitle, editTextDate, editTextExpenseDescription;
    RadioGroup radioGroupPaidWith;
    RadioButton selectedRadioBtn;
    TextView amountTextView;
    Button  addBtn;

    // Firebase Variables
    FirebaseAuth mauth;

    // Other Variables
    String dateForJson;
    String userEmail;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public addFragment() {
        // Required empty public constructor
    }

    public static addFragment newInstance(String param1, String param2) {
        addFragment fragment = new addFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getContext() fragment
        return inflater.inflate(R.layout.fragment_addfragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        // Get User Email
//        userEmail = "vdas@hawk.iit.edu";

        // Setup Dropdown
        selectTypeDropdwn = getView().findViewById(R.id.selectTypeDropdwn);
        String[] category = {"Expense", "Income"};

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),R.layout.spinner_selectedtext_color,  category);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectTypeDropdwn.setAdapter(categoryAdapter);

        // Setup Expense Title
        editTextExpenseTitle = getView().findViewById(R.id.editTextExpenseTitle);

        // Setup Date
        initDatePicker();
        editTextDate = getView().findViewById(R.id.editTextDate);
        editTextDate.setFocusable(false);
        editTextDate.setText(getTodaysDate());
        editTextDate.setOnClickListener(this);

        // Setup Expense Description
        editTextExpenseDescription = getView().findViewById(R.id.editTextExpenseDescription);

        // Setup Radio Button
        radioGroupPaidWith = getView().findViewById(R.id.radioGroupPaidWith);

        // Setup Expense Amount
        amountTextView = getView().findViewById(R.id.amountTextView);

        // Add Button
        addBtn = getView().findViewById(R.id.addBtn);
        addBtn.setOnClickListener(this);
    }

    // Event Listener
    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.addBtn:
                try {
                    onClickAddBtn();

                    // Clear out data once added
                    editTextExpenseTitle.setText("");
                    editTextExpenseDescription.setText("");
                    amountTextView.setText("");
                    editTextDate.setText(getTodaysDate());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Oops Something went wrong (Cant Add Try Again later)", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.editTextDate:
                openDatePicker();
                break;
        }
    }


    /*
    Add Button code
     */

    private void onClickAddBtn() throws JSONException {
        String category, expenseTitle, description, paidWith, money;
        String [] values;
        JSONObject finalJsonString = new JSONObject();
        Boolean isValueEmpty = false;

        category = selectTypeDropdwn.getSelectedItem().toString();
        expenseTitle = editTextExpenseTitle.getText().toString();
        description = editTextExpenseDescription.getText().toString();
        money = amountTextView.getText().toString();

        selectedRadioBtn = getView().findViewById(radioGroupPaidWith.getCheckedRadioButtonId());
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
            Toast.makeText(getContext(), "Values Cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        finalJsonString.put("Expense Title", expenseTitle);
        finalJsonString.put("Expense Description", description);
        finalJsonString.put("PaymentType", paidWith);
        finalJsonString.put("Category", category);
        finalJsonString.put("money", "$" + money);

        addToDatabase(finalJsonString);

    }

    // Add the expense data into the database
    private void addToDatabase(JSONObject expenseJson) {

        String expenseData, monthName, finalJsonString;
        Integer year;

        year = Integer.parseInt(dateForJson.split("/")[2]);
        monthName = (Month.of(Integer.parseInt(dateForJson.split("/")[0]))).toString();

        Log.i("addToDatabase: ",monthName + " " +  dateForJson.split("/")[0]);

        // Get All data
        DatabaseOperations db = new DatabaseOperations(getContext());

        expenseData = db.getExpenseData(userEmail);

        if(expenseData.equals("{}")){
            finalJsonString = String.format("{\"%s\":{\"%s\": {\"%s\":[%s]}} }", year, monthName, dateForJson, expenseJson);
            db.updateUserData(userEmail, finalJsonString);

            db.getAllData();
            Toast.makeText(getContext(), "Data Added", Toast.LENGTH_SHORT).show();
            return;
        }
        // Parse JSON if expense data is not null
            // Parse Json and add the expense data into users data
            try {
                JSONObject jObject = new JSONObject(expenseData); // Parse Entire JSON
                JSONObject yearData = jObject.optJSONObject(year.toString()); // Get Current Year Data

                // If Year Data is null
                if(yearData==null){
                    jObject.put(""+year, new JSONObject());
                    yearData = jObject.optJSONObject(year.toString());
                }

                // Get The Month Data
                JSONObject monthData = yearData.optJSONObject(monthName); // Get Current Month Data

                // If month data is null
                if(monthData == null){
                    Log.i("addToDatabase: ", "Insde if");
                    JSONObject jObj = new JSONObject();
                    JSONArray jarr = new JSONArray();
                    jarr.put(expenseJson);

                    jObj.put(dateForJson, jarr);
                    yearData.put(monthName, jObj);

                    monthData = yearData.optJSONObject(monthName); // Get Updated Current Month Data

                }else{

                    // Get Days Data
                    JSONArray daysData = monthData.optJSONArray(dateForJson); // Get Current Days Data
                    daysData.put(expenseJson);
                }

                // Update data into the database
                db.updateUserData(userEmail, jObject.toString());
                Toast.makeText(getContext(), "Data Added", Toast.LENGTH_SHORT).show();
                db.getAllData();

            }
            catch (JSONException e){
                e.printStackTrace();
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