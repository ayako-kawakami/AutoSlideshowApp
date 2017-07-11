package jp.techacademy.ayako.kawakami.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.widget.Toast;

import static jp.techacademy.ayako.kawakami.autoslideshowapp.R.styleable.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Cursor cursor;
    ImageView imageView;
    Timer mTimer;
    Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                getContentsInfo();
            }else{
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        }else{
            getContentsInfo();
        }

        Button button_next = (Button) findViewById(R.id.next);
        button_next.setOnClickListener(this);

        Button button_back = (Button) findViewById(R.id.back);
        button_back.setOnClickListener(this);

        Button button_start = (Button) findViewById(R.id.start);
        button_start.setOnClickListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getContentsInfo();
                }else{

                    final Toast toast = Toast.makeText(this, "写真へのアクセス権が無いためアプリを終了します", Toast.LENGTH_SHORT);
                    new FrameLayout(this) {
                        {
                            addView(toast.getView());
                            toast.setView(this);
                        }
                        @Override
                        public void onDetachedFromWindow() {
                            super.onDetachedFromWindow();
                            finish();
                        }
                    };
                    toast.show();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);

            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            imageView.setImageURI(imageUri);
        }
    }

    @Override
    public void onClick(View v){
        if (v.getId() == R.id.next) {
            if(cursor.moveToNext()){
            }else{
                cursor.moveToFirst();
            }
            display();
        } else if (v.getId() == R.id.back) {
            if(cursor.moveToPrevious()){
            }else{
                cursor.moveToLast();
            }
            display();
        }else if (v.getId() == R.id.start){

            TextView stop =(TextView) findViewById(R.id.start);
            String stop_str = stop.getText().toString();

            Button button_next = (Button) findViewById(R.id.next);
            Button button_back = (Button) findViewById(R.id.back);

            if(stop_str.equals("停止")){
                mTimer.cancel();
                stop.setText("再生");
                button_next.setEnabled(true);
                button_back.setEnabled(true);

            }else {
                slideShow();
                stop.setText("停止");

                button_next.setEnabled(false);
                button_back.setEnabled(false);
            }

        }
    }

    public void display(){
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        imageView.setImageURI(imageUri);
    }

    public void slideShow(){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask(){
            @Override
            public void run(){
                mHandler.post(new Runnable(){
                    @Override
                    public void run(){
                        if(cursor.moveToNext()){
                        }else{
                            cursor.moveToFirst();
                        }
                        display();
                    }
                });
            }
        },2000, 2000);
    }

}
