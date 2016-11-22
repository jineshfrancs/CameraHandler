package test.jinesh.camerahandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by Jinesh on 25-10-2016.
 */

public class CameraHandler extends Activity {
    private OnImageResultListner onImageResultListner;
    private Context context;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static int SELECT_FILE = 1;
    private Uri fileUri;
    private static final String IMAGE_DIRECTORY_NAME = "Test";
    private String filePath, fileName;
    private Activity activity;
    public static final int CAMERA_TYPE = -1;
    public static final int GALLERY_TYPE = -2;
    private FileDetails fileDetails;
    public CameraHandler() {
    }

    public CameraHandler(OnImageResultListner onImageResultListner) {
        this.onImageResultListner = onImageResultListner;
        if (onImageResultListner instanceof Activity) {
            context = (Context) onImageResultListner;
            activity = (Activity) onImageResultListner;
        }
    }

    public void showDialogToCaptureImage() {
        final CharSequence[] items = {"Choose from Gallery", "Take Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    // start the image capture Intent
                    if(activity!=null)
                    activity.startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

                } else if (items[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                // downsizing image as it throws OutOfMemory Exception for larger
                // images
                options.inSampleSize = 8;
                Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                        options);
                String[] fileTemp = fileUri.getPath().split("/");
                String filename = fileTemp[fileTemp.length - 1];
                fileName = filename;
                filePath = fileUri.getPath();
                fileDetails = compressImage(fileUri.getPath(),CAMERA_TYPE);
                if (onImageResultListner != null)
                    onImageResultListner.onImageResult(filename, filePath, fileDetails.getBitmap(), CAMERA_TYPE);

            } else if (requestCode == SELECT_FILE) {
                fileUri=getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                onSelectFromGalleryResult(data);
                if (onImageResultListner != null)
                    onImageResultListner.onImageResult(fileName, filePath, fileDetails.getBitmap(), GALLERY_TYPE);
            }
        }

    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.managedQuery(selectedImageUri, projection, null, null,
                null);
        if (cursor == null) {

        } else {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            Bitmap bm;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(selectedImagePath, options);
            final int REQUIRED_SIZE = 200;
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                    && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            fileDetails = compressImage(selectedImagePath,GALLERY_TYPE);
            fetchImageName(fileDetails.getFilePath());
        }


    }

    private void fetchImageName(String selectedImagePath) {
        String mFileNameGallery = "";
        StringTokenizer st = new StringTokenizer(selectedImagePath, "/");
        while (st.hasMoreTokens()) {
            mFileNameGallery = st.nextToken().toString();
        }
        filePath = selectedImagePath;
        fileName = mFileNameGallery;
        String selectedImagePath1 = selectedImagePath;
    }


    public FileDetails compressImage(String imageUri,int flag) {
        FileDetails fileDetails = new FileDetails();
        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        // by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        // you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        // max Height and width values of the compressed image is taken as 816x612
        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;
        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }
        //      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;
        //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];
        try {
            //          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
            Bitmap bitmapFromGallery = bmp;
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        //      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
            //Log.d("reduced_height", scaledBitmap.getHeight() + " ");
            //Log.d("reduced_width", scaledBitmap.getWidth() + " ");
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename="";
        filename=fileUri.getPath();
        try {
            out = new FileOutputStream(filename);
            //          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("exception", e.getMessage() + "");
        }
        fileDetails.setFilePath(filename);
        fileDetails.setBitmap(scaledBitmap);
        return fileDetails;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;

        }

        return inSampleSize;
    }

    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
               /* Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");*/
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("picUri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("picUri");
    }

    private class FileDetails {
        String filePath;
        Bitmap bitmap;

        String getFilePath() {
            return filePath;
        }

         void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        Bitmap getBitmap() {
            return bitmap;
        }

        void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public interface OnImageResultListner {
        void onImageResult(String imageName, String imagePath, Bitmap imageFile, int sourceType);
    }
}
