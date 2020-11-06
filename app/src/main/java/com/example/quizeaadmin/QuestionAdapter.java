package com.example.quizeaadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.MyViewHolder> {
    private List<QuestionModel> list;
    private String category;
    private DeleteListener listener;

    public QuestionAdapter(List<QuestionModel> list, String category, DeleteListener listener) {
        this.list = list;
        this.category=category;
        this.listener=listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.question_items_layout, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String question=list.get(position).getQuestion();
        String answer=list.get(position).getAnswer();
        holder.setData(question, answer, position);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView question, answer;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            question=itemView.findViewById(R.id.question);
            answer=itemView.findViewById(R.id.answer);
        }
        private void setData(String que, String ans, final int position){
            this.question.setText(position+1+". "+que);
           this.answer.setText("Ans: "+ans);

           itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent editIntent=new Intent(itemView.getContext(), AddQuestionActivity.class);
                   editIntent.putExtra("categoryName",category);
                   editIntent.putExtra("setId",list.get(position).getSetId());
                   editIntent.putExtra("position",position);
                   itemView.getContext().startActivity(editIntent);
               }
           });

           itemView.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View view) {

                   listener.onLongClick(position, list.get(position).getId());
                   return false;

               }
           });
        }
    }
    public interface DeleteListener{
        void onLongClick(int position, String id);
    }
}
