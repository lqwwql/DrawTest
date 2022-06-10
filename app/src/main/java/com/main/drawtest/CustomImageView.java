package com.main.drawtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.Nullable;

import com.bm.library.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义标注view
 */
public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {

    private DrawInfoListener drawInfoListener;
    private final int NORMAL_MODE = 101;
    private final int DRAG_MODE = 102;
    private final int SCALE_MODE = 103;
    private boolean isDraw = false;//是否标注模式
    private float clickX = 0, clickY = 0, startX = 0, startY = 0;
    private Paint paint;//画笔
    private int paintColor = Color.RED;//画笔颜色
    private float strokeWidth = 4.0f;//画笔粗细
    private Bitmap finalBitmap;//最终保存图片
    private Bitmap rectBitmap;//画矩形图片
    private Bitmap originalBitmap;//原图
    private List list = new ArrayList();//保存每次画的步骤
    private String filePath;
    private int screenWidth, screenHeight, imgWidth, imgHeight;
    private Matrix mMatrix;
    private String tvInfo;
    private GestureDetector mDetector;//手势移动
    private ScaleGestureDetector mScaleDetector;//手势缩放
    private OnClickListener mClickListener;
    private OnLongClickListener mLongClick;
    private float mScale = 1.0f;//缩放倍数
    private int mCurrentMode = NORMAL_MODE;//手势模式
    private boolean isScaling = false;
    private float horizontalOffSet = 0;//累计横向滑动距离
    private float verticalOffSet = 0;//累计纵向滑动距离
    private RectF baseRect = new RectF();//图像坐标系，用于基础位置偏移计算
    private RectF imgRect = new RectF();//图像坐标系，用于移动缩放边距判断
    private int mode = 1;
    private boolean isCanLeftScroll = true;
    private boolean isCanRightScroll = true;
    private boolean isCanUpScroll = true;
    private boolean isCanDownScroll = true;

    public CustomImageView(Context context) {
        super(context);
        init();
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //设置原始图片参数，初始化宽度填满屏幕
    public void setFileImage(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        this.filePath = filePath;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        Log.d("ufly", "screenWidth=" + screenWidth + " screenHeight=" + screenHeight);
        imgWidth = screenWidth;
        originalBitmap = BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.ARGB_8888, true);
        double proportion = (float) imgWidth / (float) Math.max(originalBitmap.getWidth(), originalBitmap.getHeight());
        Log.d("ufly", "originalBitmap getWidth=" + originalBitmap.getWidth() + " originalBitmap.getHeight=" + originalBitmap.getHeight() + " proportion=" + proportion);
        imgHeight = (int) (Math.min(originalBitmap.getWidth(), originalBitmap.getHeight()) * proportion);
        Log.d("ufly", "imgWidth=" + imgWidth + " imgHeight=" + imgHeight);
        finalBitmap = Bitmap.createScaledBitmap(originalBitmap, imgWidth, imgHeight, true);
        list.add(finalBitmap);
        setImageBitmap(finalBitmap);
        mMatrix = new Matrix();
        imgRect.set(0, 0, imgHeight, imgWidth);
        baseRect.set(0, 0, imgHeight, imgWidth);
    }

    private void init() {
        mDetector = new GestureDetector(getContext(), mGestureListener);
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //消除锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.save();
        if (finalBitmap != null) {
            //标注模式才绘制矩形
            if (isDraw) {
                canvas.drawBitmap(drawRect(), mMatrix, null);
            } else {
                if (mode == 1) {
                    drawBitmap(canvas);
                } else {
                    canvas.translate(horizontalOffSet, verticalOffSet);
                    canvas.drawBitmap(finalBitmap, getMatrix(), null);
                }
            }
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //标注模式不进行缩放移动
        if (!isDraw) {
//            mDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
        }

        clickX = event.getX();
        clickY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = clickX;
                startY = clickY;
                //标注模式才计算矩形位置
                if (isDraw) {
                    invalidate();
                    calculatePosition(event);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDraw) {
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (isDraw) {
                    finalBitmap = rectBitmap;
                    list.add(finalBitmap);
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    public void setDraw(boolean draw) {
        isDraw = draw;
    }

    /**
     * 画矩形
     */
    public Bitmap drawRect() {
        Bitmap bitmap = Bitmap.createBitmap(finalBitmap);
        Canvas canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(paintColor);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawRect(startX, startY, clickX, clickY, paint);
        rectBitmap = bitmap;
        return rectBitmap;
    }

    /**
     * 画移动、放缩后的图像
     */
    public void drawBitmap(Canvas canvas) {
        //手势拖拽
        if (mCurrentMode == DRAG_MODE || mCurrentMode == NORMAL_MODE) {
            Log.d("DRAG_MODE", "before imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
            //右移动
            if (horizontalOffSet >= 0) {
                if (Math.abs(imgRect.top) <= horizontalOffSet) {
                    Log.d("DRAG_MODE", "step1--------");
                    horizontalOffSet = Math.abs(imgRect.top);
                    isCanRightScroll = false;
                    isCanLeftScroll = true;
                }
            }
            //左移动
            else {
                if ((imgRect.bottom - screenWidth) <= Math.abs(horizontalOffSet)) {
                    Log.d("DRAG_MODE", "step3--------");
                    horizontalOffSet = -Math.abs(imgRect.bottom - screenWidth);
                    isCanLeftScroll = false;
                    isCanRightScroll = true;
                }
            }

            //下移动
            if (verticalOffSet >= 0) {
                if (Math.abs(imgRect.left) <= verticalOffSet) {
                    Log.d("DRAG_MODE", "step5--------");
                    verticalOffSet = Math.abs(imgRect.left);
                    isCanDownScroll = false;
                    isCanUpScroll = true;
                }
            }
            //上移动
            else {
                if ((imgRect.right - (float) screenHeight) <= Math.abs(verticalOffSet)) {
                    Log.d("DRAG_MODE", "step7--------");
                    verticalOffSet = -Math.abs(imgRect.right - (float) screenHeight);
                    isCanUpScroll = false;
                    isCanDownScroll = true;
                }
            }

            Log.d("DRAG_MODE", "horizontalOffSet=" + horizontalOffSet + " verticalOffSet=" + verticalOffSet + " isCanLeftScroll=" + isCanLeftScroll + " isCanRightScroll=" + isCanRightScroll + " isCanUpScroll=" + isCanUpScroll + " isCanDownScroll=" + isCanDownScroll);

            imgRect.top = baseRect.top + horizontalOffSet;
            imgRect.bottom = baseRect.bottom + horizontalOffSet;
            imgRect.left = baseRect.left + verticalOffSet;
            imgRect.right = baseRect.right + verticalOffSet;

            canvas.translate(horizontalOffSet, verticalOffSet);
            canvas.drawBitmap(finalBitmap, getMatrix(), null);
            Log.d("DRAG_MODE", "after imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
        }
        //手势缩放
        else if (mCurrentMode == SCALE_MODE) {
            Log.d("SCALE_MODE", "before imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
            Log.d("SCALE_MODE", "centerPoint x=" + (float) screenWidth / 2 + " y=" + (float) screenHeight / 2 + " mScale=" + mScale);
            Log.d("SCALE_MODE", "scrollWidth=" + ((Math.abs(imgRect.bottom) + Math.abs(imgRect.top)) * mScale) + " screenWidth=" + screenWidth);
            if ((Math.abs(imgRect.bottom) + Math.abs(imgRect.top)) * mScale < screenWidth) {
                Log.d("SCALE_MODE", "step1----------");
                mScale = 1.0f;
                imgRect.left = 0;
                imgRect.top = 0;
                imgRect.bottom = baseRect.bottom;
                imgRect.right = baseRect.right;
            } else {
                Log.d("SCALE_MODE", "step2----------");
                float leftOffSet = (((float) screenHeight / 2) - baseRect.left) * mScale - (((float) screenHeight / 2) - baseRect.left);
                float topOffSet = (((float) screenWidth / 2) - baseRect.top) * mScale - (((float) screenWidth / 2) - baseRect.top);
                float rightOffSet = (baseRect.right - ((float) screenHeight / 2)) * mScale - (baseRect.right - (float) screenHeight / 2);
                float bottomOffSet = (baseRect.bottom - ((float) screenWidth / 2)) * mScale - (baseRect.bottom - (float) screenWidth / 2);

                Log.d("SCALE_MODE", "leftOffSet=" + leftOffSet + " topOffSet=" + topOffSet + " rightOffSet=" + rightOffSet + " bottomOffSet=" + bottomOffSet);
                Log.d("SCALE_MODE", "scaleHeight=" + ((baseRect.right - baseRect.left) * mScale) + " calculateHeight=" + ((baseRect.right - baseRect.left) + leftOffSet + rightOffSet));
                Log.d("SCALE_MODE", "scaleWidth=" + ((baseRect.bottom - baseRect.top) * mScale) + " calculateWidth=" + ((baseRect.bottom - baseRect.top) + topOffSet + bottomOffSet));

                imgRect.left = baseRect.left - leftOffSet;
                imgRect.top = baseRect.top - topOffSet;
                imgRect.right = baseRect.right + rightOffSet;
                imgRect.bottom = baseRect.bottom + bottomOffSet;
            }
            Log.d("SCALE_MODE", "after imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
            canvas.scale(mScale, mScale, (float) screenWidth / 2, (float) screenHeight / 2);
            canvas.drawBitmap(finalBitmap, getMatrix(), null);
        }
    }

    /**
     * 计算矩形位于图片的坐标位置
     */
    private void calculatePosition(MotionEvent motionEvent) {
        float eventX = motionEvent.getX();
        float eventY = motionEvent.getY();
        float[] eventXY = new float[]{eventX, eventY};

        Matrix invertMatrix = new Matrix();
        getImageMatrix().invert(invertMatrix);
        invertMatrix.mapPoints(eventXY);

        int x = Integer.valueOf((int) eventXY[0]);
        int y = Integer.valueOf((int) eventXY[1]);
        Bitmap bitmap = finalBitmap;
        tvInfo = "触摸[" + eventX + "," + eventY + "] 图象[" + x + "," + y + "] " + " 大小[" + bitmap.getWidth() + "," + bitmap.getHeight() + "]";
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
        tvInfo += " 位图[" + x + "," + y + "]";
        Log.d("ufly", "tvInfo=" + tvInfo);
        if (drawInfoListener != null) {
            drawInfoListener.onDraw(tvInfo);
        }
    }

    public String getTvInfo() {
        return tvInfo;
    }

    private Runnable mClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mClickListener != null) {
                mClickListener.onClick(CustomImageView.this);
            }
        }
    };

    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public void onLongPress(MotionEvent e) {
            if (mLongClick != null) {
                mLongClick.onLongClick(CustomImageView.this);
            }
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mCurrentMode = DRAG_MODE;
            isScaling = false;
            removeCallbacks(mClickRunnable);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.d("ufly", "mGestureListener onFling e1=" + getActionString(e1) + " e2=" + getActionString(e2) + " velocityX=" + velocityX + " velocityY=" + velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d("ufly", "mGestureListener onScroll e1=" + getActionString(e1) + " e2=" + getActionString(e2) + " distanceX=" + distanceX + " distanceY=" + distanceY);
            if (!isScaling) {
                mCurrentMode = DRAG_MODE;
                isScaling = false;
                //左滑
                if (isCanLeftScroll && distanceX > 0) {
                    horizontalOffSet -= distanceX;
                }
                //右滑
                if (isCanRightScroll && distanceX < 0) {
                    horizontalOffSet -= distanceX;
                }
                //上滑
                if (isCanUpScroll && distanceY > 0) {
                    verticalOffSet -= distanceY;
                }
                //下滑
                if (isCanDownScroll && distanceY < 0) {
                    verticalOffSet -= distanceY;
                }
                invalidate();
            } else {
                mCurrentMode = SCALE_MODE;
                isScaling = true;
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            postDelayed(mClickRunnable, 250);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            return false;
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return false;
            if (mScale * scaleFactor > 3) {
                return true;
            }
            mScale *= scaleFactor;
            invalidate();
            Log.d("ufly", "mScaleListener onScale scaleFactor=" + scaleFactor + " mScale=" + mScale);
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            isScaling = true;
            mCurrentMode = SCALE_MODE;
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            isScaling = false;
            mCurrentMode = NORMAL_MODE;
        }
    };

    private String getActionString(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return "MotionEvent.ACTION_DOWN";
            case MotionEvent.ACTION_CANCEL:
                return "MotionEvent.ACTION_CANCEL";
            case MotionEvent.ACTION_BUTTON_PRESS:
                return "MotionEvent.ACTION_BUTTON_PRESS";
            case MotionEvent.ACTION_MOVE:
                return "MotionEvent.ACTION_MOVE";
            case MotionEvent.ACTION_UP:
                return "MotionEvent.ACTION_UP";
            case MotionEvent.ACTION_POINTER_UP:
                return "MotionEvent.ACTION_POINTER_UP";
            case MotionEvent.ACTION_POINTER_DOWN:
                return "MotionEvent.ACTION_POINTER_DOWN";
            case MotionEvent.ACTION_OUTSIDE:
                return "MotionEvent.ACTION_OUTSIDE";
        }
        return "";
    }

    public interface DrawInfoListener {
        void onDraw(String info);
    }

    public void setDrawInfoListener(DrawInfoListener drawInfoListener) {
        this.drawInfoListener = drawInfoListener;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}


