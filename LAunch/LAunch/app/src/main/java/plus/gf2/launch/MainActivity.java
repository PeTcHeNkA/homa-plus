package plus.gf2.launch;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import plus.proto.JsonTMap;
import plus.proto.JsonUtils;
import plus.proto.LangPackV2;
import plus.tson.TsonFile;


public class MainActivity extends ComponentActivity {
    private static final String GF2Package = "com.Sunborn.SnqxExilium.Glo";
    private boolean nowBuilding = false;
    private boolean isSettings = false;
    private int pageId = 0;

    public plus.gf2.launch.Settings settings = new plus.gf2.launch.Settings();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if(!checkCanAccessFS()){
            info("Разрешение не получено ... Без свободного доступа к файловой системе, русификатор не может продолжать работу!");
            runLater(() -> info(null), 2000);
        }
        settings.read(this);
    }


    public void onClickPlay(View v){
        if(nowBuilding)return;
        try {
            boolean isInstalled = isGF2Installed();

            if(isInstalled){
                startActivity(getPackageManager().getLaunchIntentForPackage(GF2Package));
            } else {
                info("Игра не установлена ...");
                runLater(() -> {
                    info(null);
                }, 2000);
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }


    private boolean checkCanAccessFS(){
        File file = Environment.getExternalStoragePublicDirectory("GF2Packs");
        if(file.canRead() && file.canWrite()){
            return true;
        } else {
            requestFilesPermissions();
            return file.canRead() && file.canWrite();
        }
    }


    public void onClickCheck(View v){
        try {
            if(checkCanAccessFS()){
                onClickContinue();
            } else {
                info("Доступ к файлам запрещен ...");
                runLater(() -> info(null), 2000);
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }


    public void onClickHelp(View v){
        pageId = 1;
        setContentView(R.layout.info);
        TextView text = findViewById(R.id.helpText);
        text.setMovementMethod(new ScrollingMovementMethod());
    }


    public void onClickClearCache(View v){
        File file = new File(this.getFilesDir(), "translation_cache.jobj");
        if(file.exists()){
            file.delete();
            info("Кеш был очищен");
        } else {
            info("Кеш уже очищен");
        }
        runLater(() -> info(null), 1000);
    }


    public void onClickHelpBack(View v){
        if(isSettings){
            settings.repo1 = getInputText(R.id.settings_repo1);
            settings.repo2 = getInputText(R.id.settings_repo2);
            settings.repo3 = getInputText(R.id.settings_repo3);
            settings.enableTranslation = checkInputBool(R.id.settings_translate);

            new Thread(() -> settings.write(this)).start();
        }
        pageId = 0;
        setContentView(R.layout.main);
        isSettings = false;
    }


    private boolean checkInputBool(int id){
        Checkable checkable = findViewById(id);
        return checkable.isChecked();
    }


    private String getInputText(int id){
        TextInputEditText el = findViewById(id);
        Editable text = el.getText();
        if(text == null)return null;
        return text.toString();
    }


    private void setInputText(int id, String str){
        TextInputEditText el = findViewById(id);
        Editable text = el.getText();
        if(text == null)return;
        text.clear();
        if(str == null)return;
        text.append(str);
    }


    private void setCheckInputBool(int id, boolean b){
        Checkable checkable = findViewById(id);
        if(b != checkable.isChecked()){
            checkable.toggle();
        }
    }


    public void onClickSettings(View v){
        try {
            setContentView(R.layout.settings);
            pageId = 2;
            isSettings = true;

            setInputText(R.id.settings_repo1, settings.getRepo1());
            setInputText(R.id.settings_repo2, settings.getRepo2());
            setInputText(R.id.settings_repo3, settings.getRepo3());
            setCheckInputBool(R.id.settings_translate, settings.enableTranslation);
        } catch (Throwable e){
            e.printStackTrace();
        }
    }


    private void info(String text) {
        new Handler(Looper.getMainLooper()).post(() -> {
            TextView view = null;
            if (pageId == 0) {
                view = findViewById(R.id.info_view_main);
            }

            if (view != null) {
                TextView finalView = view;

                if (text == null) {
                    finalView.setVisibility(GONE);
                } else {
                    finalView.setVisibility(VISIBLE);
                    finalView.setText(text);
                }
            } else {
                if (text == null) return;
                Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }


    private void onClickContinue() {
        if (nowBuilding) return;
        File wDir = Environment.getExternalStoragePublicDirectory("GF2Packs");

        if (!wDir.exists()) {
            wDir.mkdir();
            return;
        }
        File enPack = new File(wDir, "LangPackageTableEnusData.bytes");
        if (!enPack.exists()) {
            info("Файл LangPackageTableEnusData.bytes не найден");
            return;
        }
        nowBuilding = true;
        AtomicBoolean runOnComplete = new AtomicBoolean(true);

        Thread thread = new Thread(() -> {
            try {
                String[] allReposes = new String[]{
                        settings.getRepo1(), settings.getRepo2(), settings.getRepo3()
                };
                int i = 0;
                LangPackV2 pack = new LangPackV2();
                info("Чтение таблицы локализации ...");
                pack.read(enPack, true);

                for (String repo:allReposes) {
                    if(repo == null)continue;
                    i+=1;
                    byte[] jPack = null;
                    if(repo.startsWith("http")) {
                        info("Загрузка переводов [" + i + "] ...");

                        jPack = loadDataByUrl(repo);
                    } else if(repo.startsWith("file:")){
                        info("Чтение переводов [" + i + "] ...");

                        jPack = TsonFile.readFile(Environment.getExternalStoragePublicDirectory("GF2Packs/"+repo.substring(5)));
                    } else {
                        continue;
                    }
                    info("Анализ переводов ["+i+"] ...");
                    HashMap<String, String> map = new HashMap<>();
                    JsonUtils.readFrom(jPack, map);
                    JsonTMap jMap = new JsonTMap(map);

                    info("Применение переводов ["+i+"] ...");
                    jMap.apply(pack, false);
                }

                if(settings.enableTranslation){
                    info("Запуск перевода ML Kit");
                    MlKitTranslator translator = new MlKitTranslator();
                    translator.setPack(pack);
                    translator.setupTranslator(success -> {
                        if(success) {
                            try {
                                ITranslationCallback cb = translator.translateAll(this, () -> {
                                    try {
                                        savePack(pack, wDir);
                                    } catch (Exception e) {
                                        onErrorBuild(e);
                                    } finally {
                                        onComplete();
                                    }
                                });
                                runTranslationProgressInfo(cb);
                            } catch (Exception e) {
                                info("Ошибка перевода: " + e.getMessage());
                                onComplete();
                            }
                        } else {
                            try {
                                savePack(pack, wDir);
                            } catch (Exception e) {
                                onErrorBuild(e);
                            } finally {
                                onComplete();
                            }
                        }
                    });
                    runOnComplete.set(false);
                    return;
                }
                savePack(pack, wDir);
            } catch (Exception e){
                onErrorBuild(e);
            } finally {
                if(runOnComplete.get()) onComplete();
            }
        });
        thread.start();
    }


    private void runTranslationProgressInfo(ITranslationCallback cb){
        if(cb.isCompleted())return;
        Runnable infoDelayed = new Runnable() {
            @Override
            public void run() {
                info("Переведено "+cb.getCurrent()+" / "+cb.getTotal());
                if(!cb.isCompleted()){
                    runLater(this, 250);
                }
            }
        };
        infoDelayed.run();
    }


    private void savePack(LangPackV2 pack, File wDir) throws Exception {
        info("Сохранение ...");
        pack.write(new File(wDir, "LangPackageTableEnusDataRUS.bytes"));
        info("Готово");
    }


    private void onErrorBuild(Exception e){
        info("Ошибка сборки пакета: "+e.getMessage());
        e.printStackTrace();
    }

    private void onComplete(){
        runLater(() -> {
            nowBuilding = false;
            info(null);
        }, 2000);
    }


    private void runLater(Runnable task, int delay){
        new Handler(Looper.getMainLooper()).postDelayed(task, delay);
    }


    private boolean checkNetwork = true;
    public byte[] loadDataByUrl(String url1){
        if(checkNetwork){
            if(ensuePermsission(Manifest.permission.INTERNET))
                checkNetwork = false;
            else return null;
        }
        byte[] result = null;
        try {
            URL url = new URL(url1);
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result = in.readAllBytes();
            }
            in.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public boolean ensuePermsissions(String... perms){
        boolean result = true;
        for (String perm: perms){
            result = result & ensuePermsission(perm);
        }
        return result;
    }


    public void requestFilesPermissions(){
        var intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivityForResult(intent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data, @NonNull ComponentCaller caller) {
        super.onActivityResult(requestCode, resultCode, data, caller);
        if(requestCode == 101){
            onClickContinue();
        }
    }

    private void requestDocumentPermission(String folder) {
        var storageManager = ((StorageManager)this.getSystemService(Context.STORAGE_SERVICE));
        var intent =  storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        var targetDirectory = "Android/"+folder; // add your directory to be selected by the user
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI", Uri.class);
        }
        var scheme = uri.toString();
        scheme = scheme.replace("/root/", "/document/");
        scheme += ("/" + targetDirectory);
        uri = Uri.parse(scheme);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        startActivityForResult(intent, 92115);
    }

    public boolean ensuePermsission(String perm){
        if(this.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{perm},
                    92115
            );
        }
        return this.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }


    private ApplicationInfo gf2CachedInf = null;
    public ApplicationInfo getGF2Inf(){
        if(gf2CachedInf != null)return gf2CachedInf;
        if(!ensuePermsission(Manifest.permission.QUERY_ALL_PACKAGES))return null;

        var apps = this.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo inf:apps){
            if(inf.packageName.equals(GF2Package))return gf2CachedInf = inf;
        }
        return null;
    }


//    public File getOriginalLangFile(){//
//        return new File(Environment.getDataDirectory(), "com.Sunborn.SnqxExilium.Glo/files/LocalCache/Data/Table/LangPackageTableEnusData.bytes");
//    }

    public boolean isGF2Installed(){
        return getGF2Inf() != null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
    }
}