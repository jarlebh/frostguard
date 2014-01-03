package org.frostguard;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FrostGuardActivity extends ListActivity {
	private LayoutInflater mInflater;
	private Vector<TempDate> data;
	private TemperatureDateDAO tempDAO;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tempDAO = new TemperatureDateDAO(this);
		setContentView(R.layout.main);
		mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		data = new Vector<TempDate>();		
		TempDateAdapter adapter = new TempDateAdapter(this, R.layout.list_item,
				R.id.temp, data);
		setListAdapter(adapter);
		getListView().setTextFilterEnabled(true);
		
		runOnUiThread(new Runnable() {
		     public void run() {
		    	 tempDAO.open();
		    	((TextView)findViewById(R.id.CreditsTextView)).setText(YrDataService.getCredits());
		    	FrostGuardActivity.this.data.clear();
		    	FrostGuardActivity.this.data.addAll(tempDAO.getAllTemps());
				((BaseAdapter) getListAdapter()).notifyDataSetChanged();
				tempDAO.close();
		    }
		});
	}

	

	private class TempDateAdapter extends ArrayAdapter<TempDate> {

		public TempDateAdapter(Context context, int resource,
				int textViewResourceId, List<TempDate> objects) {
			super(context, resource, textViewResourceId, objects);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			// widgets displayed by each item in your list
			TextView item = null;
			TextView description = null;

			// data from your adapter
			TempDate rowData = getItem(position);

			// we want to reuse already constructed row views...
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			//
			holder = (ViewHolder) convertView.getTag();
			item = holder.getTemperature();
			item.setText(rowData.getTemperature().toString());

			description = holder.getTime();
			description.setText(FrostGuardHelpers.formatDate(rowData.getFromTime()));
			if (rowData.getTemperature() < 0) {
				description.setBackgroundResource(R.color.red);
			} else if (rowData.getTemperature() < 2) {
				description.setBackgroundResource(R.color.yellow);
			}
			description.setTextColor(Color.BLACK);
			return convertView;
		}
	}

	/**
	 * Wrapper for row data.
	 * 
	 */
	private class ViewHolder {
		private View mRow;
		private TextView description = null;
		private TextView item = null;

		public ViewHolder(View row) {
			mRow = row;
		}

		public TextView getTime() {
			if (null == description) {
				description = (TextView) mRow.findViewById(R.id.time);
			}
			
			return description;
		}

		public TextView getTemperature() {
			if (null == item) {
				item = (TextView) mRow.findViewById(R.id.temp);
			}
			
			return item;
		}
	}
}
