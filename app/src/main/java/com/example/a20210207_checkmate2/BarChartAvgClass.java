package com.example.a20210207_checkmate2;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.material.color.MaterialColors;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BarChartAvgClass {

BarChart bAvgChart;
MainActivity mainActivity;
ArrayList<Hba1cEntry> hba1c_data;
ArrayList<Hba1cEntry> hba1cAverageData;

XAxis bxAxis;
YAxis bLeft;
YAxis bRight;

int bxAxisTextColor;

        //Constructor
        BarChartAvgClass(MainActivity mainActivity,ArrayList<Hba1cEntry> hba1c_data,ArrayList<Hba1cEntry> hba1cAverageData) {
            bAvgChart = (BarChart) mainActivity.findViewById(R.id.BarChartAvg);
            this.mainActivity = mainActivity;
            this.hba1c_data = hba1c_data;
            this.hba1cAverageData = hba1cAverageData;

            //Initialize Colors
            bxAxisTextColor = mainActivity.getResources().getColor(R.color.line);
        }

    public void createChart(int nDays) {

        bxAxis = bAvgChart.getXAxis();
        bLeft = bAvgChart.getAxisLeft();
        bRight = bAvgChart.getAxisRight();

        bxAxis.setDrawAxisLine(false);
        bxAxis.setDrawLabels(true);
        bxAxis.setDrawGridLines(false);
        bxAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //bxAxis.setSpaceMax(0.8f);
        //bxAxis.setSpaceMin(0.8f);



        bxAxis.setTextColor(bxAxisTextColor);
        bxAxis.setTextSize(15);


        bLeft.setDrawLabels(false); // no axis labels
        bLeft.setDrawAxisLine(false); // no axis line
        bLeft.setDrawGridLines(false); // no grid lines

        bRight.setDrawLabels(false); // no axis labels
        bRight.setDrawAxisLine(false); // no axis line
        bRight.setDrawGridLines(false); // no grid lines



        //Load Data in Chart
        ArrayList<BarEntry> dataBar = new ArrayList<BarEntry>();
        ArrayList<BarEntry> dataBar2 = new ArrayList<BarEntry>();

        //Set marker Color depending on RangeValue
        ArrayList<Integer> bAcolors = new ArrayList<Integer>();
        ArrayList<String> xAAxisLabel = new ArrayList<>();

        xAAxisLabel.add(nDays + " Day\nAvg");
        float val1 = BigDecimal.valueOf(hba1cAverageData.get(0).LowRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //Low
        float val2 = BigDecimal.valueOf(hba1cAverageData.get(0).inRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //InRange
        float val3 = BigDecimal.valueOf(hba1cAverageData.get(0).HighRange * 100).setScale(0, BigDecimal.ROUND_HALF_DOWN).floatValue(); //High
        dataBar.add(new BarEntry(0f, new float[]{val1, val2, val3}));
        dataBar2.add(new BarEntry(0f, val2));
        if (val2 >= 50f)
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorInRange, Color.BLACK));
        else if (val2 >= 40f)
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorOutOfRange, Color.BLACK));
        else
            bAcolors.add(MaterialColors.getColor(mainActivity, R.attr.colorVeryOutOfRange, Color.BLACK));

        //Draw x-Axis labels
        bAvgChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAAxisLabel));
        bAvgChart.setXAxisRenderer(new ValueFormatterDateXAxis(bAvgChart.getViewPortHandler(), bAvgChart.getXAxis(), bAvgChart.getTransformer(YAxis.AxisDependency.LEFT)));

        BarDataSet dataBarSet = new BarDataSet(dataBar, "DataBarBackground");
        dataBarSet.setDrawIcons(false);
        dataBarSet.setDrawValues(false);
        int[] colorsInt = new int[]{MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK), MaterialColors.getColor(mainActivity, R.attr.colorBarInRangeBackground, Color.BLACK)};
        dataBarSet.setColors(colorsInt,MaterialColors.getColor(mainActivity, R.attr.alphaBarInRangeBackground, 80));

        //dataBarSet.setStackLabels(new String[]{"<70", "InRange", ">140"}); //Hardcoded

        BarDataSet dataBarSet2 = new BarDataSet(dataBar2, "DataBarInRange");
        dataBarSet2.setColors(bAcolors);
        dataBarSet2.setValueTextSize(20);

        ArrayList<IBarDataSet> dataSetsBar = new ArrayList<>();
        dataSetsBar.add(dataBarSet);
        dataSetsBar.add(dataBarSet2);

        BarData dataBarChart = new BarData(dataSetsBar);
        dataBarChart.setBarWidth(0.8f);
        dataBarChart.setValueFormatter(new ValueFormatterPercentage());
        dataBarChart.setValueTextColor(MaterialColors.getColor(mainActivity, R.attr.colorTextBubbleBar, Color.BLACK));

        bAvgChart.setData(dataBarChart);
        bAvgChart.setTouchEnabled(false);
        bAvgChart.setFitBars(true); //Makes an error so that the bar chart is jumping to the left at reload -> do not use
        bAvgChart.setVisibleXRangeMinimum(dataBar.size());
        bAvgChart.invalidate();
        //bAvgChart.setVisibleXRangeMaximum(1);
        bAvgChart.moveViewToX(1);
        bxAxis.setGranularity(1f);
        bAvgChart.getXAxis().setAxisMaximum(0f + 0.5f);
        bAvgChart.getXAxis().setAxisMinimum(0f - 0.5f);

        bAvgChart.getDescription().setEnabled(false);
        bAvgChart.getLegend().setEnabled(false);
        bAvgChart.setScaleEnabled(false);
        bAvgChart.setDrawValueAboveBar(false);
        bAvgChart.setExtraOffsets(0, 0, 0, 30); //X-Axis Labels are drawn correctly

    }

}
