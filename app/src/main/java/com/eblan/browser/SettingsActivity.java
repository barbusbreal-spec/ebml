package com.eblan.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private SharedPreferences prefs;
    private EditText homeUrlEdit;
    private CheckBox jsCheckbox;
    private CheckBox saveHistoryCheckbox;
    private Button clearHistoryBtn;
    private Button clearCacheBtn;
    private Button updateBtn;
    private TextView versionText;

    private static final String APP_VERSION = "1.0.0";
    private static final String FAKE_NEW_VERSION = "2.1.3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

        homeUrlEdit = (EditText) findViewById(R.id.home_url_edit);
        jsCheckbox = (CheckBox) findViewById(R.id.js_checkbox);
        saveHistoryCheckbox = (CheckBox) findViewById(R.id.save_history_checkbox);
        clearHistoryBtn = (Button) findViewById(R.id.clear_history_btn);
        clearCacheBtn = (Button) findViewById(R.id.clear_cache_btn);
        updateBtn = (Button) findViewById(R.id.update_btn);
        versionText = (TextView) findViewById(R.id.version_text);

        loadSettings();
        setupListeners();
    }

    private void loadSettings() {
        homeUrlEdit.setText(prefs.getString("home_url", "https://ya.ru"));
        jsCheckbox.setChecked(prefs.getBoolean("js_enabled", true));
        saveHistoryCheckbox.setChecked(prefs.getBoolean("save_history", true));
        versionText.setText("Версия " + APP_VERSION);
    }

    private void setupListeners() {
        clearHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Очистить историю")
                    .setMessage("Удалить всю историю посещений?")
                    .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BrowserDatabase.getInstance(SettingsActivity.this).clearHistory();
                            Toast.makeText(SettingsActivity.this, "История очищена", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            }
        });

        clearCacheBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Очистить кэш")
                    .setMessage("Очистить кэш браузера?")
                    .setPositiveButton("Очистить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(SettingsActivity.this, "Кэш очищен", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFakeUpdate();
            }
        });
    }

    // === ПРИКОЛ С ОБНОВЛЕНИЕМ ===
    private void startFakeUpdate() {
        new AlertDialog.Builder(this)
            .setTitle("Проверка обновлений")
            .setMessage("Проверяем наличие обновлений...")
            .setCancelable(false)
            .show();

        // Имитируем задержку "поиска"
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showUpdateFound();
            }
        }, 2000);
    }

    private void showUpdateFound() {
        new AlertDialog.Builder(this)
            .setTitle("Найдено обновление!")
            .setMessage("Доступна версия " + FAKE_NEW_VERSION + "\n\nЧто нового:\n• Улучшена скорость работы\n• Исправлено 47 ошибок\n• Новый интерфейс вкладок\n• Поддержка жестов\n\nРазмер: 8.4 МБ")
            .setPositiveButton("Скачать", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showFakeDownload();
                }
            })
            .setNegativeButton("Позже", null)
            .show();
    }

    private void showFakeDownload() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Загрузка обновления");
        pd.setMessage("Скачивается EblanBrowser v" + FAKE_NEW_VERSION + "...");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMax(100);
        pd.setProgress(0);
        pd.setCancelable(false);
        pd.show();

        final Handler handler = new Handler();
        final int[] progress = {0};

        final Runnable ticker = new Runnable() {
            @Override
            public void run() {
                progress[0] += (int)(Math.random() * 8) + 3;
                if (progress[0] >= 100) {
                    progress[0] = 100;
                    pd.setProgress(100);
                    pd.setMessage("Распаковка...");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pd.dismiss();
                            showInstallInstructions();
                        }
                    }, 1200);
                } else {
                    pd.setProgress(progress[0]);
                    pd.setMessage(String.format(
                        "Скачивается EblanBrowser v%s... %d%%\n%.1f МБ / 8.4 МБ",
                        FAKE_NEW_VERSION,
                        progress[0],
                        progress[0] * 8.4 / 100.0
                    ));
                    handler.postDelayed(this, 250);
                }
            }
        };
        handler.postDelayed(ticker, 250);
    }

    private void showInstallInstructions() {
        new AlertDialog.Builder(this)
            .setTitle("Установка обновления")
            .setMessage(
                "Загрузка завершена!\n\n" +
                "Для завершения установки необходимо удалить текущую версию " +
                "приложения и установить новую.\n\n" +
                "Нажмите «Удалить», затем установите скачанный файл."
            )
            .setPositiveButton("Удалить приложение", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Открываем стандартный диалог удаления Android
                    Intent intent = new Intent(Intent.ACTION_DELETE);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            })
            .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(
                        SettingsActivity.this,
                        "Обновление отменено. Файл сохранён в загрузках.",
                        Toast.LENGTH_LONG
                    ).show();
                }
            })
            .setCancelable(false)
            .show();
    }
    // === КОНЕЦ ПРИКОЛА ===

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("home_url", homeUrlEdit.getText().toString().trim());
        editor.putBoolean("js_enabled", jsCheckbox.isChecked());
        editor.putBoolean("save_history", saveHistoryCheckbox.isChecked());
        editor.apply();
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
