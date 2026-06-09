package com.eblan.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class BookmarksActivity extends Activity {

    private ListView listView;
    private List<BrowserDatabase.BrowserItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setTitle("Закладки");
        }

        listView = (ListView) findViewById(R.id.list_view);
        loadBookmarks();
    }

    private void loadBookmarks() {
        items = BrowserDatabase.getInstance(this).getBookmarks();

        if (items.isEmpty()) {
            Toast.makeText(this, "Нет закладок", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titles = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            titles[i] = items.get(i).title + "\n" + items.get(i).url;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_list_item_1, titles
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent();
                result.putExtra("url", items.get(position).url);
                setResult(RESULT_OK, result);
                finish();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(BookmarksActivity.this)
                    .setTitle("Удалить закладку?")
                    .setMessage(items.get(position).title)
                    .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BrowserDatabase.getInstance(BookmarksActivity.this)
                                .removeBookmark(items.get(position).id);
                            loadBookmarks();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
