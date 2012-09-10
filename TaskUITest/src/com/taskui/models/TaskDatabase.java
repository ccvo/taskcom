package com.taskui.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.taskui.views.TaskListActivity;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class TaskDatabase {
	public static final String TASK_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String TASK_ID = "task_id";

	private static final String DATABASE_NAME = "task";
	private static final String FTS_VIRTUAL_TABLE = "FTStask";
	private static final int DATABASE_VERSION = 1;

	private final DictionaryOpenHelper mDatabaseOpenHelper;
	private static final HashMap<String, String> mColumnMap = buildColumnMap();
	private SQLiteDatabase db;

	public TaskDatabase(Context context) {
		mDatabaseOpenHelper = new DictionaryOpenHelper(context);
		db = mDatabaseOpenHelper.getWritableDatabase();
	}

	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(TASK_ID, TASK_ID);
		map.put(TASK_TITLE, TASK_TITLE);
		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		return map;
	}

	public Cursor getTask(String rowId, String[] columns) {
		String selection = "rowid = ?";
		String[] selectionArgs = new String[] { rowId };

		return query(selection, selectionArgs, columns);

		/* This builds a query that looks like:
		 *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
		 */
	}

	private ArrayList<Task> filter(String s) {
		ArrayList<Task> temp = new ArrayList<Task>();
		for (int i = TaskListActivity.getTasks().size() - 1; i >= 0; i--) {
			Task t = TaskListActivity.getTasks().get(i);
			if (t.getMatchScore(s) > 0) {
				int j = temp.size() - 1;
				while (j >= 0 && (temp.get(j).compareTo(t) < 0)) {
					j--;
				}
				temp.add(j + 1, t);
			}
		}
		return temp;
	}

	public Cursor getTaskMatches(String query, String[] columns) {
		if (query == null || query.length() == 0) {
			//updateTable(null);
			updateTable(TaskListActivity.getTasks());
		} else {
			updateTable(filter(query));
		}

		return query(null, null, columns);
	}

	private Cursor query(String selection, String[] selectionArgs, String[] columns) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);
		builder.setProjectionMap(mColumnMap);

		Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, null);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	public long addTask(Task t) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(TASK_ID, "" + t.id);
		initialValues.put(TASK_TITLE, t.title);
		initialValues.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT);
		return db.insert(FTS_VIRTUAL_TABLE, null, initialValues);
	}

	private void updateTable(List<Task> tasks) {
		db.execSQL("DELETE FROM " + FTS_VIRTUAL_TABLE + ";");
		for (int i = tasks.size() - 1; i >= 0; i--) {
			addTask(tasks.get(i));
		}
	}

	private static class DictionaryOpenHelper extends SQLiteOpenHelper {

		private SQLiteDatabase mDatabase;
		
		private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " USING fts3 (" + TASK_ID + ", " + TASK_TITLE + ", " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID + ");";

		private DictionaryOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			mDatabase.execSQL(FTS_TABLE_CREATE);
		}

		/** Add a task to the dictionary.
		 * 
		 * @return rowId or -1 if failed */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
		}
	}
}
