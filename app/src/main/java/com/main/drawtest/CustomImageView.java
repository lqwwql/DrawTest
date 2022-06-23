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
 * left:x轴，左横坐标
 * right：x轴，右横坐标
 * top：y轴，上纵坐标
 * bottom：y轴，下纵坐标
 */
public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {

    private DrawInfoListener drawInfoListener;
    private final int NORMAL_MODE = 101;
    private final int DRAG_MODE = 102;
    private final int SCALE_MODE = 103;
    private boolean isDraw = false;//是否标注模式
    private float clickX = 0, clickY = 0, startX = 0, startY = 0;
    private float baseX, baseY;//画布坐标系原点，可变化
    private float baseScaleX, baseScaleY;//缩放中心点，可变化
    private Paint paint;//画笔
    private int paintColor = Color.RED;//画笔颜色
    private int paintColor1 = Color.BLACK;//画笔颜色
    private float strokeWidth = 4.0f;//画笔粗细
    private double proportion = 1.0f;//与原画比例
    private Bitmap baseBitmap;//最初加载图片
    private Bitmap finalBitmap;//最终保存图片
    private Bitmap rectBitmap;//画矩形图片
    private List<DimensionPoint> markList = new ArrayList();//保存每次画的步骤
    private String filePath;//用于另存为
    private int imgWidth, imgHeight, viewWidth, viewHeight;
    private String tvInfo;
    private GestureDetector mDetector;//手势移动
    private ScaleGestureDetector mScaleDetector;//手势缩放
    private OnClickListener mClickListener;
    private OnLongClickListener mLongClick;
    private float mScale = 1.0f;//缩放倍数
    private int mCurrentMode = NORMAL_MODE;//手势模式
    private boolean isScaling = false;
    private float horizontalOffSet = 0;//横向滑动距离
    private float verticalOffSet = 0;//纵向滑动距离
    private RectF baseRect = new RectF();//图像坐标系，用于基础位置偏移计算
    private RectF imgRect = new RectF();//图像坐标系，用于移动缩放边距判断
    private RectF scrollRect = new RectF();//图像坐标系，用于移动缩放边距判断

    private int mode = 1;
    private boolean isCanLeftScroll = true;
    private boolean isCanRightScroll = true;
    private boolean isCanUpScroll = true;
    private boolean isCanDownScroll = true;

    private float translateX = 0;
    private float translateY = 0;
    private boolean isTranslate = false;
    private boolean isScale = false;

    private float mScaleFactor = 1.0f;
    private boolean isInit = true;
    private boolean isRevoke = false;
    private boolean isMoving = false;
    private boolean isCanScale = true;
    private boolean isCanTranslate = true;
    private int scaleType = 1;
    private boolean isTest = true;

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
        Log.d("ufly", "setFileImage createFinalBitmap filePath=" + filePath);
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.d("ufly", "setFileImage DisplayMetrics density=" + dm.density);

        this.filePath = filePath;
        int originalWidth = 0;
        int originalHeight = 0;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        originalWidth = options.outWidth;
        originalHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        Log.d("ufly", "setFileImage DisplayMetrics originalWidth=" + originalWidth + " originalHeight=" + originalHeight);

        double proportionW = (float) viewWidth / (float) originalWidth;
        double proportionH = (float) viewHeight / (float) originalHeight;
        proportion = Math.max(proportionW, proportionH);
        imgWidth = proportionW > proportionH ? viewWidth : (int) (originalWidth * proportion);
        imgHeight = proportionH > proportionW ? viewHeight : (int) (originalHeight * proportion);
        Log.d("ufly", "setFileImage DisplayMetrics imgWidth=" + imgWidth + " imgHeight=" + imgHeight);

        Bitmap originalBitmap = BitmapFactory.decodeFile(filePath).copy(Bitmap.Config.ARGB_8888, true);
        baseBitmap = Bitmap.createScaledBitmap(originalBitmap, imgWidth, imgHeight, true);
        finalBitmap = baseBitmap;
        setImageBitmap(baseBitmap);
        imgRect.set(0, 0, imgWidth, imgHeight);
        baseRect.set(0, 0, imgWidth, imgHeight);
        scrollRect.set(0, 0, imgWidth, imgHeight);
        baseX = 0;
        baseY = 0;
        originalBitmap.recycle();
        System.gc();
    }

    private void init() {
        mDetector = new GestureDetector(getContext(), mGestureListener);
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("ufly", "onDraw start--------------------------- isDraw=" + isDraw + " isRevoke=" + isRevoke + " isMoving=" + isMoving + " mScale=" + mScale);
        //消除锯齿
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.save();
        if (isTest) {
            if (paint == null) {
                paint = new Paint();
                paint.setStyle(Paint.Style.FILL);
                paint.setAntiAlias(true);
                paint.setColor(paintColor);
                paint.setStrokeWidth(strokeWidth);
            }
            paint.setColor(Color.YELLOW);
            canvas.drawRect(0, 0, 200, 200, paint);

            Log.d("ufly", "onDraw ---------------------------mScale=" + mScale + " horizontalOffSet=" + horizontalOffSet + " verticalOffSet=" + verticalOffSet);
            if (isScale) {
                Log.d("ufly", "onDraw ---------------------------scale mScale=" + mScale + " scaleType=" + scaleType);
                canvas.translate(translateX, translateY);
                canvas.scale(mScale, mScale);
                paint.setColor(Color.RED);
                canvas.drawRect(0, 0, 200, 200, paint);
            } else if (isTranslate) {
                Log.d("ufly", "onDraw ---------------------------translate mScale=" + mScale);
                canvas.translate(translateX, translateY);
                canvas.scale(mScale, mScale);
                paint.setColor(Color.GREEN);
                canvas.drawRect(0, 0, 200, 200, paint);
            }

            Paint paint1 = new Paint();
            paint1.setColor(paintColor1);
            paint1.setStyle(Paint.Style.FILL);
            canvas.drawCircle((float) viewWidth / 2, (float) viewHeight / 2, 5, paint1);
        } else {
            if (baseBitmap != null) {
                //标注模式才绘制矩形
                if (isDraw) {
                    canvas.scale(mScale, mScale);
                    canvas.translate(imgRect.left, imgRect.top);
                    canvas.drawBitmap(drawRect(), getMatrix(), null);
                } else {
                    if (mode == 1) {
                        drawBitmap(canvas);
                    } else {
                        canvas.translate(horizontalOffSet, verticalOffSet);
                        canvas.scale(mScale, mScale);
                        canvas.drawBitmap(finalBitmap, getMatrix(), null);
                    }
                }
            }
        }

        canvas.restore();
        isInit = false;
        isScale = false;
        isTranslate = false;
        isRevoke = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //标注模式不进行缩放移动
        if (!isDraw) {
            if (isCanTranslate) {
                mDetector.onTouchEvent(event);
            }
            if (isCanScale) {
                mScaleDetector.onTouchEvent(event);
            }
        }

        clickX = event.getX();
        clickY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = clickX;
                startY = clickY;
                //标注模式才计算矩形位置
                if (isDraw) {
                    calculatePosition(event);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDraw) {
                    isMoving = true;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (isDraw) {
                    isMoving = false;
                    finalBitmap = rectBitmap;
                    markList.add(new DimensionPoint((startX + Math.abs(imgRect.left)), startY + Math.abs(imgRect.top), clickX + Math.abs(imgRect.left), clickY + Math.abs(imgRect.top), proportion));
                    calculatePosition(event);
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
        viewWidth = w;
        viewHeight = h;
        Log.d("ufly", "onSizeChanged viewWidth=" + viewWidth + " viewHeight=" + viewHeight);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
    }

    public void setDraw(boolean draw) {
        isDraw = draw;
    }

    /**
     * 画矩形
     */
    public Bitmap drawRect() {
        Bitmap bitmap;
        if (isRevoke) {
            bitmap = Bitmap.createBitmap(baseBitmap);
        } else {
            bitmap = Bitmap.createBitmap(finalBitmap);
        }
        Canvas canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(paintColor);
        paint.setStrokeWidth(strokeWidth);
        if (isMoving) {
            Log.d("ufly", "onDraw isMoving--------------------------- ");
            canvas.drawRect(startX + Math.abs(imgRect.left), startY + Math.abs(imgRect.top), clickX + Math.abs(imgRect.left), clickY + Math.abs(imgRect.top), paint);
        } else if (isRevoke) {
            Log.d("ufly", "onDraw markList--------------------------- ");
            for (DimensionPoint dimensionPoint : markList) {
                canvas.drawRect(dimensionPoint.getLeftTopX(), dimensionPoint.getLeftTopY(), dimensionPoint.getRightBottomX(), dimensionPoint.getRightBottomY(), paint);
            }
        }
        rectBitmap = bitmap;
        return rectBitmap;
    }

    /**
     * 画移动、放缩后的图像
     */
    public void drawBitmap(Canvas canvas) {
        //手势拖拽——基于放缩后的移动
        if (mCurrentMode == DRAG_MODE) {
            Log.d("ufly", "DRAG_MODE before imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
            Log.d("ufly", "DRAG_MODE before horizontalOffSet=" + horizontalOffSet + " verticalOffSet=" + verticalOffSet);
            //右移动
            if (horizontalOffSet >= 0) {
                Log.d("ufly", "DRAG_MODE 右移动-------- 剩余距离：" + (Math.abs(imgRect.left)) + " 移动距离：" + horizontalOffSet);
                isCanLeftScroll = true;
                if (Math.abs(imgRect.left) <= horizontalOffSet) {
                    Log.d("ufly", "DRAG_MODE step1--------");
                    horizontalOffSet = Math.abs(imgRect.left);
                    isCanRightScroll = false;
                } else {
                    Log.d("ufly", "DRAG_MODE step2--------");
                }
            }
            //左移动
            else {
                isCanRightScroll = true;
                Log.d("ufly", "DRAG_MODE 左移动-------- 剩余距离：" + (imgRect.right - viewWidth) + " 移动距离：" + horizontalOffSet);
                if ((imgRect.right - (float) viewWidth) <= Math.abs(horizontalOffSet)) {
                    Log.d("ufly", "DRAG_MODE step3--------");
                    horizontalOffSet = -Math.abs(imgRect.right - viewWidth);
                    isCanLeftScroll = false;
                } else {
                    Log.d("ufly", "DRAG_MODE step4--------");
                }
            }

            //下移动
            if (verticalOffSet >= 0) {
                Log.d("ufly", "DRAG_MODE 下移动-------- 剩余距离：" + (Math.abs(imgRect.top)) + " 移动距离：" + verticalOffSet);
                isCanUpScroll = true;
                if ((Math.abs(imgRect.top)) <= verticalOffSet) {
                    Log.d("ufly", "DRAG_MODE step5--------");
                    verticalOffSet = Math.abs(imgRect.top);
                    isCanDownScroll = false;
                } else {
                    Log.d("ufly", "DRAG_MODE step6--------");
                }
            }
            //上移动
            else {
                isCanDownScroll = true;
                Log.d("ufly", "DRAG_MODE 上移动-------- 剩余距离：" + (imgRect.bottom - (float) viewHeight) + " 移动距离：" + verticalOffSet);
                if ((imgRect.bottom - (float) viewHeight) <= Math.abs(verticalOffSet)) {
                    Log.d("ufly", "DRAG_MODE step7--------");
                    verticalOffSet = -Math.abs(imgRect.bottom - (float) viewHeight);
                    isCanUpScroll = false;
                } else {
                    Log.d("ufly", "DRAG_MODE step8--------");
                }
            }

            Log.d("ufly", "DRAG_MODE after horizontalOffSet=" + horizontalOffSet + " verticalOffSet=" + verticalOffSet + " isCanLeftScroll=" + isCanLeftScroll + " isCanRightScroll=" + isCanRightScroll + " isCanUpScroll=" + isCanUpScroll + " isCanDownScroll=" + isCanDownScroll);

            imgRect.left = imgRect.left + horizontalOffSet;
            imgRect.right = imgRect.right + horizontalOffSet;
            imgRect.top = imgRect.top + verticalOffSet;
            imgRect.bottom = imgRect.bottom + verticalOffSet;

            Log.d("ufly", "DRAG_MODE after imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
            canvas.translate(imgRect.left, imgRect.top);
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(finalBitmap, 0, 0, null);
            Log.d("ufly", "DRAG_MODE after translate imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom);
        }
        //手势缩放——基于移动后的放缩
        else if (mCurrentMode == SCALE_MODE) {
            Log.d("ufly", "SCALE_MODE before imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom + " mScale=" + mScale + " mScaleFactor=" + mScaleFactor);
            //缩放边距判断


            //根据坐标原点计算缩放后坐标
            imgRect.right = (baseRect.right) * mScale - imgRect.left;
            imgRect.bottom = (baseRect.bottom) * mScale - imgRect.top;

            Log.d("ufly", "SCALE_MODE after imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom + " imgWidth=" + imgWidth + " imgHeight=" + imgHeight);
            canvas.translate(imgRect.left, imgRect.top);
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(finalBitmap, 0, 0, null);
        } else {
            Log.d("ufly", "NORMAL_MODE after imgRect left=" + imgRect.left + " top=" + imgRect.top + " right=" + imgRect.right + " bottom=" + imgRect.bottom + " \n");
            canvas.translate(imgRect.left, imgRect.top);
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(finalBitmap, 0, 0, null);
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
        float imgW = (imgRect.right - imgRect.left);
        float imgH = (imgRect.bottom - imgRect.top);
        tvInfo = "触摸[" + eventX + "," + eventY + "] 图象[" + x + "," + y + "] " + " 大小[" + imgW + "," + imgH + "]";
        //Limit x, y range within bitmap
        if (x < 0) {
            x = 0;
        } else if (x > imgW - 1) {
            x = (int) (imgW - 1);
        }

        if (y < 0) {
            y = 0;
        } else if (y > imgH - 1) {
            y = (int) (imgH - 1);
        }
        tvInfo += " 位图[" + x + "," + y + "]";
        Log.d("ufly", "tvInfo=" + tvInfo);
        if (drawInfoListener != null) {
            drawInfoListener.onDraw(tvInfo, markList);
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
            if (!isScaling) {
                Log.d("ufly", "mGestureListener onScroll e1=" + getActionString(e1) + " e2=" + getActionString(e2) + " distanceX=" + distanceX + " distanceY=" + distanceY);
                mCurrentMode = DRAG_MODE;
                isScaling = false;
                //左滑
                if (isCanLeftScroll && distanceX > 0) {
                    horizontalOffSet = (-distanceX * mScale);
                }
                //右滑
                if (isCanRightScroll && distanceX < 0) {
                    horizontalOffSet = (-distanceX * mScale);
                }
                //上滑
                if (isCanUpScroll && distanceY > 0) {
                    verticalOffSet = (-distanceY * mScale);
                }
                //下滑
                if (isCanDownScroll && distanceY < 0) {
                    verticalOffSet = (-distanceY * mScale);
                }
                invalidate();
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
            mScaleFactor = scaleFactor;
            if (mScale < 1.0f) {
                mScale = 1.0f;
                invalidate();
                return true;
            }
            invalidate();
            Log.d("ufly", "SCALE_MODE mScaleListener onScale mScaleFactor=" + mScaleFactor + " mScale=" + mScale);
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.d("ufly", "SCALE_MODE mScaleListener onScaleBegin----------");
            isScaling = true;
            mCurrentMode = SCALE_MODE;
            scrollRect.set(imgRect);
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d("ufly", "SCALE_MODE mScaleListener onScaleEnd----------");
            isScaling = false;
            mCurrentMode = SCALE_MODE;
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
        void onDraw(String info, List<DimensionPoint> data);
    }

    public void setDrawInfoListener(DrawInfoListener drawInfoListener) {
        this.drawInfoListener = drawInfoListener;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }


    public void scale(float scaleValue, int scaleType) {
        isScale = true;
        mScale = scaleValue;
        this.scaleType = scaleType;
        invalidate();
    }

    public void translate(float x, float y) {
        isTranslate = true;
        translateX = x;
        translateY = y;
        invalidate();
    }

    public void revokeRect() {
        if (markList.isEmpty()) {
            Log.d("ufly", "markList.isEmpty()");
            return;
        }
        markList.remove(markList.size() - 1);
        isRevoke = true;
        invalidate();
        if (drawInfoListener != null) {
            drawInfoListener.onDraw(tvInfo, markList);
        }
    }

    public void drawNewRect(List<DimensionPoint> newRect) {
        markList.addAll(newRect);
        invalidate();
    }

    public boolean isCanScale() {
        return isCanScale;
    }

    public void setCanScale(boolean canScale) {
        isCanScale = canScale;
    }

    public boolean isCanTranslate() {
        return isCanTranslate;
    }

    public void setCanTranslate(boolean canTranslate) {
        isCanTranslate = canTranslate;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }
}


