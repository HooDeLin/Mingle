package com.orbital2015.mingle;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UserListItemAdapter extends BaseAdapter {

    Context context;
    List<UserListItem> userListItems;

    public UserListItemAdapter(Context context, List<UserListItem>userListItems){
        this.context = context;
        this.userListItems = userListItems;
    }

    @Override
    public int getCount(){
        return userListItems.size();
    }

    @Override
    public Object getItem(int position){
        return userListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return userListItems.indexOf(getItem(position));
    }

    private class ViewHolder{
        ImageView profilePic;
        TextView memberName;
        TextView status;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;

        LayoutInflater mInflator = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if(convertView == null){
            convertView = mInflator.inflate(R.layout.user_list_item, null);
            holder = new ViewHolder();

            holder.memberName = (TextView) convertView.findViewById(R.id.userListItemName);
            holder.profilePic = (ImageView) convertView.findViewById(R.id.userListItemProfilePic);
            holder.status = (TextView) convertView.findViewById(R.id.userListItemStatus);

            UserListItem rowPosition = userListItems.get(position);

            holder.profilePic.setImageBitmap(rowPosition.getProfilePic());
            holder.memberName.setText(rowPosition.getMemberName());
            holder.status.setText(rowPosition.getStatus());

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }
}
