package goldbook.com.twitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SqlClient extends SQLiteOpenHelper {

	private static final String HITOKUCHI = "http://feeds.feedburner.com/hitokuchi_";
	private static final String TENKO2_SQL_CLIENT = "tenko2.SqlClient";
	private static final String AREA_SEARCH_SQL = "select url.num from yohoku, url " +
			"where yohoku.府県予報区=url.AdminArea and yohoku.一次細分区域=url.primDivArea " +
			"and 府県予報区 = ? and (区域 = ? or 区域 = ?) " +
			"and ((限定 = ? or 区域除外 not like ?) or (限定 is NULL and 区域除外 is NULL) ) limit 1";
	private Context RootContext;

	private String url;
	private String name;
	private boolean isDatabaseExist = true;

	/**
	 * @return urlNum
	 */
	public String getUrlNum() {
		return url;
	}

	public SqlClient(Context context, String name) {
		super(context, name, null, 1);
		RootContext = context;
		this.name = name;
	}

	/**
	 * @param adminArea
	 * @param SubAdminArea
	 * @param locality
	 * @throws IOException
	 */
	public void search(String adminArea, String SubAdminArea, String locality)
			throws IOException {
		SQLiteDatabase db = getReadableDatabase();

		if (!isDatabaseExist) {
			createDatabase();
			db.close();
			db = getReadableDatabase();
		}

		String[] areaNames = new String[] { adminArea, SubAdminArea, locality,
				locality, "%" + locality + "%" };
		Cursor c = db.rawQuery(AREA_SEARCH_SQL, areaNames);

		c.moveToFirst(); // 最初のカラムへ移動
		url = HITOKUCHI + c.getInt(c.getColumnIndex("num"));
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private void createDatabase() throws IOException {
		Log.d(TENKO2_SQL_CLIENT, "onCreate!");
		AssetManager manager = RootContext.getResources().getAssets();

		File pasteFile = new File("/data/data/" + RootContext.getPackageName()
				+ "/databases/", name);
		pasteFile.mkdirs();

		pasteFile.createNewFile();

		// ストリーム準備
		InputStream input = manager.open(name); // コピー元
		// ZipInputStream zipInput = new ZipInputStream(input);
		OutputStream output = new FileOutputStream(pasteFile);

		// コピー貼付け作業開始
		Log.d(TENKO2_SQL_CLIENT, "start Extract ZipFile");

		int DEFAULT_BUFFER_SIZE = 1024 * 4;
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}

		// ストリームを閉じる
		input.close();
		output.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// onCreateでデータ投入を行うと失敗したのでフラグのみ立てる
		isDatabaseExist = false;
	}
}
