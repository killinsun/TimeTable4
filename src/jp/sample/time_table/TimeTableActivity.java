package jp.sample.time_table;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.sample.db_helper.TimeTableSqlHelper;
import jp.sample.sns_sdk.ReceiveException;
import jp.sample.sns_sdk.SnsReceiver;
import jp.sample.time_table_info.TimeTableInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
//デバッグ用key

public class TimeTableActivity extends Activity implements OnClickListener, OnGroupClickListener, OnItemLongClickListener, android.content.DialogInterface.OnClickListener, OnCancelListener {
	/** デバッグ用タグ*/
	private static final String TAG = "TimeTableActivity";
	private final static int REQUES_TTIME_TABLE_EDIT=1;
	private final static int REQUES_TTIME_TABLE_LIST=2;
	public static boolean endFlag;

	/** Button 新規登録ボタン用 */
	private Button addBtn;
	/** Button 時間割確認ボタン用 */
	//	private Button listBtn;

	private static String UID = "test user";


	/*-----僕らの追加要素たち-----*/

	//曜日ボタンと日付テキスト
	private Button monButton, tueButton, wedButton, thuButton, friButton, satButton, sunButton;
	private TextView monD, tueD, wedD, thuD, friD, satD, sunD;
	//	private Button optButton;
	private Button visibleButton, invisibleButton;
	private Button dbClearButton;

	//リストと現在時間とインテント
	private ExpandableListView dayList;
	private TextView currentWeekDayView;
	private TextView dayNowTextView;
	private Intent intent;

	//データ関連
	private TimeTableSqlHelper dbHelper;
	private SQLiteDatabase db;
	private SharedPreferences getPreference;

	//ExpandList用
	private String currentWeekDay;
	private int limit;
	private static final String[] weekDayTrue = {"月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日"};
	private static final String[] timeTrue = { "0時限目", "1時限目", "2時限目", "3時限目", "4時限目", "5時限目", "6時限目", "7時限目" };

	//ExpandListに表示する用
	private String[] title;
	private String[] todo;
	private String[] type;
	private String[] time_table;

	//現在日時取得用(util.Date)
	private Time time;
	private String nowDate;


	//ExpandList長押し用ダイアログの項目リスト
	private final CharSequence[] adbList = {"編集", "削除"};
	//ダイアログを出した（長押しした）項目番目
	private int clickedItemNumber;

	//バックボタンを押したときの確認用(trueの状態で押すと終了)
	boolean backButtonFirstFlag = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		endFlag = false;

		//新規登録ボタンのインスタンス生成
		addBtn = (Button) findViewById(R.id.newButton);

		//新規登録ボタンをクリックリスナーに登録
		addBtn.setOnClickListener(this);

		//時間割確認ボタンのインスタンス生成
		//		listBtn = (Button) findViewById(R.id.listButton);

		//時間割確認ボタンのインスタンス生成
		//		listBtn.setOnClickListener(this);


		/*-------------------ここからあいやのターン-------------------*/

		//DB準備
		dbHelper = new TimeTableSqlHelper(this);
		db = dbHelper.getWritableDatabase();

		//現在日にち表示の準備
		dayNowTextView = (TextView)findViewById(R.id.dayNowText);

		//現在曜日表示の準備
		currentWeekDayView = (TextView)findViewById(R.id.currentWeekDayView);

		time = new Time("Asia/Tokyo");
		time.setToNow();
		nowDate = time.year + "/" +
				(time.month + 1) + "/" +
				time.monthDay + "(" +
				weekDayTrue[time.weekDay-1].substring(0,1) + ")";	//配列にあわせるために-1
		dayNowTextView.setText(nowDate);

		currentWeekDay = weekDayTrue[time.weekDay-1];

