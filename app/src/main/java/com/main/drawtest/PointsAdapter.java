package com.main.drawtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class PointsAdapter extends BaseAdapter {

    private List<DimensionPoint> points;
    private Context context;

    public PointsAdapter(List<DimensionPoint> points, Context context) {
        this.points = points;
        this.context = context;
    }

    @Override
    public int getCount() {
        return points == null ? 0 : points.size();
    }

    @Override
    public Object getItem(int i) {
        return points == null ? null : points.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = View.inflate(context, R.layout.item_point, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.pointNum.setText("标注点" + (i + 1));
        viewHolder.leftTop.setText("LeftTop[" + points.get(i).getLeftTopX() + "," + points.get(i).getLeftTopY() + "]");
        viewHolder.rightBottom.setText("RightBottom[" + points.get(i).getRightBottomX() + "," + points.get(i).getRightBottomY() + "]");
        return view;
    }

    public class ViewHolder {
        public TextView pointNum, leftTop, rightBottom;

        public ViewHolder(View view) {
            this.pointNum = view.findViewById(R.id.tv_point_num);
            this.leftTop = view.findViewById(R.id.tv_left_top);
            this.rightBottom = view.findViewById(R.id.tv_right_bottom);
        }
    }
}
