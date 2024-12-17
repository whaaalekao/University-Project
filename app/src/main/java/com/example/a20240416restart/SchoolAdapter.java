package com.example.a20240416restart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SchoolAdapter extends BaseAdapter {
    private Context context;
    private List<String> schoolList;
    private LayoutInflater inflater;

    public SchoolAdapter(Context context, List<String> schoolList) {
        this.context = context;
        this.schoolList = schoolList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return schoolList.size();
    }

    @Override
    public Object getItem(int position) {
        return schoolList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.school_item, parent, false);
            holder = new ViewHolder();
            holder.schoolName = convertView.findViewById(R.id.school_item);
            holder.schoolImage = convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String school = schoolList.get(position);
        holder.schoolName.setText(school);

        switch (school) {
            case "屏商校區":
                holder.schoolImage.setImageResource(R.drawable.pinshan_restaurant);
                break;
            case "民生校區":
                holder.schoolImage.setImageResource(R.drawable.minshan_restaurant);
                break;
            case "屏師校區":
                holder.schoolImage.setImageResource(R.drawable.linshanrestauran);
                break;
            default:
                holder.schoolImage.setImageResource(R.drawable.ic_launcher_foreground);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        TextView schoolName;
        ImageView schoolImage;
    }
}
