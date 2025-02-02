package com.example.a20210207_checkmate2;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.renderer.DataRenderer;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.color.MaterialColors;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class LineChartClass {
    LineChart mChart;

    MainActivity mainActivity;
    ArrayList<Hba1cEntry> hba1c_data;
    ArrayList<ILineDataSet> dataSets;
    ArrayList<Integer> colors;
    int daysGoingBackCount;

    int xAxisTextColor;
    int bubbleColorSelected;
    int bubbleColorInRange;
    int bubbleColorAboveRange;
    int bubbleColorHighAbRange;
    int bubbleNotEnoughDataColor;
    int textColor;

    //Constructor
    LineChartClass(MainActivity mainActivity, ArrayList<Hba1cEntry> hba1c_data, int daysGoingBackCount) {
        mChart = (LineChart) mainActivity.findViewById(R.id.LineChart);
        this.mainActivity = mainActivity;
        this.hba1c_data = hba1c_data;
        this.daysGoingBackCount = daysGoingBackCount;

        //Initialize Colors
        xAxisTextColor = mainActivity.getResources().getColor(R.color.line);
        bubbleColorSelected = MaterialColors.getColor(mainActivity, R.attr.colorDayLine, Color.BLACK);
        bubbleColorInRange = MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK);
        bubbleColorAboveRange = MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK);
        bubbleColorHighAbRange = MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK);
        bubbleNotEnoughDataColor = MaterialColors.getColor(mainActivity, R.attr.colorNotEnoughData, Color.BLACK);
        textColor = MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK);
    }

    public void createChart(int daySelected, boolean useDaySelected) {

        //Get Values from Preferences
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        float hba1cGoal = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_GOALS,"6.3"));
        float hba1cHigh = Float.parseFloat(sharedPref.getString(SettingsActivity.KEY_PREF_HBA1C_VERY_HIGH,"7.0"));

        //Check Rotation of smartphone and modify Bubble and Line Position (NOT Text)
        int orientation = mainActivity.getResources().getConfiguration().orientation;


        //Calculate the Delta Ymax/Ymin from Data and calculate the modifier based on the result
        //Get MinMax from HbA1c data
        double hba1cMax =hba1c_data.get(0).hba1c;
        double hba1cMin = hba1cMax;

        for(int i=1;i<hba1c_data.size();i++){
            double compareValue = hba1c_data.get(i).hba1c;
            if (compareValue > hba1cMax)
                hba1cMax = compareValue;
            if (compareValue < hba1cMin)
                hba1cMin = compareValue;
        }

        //Format Axes
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceMax(0.8f);
        xAxis.setSpaceMin(0.8f);
        xAxis.setTextSize(15); //Leave space on right side
        xAxis.setTextColor(xAxisTextColor);
        xAxis.setCenterAxisLabels(false);

        YAxis left = mChart.getAxisLeft();
        left.setDrawLabels(false); // no axis labels
        left.setDrawAxisLine(false); // no axis line
        left.setDrawGridLines(false); // no grid lines

        YAxis right = mChart.getAxisRight();
        right.setDrawLabels(false); // no axis labels
        right.setDrawAxisLine(false); // no axis line
        right.setDrawGridLines(false); // no grid lines

        //Load Data in Chart
        ArrayList<Entry> dataLine= new ArrayList<>();
        ArrayList<Entry> dataBubble = new ArrayList<>();
        ArrayList<Entry> dataBubbleSel = new ArrayList<>();
        ArrayList<Entry> dataText = new ArrayList<>();

        //for (Hba1cEntry element:hba1c_data){
        float hba1c_value = 0;
        final ArrayList<String> xAxisLabel = new ArrayList<>();
        int count = hba1c_data.size() - 1;

        //Get maximum number of measurements a day -> detect days without enough data
        double maxValues = 0;
        for(Hba1cEntry entry : hba1c_data){
            if (entry.nValues > maxValues)
                maxValues = entry.nValues;
        }

        //Initialize new color array
        colors = new ArrayList<Integer>();
        int colorBubbleSel = 0;

        for (int i = 0; i <= count; i++) {

            hba1c_value = BigDecimal.valueOf(hba1c_data.get(count - i).hba1c).setScale(1, BigDecimal.ROUND_HALF_DOWN).floatValue();

            //Set X-Axis Label
            SimpleDateFormat df = new SimpleDateFormat("E\ndd/MM", Locale.getDefault()); // Quoted "Z" to indicate UTC, no timezone offset
            String dateString = df.format(hba1c_data.get(count - i).date);
            xAxisLabel.add(dateString);

            //Calculate bubble to text shift
            float radiusPixels = Utils.convertDpToPixel(20);


            //Add bubble and line values
            dataBubble.add(new Entry((float) i, hba1c_value, hba1c_value));
            //Add line only when in HbA1c area
            if (count - daysGoingBackCount < i)
                dataLine.add(new Entry((float) i, hba1c_value, hba1c_value));

            float value = hba1c_value;

            //Add second data set for the hba1c values
            dataText.add(new Entry((float) i, hba1c_value, hba1c_value));

            if ((hba1c_data.get(count - i).nValues > (0.5 * maxValues)) || i == count) {
                if (value <= hba1cGoal)
                    colors.add(bubbleColorInRange);
                else if (value > hba1cGoal && value < hba1cHigh)
                    colors.add(bubbleColorAboveRange);
                else
                    colors.add(bubbleColorHighAbRange);
            } else {
                colors.add(bubbleNotEnoughDataColor);
            }

            //Color day selected brighter
            if (useDaySelected == true && i == daySelected){

                colorBubbleSel = colors.get(i);
                dataBubbleSel.add(new Entry((float) i, hba1c_value, hba1c_value));
                colors.set(i, Color.WHITE);

            }
        }

        //-------------------------------------------------------------------------------
        //Format Line and Textdata
        //-------------------------------------------------------------------------------
        LineDataSet dataLineSet = new LineDataSet(dataLine, "Line");
        LineDataSet dataBubbleSet = new LineDataSet(dataBubble, "Bubble");
        LineDataSet dataTextSet = new LineDataSet(dataText, "Text");
        LineDataSet dataBubbleSelSet = new LineDataSet(dataBubbleSel, "BubbleSel");

        mChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLabel));
        //xAxis.setGranularity(1f); // minimum axis-step (interval) is
        mChart.setXAxisRenderer(new ValueFormatterDateXAxis(mChart.getViewPortHandler(), mChart.getXAxis(), mChart.getTransformer(YAxis.AxisDependency.LEFT)));
        //xAxis.setLabelRotationAngle(20);

        //Format Line Data
        dataLineSet.setLineWidth(5);
        dataLineSet.setColor(mainActivity.getResources().getColor(R.color.line));
        dataLineSet.setDrawValues(false);
        dataLineSet.setHighlightEnabled(false);

        //Format Text Data
        dataTextSet.setDrawCircles(false);
        dataTextSet.setValueTextSize(20);
        dataTextSet.setValueTextColor(textColor);
        dataTextSet.enableDashedLine(0f,1f,0f);
        dataTextSet.setValueFormatter(new ValueFormatterOneDecimal());
        dataTextSet.setHighlightEnabled(false);

        //Format Bubble Data
        dataBubbleSet.setCircleColors(colors);
        dataBubbleSet.setDrawValues(false);
        //dataBubbleSet.setValueTextColors(Collections.singletonList(mainActivity.getResources().getColor(R.color.blue)));
        dataBubbleSet.setCircleRadius(20);
        dataBubbleSet.enableDashedLine(0f,1f,0f);
        dataBubbleSet.setDrawCircleHole(false);
        //dataBubbleSet.setValueFormatter(new ValueFormatterOneDecimal());
        dataBubbleSet.setHighlightEnabled(true);
        dataBubbleSet.setDrawHighlightIndicators(false);

        //Format Bubble Data Selected
        dataBubbleSelSet.setFillAlpha(0);
        dataBubbleSelSet.setCircleColor(colorBubbleSel);
        dataBubbleSelSet.setCircleRadius(17);
        dataBubbleSelSet.setDrawValues(false);
        dataBubbleSelSet.setDrawCircleHole(false);

        dataSets = new ArrayList<>();
        dataSets.add(dataLineSet);
        dataSets.add(dataTextSet);
        dataSets.add(dataBubbleSet);
        dataSets.add(dataBubbleSelSet);

        //-------------------------------------------------------------------------------
        //Render Line Chart
        //-------------------------------------------------------------------------------
        LineData data = new LineData(dataSets);

        mChart.setData(data);
        mChart.invalidate();

        mChart.setRenderer(new CenteredTextLineChartRenderer(mChart,mChart.getAnimator(),mChart.getViewPortHandler()));

        //Set number of displayed values to 6
        mChart.setVisibleXRangeMaximum(6);
        mChart.getDescription().setEnabled(false);
        mChart.getLegend().setEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setExtraOffsets(0, 10, 0, 0); //X-Axis Labels are drawn correctly

        //mChart.setViewPortOffsets(0f,0f,120f,0f);
    }

}


