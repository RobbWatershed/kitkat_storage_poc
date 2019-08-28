package com.example.platypus.kitkat_storage_poc;

import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.platypus.kitkat_storage_poc.util.ConstsImport;
import com.example.platypus.kitkat_storage_poc.util.FileHelper;
import com.example.platypus.kitkat_storage_poc.util.PermissionUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.security.InvalidParameterException;

import timber.log.Timber;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {

    TextView txt1;
    View txt2;
    int mode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::onFabClick);

        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);

        RadioGroup theRadio = findViewById(R.id.radioGroup);
        theRadio.setOnCheckedChangeListener(this::onChangeRoot);
        theRadio.check(R.id.btn1);

        if (!PermissionUtil.requestExternalStoragePermission(this, ConstsImport.RQST_STORAGE_PERMISSION))
            Timber.e("Storage permission denied");
    }

    private void onChangeRoot(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case (R.id.btn1):
                txt1.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
                txt2.setVisibility(View.GONE);
                mode = 1;
                break;
            case (R.id.btn2):
                txt1.setText(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath());
                txt2.setVisibility(View.VISIBLE);
                mode = 2;
                break;
            default:
                throw new InvalidParameterException("Not implemented");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFabClick(View view) {

        EditText txtEdit = findViewById(R.id.subfolder);

        File targetFolder;
        if (1 == mode) targetFolder = Environment.getExternalStorageDirectory();
        else targetFolder = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);

        targetFolder = new File(targetFolder, txtEdit.getText().toString());
        Timber.i("Target : %s", targetFolder.getAbsolutePath());

        String message;
        if (FileHelper.createDirectory(targetFolder))
        {
            Timber.i("Target folder created");
            if (FileHelper.isWritable(targetFolder))
            {
                Timber.i("Target folder writable");
                message = "Success";
            } else message = "Target folder created but not writable";
        } else message = "Target folder not created";

        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
