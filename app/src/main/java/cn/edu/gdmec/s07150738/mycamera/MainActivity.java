package cn.edu.gdmec.s07150738.mycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback{

    private SurfaceView mSurfaceView;//相机视频浏览
    private ImageView mImageView;//相片
    private SurfaceHolder mSurfaceHolder;
    private ImageView shutter;//快照按钮
    private Camera mCamera = null;//相机
    private boolean mPreviewRunning;//运行相机浏览
    private static final int MENU_START = 1;
    private static final int MENU_SENSOR=2;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏标题，设置全屏,必须要在setContentView方法之前执行
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置布件
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView)findViewById(R.id.camera);
        mImageView = (ImageView)findViewById(R.id.image);
        shutter = (ImageView)findViewById(R.id.shutter);
        //设置快照按钮事件
        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder = mSurfaceView.getHolder();
        //设置SurfaceHolder回调事件
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setCameraParams();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


        try {
            //判断是否运行相机，运行就停止掉
            if (mPreviewRunning){
                mCamera.stopPreview();
            } //启动相机
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mPreviewRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            //停止相机预览
            mCamera.stopPreview();
            mPreviewRunning = false;
            //回收相机
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onClick(View v) {
        //判断是否可以进行拍照
        if (mPreviewRunning){
            shutter.setEnabled(false);
            //设置自动对焦
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    mCamera.takePicture(mShutterCallback,null,mPictureCallback);
                }
            });
        }
    }
    //相机图片拍照回调函数
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera1) {
            //判断线片数据是否为空
            if (data!=null){
                saveAndShow(data);
            }
        }
    };
    //快照回调函数
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            System.out.println("快照回调函数.....");
        }
    };
    //设置Camera参数
    public void setCameraParams(){
        if (mCamera != null){
            return ;
        }
        //创建相机，打开相机
        mCamera = Camera.open();
        //设置相机参数
        Camera.Parameters params = mCamera.getParameters();
        //拍照自动对焦
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //设置预览帧速率
        params.setPreviewFrameRate(3);
        //设置预览格式
        params.setPreviewFormat(PixelFormat.YCbCr_422_SP);
        //设置图pain质量百分比
        params.set("jpeg_quality",85);
        List<Camera.Size> list =params.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        int w = size.width;
        int h = size.height;
        //设置图片大小
        params.setPictureSize(w,h);
        //设置自动闪光灯
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,MENU_START,0,"重拍");
        menu.add(0,MENU_SENSOR,0,"打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_START){
            //重启相机拍照
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if (item.getItemId() == MENU_SENSOR){
            Intent intent = new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    //保存和显示图片
    public void saveAndShow(byte[] data){
        try {
            //图片id
        String imageId = System.currentTimeMillis()+"";
        //图片保存路径
        String pathName = android.os.Environment.getExternalStorageDirectory().getPath()+"/mycamera";
        //创建文件
        File file = new File(pathName);
        if (!file.exists()){
            file.mkdirs();
        }
        //创建文件
        pathName+="/"+imageId+".jpeg";
        file = new File(pathName);
        if (!file.exists()){

                file.createNewFile();
        }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album = new AlbumActivity();
            //读取相片Bitmap
            bitmap = album.loadImage(pathName);
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);
            //停止相机浏览
            if (mPreviewRunning){
                mCamera.stopPreview();
                mPreviewRunning = false;
            }
            shutter.setEnabled(true);

            } catch (IOException e) {
                e.printStackTrace();
            }


    }
}
