package com.example.projet;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase database;
    private ArrayAdapter<String> adapter;
    private List<String> articles;

    private ListView listView;
    private TextView articleTitleTextView;
    private TextView articleContentTextView;
    private EditText newArticleTitleEditText;
    private EditText newArticleContentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        listView = findViewById(R.id.listView);
        articleTitleTextView = findViewById(R.id.articleTitleTextView);
        articleContentTextView = findViewById(R.id.articleContentTextView);
        newArticleTitleEditText = findViewById(R.id.newArticleTitleEditText);
        newArticleContentEditText = findViewById(R.id.newArticleContentEditText);

        articles = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, articles);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String title = articles.get(position);
                String content = getArticleContent(title);
                articleTitleTextView.setText(title);
                Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
            }
        });

        loadArticles();
    }

    private void loadArticles() {
        articles.clear();

        Cursor cursor = database.query(
                DatabaseContract.ArticleEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int columnIndexTitle = cursor.getColumnIndex(DatabaseContract.ArticleEntry.COLUMN_TITLE);
            int columnIndexContent = cursor.getColumnIndex(DatabaseContract.ArticleEntry.COLUMN_CONTENT);

            do {
                String title = cursor.getString(columnIndexTitle);
                String content = cursor.getString(columnIndexContent);

                // Utilisez les valeurs récupérées ici
                articles.add(title);

            } while (cursor.moveToNext());
        }

        cursor.close();

        adapter.notifyDataSetChanged();
    }

    private void addArticle(String title, String content) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ArticleEntry.COLUMN_TITLE, title);
        values.put(DatabaseContract.ArticleEntry.COLUMN_CONTENT, content);

        long newRowId = database.insert(DatabaseContract.ArticleEntry.TABLE_NAME, null, values);

        if (newRowId != -1) {
            Toast.makeText(this, "Contenu de l'article : " + content, Toast.LENGTH_LONG).show();
            newArticleTitleEditText.setText("");
            newArticleContentEditText.setText("");
            loadArticles();
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout de l'article.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getArticleContent(String title) {
        String[] projection = {
                DatabaseContract.ArticleEntry.COLUMN_CONTENT
        };

        String selection = DatabaseContract.ArticleEntry.COLUMN_TITLE + " = ?";
        String[] selectionArgs = { title };

        Cursor cursor = database.query(DatabaseContract.ArticleEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, null);

        String content = "";

        if (cursor.moveToNext()) {
            int columnIndexContent = cursor.getColumnIndex(DatabaseContract.ArticleEntry.COLUMN_CONTENT);

            if (columnIndexContent >= 0) {
                content = cursor.getString(columnIndexContent);
            }
        }

        cursor.close();

        return content;
    }

    public void onAddArticleClick(View view) {
        String title = newArticleTitleEditText.getText().toString().trim();
        String content = newArticleContentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
        } else {
            addArticle(title, content);
        }
    }
}
