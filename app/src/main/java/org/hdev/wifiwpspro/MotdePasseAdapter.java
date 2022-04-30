package org.hdev.wifiwpspro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MotdePasseAdapter extends ArrayAdapter {
    Activity context;
    ArrayList<MotdePasse> pwdList;
    Typeface typeface;

    public MotdePasseAdapter(Activity context, ArrayList<MotdePasse> pwdList) {
        super(context, R.layout.listitem_tit, pwdList);
        this.context = context;
        this.pwdList = pwdList;
        this.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto.otf");
    }

    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = context.getLayoutInflater().inflate(R.layout.listitem_mot_de_passe, null);
        ((TextView) item.findViewById(R.id.NetworkName)).setText(( pwdList.get(position)).getNom_reseau());
        TextView lblPWD = item.findViewById(R.id.LblNetworkPassword);
        lblPWD.setTypeface(typeface);
        lblPWD.setText((pwdList.get(position)).getMo_depasse_net());
        return item;
    }
}