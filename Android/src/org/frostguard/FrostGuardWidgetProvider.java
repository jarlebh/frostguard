package org.frostguard;

import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class FrostGuardWidgetProvider extends AppWidgetProvider {
	TemperatureDateDAO db = null;
	public FrostGuardWidgetProvider() {
	}
	@Override
	public void onUpdate(final Context context,final AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		db = new TemperatureDateDAO(context);
		startService(context);
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            final int appWidgetId = appWidgetIds[i];
            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, FrostGuardActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.frostguard_appwidget);
            views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
            
            Log.i("Widget","onPost Called");
            db.open();
			final List<TempDate> datalist = db.getAllTemps();
			db.close();
			int lowest = FrostGuardHelpers.getLowestTemp(datalist).getTemperature();
			views.setTextViewText(R.id.widgetButton, Integer.toString(lowest));
			if (lowest < 0) {
				views.setTextColor(R.id.widgetButton, context.getResources().getColor(R.color.red));
			} else if (lowest < 3) {
				views.setTextColor(R.id.widgetButton, context.getResources().getColor(R.color.yellow));
			}
			Log.i("Widget","Set "+Integer.toString(lowest)+" on "+views);
			appWidgetManager.updateAppWidget(appWidgetId, views);
        }
     
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
	private void startService(final Context context) {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		// add 5 minutes to the calendar object
//		cal.add(Calendar.MINUTE, 1);
		
		Intent intent = new Intent(context, YrDataService.class);
		PendingIntent serviceIntent = PendingIntent.getService(context, 0, intent, 0);
		 // Get the AlarmManager service
		 AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		 am.setInexactRepeating(AlarmManager.RTC, cal.getTimeInMillis(),1000*60, serviceIntent);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

}
