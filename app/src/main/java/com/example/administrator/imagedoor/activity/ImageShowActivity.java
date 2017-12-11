package com.example.administrator.imagedoor.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.cmm.rkadcreader.adcNative;
import com.cmm.rkgpiocontrol.rkGpioControlNative;
import com.example.administrator.imagedoor.R;
import com.example.administrator.imagedoor.Util.FileUtil;
import com.example.administrator.imagedoor.Util.GetDataUtil;
import com.example.administrator.imagedoor.Util.UtilToast;
import com.example.administrator.imagedoor.bean.WhiteList;
import com.example.administrator.imagedoor.service.MyService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/11/30.
 */

public class ImageShowActivity extends AppCompatActivity {
    private String str;
    private boolean isOpenDoor = false;
    private String filePath;
    private Handler handler = new Handler();
    private ImageView face_img;
    private TextView piao_no;
    private TextView name_tv;
    private TextView idcard_no;
    private TextView company;
    private MyService myService;
    private MyService.MsgBinder myBinder;
    private boolean isShowError = false;
    private String imageName;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MsgBinder) service;
            myBinder.initReadCard();
            myService = myBinder.getService();
            myService.setOnProgressListener(new MyService.OnDataListener() {
                @Override
                public void onMsg(String code) {
                    str = code;
                    //重置
                    face_img.setImageResource(R.drawable.img_bg);
                    piao_no.setText("");
                    name_tv.setText("");
                    idcard_no.setText("");
                    company.setText("");
                    isShowError = false;
                    findCount();
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        face_img = findViewById(R.id.face_img);
        piao_no = findViewById(R.id.piao_no);
        name_tv = findViewById(R.id.name);
        idcard_no = findViewById(R.id.idcard_no);
        company = findViewById(R.id.company);
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, connection, BIND_AUTO_CREATE);
        rkGpioControlNative.init();
    }

    private void findCount() {
        String sStr = str.toUpperCase().trim();
       WhiteList whiteList =  GetDataUtil.getDataBooean(sStr);
       if(whiteList != null){
           //这次只需要 姓名 身份证 公司 照片
           piao_no.setText(sStr);
           imageName  = whiteList.getNum();
           name_tv.setText(whiteList.getName());
           idcard_no.setText(whiteList.getIdCardNo());
           company.setText(whiteList.getCompany());
           isShowError = true;
           //开门
           if(!isOpenDoor){
               isOpenDoor = true;
               rkGpioControlNative.ControlGpio(1, 0);//开门
               handler.postDelayed(runnable,500);
           }
           findImage();
       }else {
           piao_no.setText("没有查询到相应的票号！");
       }
    }

    private void findImage() {
        // TODO: 2017/11/30 拼接图片路径
        filePath = FileUtil.getPath() + File.separator  + "photo" + File.separator + imageName + ".jpg";
        File file = new File(filePath);
        if(file.exists()){
                Glide.with(this).load(filePath).error(R.drawable.img_bg).into(face_img);
        }else {
            face_img.setImageResource(R.drawable.img_bg);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(isOpenDoor){
                rkGpioControlNative.ControlGpio(1, 1);//关门
                isOpenDoor = false;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        adcNative.close(0);
        adcNative.close(2);
        rkGpioControlNative.close();
    }


}
