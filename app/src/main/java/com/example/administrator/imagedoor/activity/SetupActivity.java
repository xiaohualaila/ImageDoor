package com.example.administrator.imagedoor.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.greendaodemo.greendao.GreenDaoManager;
import com.example.administrator.greendaodemo.greendao.gen.WhiteListDao;
import com.example.administrator.imagedoor.R;
import com.example.administrator.imagedoor.Util.FileUtil;
import com.example.administrator.imagedoor.Util.GetDataUtil;
import com.example.administrator.imagedoor.Util.LoadingDialog;
import com.example.administrator.imagedoor.Util.SharedPreferencesUtil;
import com.example.administrator.imagedoor.Util.UtilToast;
import com.example.administrator.imagedoor.bean.WhiteList;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/30.
 */

public class SetupActivity  extends AppCompatActivity  implements View.OnClickListener{

    private TextView add_excel;
    private TextView tv_finish;
    private TextView look_photo_num;
    private String path;
    private ArrayList<WhiteList> mCountryModels;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        add_excel = findViewById(R.id.add_excel);
        tv_finish = findViewById(R.id.tv_finish);
        look_photo_num = findViewById(R.id.look_photo_num);
        add_excel.setOnClickListener(this);
        tv_finish.setOnClickListener(this);
        isHaveImage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_excel:
                isHaveImage();
                break;
            case R.id.tv_finish:
                toImageShowActivity();
                break;
        }
    }

    private void isHaveImage() {
        File f = new File(FileUtil.getPath(),"photo");
        if (!f.exists()) {
            f.mkdir();
            look_photo_num.setText("没有图片！");
        }else {
           String[] files=f.list();
            int num = files.length;
            look_photo_num.setText("图片数量" + num + "张");
            getExcel();
        }
    }

    private void toImageShowActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ImageShowActivity.class);
        startActivity(intent);
        finish();
    }

    //判断Excel文件是否存在
    private void getExcel() {
        path = FileUtil.getPath()+ File.separator +"door.xls";
        File file = new File(path);
        if(!file.exists()){
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_msg)//dialog_msg
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();
            return;
        }else {//存在
            add_excel.setText("正在导入Excel表！");
            add_excel.setEnabled(false);
            new ExcelDataLoader().execute(path);
        }
    }

    //在异步方法中 调用
    private class ExcelDataLoader extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {

            return GetDataUtil.getXlsData(params[0], 0);
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {

            if(isSuccess){
                WhiteListDao whiteListDao = GreenDaoManager.getInstance().getSession().getWhiteListDao();
                //存在数据
                add_excel.setText("加载成功！共"+whiteListDao.loadAll().size() + "条记录");
                toImageShowActivity();
            }else {
                //加载失败
                add_excel.setText(R.string.load_fail);
            }
            add_excel.setEnabled(true);
        }
    }
}
