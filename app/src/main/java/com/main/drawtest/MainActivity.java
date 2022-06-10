package com.main.drawtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static int PHOTO_REQUEST_GALLERY = 10011;
    private CustomImageView imageView;
    private Button btnMark,btnChangeMode;
    private boolean isMarking = false;
    private TextView tvInfo;
    private int mode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        tvInfo = findViewById(R.id.tv_info);
        imageView = findViewById(R.id.iv_photo);
        btnMark = findViewById(R.id.btn_mark);
        btnChangeMode = findViewById(R.id.btn_change_mode);

        findViewById(R.id.btn_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matisse.from(MainActivity.this)
                        .choose(MimeType.ofImage())
                        .capture(true)
                        .captureStrategy(new CaptureStrategy(true, "com.main.drawtest.fileProvider"))
                        .countable(true)
                        .maxSelectable(1)
//                        .restrictOrientation(ActivityInfo.S)
                        .theme(R.style.UoperationMatisse)
                        .imageEngine(new GlideEngine())
                        .forResult(PHOTO_REQUEST_GALLERY);
            }
        });

        btnChangeMode.setText("模式"+mode);
        btnChangeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == 1) {
                    mode = 2;
                } else {
                    mode = 1;
                }
                btnChangeMode.setText("模式"+mode);
                imageView.setMode(mode);
            }
        });

        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMarking) {
                    btnMark.setText("开始标注");
                    imageView.setDraw(false);
                    isMarking = false;
                } else {
                    btnMark.setText("结束标注");
                    imageView.setDraw(true);
                    isMarking = true;
                }
            }
        });

        imageView.setDrawInfoListener(new CustomImageView.DrawInfoListener() {
            @Override
            public void onDraw(String info) {
                tvInfo.setText(info);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        getPermission();
    }

    private void getPermission() {
        XXPermissions.with(this)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "已获取全部权限", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "未获取权限：", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PHOTO_REQUEST_GALLERY) {
            List<String> photoList = Matisse.obtainPathResult(data);
            if (photoList != null && !photoList.isEmpty()) {
//                Glide.with(this)
//                        .load(photoList.get(0))
//                        .into(imageView);
                imageView.setFileImage(photoList.get(0));
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float eventX = motionEvent.getX();
        float eventY = motionEvent.getY();
        float[] eventXY = new float[]{eventX, eventY};

        Matrix invertMatrix = new Matrix();
        ((ImageView) view).getImageMatrix().invert(invertMatrix);

        invertMatrix.mapPoints(eventXY);
        int x = Integer.valueOf((int) eventXY[0]);
        int y = Integer.valueOf((int) eventXY[1]);

        Drawable imgDrawable = ((ImageView) view).getDrawable();
        Bitmap bitmap = ((BitmapDrawable) imgDrawable).getBitmap();

        String info = "触摸[" + eventX + "," + eventY + "] 图像[" + x + "," + y + "] " + " 大小[" + bitmap.getWidth() + "," + bitmap.getHeight() + "]";

        //Limit x, y range within bitmap
        if (x < 0) {
            x = 0;
        } else if (x > bitmap.getWidth() - 1) {
            x = bitmap.getWidth() - 1;
        }

        if (y < 0) {
            y = 0;
        } else if (y > bitmap.getHeight() - 1) {
            y = bitmap.getHeight() - 1;
        }
        info += " 图内[" + x + "," + y + "]";
        tvInfo.setText(info);
        return true;
    }
}