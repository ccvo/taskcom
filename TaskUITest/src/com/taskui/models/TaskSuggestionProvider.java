package com.taskui.models;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class TaskSuggestionProvider extends ContentProvider {

	public static String AUTHORITY = "com.taskui.models.TaskSuggestionProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/task");
	public static final String TASKS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.android.taskdict";
	public static final String KEYWORDS_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.android.taskdict";

	private TaskDatabase mDictionary;

	private static final int SEARCH_TASKS = 0;
	private static final int GET_TASK = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static final int REFRESH_SHORTCUT = 3;
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "task", SEARCH_TASKS);
		matcher.addURI(AUTHORITY, "task/#", GET_TASK);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
		return matcher;
	}

	@Override
	public boolean onCreate() {
		mDictionary = new TaskDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// Use the UriMatcher to see what kind of query we have and format the db query accordingly
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			if (selectionArgs == null) {
				throw new IllegalArgumentException("selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		case SEARCH_TASKS:
			if (selectionArgs == null) {
				return search(null);
			}else{
				return search(selectionArgs[0]);
			}
		case GET_TASK:
			return getTask(uri);
		case REFRESH_SHORTCUT:
			return refreshShortcut(uri);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	private Cursor getSuggestions(String query) {
		if(query != null){
			query = query.toLowerCase();
		}
		String[] columns = new String[] { BaseColumns._ID, TaskDatabase.TASK_ID, TaskDatabase.TASK_TITLE, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
		return mDictionary.getTaskMatches(query, columns);
	}

	private Cursor search(String query) {
		if(query != null){
			query = query.toLowerCase();
		}
		String[] columns = new String[] { BaseColumns._ID, TaskDatabase.TASK_ID, TaskDatabase.TASK_TITLE };

		return mDictionary.getTaskMatches(query, columns);
	}

	private Cursor getTask(Uri uri) {
		String rowId = uri.getLastPathSegment();
		String[] columns = new String[] { TaskDatabase.TASK_ID, TaskDatabase.TASK_TITLE };

		return mDictionary.getTask(rowId, columns);
	}

	private Cursor refreshShortcut(Uri uri) {
		String rowId = uri.getLastPathSegment();
		String[] columns = new String[] { BaseColumns._ID, TaskDatabase.TASK_ID, TaskDatabase.TASK_TITLE, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
		return mDictionary.getTask(rowId, columns);
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case SEARCH_TASKS:
			return TASKS_MIME_TYPE;
		case GET_TASK:
			return KEYWORDS_MIME_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		case REFRESH_SHORTCUT:
			return SearchManager.SHORTCUT_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}