		String[] weekDays = new String[7];
		switch (time.weekDay) {
		case 1:
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay+1);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay+2);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay+3);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay+4);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay+5);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay+6);
			break;

		case 2:
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay+1);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay+2);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay+3);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay+4);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay+5);
			break;

		case 3:
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay-2);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay+1);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay+2);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay+3);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay+4);
			break;

		case 4:
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay-3);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay-2);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay+1);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay+2);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay+3);
			break;

		case 5:
			weekDays[0] = " " + (time.month + 1) + "/" +
					(time.monthDay-4);
			weekDays[1] = " " + (time.month + 1) + "/" +
					(time.monthDay-3);
			weekDays[2] = " " + (time.month + 1) + "/" +
					(time.monthDay-2);
			weekDays[3] = " " + (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[4] = " " + (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[5] = " " + (time.month + 1) + "/" +
					(time.monthDay+1);
			weekDays[6] = " " + (time.month + 1) + "/" +
					(time.monthDay+2);
			break;

		case 6:
			//			Log.d("debug", String.valueOf(time.monthDay));
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay-5);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay-4);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay-3);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay-2);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay+1);
			break;

		case 7:
			weekDays[0] = (time.month + 1) + "/" +
					(time.monthDay-6);
			weekDays[1] = (time.month + 1) + "/" +
					(time.monthDay-5);
			weekDays[2] = (time.month + 1) + "/" +
					(time.monthDay-4);
			weekDays[3] = (time.month + 1) + "/" +
					(time.monthDay-3);
			weekDays[4] = (time.month + 1) + "/" +
					(time.monthDay-2);
			weekDays[5] = (time.month + 1) + "/" +
					(time.monthDay-1);
			weekDays[6] = (time.month + 1) + "/" +
					(time.monthDay);
			break;

		default:
			Log.d("debug", "settingWeedDays is error");
			break;
		}

		//曜日ボタン処理
		monButton = (Button)findViewById(R.id.monButton);
		monD = (TextView)findViewById(R.id.monDay);
		monD.setText(weekDays[0]);
		monButton.setOnClickListener(this);
		tueButton = (Button)findViewById(R.id.tueButton);
		tueD = (TextView)findViewById(R.id.tueDay);
		tueD.setText(weekDays[1]);
		tueButton.setOnClickListener(this);
		wedButton = (Button)findViewById(R.id.wedButton);
		wedD = (TextView)findViewById(R.id.wedDay);
		wedD.setText(weekDays[2]);
		wedButton.setOnClickListener(this);
		thuButton = (Button)findViewById(R.id.thuButton);
		thuD = (TextView)findViewById(R.id.thuDay);
		thuD.setText(weekDays[3]);
		thuButton.setOnClickListener(this);
		friButton = (Button)findViewById(R.id.friButton);
		friD = (TextView)findViewById(R.id.friDay);
		friD.setText(weekDays[4]);
		friButton.setOnClickListener(this);
		satButton = (Button)findViewById(R.id.satButton);
		satD = (TextView)findViewById(R.id.satDay);
		satD.setText(weekDays[5]);
		satButton.setOnClickListener(this);
		sunButton = (Button)findViewById(R.id.sunButton);
		sunD = (TextView)findViewById(R.id.sunDay);
		sunD.setText(weekDays[6]);
		sunButton.setOnClickListener(this);
		monButton.setBackgroundColor(Color.WHITE);
		tueButton.setBackgroundColor(Color.WHITE);
		wedButton.setBackgroundColor(Color.WHITE);
		thuButton.setBackgroundColor(Color.WHITE);
		friButton.setBackgroundColor(Color.WHITE);
		satButton.setBackgroundColor(Color.WHITE);
		sunButton.setBackgroundColor(Color.WHITE);

		//オプションボタン処理
		//		optButton = (Button)findViewById(R.id.optButton);
		//		optButton.setOnClickListener(this);

		//可視化、不可視化関連
		visibleButton = (Button)findViewById(R.id.visibleButton);
		visibleButton.setOnClickListener(this);
		invisibleButton = (Button)findViewById(R.id.invisibleButton);
		invisibleButton.setOnClickListener(this);

		dbClearButton = (Button)findViewById(R.id.dbClearButton);
		dbClearButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				File file = new File("/data/data/jp.sample.time_table/databases/time_table.db");
				file.delete();
				dbHelper = new TimeTableSqlHelper(TimeTableActivity.this);
				db = dbHelper.getWritableDatabase();
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
			}
		});

		//ExpandList連携
		dayList = (ExpandableListView)findViewById(R.id.dayExpandList);
		dayList.setOnGroupClickListener(this);
		dayList.setOnItemLongClickListener(this);

		onClick(new View(this));
	}

	@Override
	public void onClick(DialogInterface paramDialogInterface, int paramInt) {
		// TODO 自動生成されたメソッド・スタブ
		if(adbList[paramInt].equals("編集")){
			Log.d("debug", adbList[paramInt].toString() + "\n" + paramInt);
		}else if(adbList[paramInt].equals("削除")){
			//選択した場所の予定をDBから削除
			//ifExist文もつけたい
			//			System.out.println(time_table.length);
			//			for(int i=0; i<time_table.length; i++){
			//				System.out.println(i + " : " + time_table[i]);
			//			}
			//			Log.d("debug", String.valueOf(currentWeekDay));
			//			Log.d("debug", time_table[clickedItemNumber] + " : " + clickedItemNumber);
			db.execSQL("delete from " + TimeTableSqlHelper.TIME_TABLE +
					" where week = '" + currentWeekDay + "'" +
					" and time_table = '" + timeTrue[clickedItemNumber] + "'" +
					";");
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
		}else{
			Log.d("debug", adbList[paramInt] + " Button is failed");
		}
	}

	//親要素クリック時
	@Override
	public boolean onGroupClick(ExpandableListView arg0, View arg1, int number, long arg3) {
		// TODO 自動生成されたメソッド・スタブ
		boolean existFlag = false;
		//createExpandListメソッドで残った配列が上書きされていないことを
		//利用して、総マッチングをかける
		for(int i=0; i<time_table.length; i++){
			if(timeTrue[number].equals(time_table[i])){
				existFlag = true;
				break;
			}
		}
		if(!existFlag){
			//			Log.d("debug", "matching is suceed");
			intent = new Intent(this, TimeTableEditActivity.class);
			startActivity(intent);
		}
		return false;
	}

	//親要素（多分子要素も）長押しクリック時
	@Override
	public boolean onItemLongClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		// TODO 自動生成されたメソッド・スタブ
		//		Log.d("debug", "onItemLongClick");
		//ダイアログ要素追加
		//		Log.d("debug", String.valueOf(paramInt) + " : " + String.valueOf(paramLong));
		clickedItemNumber = paramInt;
		AlertDialog.Builder adbuilder = new AlertDialog.Builder(this);
		adbuilder.setTitle("Manu");
		adbuilder.setItems(adbList,this);
		//AlertDialog表示
		adbuilder.show();

		return false;
	}


	public void onActivityResult(int requestCode, int resultCode, Intent intent){
		if(requestCode == REQUES_TTIME_TABLE_EDIT && resultCode == RESULT_OK){
			//同期終了時にアラート
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setPositiveButton("OK", null);
			// アラートダイアログのメッセージを設定します
			alertDialogBuilder.setMessage("スケジュールの登録が完了しました。");
			AlertDialog alerSyncFalseDialog = alertDialogBuilder.create();
			// アラートダイアログを表示します
			alerSyncFalseDialog.show();
		}

		Log.d(TAG,"onActivityResult");
	}

	//通常ボタンクリック時
	public void onClick(View v) {

		//いったん全てのボタンを白に戻す
		//関係ないボタンを押すと全てが白のままになってしまうが、現状では他は画面遷移するので問題ないと判断
		monButton.setBackgroundColor(Color.WHITE);
		tueButton.setBackgroundColor(Color.WHITE);
		wedButton.setBackgroundColor(Color.WHITE);
		thuButton.setBackgroundColor(Color.WHITE);
		friButton.setBackgroundColor(Color.WHITE);
		satButton.setBackgroundColor(Color.WHITE);
		sunButton.setBackgroundColor(Color.WHITE);

		//		Log.d("debug", String.valueOf(v.getId()));
		switch (v.getId()) {
		//新規作成のボタンをクリック
		case R.id.newButton:
			Log.d(TAG,"新規登録ボタンがクリックされました"); //デバッグ用(LogCatに表示)
			intent = new Intent(TimeTableActivity.this, TimeTableEditActivity.class);
			startActivityForResult(intent,REQUES_TTIME_TABLE_EDIT);
			break;

			//時間割確認のボタンをクリック
			//		case R.id.listButton:
			//			Log.d(TAG,"時間割確認ボタンがクリックされました"); //デバッグ用(LogCatに表示)
			//			intent = new Intent(TimeTableActivity.this,TimeTableListActivity.class);
			//			startActivityForResult(intent,REQUES_TTIME_TABLE_LIST);
			//			break;

		case R.id.monButton:
			currentWeekDay = "月曜日";
			monButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.tueButton:
			currentWeekDay = "火曜日";
			tueButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.wedButton:
			currentWeekDay = "水曜日";
			wedButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.thuButton:
			currentWeekDay = "木曜日";
			thuButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.friButton:
			currentWeekDay = "金曜日";
			friButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.satButton:
			currentWeekDay = "土曜日";
			satButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

		case R.id.sunButton:
			currentWeekDay = "日曜日";
			sunButton.setBackgroundColor(Color.BLUE);
			setCurremtDb();
			createExpandList(title, todo, type, time_table);
			break;

			//		case R.id.optButton:
			//			intent = new Intent(this, PrefsActivity.class);
			//			startActivity(intent);
			//			break;

		case R.id.visibleButton:
			dayList.setVisibility(View.VISIBLE);
			break;

		case R.id.invisibleButton:
			dayList.setVisibility(View.INVISIBLE);
			break;

			//おそらく初期値が-1なので、どのボタンでもないとここに入る、
		case -1:						//それを利用してnew View(this)での強制イベントをここに分岐させている（本来は特別なIDを発行したほうがバグには強いかもしれない）
			switch (time.weekDay - 1) {		//他でも配列用に-1しているのでわかりやすく-1している
			case 0:
				monButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 1:
				tueButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 2:
				wedButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 3:
				thuButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 4:
				friButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 5:
				satButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			case 6:
				sunButton.setBackgroundColor(Color.BLUE);
				setCurremtDb();
				createExpandList(title, todo, type, time_table);
				break;

			default:
				Log.d("error", "Unknown WeekDay = " + time.weekDay);
				break;
			}
			break;

		default:
			Log.d("error", "UnknownButton clicked = " + v.getId());
			break;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu){
		Log.d(TAG,"onCraeteOptionMenu");

		menu.add(Menu.NONE , Menu.FIRST, Menu.NONE,"データ受信");

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
		case Menu.FIRST:
			//データを受信
			SnsReceiver receiver = new SnsReceiver();
			List<TimeTableInfo> list;
			try {
				list = receiver.receive(UID);
			} catch (ReceiveException e) {
				e.printStackTrace();
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				return false;
			}

			//受信したデータを１レコードごとデータベースに追加
			TimeTableSqlHelper h = new TimeTableSqlHelper(this);
			for (TimeTableInfo info : list) {
				h.insert(info);
			}
			h.close();
			Toast.makeText(this, "データを受信しました。", Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
	}



	/*-----ここから全てあいやのターン-----*/


	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();

		//他の画面でバックボタン長押しが発生して戻ってきた場合強制終了
		if(endFlag){
			Toast.makeText(this, "アプリを強制終了します。", Toast.LENGTH_SHORT ).show();
			finish();
		}

		backButtonFirstFlag = false;

		//xmlから設定したリミットを読み込む
		getPreference = getSharedPreferences("PrefsActivity", MODE_PRIVATE);
		limit = getPreference.getInt("listLimit", 7);

		//ここで現在日付の曜日をcurrentWeekDayに設定

		//（setCurrentDbに使うため）

		setCurremtDb();
		createExpandList(title, todo, type, time_table);
	}

	private void setCurremtDb() {
		// TODO 自動生成されたメソッド・スタブ
		//押された登録日付をビューに表示
		//    	currentWeekDayView.setText(currentWeekDay);

		//title, todo, type, time_tableのクエリを発行する
		String sql = "select title, todo, type, time_table from " + TimeTableSqlHelper.TIME_TABLE +
				" where week = '" + currentWeekDay + "'" +
				" order by title ;";
		Cursor cursor = db.rawQuery(sql, null);
		title = new String[cursor.getCount()];
		todo = new String[cursor.getCount()];
		type = new String[cursor.getCount()];
		time_table = new String[cursor.getCount() + 1];
		cursor.moveToFirst();
		for(int i=0; i<cursor.getCount(); i++){
			title[i] = cursor.getString(0);
			todo[i] = cursor.getString(1);
			type[i] = cursor.getString(2);
			time_table[i] = cursor.getString(3);
			cursor.moveToNext();
		}
		for(int i=0; i<time_table.length-1; i++){
			/*	以下primary key(week, time_table)を使うことで不要になった処理
			//title   |  week  | time_table
			//test1   | 月曜日 | 1限目
			//test1-1 | 月曜日 | 1限目
			//のようになっているとcreateExpandListのnulljugdがおかしくなってしまうことの対処
//			Log.d("debug", time_table[i] + " : " + time_table[i+1] + " = " + (time_table[i].equals(time_table[i+1])));
//			if(time_table[i].equals(time_table[i+1])){
//				String distDeleteSql = "delete from " + TimeTableSqlHelper.TIME_TABLE +
//								" where title = '" + title[i] + "'" +
//								" and week = '" + currentWeekDay + "'" +
//								" and time_table = '" + time_table[i] + "'" +
//								" and todo = '" + todo[i] + "'" +
//								" and type = '" + type[i] + "'" +
//								";";
//				db.execSQL(distDeleteSql);
//			}
			 */
		}

		//createExpandList最初の分岐判定での配列オーバー対策、元からnullになっているのかもしれないが、書いておく
		time_table[time_table.length-1] = null;
	}

	//weekは現在0("月曜日")を考慮した状態、後で追加する
	private void createExpandList(String[] title, String[] todo, String[] type, String[] time_table) {
		// TODO 自動生成されたメソッド・スタブ

		String[] parentArray = { "0限目", "1限目", "2限目", "3限目", "4限目", "5限目", "6限目", "7限目" };
		//		String[] childArray = new String[todo.length];

		List<Map<String, Object>> groupData = new ArrayList<Map<String,Object>>();
		List<List<Map<String, Object>>> childData = new ArrayList<List<Map<String,Object>>>();

		String item;
		Map<String, Object> group;
		int itemsPointer = 0;
		boolean nullJudg;	//現在の時限に情報があるかの判定用
		String[] childArray;
		for(int i=0; i<=limit; i++){
			//何回か同じ判定をするので、まとめて行う
			//判定内容はこの時限に予定があるかないか
			nullJudg = timeTrue[i].equals(time_table[itemsPointer]);

			//予定の入っている（行のある）時限のみ各項目を設定する
			if(nullJudg){
				childArray = new String[4];
				childArray[0] = "タイトル : " + title[itemsPointer];
				childArray[1] = "場所 : " + "DBにまだないよ";
				childArray[2] = "種類 : " + type[itemsPointer];
				childArray[3] = "備考 : " + todo[itemsPointer];		//nullでも大丈夫だった
				//アイテムポインタは下でインクリメントするので、ここではしない
			}else{
				childArray = new String[0];
				//				childArray[0] = "タイトル : null";
				//				childArray[1] = "場所 : null";
				//				childArray[2] = "種類 : null";
				//				childArray[3] = "備考 : null";
			}

			//親リスト作成
			group = new HashMap<String, Object>();
			item = parentArray[i];

			//  i限目 != titleの中のi限目のタイトルであることに注意
			if(nullJudg){
				group.put("PTag", item + "  " + title[itemsPointer]);
				group.put("appendInfo", "[場所]   " + "まだないよ！");
				itemsPointer++;
			}else{
				group.put("PTag", item + " +");
			}
			groupData.add(group);


			//子リスト作成
			List<Map<String, Object>> childList = new ArrayList<Map<String,Object>>();
			Map<String, Object> childHash;
			for(String childItem : childArray){
				childHash = new HashMap<String, Object>();
				childHash.put("PTag", item);
				childHash.put("CTag", childItem);
				childList.add(childHash);
			}
			childData.add(childList);
		}

		//上で作ったものをひとつに合体
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this,
				groupData,
				android.R.layout.simple_expandable_list_item_2,
				new String[]{"PTag", "appendInfo"},
				new int[]{android.R.id.text1, android.R.id.text2},
				childData,
				R.layout.raw,
				new String[]{"CTag"},
				new int[]{R.id.child_text}
				);
		//セット
		dayList.setAdapter(adapter);
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(this, "アプリを強制終了します。", Toast.LENGTH_SHORT ).show();
			TimeTableActivity.endFlag = true;
			finish();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public void onCancel() {
		// TODO 自動生成されたメソッド・スタブ
		Log.d("debug", "keyPressed");
		if(!backButtonFirstFlag){
			Toast.makeText(this, "アプリを終了しますか？", Toast.LENGTH_LONG).show();
		}else if(backButtonFirstFlag){
			finish();
		}


	}



}