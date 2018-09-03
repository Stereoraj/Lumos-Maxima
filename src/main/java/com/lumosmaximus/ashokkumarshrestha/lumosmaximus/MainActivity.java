package com.lumosmaximus.ashokkumarshrestha.lumosmaximus;

import android.Manifest;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener, ShakeDetector.Listener {
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2, fab3, fab4;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    final private int REQUEST_CODE_CAMERA_PERMISSIONS = 124;

    ImageButton btnSwitch;
    int wandOn[], wandOff[];
    Animation animSequential;

    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    MediaPlayer mp;
    private PrefManager prefManager;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        changeStatusBarColor();
        setContentView(R.layout.activity_main);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        wandOn = new int[]{R.drawable.wand1on, R.drawable.wand2on, R.drawable.wand3on, R.drawable.wand4on, R.drawable.wand5on};
        wandOff = new int[]{R.drawable.wand1off, R.drawable.wand2off, R.drawable.wand3off, R.drawable.wand4off, R.drawable.wand5off};

        prefManager = new PrefManager(this);
        pos = prefManager.getWandPos();
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
        btnSwitch.setImageResource(wandOff[pos]);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        fab4 = (FloatingActionButton) findViewById(R.id.fab4);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        // load the animation
        animSequential = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sequential);

        // set animation listener
        animSequential.setAnimationListener(this);

        //check flashlight
        checkFlashlight();

        // get the camera
        getCamera();

        // displaying button image
        //isFlashOn = false;
        //toggleButtonImage();

        //turnLight();

        // Switch button click event to toggle flash on/off
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnLight();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"Wand selected",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, WandsActivity.class));
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"Settings selected",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));

            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"About selected",Toast.LENGTH_LONG).show();
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(v.getContext(),"Share selected",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, MainActivity.this.getResources().getString(R.string.app_share_message) + "\n https://play.google.com/store/apps/details?id=com.lumosmaximus.ashokkumarshrestha.lumosmaximus");
                startActivity(Intent.createChooser(intent, MainActivity.this.getResources().getString(R.string.app_share_title)));
            }
        });

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

    public void animateFAB() {

        if (isFabOpen) {

            fab.startAnimation(rotate_backward);

            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab4.startAnimation(fab_close);

            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            fab4.setClickable(false);

            isFabOpen = false;
        } else {

            fab.startAnimation(rotate_forward);

            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab4.startAnimation(fab_open);

            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            fab4.setClickable(true);

            isFabOpen = true;
        }
    }

    //turn light on/off on button image clicked
    private void turnLight() {
        vibrateDevice();
        if (isFlashOn) {
            // turn off flash
            turnOffFlash();
        } else {
            // turn on flash
            if (animateWand()) {
                btnSwitch.startAnimation(animSequential);
            } else {
                turnOnFlash();
            }
        }
    }

    private boolean animateWand() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isAnimationOn = prefs.getBoolean("pref_notifications_animation", true);

        if (isAnimationOn) {
            return true;
        }
        return false;

    }

    //check flashlight
    private void checkFlashlight() {
        // First check if device is supporting flashlight or not
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Error");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
        }
    }

    // Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                //check for marshmallow permissions
                //checkAndroidVersion();
                Log.e("Camera Error. Error: ", e.getMessage());
            }
        }
    }


    // Turning On flash
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                checkAndroidVersion();
                //getCamera();
                if (camera == null || params == null) {
                    return;
                }
                //turnOnFlash();
                //return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;

            // changing button/switch image
            toggleButtonImage();
        }

    }


    // Turning Off flash
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;

            // changing button/switch image
            toggleButtonImage();
        }
    }


    // Playing sound
    // will play button toggle sound on flash on / off
    private void playSound() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isPlaySoundOn = prefs.getBoolean("pref_notifications_sound", true);
        if (isPlaySoundOn) {
            if (isFlashOn) {
                mp = MediaPlayer.create(MainActivity.this, R.raw.nox);
            } else {
                mp = MediaPlayer.create(MainActivity.this, R.raw.lumos);
            }
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.release();
                }
            });
            mp.start();
        }
    }

    /*
     * Toggle switch button images
     * changing image states to on / off
     * */
    private void toggleButtonImage() {
        if (isFlashOn) {
            btnSwitch.setImageResource(wandOn[pos]);
        } else {
            btnSwitch.setImageResource(wandOff[pos]);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pos = prefManager.getWandPos();
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
        btnSwitch.setImageResource(wandOff[pos]);
        //close FAB on coming to homescreen
        if (isFabOpen) {
            animateFAB();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params
        getCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void hearShake() {
        //Toast.makeText(this, "Don't shake me, bro!", Toast.LENGTH_SHORT).show();
        if (shakeDetect()) {
            turnLight();
        }
    }

    //check shared prefference for shake detection
    private boolean shakeDetect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isShakeOn = prefs.getBoolean("pref_notifications_shake", true);
        if (isShakeOn) {
            return true;
        }
        return false;

    }

    //check shared prefference and vibrate the device
    private void vibrateDevice() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isVibrateOn = prefs.getBoolean("pref_notifications_vibrate", true);
        if (isVibrateOn) {
            //Toast.makeText(this, "vibrating yeah!", Toast.LENGTH_SHORT).show();
            Vibrator vibrator = (Vibrator) this.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        turnOnFlash();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    private void checkPermissions() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {

                /*showMessageOKCancel("You need to allow access to Camera Flash Light! ",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[] {Manifest.permission.CAMERA},
                                        REQUEST_CODE_CAMERA_PERMISSIONS);
                            }
                        });
                return;*/
            }

            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Camera Permission");
            alertDialogBuilder.setMessage("You need to allow access to Camera to use Flash Light! ");
            alertDialogBuilder.setPositiveButton("GOT IT",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    //do nothing
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CODE_CAMERA_PERMISSIONS);
                }
            });
            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    alertPermission();
                    dialog.cancel();
                }
            });
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


            return;
        }
        //return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_CAMERA_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //insertDummyContact();
                    Toast.makeText(MainActivity.this, "Camera Access Granted!", Toast.LENGTH_SHORT).show();
                    getCamera();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Camera Access Denied!", Toast.LENGTH_SHORT).show();
                    alertPermission();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //check for marshmallow permissions
    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    public void alertPermission() {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Camera Permission");
        alertDialogBuilder.setMessage("Camera access denied for Lumos! Please enable camera access to fully enjoy the app. You can change the app permission by Settings -> Lumos -> Permissions");
        alertDialogBuilder.setPositiveButton("GOT IT",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                //do nothing
            }
        });
        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}