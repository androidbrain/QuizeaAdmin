package com.example.quizeaadmin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.UUID;

public class SetsActivity extends AppCompatActivity {
    private Dialog loadingDialog;
    private GridAdapter adapter;
    private String categoryName;
    private DatabaseReference myRef;
    private List<String> sets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        categoryName = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(categoryName);

        GridView gridView = findViewById(R.id.gridViewID);
        myRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.button_background));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        sets = CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getSets();

        adapter = new GridAdapter(sets, getIntent().getStringExtra("title"), new GridAdapter.GridListener() {
            @Override
            public void addSets() {
                loadingDialog.show();
                final String id = UUID.randomUUID().toString();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference().child("categories").child(getIntent().getStringExtra("key")).child("sets").child(id).setValue("SET ID")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    sets.add(id);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(SetsActivity.this, "Something Went wrong! ", Toast.LENGTH_SHORT).show();
                                }
                                loadingDialog.dismiss();
                            }
                        });
            }

            @Override
            public void onLongClick(final String setId, int position) {
                new AlertDialog.Builder(SetsActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete " + position)
                        .setMessage("Are you sure, to delete this Set?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.dismiss();
                                myRef.child("SETS").child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myRef.child("categories").child(CategoryActivity.list.get(getIntent().getIntExtra("position", 0)).getKey()).child("sets")
                                                    .child(setId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        sets.remove(setId);
                                                        adapter.notifyDataSetChanged();
                                                    }else {
                                                        Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingDialog.dismiss();
                                                }
                                            });

                                        } else {
                                            Toast.makeText(SetsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();

                                        }

                                    }
                                });

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });
        gridView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
