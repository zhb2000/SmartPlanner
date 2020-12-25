package com.my.smartplanner.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.my.smartplanner.R;

/**
 * 自定义控件  用来显示每日目标的完成率
 */
public class ActionRateView extends View {
    private float mMiniRateWidth = 5f;  //细圆的宽度
    private float mMaxRateWidth = 20f;  //大圆的宽度
    private float mMaxTextSize = 12f;  //大字体的大小
    private float mMiniTextSize = 10f; //小字体的大小
    private float mCircularDiameter = 0.0f; //圆的直径
    private float mMarginTopRadius = 0.03f;//圆距离顶端和底端的距离3%
    private float mMarginLeftRadius = 0.3f;//圆距离顶端和底端的距离30%
    private float mStartAngle = 270; //圆开始的角度
    private float mAngleLength = 360; //整个原型的的夹角度
    private float mCurrentAngleLength = 0.0f;//外层圆的夹角
    private int mAnimationTime = 300;//动画的时常
    private String mDescription = "0";
    private int mDescriptionTemp = 0;

    private String mUnitOfDescription = "目标完成率(%)";
    private float mUnitOfDescriptionSize = 24.0f;  //字体
    private float mUnitOfDescriptionSizeRate = 0.04f;  //字体与屏幕宽度的计算率
    private float mDescriptionSize = 32.0f;  //字体
    private float mDescriptionSizeRate = 0.12f;  //字体

    public ActionRateView(Context context) {
        super(context);
    }

    public ActionRateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionRateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float centerX = width / 2;
        float left = width * mMarginLeftRadius;
        float top = width * mMarginTopRadius;
        float right = width * (1 - mMarginLeftRadius);
        float bottom = top + (right - left);
        mCircularDiameter = right - left;
        mUnitOfDescriptionSize = width * mUnitOfDescriptionSizeRate;
        mDescriptionSize = width * mDescriptionSizeRate;

        RectF rectF = new RectF(left, top, right, bottom);
        /*【第一步】绘制小圆*/
        drawArcMiniCircle(canvas, rectF);
        /*【第二步】绘制当前进度的粗圆弧*/
        drawArcMaxRate(canvas, rectF);
        /*【第四步】绘制单位文字*/
        drawTextStepString(canvas, centerX);
        /*【第三步】绘制XXX率数字*/
        drawTextNumber(canvas, centerX);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int minimumWidth = getSuggestedMinimumWidth();
//        final int minimumHeight = getSuggestedMinimumHeight();
        int width = getDefaultSize(minimumWidth, widthMeasureSpec);
        int height = width * 46 / 100;  //取宽度的46%作为高度
        setMeasuredDimension(widthMeasureSpec, height);
    }

    //绘制内部细圆圈的方法
    private void drawArcMiniCircle(Canvas canvas, RectF rectF) {
        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.color_gray)); /* 默认画笔颜色 */
        /* 结合处为圆弧*/
//        paint.setStrokeJoin(Paint.Join.ROUND);
        /* 设置画笔的样式 Paint.Cap.Round ,Cap.SQUARE等分别为圆形、方形*/
//        paint.setStrokeCap(Paint.Cap.ROUND);
        /* 设置画笔的填充样式 Paint.Style.FILL  :填充内部;Paint.Style.FILL_AND_STROKE  ：填充内部和描边;  Paint.Style.STROKE  ：仅描边*/
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);/*抗锯齿功能*/
        paint.setStrokeWidth(mMiniRateWidth);/*设置画笔宽度*/
