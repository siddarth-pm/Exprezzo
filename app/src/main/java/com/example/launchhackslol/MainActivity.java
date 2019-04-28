package com.example.launchhackslol;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.provider.MediaStore;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.launchhackslol.Helper.GraphicOverlay;
import com.example.launchhackslol.Helper.RectOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    ArrayList<Integer> happyDone = new ArrayList<>();
    ArrayList<Integer> sadDone = new ArrayList<>();

    String[] happySongs = new String[]{"Happy by Pharrell Williams",
            "Can't Stop the Feeling by Justin Timberlake",
            "High Hopes by Panic! at the Disco",
            "Sucker by Jonas Brothers",
            "Best Day of My Life by American Authors"};
    String[] sadSongs = new String[]{"Changes by XXXtentacion",
            "Say You Won't Let Go by James Arthur",
            "Too Good at Goodbyes by Sam Smith",
            "November Rain by Kris Wu",
            "Mercy by Shawn Mendes"};

    CameraView cameraView;
    GraphicOverlay graphicOverlay;
    Button btnDetect;

    FirebaseVisionFace face;
    Boolean facePresent = false;
    int count = 0;


    AlertDialog waitingDialog;


    protected void onResume(){
        super.onResume();
        cameraView.start();

    }

    protected void onPause(){
        super.onPause();
        cameraView.stop();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cameraView = (CameraView)findViewById(R.id.cameraView);
        graphicOverlay = (GraphicOverlay)findViewById(R.id.graphic_overlay);
        btnDetect = (Button)findViewById(R.id.btn_detect);

        waitingDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();

            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                cameraView.stop();

                runFaceDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void runFaceDetector(Bitmap bitmap) {

        if (bitmap != null) {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .build();

            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(highAccuracyOpts);

            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                @Override
                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                    processFaceResult(firebaseVisionFaces);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            Log.i("Error message", "bitmap was null");

        }

    }


    private void processFaceResult(List<FirebaseVisionFace> firebaseVisionFaces) {
        waitingDialog.dismiss();
        for (FirebaseVisionFace face : firebaseVisionFaces) {
            if (firebaseVisionFaces != null) {
                Rect bounds = face.getBoundingBox();

                RectOverlay rect = new RectOverlay(graphicOverlay, bounds);
                graphicOverlay.add(rect);
                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    float smileProb = face.getSmilingProbability();
                    if (smileProb >= 0.35) {
                        Toast.makeText(this, "You seem to be in a GOOD MOOD!!! " + getNewHappySong(), Toast.LENGTH_LONG).show();

                    } else if (smileProb < 0.35 && smileProb > 0.05) {
                        Toast.makeText(this, "Hmmm not too happy, huh... " + getNewSadSong(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Whoaaa, why u sad bro :( " + getNewSadSong(), Toast.LENGTH_LONG).show();
                    }
                }
                count++;
            } else {
                Toast.makeText(this, "No faces available", Toast.LENGTH_SHORT).show();
            }


        }
    }

    private String getNewHappySong(){
        Random rand = new Random();
        int r = rand.nextInt(4);
        return happySongs[r];
    }

    private String getNewSadSong(){
        Random rand = new Random();
        int r = rand.nextInt(4);
        return sadSongs[r];
    }

}
