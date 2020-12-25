package com.my.smartplanner.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.my.smartplanner.R;

import java.util.List;

/**
 * 柱状图的绘制
 */
public class ColumnView extends View {

    private String[] transverse;    //横列的刻度值数组
    private String[] vertical;      //竖列的刻度值数组
    private List<Integer> colors;    //画笔颜色集合
    private int[] high;           //柱状图高度数值数组

    //整个画布的宽、高
    private int width;
    private int height;
    private int maxHigh;//最高柱状图的高度
    // X,Y轴的单位长度 (类似网格切割)
    private int xScale;
    private int yScale;

    // 默认边距
    private int margin = 20;
    // 距离左边偏移量
    private int marginX = 30;
    // 原点坐标
    private int xPoint;
    private int yPoint;

    // 画笔
    private Paint paintAxes;            //画轴
    private Paint paintCoordinate;      //画坐标（文字或数值）
    private Paint paintRectF;           //画矩形

    public ColumnView(Context context) {
        super(context);
    }

    //在构造方法中传入横列的刻度值数组、竖列的刻度值数组、画笔颜色集合、柱状图高度数值数组
    public ColumnView(Context context, String[] transverse, List<Integer> colors, int[] high) {
        super(context);
        this.transverse = new String[transverse.length + 1];
//        this.transverse[0] = "";//第一位要为空不然文字会重叠
//        for (int i = 0; i < transverse.length; i++) {
//            this.transverse[i + 1] = transverse[i];
//            Log.i("HealthActivity", i + "-------Out!" + transverse[i]);
//        }
        this.transverse = transverse;
        //利用High中的数组计算出合适的y轴刻度
        maxHigh = 0;
        for (int value : high) {
            if (maxHigh < value)
                maxHigh = value;
        }

        Log.i("HealthActivity", "Max:" + maxHigh);
        int scale = (int) (0.25 * maxHigh);
        this.vertical = new String[5];
        for (int i = 0; i < 4; i++) {
            vertical[i] = String.valueOf(i * scale);
        }
        vertical[4] = String.valueOf(maxHigh);
        this.colors = colors;
        this.high = high;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init();
        drawAxesLine(canvas, paintAxes);            //绘制横、竖轴
        drawCoordinate(canvas, paintCoordinate);   //绘制坐标（文字或数值）
        drawColumn(canvas, paintRectF, high);//画柱形图
    }

    //初始化数据值和画笔
    public void init() {
        //计算原点坐标和单位长度
        xPoint = margin + marginX;
        yPoint = this.getHeight() - margin;
        xScale = (this.getWidth() - 2 * margin - marginX) / (transverse.length + 1);//此处可能除以0
        yScale = (this.getHeight() - 2 * margin) / (vertical.length - 1);

        paintAxes = new Paint();     //画轴(横轴和竖轴)
        paintAxes.setStyle(Paint.Style.STROKE);
        paintAxes.setAntiAlias(true);
        paintAxes.setDither(true);
        paintAxes.setColor(ContextCompat.getColor(getContext(), colors.get(0)));
        paintAxes.setStrokeWidth(1);

        paintCoordinate = new Paint(Paint.ANTI_ALIAS_FLAG);    //画坐标（文字或数值）
        paintCoordinate.setColor(ContextCompat.getColor(getContext(), colors.get(1)));
        paintCoordinate.setTextSize(22f);

        paintRectF = new Paint();       //画矩形
        paintRectF.setColor(ContextCompat.getColor(getContext(), R.color.color_blue));
        paintRectF.setStyle(Paint.Style.FILL);
        paintRectF.setDither(true);
        paintRectF.setAntiAlias(true);
    }


    // 绘制坐标轴
    private void drawAxesLine(Canvas canvas, Paint paint) {
        // 画 X 轴
        canvas.drawLine(xPoint + 20, yPoint - 20, this.getWidth() - (float) margin / 6 + 20, yPoint - 20, paint);
        // 画 Y 轴
        canvas.drawLine(xPoint + 20, yPoint - 20, xPoint + 20, (float) margin / 6 - 20, paint);
    }

    // 绘制坐标刻度 （可以根据自己需要进行微调）
    private void drawCoordinate(Canvas canvas, Paint paint) {
        // X轴坐标
        for (int i = 0; i < transverse.length; i++) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(40);
            int startX = xPoint + (i + 1) * xScale;
            canvas.drawText(transverse[i], startX, this.getHeight() - (float) margin / 6, paint);
        }

        // Y轴坐标

        for (int i = 0; i <= (vertical.length - 1); i++) {
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(40);
            int startY = yPoint - i * yScale;
            int offsetX;
            switch (vertical[i].length()) {
                case 1:
                    offsetX = 28;
                    break;
                case 2:
                    offsetX = 20;
                    break;
                case 3:
                    offsetX = 12;
                    break;
                case 4:
                    offsetX = 5;
                    break;
                default:
                    offsetX = 0;
                    break;
            }
            int offsetY;
            if (i == 0) {
                offsetY = 0;
            } else {
                offsetY = margin / 5;
            }
            canvas.drawText(vertical[i], (float) margin / 4 + offsetX - 10, startY + offsetY + 10, paint);
        }


    }


    // 绘制单柱形
    private void drawColumn(Canvas canvas, Paint paint, int[] highData) {
        for (int i = 0; i < transverse.length; i++) {
            int startX = xPoint + (i + 1) * xScale;
            final float colWidth = 40;
            RectF rect = new RectF(startX - colWidth / 2, toY(highData[i]), startX + colWidth / 2, getColumnBottom());
            canvas.drawRect(rect, paint);
            paint.setTextSize(40);
            String a = String.valueOf(highData[i]);
            paint.setTextAlign(Paint.Align.CENTER);
            final float textMarginBottom = 10;
            canvas.drawText(a, startX, toY(highData[i]) - textMarginBottom, paint);
        }
    }

    //数据按比例转换坐标（可以根据需要自己设置转换比例）
    //转换坐标有问题
    private float toY(int num) {
        float bottom = getColumnBottom();
        float a = (maxHigh == 0) ? 0 : (float) num / maxHigh;
        float chartHeight = this.getHeight();
        final float spaceForText = 100;
        float length = a * chartHeight - spaceForText;
        return (length > 0) ? bottom - length : bottom;
    }

    private float getColumnBottom() {
        return this.getHeight() - margin * 2;
    }
}