package com.davidproject.practice2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ThirdActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int STORED = 1;
    private static final int FAIL = 0;
    private ImageView myImageView;
    private EditText myEditText;
    private Bitmap imageToBeSent = null;
    private TextView myTextView;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message m){
            switch(m.what){
                case STORED:
                    myTextView.setText("Server has stored the image, thank you");
                    myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    break;
                case FAIL:
                    myTextView.setText("Image storing failed, sorry");
                    myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        myImageView = (ImageView)findViewById(R.id.preview2);
        myEditText = (EditText)findViewById(R.id.editText3);
        myTextView = (TextView)findViewById(R.id.message2);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageToBeSent = (Bitmap)extras.get("data");
            myImageView.setImageBitmap(imageToBeSent);
            myTextView.setText("Click send when you are ready");
            myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        else if(requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedPhoto = data.getData();
            try{
                InputStream imageStream = getContentResolver().openInputStream(selectedPhoto);
                imageToBeSent = BitmapFactory.decodeStream(imageStream);
                myImageView.setImageBitmap(imageToBeSent);
                myTextView.setText("Click send when you are ready");
                myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            } catch(Exception e){
                e.printStackTrace();
            }

        }
    }

    public void clear(View v){
        myEditText.setText("");
        myEditText.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void send(View v){
        if (imageToBeSent != null && !myEditText.getText().toString().equals("")) {
            myTextView.setText("Waiting for server response...");
            myTextView.setGravity(Gravity.CENTER_HORIZONTAL);
            new Thread(){
                @Override
                public void run(){
                    synchronized (this){
                        try {
                            Socket socket = new Socket("10.0.2.2", 4567);
                            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                            InputStream is = socket.getInputStream();
                            os.write("expert".getBytes());
                            os.flush();
                            Thread.currentThread().sleep(500);
                            os.write(myEditText.getText().toString().getBytes());
                            os.flush();
                            Thread.currentThread().sleep(500);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            imageToBeSent.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();
                            os.write(imageData);
                            os.flush();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            Thread.currentThread().sleep(2000);
                            String result = reader.readLine();
                            System.out.println(result);
                            if (result.equals("stored")) {
                                handler.sendEmptyMessage(1);
                            } else {
                                handler.sendEmptyMessage(0);
                            }
                            os.close();
                            baos.close();
                            reader.close();
                            is.close();
                            socket.close();
                        } catch(IOException e){
                            e.printStackTrace();
                            handler.sendEmptyMessage(0);
                        }
                        catch (InterruptedException e){
                            e.printStackTrace();
                            handler.sendEmptyMessage(0);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            handler.sendEmptyMessage(0);
                        }
                    }
                }
            }.start();
        }
        else{
            myTextView.setText("You need to have a picture and a name");
        }
    }
}