//        canvas.drawArc(rectF, mStartAngle, mAngleLength, false, paint);
        float radius = (rectF.bottom - rectF.top) / 2; //小圆半径
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), radius, paint);//画圆

    }

    //绘制外部粗圆弧的方法
    private void drawArcMaxRate(Canvas canvas, RectF rectF) {
        Paint paintCurrent = new Paint();
        paintCurrent.setStrokeJoin(Paint.Join.ROUND);
        paintCurrent.setStrokeCap(Paint.Cap.ROUND);//圆角弧度
        paintCurrent.setStyle(Paint.Style.STROKE);//设置填充样式
        paintCurrent.setAntiAlias(true);//抗锯齿功能
        paintCurrent.setStrokeWidth(mMaxRateWidth);//设置画笔宽度
        paintCurrent.setColor(getResources().getColor(R.color.color_white));//设置画笔颜色

        /*绘制圆弧的方法
          drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)//画弧，
         参数一是RectF对象，一个矩形区域椭圆形的界限用于定义在形状、大小、电弧，
         参数二是起始角(度)在电弧的开始，圆弧起始角度，单位为度。
         参数三圆弧扫过的角度，顺时针方向，单位为度,从右中间开始为零度。
         参数四是如果这是true(真)的话,在绘制圆弧时将圆心包括在内，通常用来绘制扇形；如果它是false(假)这将是一个弧线,
         参数五是Paint对象；
         */
        canvas.drawArc(rectF, mStartAngle, mCurrentAngleLength, false, paintCurrent);
    }

    //绘制中心大字体的方法
    private void drawTextNumber(Canvas canvas, float centerX) {

        Paint vTextPaint = new Paint();
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setAntiAlias(true);//抗锯齿功能
        vTextPaint.setTextSize(mDescriptionSize);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        vTextPaint.setTypeface(font);//字体风格
        vTextPaint.setColor(getResources().getColor(R.color.color_white));
        Rect bounds_Number = new Rect();
        vTextPaint.getTextBounds(mDescription, 0, mDescription.length(), bounds_Number);
        canvas.drawText(mDescription, centerX, getHeight() / 2, vTextPaint);

    }

    private void drawTextStepString(Canvas canvas, float centerX) {
        Paint vTextPaint = new Paint();
//        vTextPaint.setTextSize(dipToPx(16));
        vTextPaint.setTextSize(mUnitOfDescriptionSize);
        vTextPaint.setTextAlign(Paint.Align.CENTER);
        vTextPaint.setAntiAlias(true);//抗锯齿功能
        vTextPaint.setColor(getResources().getColor(R.color.color_white));
        Rect bounds = new Rect();
        vTextPaint.getTextBounds(mUnitOfDescription, 0, mUnitOfDescription.length(), bounds);
        Rect bounds_Number = new Rect();
        vTextPaint.getTextBounds(mDescription, 0, mDescription.length(), bounds_Number);
        canvas.drawText(mUnitOfDescription, centerX, getHeight() / 2 + bounds.height() + bounds.height() / 2, vTextPaint);

    }

    /**
     * 获取当前步数的数字的高度
     *
     * @param fontSize 字体大小
     * @return 字体高度
     */
    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Rect bounds_Number = new Rect();
        paint.getTextBounds(mDescription, 0, mDescription.length(), bounds_Number);
        return bounds_Number.height();
    }


    public void setCurrentCount(int totalNum, int currentCounts) {
        mDescription = currentCounts + "";
        mDescriptionTemp = currentCounts;
        if (currentCounts > totalNum) {
            currentCounts = totalNum;
        }
        /*所走步数占用总共步数的百分比*/
        float scale = (float) currentCounts / totalNum;
        /*换算成弧度最后要到达的角度的长度-->弧长*/
        float currentAngleLength = scale * mAngleLength;
        /*开始执行动画*/
        setAnimation(0, currentAngleLength, mAnimationTime);
    }

    /**
     * 为进度设置动画
     * ValueAnimator是整个属性动画机制当中最核心的一个类，属性动画的运行机制是通过不断地对值进行操作来实现的，
     * 而初始值和结束值之间的动画过渡就是由ValueAnimator这个类来负责计算的。
     * 它的内部使用一种时间循环的机制来计算值与值之间的动画过渡，
     * 我们只需要将初始值和结束值提供给ValueAnimator，并且告诉它动画所需运行的时长，
     * 那么ValueAnimator就会自动帮我们完成从初始值平滑地过渡到结束值这样的效果。
     */
    private void setAnimation(float last, final float current, int length) {
        ValueAnimator progressAnimator = ValueAnimator.ofFloat(last, current);
        progressAnimator.setDuration(length);
        progressAnimator.setTarget(mCurrentAngleLength);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentAngleLength = (float) animation.getAnimatedValue();
                float temp = mDescriptionTemp * (mCurrentAngleLength / current);
                mDescription = temp >= 10 ? temp >= 100 ? (temp + "").substring(0, 3) : (temp + "").substring(0, 2) : (temp + "").substring(0, 1);
                invalidate();//重新绘制页面
            }

        });
        progressAnimator.start();
    }

}
