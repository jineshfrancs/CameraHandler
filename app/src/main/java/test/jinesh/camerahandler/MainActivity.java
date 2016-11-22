package test.jinesh.camerahandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements CameraHandler.OnImageResultListner {
    Button button;
    CameraHandler handler;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler=new CameraHandler(this);
        button= (Button) findViewById(R.id.button);
        imageView= (ImageView) findViewById(R.id.imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handler.showDialogToCaptureImage();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handler.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageResult(String imageName, String imagePath, Bitmap imageFile, int sourceType) {
        if(sourceType==CameraHandler.CAMERA_TYPE){
            Log.e("imageName",imageName);
            Log.e("imagePath",imagePath);
            Log.e("type","CameraType");
            imageView.setImageBitmap(imageFile);
        }else if(sourceType==CameraHandler.GALLERY_TYPE){
            Log.e("imageName",imageName);
            Log.e("imagePath",imagePath);
            Log.e("type","GalleryType");
            imageView.setImageBitmap(imageFile);
        }
    }
}
