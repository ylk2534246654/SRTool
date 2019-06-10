package com.yx.srtool.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yx.srtool.Activity.MainActivity;
import com.yx.srtool.R;

import java.io.File;
import java.util.List;

public class GalaxyAdapter extends RecyclerView.Adapter<GalaxyAdapter.eiewHolder>
{
	private Context mContext;
	private List<Bean> mDatas;
	public GalaxyAdapter(Context context, List<Bean> datas){
		mContext=context;
		mDatas=datas;
	}

	/**
	 *
	 * @param p1
	 * @param p2
	 * @return
	 */
	@Override
	public eiewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		View item=LayoutInflater.from(p1.getContext()).inflate(R.layout.item_ship,p1,false);
		eiewHolder viewHolder=new eiewHolder(item);
		return viewHolder;
	}
	/**
	 * 设置菜单事件
	 * @param p1
	 * @param p2
	 */
	@Override
	public void onBindViewHolder(final eiewHolder p1, final int p2){/**p2位索引**/
		final Bean bean = mDatas.get(p2);
		if(bean.gettitle()!=null && !bean.gettitle().equals("") && bean.getid()!=null && !bean.getid().equals("")){
			p1.ship_name.setText(bean.gettitle());
			p1.ship_id.setText(bean.getid());
			p1.ship_image.setImageResource(R.drawable.image_ship);
			p1.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {//item 点击事件
					onClick.onClick(bean.gettitle(),bean.getid());
				}
			});
			p1.ship_delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {//删除
					new AlertDialog.Builder(mContext)
							.setTitle("删除")
							.setMessage("是否删除"+bean.gettitle())
							.setPositiveButton("我手滑了", null)
							.setNegativeButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									File file=new File(MainActivity.path+"galaxy/"+bean.gettitle());
									file.delete();
									MainActivity.newgalaxy();
								}
							}).show();
				}
			});
		}
	}

	/**
	 * 设置索引数量
	 * @return
	 */
	@Override
	public int getItemCount()
	{
		// TODO: Implement this method
		return mDatas==null?0:mDatas.size();
	}
	/**
	 * ---
	 */
	public static class eiewHolder extends ViewHolder {
		ImageView ship_image;
		TextView ship_name,ship_id;
		ImageButton ship_delete;
		public eiewHolder(View itemView) {
			super(itemView);
			ship_image=(ImageView)itemView.findViewById(R.id.ship_image);
			ship_name=(TextView)itemView.findViewById(R.id.ship_name);
			ship_id=(TextView)itemView.findViewById(R.id.ship_id);
			ship_delete=(ImageButton)itemView.findViewById(R.id.ship_delete);
		}
	}

	private GalaxyAdapter.onHistoryClick onClick;
	//onclick
	public void setOnitemClickLintener(GalaxyAdapter.onHistoryClick onitemClick)
	{
		this.onClick = onitemClick;
	}
	public interface onHistoryClick
	{
		void onClick(String title,String position);
	}
}
