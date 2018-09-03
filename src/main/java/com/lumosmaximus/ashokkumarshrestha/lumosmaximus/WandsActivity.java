package com.lumosmaximus.ashokkumarshrestha.lumosmaximus;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class WandsActivity extends AppCompatActivity{

    private int index = -1;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_wands);

        // making notification bar transparent
        changeStatusBarColor();

        prefManager = new PrefManager(this);

        Button btnBack = (Button)findViewById(R.id.btn_back);
        Button btnSelect = (Button)findViewById(R.id.btn_select);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(),"Select a Wand!",Toast.LENGTH_LONG).show();
                index = -1;
                AlertDialogView();
            }
        });

    }

    private void AlertDialogView() {
        final CharSequence[] items = { "Albus Dumbledore", "Harry Potter", "Hermione Granger", "Lord Voldemort", "Ron Weasley" };

        //AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SELECT A WAND");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
                        index = item;
                    }
                });

        builder.setPositiveButton("SELECT", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(index != -1) {
                    Toast.makeText(getApplicationContext(), items[index] + "'s Wand selected!", Toast.LENGTH_SHORT).show();
                    prefManager.setWandPos(index);
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
