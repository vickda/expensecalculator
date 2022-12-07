package com.example.expensecalculator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ExpenseCustomAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<Statement> statements;

    ExpenseCustomAdapter(Context context, List<Statement> customizedListView){
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        statements = customizedListView;
    }

    @Override
    public int getCount() {
        return statements.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView date = null, paymentType = null, category = null, money = null;
        ImageView statementImage = null;
        String categoryValue = statements.get(position).getCategory();

        // Getting all View by IDs
        if(convertView == null){
            convertView = inflater.inflate(R.layout.mainlistlayout, parent, false);

            paymentType = convertView.findViewById(R.id.spendMethodTV);
            category = convertView.findViewById(R.id.expenseCategoryTV);
            date = convertView.findViewById(R.id.expenseDate);
            money = convertView.findViewById(R.id.amount);
            statementImage = convertView.findViewById(R.id.statementImage);

        }

        // Assigning values to each textviews
        paymentType.setText(statements.get(position).getPaymentType());
        category.setText(categoryValue);
        date.setText(statements.get(position).getDate());
        money.setText(statements.get(position).getMoney());

        // Set the statement image based on category
        if(categoryValue.equals("Expense")){
            statementImage.setImageResource(R.drawable.expense);
        }else if (categoryValue.equals("Income")){
            statementImage.setImageResource(R.drawable.income);
        }

        Log.i("get view", "getView: " + statements.get(position).getPaymentType());

        return convertView;
    }
}
