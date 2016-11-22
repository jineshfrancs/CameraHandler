# CameraHandler
Utility class to capture image from gallery or camera with image compression without reducing quality.This lib will reduce image size from 0.99mb to 46kb with same image quality.

![Output sample](https://github.com/jineshfrancs/CameraHandler/blob/master/screens/screen_camera_handler.gif)

Create object for CameraHandler and call showDialogToCaptureImage() method to open Capture dialog
'''
 handler=new CameraHandler(this);
 handler.showDialogToCaptureImage();
'''
Override OnActivityResult and implement CameraHandler.OnImageResultListner interface

'''
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
    
'''
You can change the default directory name to capture the images to sd card by changing this variable name in CameraHandler.java
private static final String IMAGE_DIRECTORY_NAME = "Give_a_directory_name_to_store_captured_images";
