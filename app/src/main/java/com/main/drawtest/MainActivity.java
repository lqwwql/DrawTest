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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private final static int PHOTO_REQUEST_GALLERY = 10011;
    private CustomImageView imageView;
    private Button btnMark, btnChangeMode;
    private boolean isMarking = false;
    private int mode = 1;
    private ListView lvPoints;
    private PointsAdapter pointsAdapter;
    private List<DimensionPoint> dimensionPointList;
    private List<DimensionPoint> testPointList;
    private RelativeLayout rlPointInfo;
    private EditText etTransX, etTransY, etScale;
    private float mScale = 2f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐藏导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.iv_photo);
        btnMark = findViewById(R.id.btn_mark);
        btnChangeMode = findViewById(R.id.btn_click1);
        lvPoints = findViewById(R.id.lv_points);
        etTransX = findViewById(R.id.et_trans_x);
        etTransY = findViewById(R.id.et_trans_y);
        etScale = findViewById(R.id.et_scale);

        dimensionPointList = new ArrayList<>();
        testPointList = new ArrayList<>();
        pointsAdapter = new PointsAdapter(dimensionPointList, this);
        lvPoints.setAdapter(pointsAdapter);

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

//        btnChangeMode.setText("模式" + mode);
        btnChangeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (mode == 1) {
//                    mode = 2;
//                } else {
//                    mode = 1;
//                }
//                btnChangeMode.setText("模式" + mode);
//                imageView.setMode(mode);
//                mScale += 0.5f;
                String scaleStr = etScale.getText().toString();
                if (scaleStr == null || scaleStr.length() <= 0) {
                    return;
                }
                float scale = Float.valueOf(scaleStr);
                imageView.scale(scale, 1);
//                imageView.drawNewRect(testPointList);
            }
        });

        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (isMarking) {
//                    btnMark.setText("开始标注");
//                    imageView.setDraw(false);
//                    isMarking = false;
//                } else {
//                    btnMark.setText("结束标注");
//                    imageView.setDraw(true);
//                    isMarking = true;
//                }
                String scaleStr = etScale.getText().toString();
                if (scaleStr == null || scaleStr.length() <= 0) {
                    return;
                }
                float scale = Float.valueOf(scaleStr);
                imageView.scale(scale, 2);
            }
        });

        findViewById(R.id.btn_click2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String xStr = etTransX.getText().toString();
                String yStr = etTransY.getText().toString();
                if (xStr == null || xStr.length() <= 0) {
                    return;
                }
                if (yStr == null || yStr.length() <= 0) {
                    return;
                }
                float transX = Float.valueOf(xStr);
                float transY = Float.valueOf(xStr);
                imageView.translate(transX, transY);
//                imageView.revokeRect();
            }
        });

        imageView.setDrawInfoListener(new CustomImageView.DrawInfoListener() {
            @Override
            public void onDraw(String info, List<DimensionPoint> data) {
                dimensionPointList.clear();
                if (data == null || data.isEmpty()) {
                    rlPointInfo.setVisibility(View.GONE);
                } else {
                    rlPointInfo.setVisibility(View.VISIBLE);
                    dimensionPointList.addAll(data);
                    testPointList.addAll(data);
                }
                pointsAdapter.notifyDataSetChanged();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "click view , hide bar", Toast.LENGTH_SHORT).show();
            }
        });
        CheckBox cbScale = findViewById(R.id.cb_scale);
        cbScale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                imageView.setCanScale(b);
            }
        });
        CheckBox cbTranslate = findViewById(R.id.cb_translate);
        cbTranslate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                imageView.setCanTranslate(b);
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
        return true;
    }
}