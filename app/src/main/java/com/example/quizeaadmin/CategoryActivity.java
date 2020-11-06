package com.example.quizeaadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryActivity extends AppCompatActivity {

    FirebaseDatabase database=FirebaseDatabase.getInstance();
    DatabaseReference myReference=database.getReference();

    private Dialog loadingDialog, addCategoryDialog;
    private CircleImageView addCategoryImage;
    private EditText addCategoryName;
    private Button addCategoryNameButton;

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    public static List<CategoryModel> list = new ArrayList<>();
    private Uri image;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbarset);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog=new Dialog( this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.button_background));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);

        setCategoryDialog();


        recyclerView = findViewById(R.id.recyclerviewID);
        list = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

         adapter = new CategoryAdapter(list, new CategoryAdapter.DeleteListener() {
             @Override
             public void onDelete(final String key, final int position) {
                new AlertDialog.Builder(CategoryActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Category")
                        .setMessage("Are you sure, to delete this category?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.dismiss();
                                myReference.child("categories").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            for (String setIds :list.get(position).getSets()){
                                                myReference.child("SETS").child(setIds).removeValue();
                                            }
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                           loadingDialog.dismiss();
                                        } else {
                                            Toast.makeText(CategoryActivity.this, "Failed to Delete !", Toast.LENGTH_SHORT).show();
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
        recyclerView.setAdapter(adapter);
        loadingDialog.show();
        myReference.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                   // list.add(dataSnapshot.getValue(CategoryModel.class));
                    List<String> sets=new ArrayList<>();
                    for (DataSnapshot dataSnapshot1:dataSnapshot.child("sets").getChildren()){
                        sets.add(dataSnapshot1.getKey());
                    }
                    list.add(new CategoryModel(
                            dataSnapshot.child("name").getValue().toString(),
                            sets,
                            dataSnapshot.child("url").getValue().toString(),
                            dataSnapshot.getKey()
                            ));
                }
                adapter.notifyDataSetChanged();
                loadingDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add){
            ///show dialog
            addCategoryDialog.show();
        }
        if (item.getItemId()==R.id.logout){
            new AlertDialog.Builder(CategoryActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                    .setTitle("Logout")
                    .setMessage("Are you sure, to Logout?")
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadingDialog.dismiss();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent=new Intent(CategoryActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        }
        return super.onOptionsItemSelected(item);
    }
    private void setCategoryDialog(){
        addCategoryDialog=new Dialog( this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog_layout);
        addCategoryDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.border));
        addCategoryDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addCategoryDialog.setCancelable(true);

        addCategoryImage=addCategoryDialog.findViewById(R.id.addImage);
        addCategoryNameButton=addCategoryDialog.findViewById(R.id.add_C_button);
        addCategoryName=addCategoryDialog.findViewById(R.id.etAddCategory);

        addCategoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 101);
            }
        });

        addCategoryNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addCategoryName.getText().toString().isEmpty()){
                    addCategoryName.setError("Required!");
                    return;
                }
                for(CategoryModel model:list){
                    if (addCategoryName.getText().toString().equals(model.getName())){
                        addCategoryName.setError("Category Already Exists!");
                        return;
                    }
                }
                if(image==null){
                    Toast.makeText(CategoryActivity.this, "Upload Your Image!", Toast.LENGTH_SHORT).show();
                    return;
                }
                addCategoryDialog.dismiss();
                uploadFile();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==101){
            if (resultCode==RESULT_OK){
                 image=data.getData();
                addCategoryImage.setImageURI(image);
            }
        }
    }

    private void uploadFile(){
        loadingDialog.show();
        StorageReference storageReference= FirebaseStorage.getInstance().getReference();
        final StorageReference imageReference=storageReference.child("categories").child(image.getLastPathSegment());
        UploadTask uploadTask = imageReference.putFile(image);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return imageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadUrl=task.getResult().toString();
                            UploadCategoryName();
                        }else {
                            Toast.makeText(CategoryActivity.this, "Something Error !", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    }
                });
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                } else {
                    Toast.makeText(CategoryActivity.this, "Something Error !", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            }
        });
    }

    private void UploadCategoryName(){

        Map<String, Object> map=new HashMap<>();
        map.put("name", addCategoryName.getText().toString());
        map.put("sets", 0);
        map.put("url", downloadUrl);

      FirebaseDatabase database=FirebaseDatabase.getInstance();
      final String id= UUID.randomUUID().toString();
      database.getReference().child("categories").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()){
                  list.add(new CategoryModel(addCategoryName.getText().toString(),new ArrayList<String>(), downloadUrl,id));
                  adapter.notifyDataSetChanged();
              }else {
                  Toast.makeText(CategoryActivity.this, "Something Error !", Toast.LENGTH_SHORT).show();
              }
              loadingDialog.dismiss();
          }
      });
    }
}
