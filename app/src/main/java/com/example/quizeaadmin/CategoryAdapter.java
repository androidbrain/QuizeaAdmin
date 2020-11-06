package com.example.quizeaadmin;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private List<CategoryModel> categoriesModelList;
    private DeleteListener deleteListener;

    public CategoryAdapter(List<CategoryModel> categoriesModelList, DeleteListener deleteListener) {
        this.categoriesModelList = categoriesModelList;
        this.deleteListener=deleteListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(categoriesModelList.get(position).getUrl(),
                categoriesModelList.get(position).getName(),
                categoriesModelList.get(position).getKey(),
                position);

    }

    @Override
    public int getItemCount() {

        return categoriesModelList.size();
    }
     class MyViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView imageView;
        private TextView title;
        private ImageButton delete;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            title = itemView.findViewById(R.id.title);
            delete = itemView.findViewById(R.id.delete_btn);
        }
        private void setData(String url, final String title,final String key, final int position ){
            Glide.with(itemView.getContext()).load(url).into(imageView);
            this.title.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent setIntent = new Intent(itemView.getContext(), SetsActivity.class);
                    setIntent.putExtra("title", title);
                    setIntent.putExtra("position", position);
                    setIntent.putExtra("key", key);
                    itemView.getContext().startActivity(setIntent);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteListener.onDelete(key, position);
                }
            });

        }
    }

    public interface DeleteListener{
        public void onDelete(String key, int position);
    }
}
