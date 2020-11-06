package com.example.quizeaadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import javax.microedition.khronos.opengles.GL;

public class GridAdapter extends BaseAdapter {
        private String categoryTitle;
    private GridListener gridListener;
    public List<String> sets;

    public GridAdapter(List<String> sets, String categoryTitle, GridListener gridListener) {
        this.sets = sets;
        this.categoryTitle=categoryTitle;
        this.gridListener=gridListener;
    }

    @Override
    public int getCount() {
        return sets.size()+1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if(convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_items,parent,false);
        }
        else {
            view = convertView;
        }
        if (position==0){
            ( (TextView)view.findViewById(R.id.textView_si)).setText("+");

        }else {
            ( (TextView)view.findViewById(R.id.textView_si)).setText(String.valueOf(position));

        }
       view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position==0){
                    //add
                    gridListener.addSets();
                }else {
                    Intent questionIntent=new Intent(parent.getContext(), QuestionsActivity.class);
                    questionIntent.putExtra("categoryTitle", categoryTitle);
                    questionIntent.putExtra("setId", sets.get(position-1));
                    parent.getContext().startActivity(questionIntent);
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (position!=0){
                    gridListener.onLongClick(sets.get(position-1), position);
                }
                return false;
            }
        });
        return view;
    }
    public interface GridListener{
        public void addSets();
        public void onLongClick(String setId,int position);
    }
}
