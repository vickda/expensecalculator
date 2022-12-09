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

        String categoryValue = statements.get(position).getCategory();
        ViewHolderItems viewHolderItems;

        // Getting all View by IDs
        if(convertView == null){
            convertView = inflater.inflate(R.layout.mainlistlayout, parent, false);

            viewHolderItems = new ViewHolderItems();

            viewHolderItems.paymentType = convertView.findViewById(R.id.spendMethodTV);
            viewHolderItems.category = convertView.findViewById(R.id.expenseCategoryTV);
            viewHolderItems.date = convertView.findViewById(R.id.expenseDate);
            viewHolderItems.money = convertView.findViewById(R.id.amount);
            viewHolderItems.statementImage = convertView.findViewById(R.id.statementImage);

            // Setting our object into the convert View
            convertView.setTag(viewHolderItems);

        }

        ViewHolderItems holder = (ViewHolderItems) convertView.getTag();

        // Assigning values to each textviews
        holder.paymentType.setText(statements.get(position).getPaymentType());
        holder.category.setText(categoryValue);
        holder.date.setText(statements.get(position).getDate());
        holder.money.setText(statements.get(position).getMoney());

        // Set the statement image based on category
        if(categoryValue.equals("Expense")){
            holder.statementImage.setImageResource(R.drawable.expense);
        }else if (categoryValue.equals("Income")){
            holder.statementImage.setImageResource(R.drawable.income);
        }

        return convertView;
    }

    protected static class ViewHolderItems {


        TextView date , paymentType, category, money;
        ImageView statementImage;

//        TextView date = null, paymentType = null, category = null, money = null;
//        ImageView statementImage = null;
    }
}
