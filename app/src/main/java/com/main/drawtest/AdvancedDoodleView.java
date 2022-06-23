package com.main.drawtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

import cn.forward.androids.ScaleGestureDetectorApi27;
import cn.forward.androids.TouchGestureDetector;

/**
 * <p>文件描述：<p>
 * <p>作者：asus<p>
 * <p>创建时间：2022/6/21<p>
 */
public class AdvancedDoodleView extends View {

    private final static String TAG = "AdvancedDoodleView";


    private final static float MAX_SCALE = 2f;
    private final static float MIN_SCALE = 0.3f;


    private Paint mPaint = new Paint();
    //    private List<PathItem> mPathList = new ArrayList<>(); // 保存涂鸦轨迹的集合
    private List<DimensionPoint> markList = new ArrayList();// 保存涂鸦轨迹的集合
    private TouchGestureDetector mTouchGestureDetector; // 触摸手势监听
    private float mLastX, mLastY;
    private DimensionPoint mCurrentPathItem; // 当前的涂鸦轨迹
    private DimensionPoint mSelectedPathItem; // 选中的涂鸦轨迹

    private Bitmap mBitmap;
    private float mBitmapTransX, mBitmapTransY, mBitmapScale = 1;

    private boolean mStartTag = false;
    private DrawInfoListener drawInfoListener;

    private boolean mEnableTag;

