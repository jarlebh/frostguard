package org.frostguard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class YrDataService extends IntentService {
	private static final String YR_KONGSBERG = "http://www.yr.no/place/Norway/Buskerud/Kongsberg/Kongsberg/forecast_hour_by_hour.xml";
	private static String LOG_NAME = "YrDataService";
	private List<TempDate> yrData;
	private TemperatureDateDAO tempDAO;
	private static String credits;
	private static Date nextUpdate;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");
	private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    public YrDataService() {
    	super("YrDataService");
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        tempDAO = new TemperatureDateDAO(getBaseContext());
        // Display a notification about us starting.  We put an icon in the status bar.
        
        super.onCreate();
    }

   
    /**
     * Show a notification while this service is running.
     */
    private void showNotification(TempDate temp) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.app_name);

        // Set the icon, scrolling text and timestamp
        String update = temp.getTemperature()+" at "+FrostGuardHelpers.formatDate(temp.getFromTime());
		Notification notification = new Notification(R.drawable.appwidget_ice, update,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FrostGuardActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, update,
                       text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		try {
			tempDAO.open();
			if (nextUpdate == null || (System.currentTimeMillis()) > nextUpdate.getTime()) {
				String result  = callYr(YR_KONGSBERG);
				handleYrData(result);
			} else {
				Log.i(LOG_NAME, "Using existing data, next update "+nextUpdate);
				yrData = tempDAO.getAllTemps();
			}
		} catch (ClientProtocolException e) {
			Log.e(LOG_NAME, "Failed to load from YR", e);
		} catch (IOException e) {
			Log.e(LOG_NAME, "Failed to load from YR", e);
		} finally {
			tempDAO.close();
		}
	}
	private String callYr(String... uri) throws ClientProtocolException,IOException {
		String responseString = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
			response = httpclient.execute(new HttpGet(uri[0]));
			StatusLine statusLine = response.getStatusLine();

			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
				throw new IOException(statusLine.getReasonPhrase());
			}
		return responseString;
	}
	private void handleYrData(String result) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(new StringReader(
				result));
		// parse the XML as a W3C Document
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builderFactory.setNamespaceAware(true);

		try {
			yrData = new ArrayList<TempDate>();
			DocumentBuilder builder = builderFactory
					.newDocumentBuilder();
			Document document = builder.parse(inputSource);			
			String nextUpdStr = ((String) xpath.evaluate(
					"/weatherdata/meta/nextupdate", document,
					XPathConstants.STRING));
			String creditStr = ((String) xpath.evaluate(
					"/weatherdata/credit/link/@text", document,
					XPathConstants.STRING));
			credits = creditStr + " next update "+nextUpdStr;
			nextUpdate = DATE_FORMAT.parse(nextUpdStr);
			NodeList nodes = (NodeList) xpath.evaluate(
					"/weatherdata/forecast/tabular/time/temperature",
					document, XPathConstants.NODESET);
			
			yrData.clear();
			for (int i = 0; i < nodes.getLength(); i++) {
				Element tempNode = (Element) nodes.item(i);
				String value = tempNode.getAttribute("value");
				Integer temp = Integer.valueOf(value);
				if (temp < 2) {
					Element time = (Element) tempNode.getParentNode();
					String fromStr = time.getAttribute("from");
					Log.i("Frost", fromStr);
					TempDate tempDate = new TempDate(temp,
							DATE_FORMAT.parse(fromStr));
					yrData.add(tempDate);
				}
			}
			TempDate lowest = FrostGuardHelpers.getLowestTemp(yrData);
			showNotification(lowest);
			tempDAO.resetEntries(yrData);
		} catch (Exception e) {
			Log.e(LOG_NAME, "Failed to load from YR", e);
		}
		Log.i(LOG_NAME, "Downloaded new data");
	}
	public static String getCredits() {
		return credits;
	}
}