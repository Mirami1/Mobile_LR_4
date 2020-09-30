package com.example.mobile_lr_4;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    Button selectImageBtn;
    Button detectFacesBtn;
    ImageView imageView;

    private static int RESULT_LOAD_IMAGE = 1;

    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkReadFromStoragePerm();

        imageView = (ImageView) findViewById(R.id.imageView);
        selectImageBtn = (Button) findViewById(R.id.selectImageButton);

        // реакция кнопки выбора изображения
        selectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // вызов окна галерии
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        detectFacesBtn = (Button) findViewById(R.id.detectFacesButton);
        detectFacesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Bitmap inputImage = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                    // делаем кисть
                    Paint paint = new Paint();
                    paint.setStrokeWidth(10);
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);

                    //делаем основу финальной картинки
                    Bitmap finalImage = Bitmap.createBitmap(inputImage.getWidth(), inputImage.getHeight(), Bitmap.Config.RGB_565);
                    //холст
                    Canvas canvas = new Canvas(finalImage);
                    canvas.drawBitmap(inputImage, 0, 0, null);

                    //фейсдетектор без отслеживания по видео, с частями лиц
                    FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                            .setTrackingEnabled(false)
                            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                            .setMode(FaceDetector.ACCURATE_MODE)
                            .build();

                    // проверка на готовность
                    if (!faceDetector.isOperational()) {
                        Toast.makeText(MainActivity.this, "Detector is not ready yet!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //делаем кадр и детектим лица
                    Frame frame = new Frame.Builder().setBitmap(inputImage).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);

                    for (int i = 0; i < faces.size(); i++) {
                        Face face = faces.valueAt(i);

                        float x1 = face.getPosition().x;
                        float y1 = face.getPosition().y;
                        float x2 = x1 + face.getWidth();
                        float y2 = y1 + face.getHeight();

                        RectF rect = new RectF(x1, y1, x2, y2);
                        //рисуем прямоугольники
                        canvas.drawRect(rect, paint);
                        // почемаем части лица
                        for (Landmark landmark : face.getLandmarks()) {
                            int circleX = (int) landmark.getPosition().x;
                            int circleY = (int) landmark.getPosition().y;

                            canvas.drawCircle(circleX, circleY, 1, paint);


                        }
                    }
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), finalImage));
                    faceDetector.release();
                }
                catch (Exception e){
                    e.toString();
                }
            }
        });
    }

    // проверка разрешения на использование хранилища
    public boolean checkReadFromStoragePerm() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                return true;
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }

        } else
            return true;
    }
    // возрат выбранной картинки
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            //ставим пикчу по URI
            selectedImage = data.getData();
            imageView.setImageURI(selectedImage);
        }
    }
}