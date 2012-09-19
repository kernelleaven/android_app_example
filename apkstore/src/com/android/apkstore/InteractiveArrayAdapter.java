package com.android.apkstore;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class InteractiveArrayAdapter extends ArrayAdapter<Model>
{
  private final Activity context;
  private final List<Model> list;

  public InteractiveArrayAdapter(Activity paramActivity, List<Model> paramList){
    super(paramActivity, R.layout.rowbuttonlayout, paramList);
    this.context = paramActivity;
    this.list = paramList;
  }

  public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
  {
    View localView=paramView;
    ViewHolder localViewHolder1 ;
    ViewHolder localViewHolder2;
    if (localView == null){
      localView = this.context.getLayoutInflater().inflate(R.layout.rowbuttonlayout, null);
      localViewHolder2 = new ViewHolder();;
      localViewHolder2.pic = ((ImageView)localView.findViewById(R.id.pic));
      localViewHolder2.text = ((TextView)localView.findViewById(R.id.label));
      localViewHolder2.checkbox = ((CheckBox)localView.findViewById(R.id.check));
      
      localViewHolder2.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
        public void onCheckedChanged(CompoundButton bt, boolean paramBoolean){
          ((Model)((CheckBox)bt).getTag()).setSelected(bt.isChecked());
        }
      });
      localView.setTag(localViewHolder2);   
      
      localViewHolder2.checkbox.setTag(this.list.get(paramInt));
      localViewHolder1 = (ViewHolder)localView.getTag();
	}else{
	      ((ViewHolder)localView.getTag()).checkbox.setTag(this.list.get(paramInt));
	      localViewHolder1 = (ViewHolder)localView.getTag();
	}
	localViewHolder1.checkbox.setChecked(((Model)this.list.get(paramInt)).isSelected());
	Drawable  ddraw=((Model)this.list.get(paramInt)).getIcon();
    localViewHolder1.pic.setImageDrawable(ddraw);
    
    
    if (!((Model)localViewHolder1.checkbox.getTag()).isSelected()){
  	  	localViewHolder1.text.setTextColor(Color.rgb(255, 255, 255));
        localViewHolder1.text.setText(((Model)this.list.get(paramInt)).getName());
        localViewHolder1.text.setTextSize(15.0F);
    }else{
    	localViewHolder1.text.setTextColor(Color.rgb(255, 255, 255));
    	localViewHolder1.text.setText(((Model)this.list.get(paramInt)).getName());
	    localViewHolder1.text.setTextSize(15.0F);
    }
    return localView;
  }

  static class ViewHolder
  {
    protected CheckBox checkbox;
    protected ImageView pic;
    protected TextView text;
  }
}