    public AdvancedDoodleView(Context context, Bitmap bitmap) {
        super(context);
        mBitmap = bitmap;

        // 设置画笔
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(16);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        // 由手势识别器处理手势
        mTouchGestureDetector = new TouchGestureDetector(getContext(), new TouchGestureDetector.OnTouchGestureListener() {

            RectF mRectF = new RectF();

            // 缩放手势操作相关
            Float mLastFocusX;
            Float mLastFocusY;
            float mTouchCentreX, mTouchCentreY;

            @Override
            public boolean onScaleBegin(ScaleGestureDetectorApi27 detector) {
                Log.e(TAG, "onScaleBegin: ");
                mLastFocusX = null;
                mLastFocusY = null;
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetectorApi27 detector) {
                Log.e(TAG, "onScaleEnd: ");
            }

            @Override
            public boolean onScale(ScaleGestureDetectorApi27 detector) { // 双指缩放中
                Log.e(TAG, "onScale: ");
                // 屏幕上的焦点
                mTouchCentreX = detector.getFocusX();
                mTouchCentreY = detector.getFocusY();

                if (mLastFocusX != null && mLastFocusY != null) { // 焦点改变
                    float dx = mTouchCentreX - mLastFocusX;
                    float dy = mTouchCentreY - mLastFocusY;
                    // 移动图片
                    //                    mBitmapTransX = mBitmapTransX + dx;
                    //                    mBitmapTransY = mBitmapTransY + dy;

                    if (mBitmapTransX + dx < Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * (-1) / 2) {
                        mBitmapTransX = -1f * Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * (-1) / 2;
                    } else if (bitmap.getWidth() * mBitmapScale + mBitmapTransX + dx > Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * 3f / 2f) {
                        mBitmapTransX = Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * 3f / 2f - bitmap.getWidth() * mBitmapScale - dx;
                    } else {
                        mBitmapTransX = mBitmapTransX + dx;
                    }
                    if (mBitmapTransY + dy < Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * (-1) / 2f) {
                        mBitmapTransY = Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * (-1) / 2;
                    } else if (bitmap.getHeight() * mBitmapScale + mBitmapTransY + dy > Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * 3f / 2f) {
                        mBitmapTransY = Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * 3f / 2f - bitmap.getHeight() * mBitmapScale - dy;
                    } else {
                        mBitmapTransY = mBitmapTransY + dy;
                    }
                }

                // 缩放图片
                mBitmapScale = mBitmapScale * detector.getScaleFactor();
                Log.e(TAG, "onScale: mBitmapScale =" + mBitmapScale);
                if (mBitmapScale < MIN_SCALE) {
                    mBitmapScale = MIN_SCALE;
                } else if (mBitmapScale > MAX_SCALE) {
                    mBitmapScale = MAX_SCALE;
                }
                invalidate();

                mLastFocusX = mTouchCentreX;
                mLastFocusY = mTouchCentreY;

                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) { // 单击选中
               /* float x = toX(e.getX()), y = toY(e.getY());
                boolean found = false;
                for (DimensionPoint point : markList) {

                }
               *//* for (PathItem path : mPathList) { // 绘制涂鸦轨迹
                    path.mPath.computeBounds(mRectF, true); // 计算涂鸦轨迹的矩形范围
                    mRectF.offset(path.mX, path.mY); // 加上偏移
                    if (mRectF.contains(x, y)) { // 判断是否点中涂鸦轨迹的矩形范围内
                        found = true;
                        mSelectedPathItem = path;
                        break;
                    }
                }*//*
                if (!found) { // 没有点中任何涂鸦
                    mSelectedPathItem = null;
                }
                invalidate();*/
                return true;
            }

            @Override
            public void onScrollBegin(MotionEvent e) { // 滑动开始
                Log.e(TAG, "onScrollBegin: ");
                if (!mStartTag) {
                    mLastFocusX = null;
                    mLastFocusY = null;
                } else {
                    float x = toX(e.getX()), y = toY(e.getY());
                    if (x < 0) {
                        x = 0;
                    } else if (x > mBitmap.getWidth()) {
                        x = mBitmap.getWidth();
                    }
                    if (y < 0) {
                        y = 0;
                    } else if (y > mBitmap.getHeight()) {
                        y = mBitmap.getHeight();
                    }
                  /*  if (mSelectedPathItem == null) {
                        mCurrentPathItem = new PathItem(); // 新的涂鸦
                        mPathList.add(mCurrentPathItem); // 添加的集合中
                        mCurrentPathItem.mPath.moveTo(x, y);
                    }*/
                    if (mSelectedPathItem == null) {
                        mCurrentPathItem = new DimensionPoint();
                        mCurrentPathItem.setLeftTopX(x);
                        mCurrentPathItem.setLeftTopY(y);
                        markList.add(mCurrentPathItem); // 添加的集合中
                    }
                    mLastX = x;
                    mLastY = y;
                    invalidate(); // 刷新
                }

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { // 滑动中
                if (!mStartTag) {
                    if (mBitmapTransX - distanceX < Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * (-1) / 2) {
                        mBitmapTransX = Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * (-1f / 2f);
                    } else if (bitmap.getWidth() * mBitmapScale + mBitmapTransX - distanceX > Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * 3f / 2f) {
                        mBitmapTransX = Math.max(bitmap.getWidth() * mBitmapScale, AppConstant.ScreenWidth) * 3f / 2f - bitmap.getWidth() * mBitmapScale + distanceX;
                    } else {
                        mBitmapTransX = mBitmapTransX - distanceX;
                    }
                    if (mBitmapTransY - distanceY < Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * (-1f / 2f)) {
                        mBitmapTransY = Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * (-1f / 2f);
                    } else if (bitmap.getHeight() * mBitmapScale + mBitmapTransY - distanceY > Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * 3f / 2f) {
                        mBitmapTransY = Math.max(bitmap.getHeight() * mBitmapScale, AppConstant.ScreenHeight) * 3f / 2f - bitmap.getHeight() * mBitmapScale + distanceY;
                    } else {
                        mBitmapTransY = mBitmapTransY - distanceY;
                    }

                    invalidate();

                    mLastFocusX = e2.getX();
                    mLastFocusY = e2.getY();
                } else {
                    float x = toX(e2.getX()), y = toY(e2.getY());
                    if (x < 0) {
                        x = 0;
                    } else if (x > mBitmap.getWidth()) {
                        x = mBitmap.getWidth();
                    }
                    if (y < 0) {
                        y = 0;
                    } else if (y > mBitmap.getHeight()) {
                        y = mBitmap.getHeight();
                    }
                    if (mSelectedPathItem == null) { // 没有选中的涂鸦
                        mCurrentPathItem.setRightBottomX(x);
                        mCurrentPathItem.setRightBottomY(y);

                        //                        mCurrentPathItem.mPath.quadTo(
                        //                                mLastX,
                        //                                mLastY,
                        //                                (x + mLastX) / 2,
                        //                                (y + mLastY) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                    } else { // 移动选中的涂鸦
                        //                        mSelectedPathItem.mX = mSelectedPathItem.mX + x - mLastX;
                        //                        mSelectedPathItem.mY = mSelectedPathItem.mY + y - mLastY;
                    }
                    mLastX = x;
                    mLastY = y;
                    invalidate(); // 刷新
                }
                return true;
            }

            @Override
            public void onScrollEnd(MotionEvent e) { // 滑动结束
                Log.e(TAG, "onScrollEnd: ");
                if (mStartTag) {
                    float x = toX(e.getX()), y = toY(e.getY());
                    if (x < 0) {
                        x = 0;
                    } else if (x > mBitmap.getWidth()) {
                        x = mBitmap.getWidth();
                    }
                    if (y < 0) {
                        y = 0;
                    } else if (y > mBitmap.getHeight()) {
                        y = mBitmap.getHeight();
                    }
                    if (mSelectedPathItem == null) {
                        mCurrentPathItem.setRightBottomX(x);
                        mCurrentPathItem.setRightBottomY(y);
                        //                        mCurrentPathItem.mPath.quadTo(
                        //                                mLastX,
                        //                                mLastY,
                        //                                (x + mLastX) / 2,
                        //                                (y + mLastY) / 2); // 使用贝塞尔曲线 让涂鸦轨迹更圆滑
                        mCurrentPathItem = null; // 轨迹结束
                    }
                    if (drawInfoListener != null) {
                        drawInfoListener.onDraw(markList);
                    }
                    invalidate(); // 刷新
                }
            }

        });

        // 针对涂鸦的手势参数设置
        // 下面两行绘画场景下应该设置间距为大于等于1，否则设为0双指缩放后抬起其中一个手指仍然可以移动
        mTouchGestureDetector.setScaleSpanSlop(1); // 手势前识别为缩放手势的双指滑动最小距离值
        mTouchGestureDetector.setScaleMinSpan(1); // 缩放过程中识别为缩放手势的双指最小距离值
        mTouchGestureDetector.setIsLongpressEnabled(false);
        mTouchGestureDetector.setIsScrollAfterScaled(false);
    }

    public void setEnableTag(boolean enableTag) {
        mEnableTag = enableTag;
    }

    public void setStartTag(boolean startTag) {
        mStartTag = startTag;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) { //view绘制完成时 大小确定
        super.onSizeChanged(width, height, oldw, oldh);
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float nw = w * 1f / getWidth();
        float nh = h * 1f / getHeight();
        float centerWidth, centerHeight;
        // 1.计算使图片居中的缩放值
        if (nw > nh) {
            mBitmapScale = 1 / nw;
            centerWidth = getWidth();
            centerHeight = (int) (h * mBitmapScale);
        } else {
            mBitmapScale = 1 / nh;
            centerWidth = (int) (w * mBitmapScale);
            centerHeight = getHeight();
        }
        // 2.计算使图片居中的偏移值
        mBitmapTransX = (getWidth() - centerWidth) / 2f;
        mBitmapTransY = (getHeight() - centerHeight) / 2f;
        invalidate();
    }

    /**
     * 将屏幕触摸坐标x转换成在图片中的坐标
     */
    public final float toX(float touchX) {
        return (touchX - mBitmapTransX) / mBitmapScale;
    }

    /**
     * 将屏幕触摸坐标y转换成在图片中的坐标
     */
    public final float toY(float touchY) {
        return (touchY - mBitmapTransY) / mBitmapScale;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 画布和图片共用一个坐标系，只需要处理屏幕坐标系到图片（画布）坐标系的映射关系(toX toY)
        canvas.translate(mBitmapTransX, mBitmapTransY);
        canvas.scale(mBitmapScale, mBitmapScale);

        // 绘制图片
        canvas.drawBitmap(mBitmap, 0, 0, null);
        for (DimensionPoint point : markList) {
            canvas.save();
            if (mSelectedPathItem == point) {
                mPaint.setColor(Color.YELLOW); // 点中的为黄色
            } else {
                mPaint.setColor(Color.RED); // 其他为红色
            }
            canvas.drawRect(point.getLeftTopX(), point.getLeftTopY(), point.getRightBottomX(), point.getRightBottomY(), mPaint);
            canvas.restore();
        }
        /*for (PathItem path : mPathList) { // 绘制涂鸦轨迹
            canvas.save();
            canvas.translate(path.mX, path.mY); // 根据涂鸦轨迹偏移值，偏移画布使其画在对应位置上
            if (mSelectedPathItem == path) {
                mPaint.setColor(Color.YELLOW); // 点中的为黄色
            } else {
                mPaint.setColor(Color.RED); // 其他为红色
            }
            canvas.drawPath(path.mPath, mPaint);
            canvas.restore();
        }*/
    }

    public void setMarkList(List<DimensionPoint> markList) {
        this.markList = markList;
    }


    public void revokeRect() {
        if (markList.isEmpty()) {
            Log.d("ufly", "markList.isEmpty()");
            return;
        }
        markList.remove(markList.size() - 1);
        invalidate();
        if (drawInfoListener != null) {
            drawInfoListener.onDraw(markList);
        }
    }

    public interface DrawInfoListener {
        void onDraw(List<DimensionPoint> data);
    }

    public void setDrawInfoListener(DrawInfoListener drawInfoListener) {
        this.drawInfoListener = drawInfoListener;
    }

}