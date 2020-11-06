package com.example.quizeaadmin;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QuestionsActivity extends AppCompatActivity {
    public static final int CELL_COUNT = 6;
    public static List<QuestionModel> list;
    private Button addQuestion, addThroughExcel;
    private RecyclerView recyclerView;
    private QuestionAdapter adapter;
    private Dialog loadingDialog;
    private DatabaseReference myRef;
    private String setId, categoryName;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        addQuestion = findViewById(R.id.addQuestionBtn);
        addThroughExcel = findViewById(R.id.addThroughExcelBtn);
        recyclerView = findViewById(R.id.recyclerViewQues);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myRef = FirebaseDatabase.getInstance().getReference();
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.button_background));
        loadingDialog.getWindow().setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false);
        loadingText = loadingDialog.findViewById(R.id.textView3);


        categoryName = getIntent().getStringExtra("categoryTitle");
        setId = getIntent().getStringExtra("setId");
        getSupportActionBar().setTitle(categoryName);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        getData(categoryName, setId);

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionsActivity.this, AddQuestionActivity.class);
                intent.putExtra("categoryName", categoryName);
                intent.putExtra("setId", setId);
                startActivity(intent);
            }
        });
        addThroughExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(QuestionsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectFile();

                } else {
                    ActivityCompat.requestPermissions(QuestionsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFile();
            } else {
                Toast.makeText(this, "Please Grant Permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select file"), 102);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102) {
            if (resultCode == RESULT_OK) {
                String filePath = data.getData().getPath();
                if (filePath.endsWith(".xlsx")) {
                    // Toast.makeText(this, "File Selected", Toast.LENGTH_SHORT).show();
                    readFiles(data.getData());
                } else {
                    Toast.makeText(this, "Please Choose Excel File", Toast.LENGTH_SHORT).show();

                }
            }

        }
    }

    private void readFiles(final Uri fileUri) {

        loadingText.setText("Scanning Questions...");
        loadingDialog.show();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {


                final HashMap<String, Object> parentmap = new HashMap<>();
                final List<QuestionModel> tempList = new ArrayList<>();
                try {
                    InputStream intStream = getContentResolver().openInputStream(fileUri);
                    XSSFWorkbook workBook = new XSSFWorkbook(intStream);
                    XSSFSheet sheet = workBook.getSheetAt(0);
                    FormulaEvaluator formulaEvaluator = workBook.getCreationHelper().createFormulaEvaluator();
                    int rowCount = sheet.getPhysicalNumberOfRows();
                    if (rowCount > 0) {
                        for (int r = 0; r < rowCount; r++) {
                            Row row = sheet.getRow(r);
                            if (row.getPhysicalNumberOfCells() == CELL_COUNT) {
                                String question = getCellData(row, 0, formulaEvaluator);
                                String a = getCellData(row, 1, formulaEvaluator);
                                String b = getCellData(row, 2, formulaEvaluator);
                                String c = getCellData(row, 3, formulaEvaluator);
                                String d = getCellData(row, 4, formulaEvaluator);
                                String correctAns = getCellData(row, 5, formulaEvaluator);
                                if (correctAns.equals(a) || correctAns.equals(b) || correctAns.equals(c) || correctAns.equals(d)) {
                                    HashMap<String, Object> questionMap = new HashMap<>();
                                    questionMap.put("question", question);
                                    questionMap.put("optionA", a);
                                    questionMap.put("optionB", b);
                                    questionMap.put("optionC", c);
                                    questionMap.put("optionD", d);
                                    questionMap.put("correctAns", correctAns);
                                    questionMap.put("setId", setId);
                                    String id = UUID.randomUUID().toString();
                                    parentmap.put(id, questionMap);
                                    tempList.add(new QuestionModel(question, id, a, b, c, d, correctAns, setId));
                                } else {
                                    final int finalR = r;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadingText.setText("Loading....");
                                            loadingDialog.dismiss();
                                            Toast.makeText(QuestionsActivity.this, "Row No " + (finalR + 1) + " has no correct answer!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    return;
                                }
                            } else {
                                final int finalR1 = r;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loadingText.setText("Loading....");
                                        loadingDialog.dismiss();
                                        Toast.makeText(QuestionsActivity.this, "Row No " + (finalR1 + 1) + " has incorrect data!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                return;
                            }

                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingText.setText("Uploading....");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("SETS").child(setId)
                                        .setValue(parentmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.addAll(tempList);
                                            adapter.notifyDataSetChanged();
                                        } else {
                                            loadingText.setText("Loading....");
                                            loadingDialog.dismiss();
                                            Toast.makeText(QuestionsActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingDialog.dismiss();

                                    }
                                });


                            }
                        });


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingText.setText("Loading....");
                                loadingDialog.dismiss();
                                Toast.makeText(QuestionsActivity.this, "File is Empty!", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                } catch (final FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingText.setText("Loading....");
                            loadingDialog.dismiss();
                            Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (final IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingText.setText("Loading....");
                            loadingDialog.dismiss();
                            Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    private String getCellData(Row row, int cellPosition, FormulaEvaluator formulaEvaluator) {
        String value = "";
        Cell cell = row.getCell(cellPosition);
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return value + cell.getBooleanCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                return value + cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return value + cell.getStringCellValue();
            default:
                return value;
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

///////////////////////////////////////////////

    private void getData(final String categoryName, final String setId) {

        loadingDialog.show();
        myRef.child("SETS").child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String question = dataSnapshot.child("question").getValue().toString();
                    String a = dataSnapshot.child("optionA").getValue().toString();
                    String b = dataSnapshot.child("optionB").getValue().toString();
                    String c = dataSnapshot.child("optionC").getValue().toString();
                    String d = dataSnapshot.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot.child("correctAns").getValue().toString();
                    String id = dataSnapshot.getKey();
                    list.add(new QuestionModel(question, id, a, b, c, d, correctAns, setId));
                }
                loadingDialog.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                finish();
            }
        });

        adapter = new QuestionAdapter(list, categoryName, new QuestionAdapter.DeleteListener() {
            @Override
            public void onLongClick(final int position, final String id) {
                new AlertDialog.Builder(QuestionsActivity.this, R.style.Theme_AppCompat_Light_Dialog)
                        .setTitle("Delete Question")
                        .setMessage("Are you sure, to delete this Question?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                loadingDialog.dismiss();
                                myRef.child("SETS").child(setId).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            list.remove(position);
                                            adapter.notifyItemRemoved(position);
                                        } else {
                                            Toast.makeText(QuestionsActivity.this, "Failed to Delete !", Toast.LENGTH_SHORT).show();

                                        }
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }
}