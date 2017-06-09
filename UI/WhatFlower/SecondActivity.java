package com.davidproject.practice2;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MenuItem;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SecondActivity extends AppCompatActivity {
    private static final int HAS_RESULT = 1;
    private static final int SERVER_NO_RESPONSE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;
    private ImageView myView;
    private TextView message;
    private Bitmap imageToBeSent = null;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message m){
            switch(m.what){
                case HAS_RESULT:
                    String rs = (String)m.obj;
                    message.setText(rs);
                    message.setGravity(Gravity.CENTER_HORIZONTAL);
                    break;
                case SERVER_NO_RESPONSE:
                    String fr = (String)m.obj;
                    message.setText(fr);
                    message.setGravity(Gravity.CENTER_HORIZONTAL);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        myView = (ImageView)findViewById(R.id.preview2);
        message = (TextView)findViewById(R.id.message1);
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void camera(View v){
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera, REQUEST_IMAGE_CAPTURE);
    }

    public void gallery(View v){
        Intent gallery = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, REQUEST_GALLERY);
    }


    public void discern(View v) {
        if (imageToBeSent != null) {
            message.setText("Waiting for server response...");
            message.setGravity(Gravity.CENTER_HORIZONTAL);
            new Thread() {
                @Override
                public void run() {
                    synchronized (this) {
                        try {
                            Socket socket = new Socket("10.0.2.2", 4567);
                            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                            InputStream is = socket.getInputStream();
                            os.write("normal".getBytes());
                            os.flush();
                            Thread.currentThread().sleep(1000);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageToBeSent.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();
                            os.write(imageData);
                            os.flush();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String result = "";
                            Thread.currentThread().sleep(3000);
                            try {
                                result = reader.readLine();
                                System.out.println(result);
                                if (result == null || result.equals("")) {
                                    Message fail = new Message();
                                    String failString = "Server no response, sorry";
                                    fail.what = SERVER_NO_RESPONSE;
                                    fail.obj = failString;
                                    handler.sendMessage(fail);
                                } else {
                                    Message suc = new Message();
                                    String resultString = "Result: " + result;
                                    suc.what = HAS_RESULT;
                                    suc.obj = resultString;
                                    handler.sendMessage(suc);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            os.close();
                            is.close();
                            reader.close();
                            socket.close();
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
        else{
            message.setText("You need to have a image to send");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageToBeSent = (Bitmap)extras.get("data");
            myView.setImageBitmap(imageToBeSent);
            message.setText("Click WhatFlower");
            message.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        else if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedPhoto = data.getData();
            try{
                InputStream imageStream = getContentResolver().openInputStream(selectedPhoto);
                imageToBeSent = BitmapFactory.decodeStream(imageStream);
                myView.setImageBitmap(imageToBeSent);
                message.setText("Click WhatFlower");
                message.setGravity(Gravity.CENTER_HORIZONTAL);
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    }
}
