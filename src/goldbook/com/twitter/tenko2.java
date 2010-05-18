package goldbook.com.twitter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class tenko2 extends Activity implements LocationListener {
	private static final String NULLPO = "ぬるぽいんと（恐らくGPS情報の取得失敗）";
	private static final String YOHOKU = "yohoku.sqlite";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 状況から最適な位置情報サービスを選択し、最近取得した位置情報を取得
		LocationManager manager = (LocationManager) this
				.getSystemService(tenko2.LOCATION_SERVICE);
		boolean enableOnly = true;
		String bestProvider = manager.getBestProvider(new Criteria(),
				enableOnly);

		manager.requestLocationUpdates(bestProvider, 1, 1, this);
		Location location = manager.getLastKnownLocation(bestProvider);

		// ジオコーダに渡して住所を取得
		Geocoder geocoder = new Geocoder(this, Locale.JAPAN);
		Address address = null;
		try {
			List<Address> list = geocoder.getFromLocation(location
					.getLatitude(), location.getLongitude(), 5);

			address = list.get(0);
			int i = 0;
			while ((address = list.get(i++)).getLocality() == null) {

			}

			// sqlite検索クラスに渡してひとくち予報のURL末尾番号を得る
			SqlClient client = new SqlClient(this, YOHOKU);
			String subAdminArea = address.getSubAdminArea();
			if (subAdminArea == null) {
				subAdminArea = "hoge";
			}
			client.search(address.getAdminArea(), subAdminArea, address
					.getLocality());

			// ひとくち予報のURLを表示
			TextView tv = new TextView(this);
			tv.setText(client.getUrlNum());
			Linkify.addLinks(tv, Linkify.ALL);
			tv.setLinksClickable(true);

			setContentView(tv);

			// 位置取得終了
			manager.removeUpdates(this);

		}catch (CursorIndexOutOfBoundsException e){
			dispText("予報区の特定ができませんでした\n"+address.getAddressLine(1));
		} catch (NullPointerException e) {
			dispText(NULLPO);

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}
	}

	private void dispText(String text) {
		TextView tv = new TextView(this);
		tv.setText(text);
		setContentView(tv);
	}

	public void onLocationChanged(Location location) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onProviderDisabled(String s) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onProviderEnabled(String s) {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void onStatusChanged(String s, int i, Bundle bundle) {
		// TODO 自動生成されたメソッド・スタブ

	}
}