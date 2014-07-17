package *****.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import ***********

public class Databases {
	
	public enum ColumnType {
		INTEGER,
		TEXT,
	}

	public static interface IColumn {
		public abstract String getName();
		public abstract ColumnType getColumnType();
		public abstract Table getTable();
		public abstract String fullname();
	}

	public static interface ContentValuesEater {

		/**
		 * 
		 * @param values
		 * @return true to continue eating, false for a drink
		 */
		boolean eat(ContentValues values);

	}

	public static abstract class ContentValuesProducer {

		/**		 
		 * @return null if no more values
		 */
		public abstract ContentValues contentValuesProduce();
		
		/**
		 *  Callback after produced values were inserted
		 *  
		 * @param rowID ID of inserted row
		 * @param values Values
		 */
		public void burp(long rowID, ContentValues values){
		}
	}
	
	public enum Table {
		AAAA(aaaa.Column.values()),
		AAAB(aaab.Column.values()),
		AAABITEM(aaabItem.Column.values()),
		AAAC(AAAC.Column.values()),
		AAAD(aaaasProgrammed.Column.values()),
		AAAF(AAAF.Column.values()),
		FAVORITES(Favorites.Column.values()),
		AAAG(AAAG.Column.values()),
		AAAH(AAAH.Column.values()),
		AAAJ(AAAJ.Column.values()),
		CATEGORIES(Category.Column.values()),
		METRICS(Metric.Column.values()),
		aaaa_SUBITEMS(aaaaSubitem.Column.values()),
		aaaa_SUBMETRICS(aaaaSubmetric.Column.values()),
		;
		private String name;
		private IColumn[] columns;

		private Table(IColumn[] columns) {
			this.columns = columns;
		}

		public String getName() {
			if(name==null) name = toString().toLowerCase(Locale.getDefault());
			return name;
		}

		public IColumn[] getColumns() {
			return columns;
		}
	}

	private static String[] IColumnsToStrings(IColumn[] columns) {
		if (columns == null) return null;
		String[] sColumns = new String[columns.length];
		for (int i = 0; i < sColumns.length; i++) sColumns[i] = columns[i].getName();
		return sColumns;
	}

	public static void getAndEat(Table table, IColumn[] columns, ContentValuesEater eater, String whereClause, String[] whereArgs, String groupBy, String having, String orderBy, String limit) {
		synchronized (table) {
			String[] sColumns = IColumnsToStrings(columns);
			SQLiteDatabase db = DatabaseHelper.openDatabase();
			Cursor cur = db.query(table.getName(), sColumns, whereClause, whereArgs, groupBy, having, orderBy, limit);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				ContentValues values = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, values);
				if (!eater.eat(values)) break;
				cur.moveToNext();
			}
			DatabaseHelper.tryCloseDatabase();
		}
	}

	public static long insert(Table table, ContentValues contentValues)
	{
		long rowID;
		synchronized (table) {
			SQLiteDatabase db = DatabaseHelper.openDatabase();
			rowID = db.insert(table.getName(), null, contentValues);
			DatabaseHelper.tryCloseDatabase();
		}
		return rowID;
	}
	
	public static int update(Table table, ContentValues contentValues, String whereClause, String[] whereArgs) {
		int updatedCount;
		synchronized (table) {
			SQLiteDatabase db = DatabaseHelper.openDatabase();
			updatedCount = db.update(table.getName(), contentValues, whereClause, whereArgs);
			DatabaseHelper.tryCloseDatabase();
		}
		return updatedCount;
	}
	
	public static void insert(Table table, ContentValuesProducer producer)
	{
		synchronized (table) {
			SQLiteDatabase db = DatabaseHelper.openDatabase();
			db.beginTransaction();
			ContentValues cVal;
			while (true)
			{
				cVal = producer.contentValuesProduce();
				if (cVal == null) break;
				long rowID = db.insert(table.getName(), null, cVal);
				producer.burp(rowID, cVal);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			DatabaseHelper.tryCloseDatabase();
		}
	}
	
	
	public static int remove(Table table, String whereClause, String[] whereArgs)
	{
		int rowCount;
		synchronized (table) {
			SQLiteDatabase db = DatabaseHelper.openDatabase();
			rowCount = db.delete(table.getName(), whereClause, whereArgs);
			DatabaseHelper.tryCloseDatabase();
		}
		return rowCount;
	}
	
	public static int clear(Table table)
	{
		int rowCount = remove(table, null, null);		
		return rowCount;	
		
	}
	
	public static void eraseArchive(){
    	Databases.clear(Table.AAAB);
    	Databases.clear(Table.AAABITEM);
    	Databases.clear(Table.AAAC);
    	//cashless payments
    	Databases.clear(Table.AAAH);
    	Databases.clear(Table.AAAG);
    	Databases.clear(Table.AAAJ);
    	vacuumDB();
	}
	
	public static void vacuumDB()
	{
		SQLiteDatabase db = DatabaseHelper.openDatabase();
		synchronized (db) {
			db.execSQL("VACUUM");
		}
		DatabaseHelper.tryCloseDatabase();

	}
	
	public static void whereBuilder(StringBuilder whereClause, ArrayList<String> whereArgs, String param, String paramValue, String andOr) {
		whereClause.append(whereClause.length() == 0 ? param : " " + andOr + " " + param);
		if (paramValue != null)
			whereArgs.add(paramValue);
	}	
	
	

	public static void makeCustomReport(ContentValuesEater eater, Long dateFrom, Long dateTo, Long articleFrom, Long articleTo, Integer order, DeviceFlavor<PrinterModel, PrinterConfiguration, PrinterWrapper<?>> printerFlavor)
	{
		synchronized (Table.AAAB) {
			synchronized (Table.AAABITEM) {
				SQLiteDatabase db = DatabaseHelper.openDatabase();
				String whereFrom = "";
				String whereTo = "";
				String whereArticleFrom = "";
				String whereArticleTo = "";
				String orderBy = "";

				if (dateFrom != null)
				{
					whereFrom = " AND " + aaab.Column.CR_DATE.getName() + " >= '" + dateFrom.toString() + "' ";
				}
				if (dateTo != null)
				{
					whereTo = " AND " + aaab.Column.CR_DATE.getName() + " <= '" + dateTo.toString() + "' ";
				}
				if (articleFrom != null)
				{
					whereArticleFrom = " AND  " + aaabItem.Column.aaaa_ARTICLE.getName() + "   >= '" + articleFrom + "' ";
				}
				if (articleTo != null)
				{
					whereArticleTo = " AND " + aaabItem.Column.aaaa_ARTICLE.getName() + "  <= '" + articleTo + "' ";
				}

				if (order != null)
				{
					switch (order) {
					case 0: {
						orderBy = "";
						break;
					}

					case 1: {
						orderBy = " ORDER BY CAST(" + aaabItem.Column.aaaa_ARTICLE.getName() + " as long)";
						break;
					}
					case 2: {
						orderBy = " ORDER BY " + aaabItem.Column.aaaa_NAME.getName();
						break;
					}
					default:
						break;
					}
				}

				String tnr = Table.AAAB.getName(); // table name aaab
				String tnrI = Table.AAABITEM.getName(); // table name aaabItem
				String rdbT = aaab.Column.T.getName();

				String sqlSubQuery = "SELECT " + aaab.Column._ID.getName() + " FROM " + tnr + " WHERE " + aaab.Column.FINISHED.getName() + "= '1' AND " + rdbT + " <> '"
					+ ReceiptType.SERVICE_RECEIPT.toString() + "' AND "
					+ rdbT + " <> '" + ReceiptType.START_RECEIPT.toString() + "'" + whereFrom + whereTo;
				
				if (printerFlavor != null)
					sqlSubQuery += " AND " + aaab.Column.FLAGS.getName() + " & 2 = CAST(" + DoublePrintingHelper.getFlavorFlagsForPrinter(printerFlavor) + " AS INTEGER)";

				String sqlQuery = "SELECT " + tnrI + "." + aaabItem.Column._ID.getName() + ", " + tnrI + "." + aaabItem.Column.aaaa_ARTICLE.getName() + ", " + tnrI + "."
					+ aaabItem.Column.aaaa_NAME.getName()
					+ " ,  SUM(" + tnrI + "." + aaabItem.Column.aaaa_SUM.getName() + " ) AS " + aaabItem.Column.aaaa_SUM.getName()
					+ " ,  SUM(CASE WHEN (" + tnr + "." + rdbT + " = '" + ReceiptType.SALE_RECEIPT.getType() + "') THEN " + tnrI + "." + aaabItem.Column.aaaa_SUM.getName()+ "  ELSE 0 END) AS " + CustomReportColumn.SUM_DEB.getName()
					+ " ,  SUM(CASE WHEN (" + tnr + "." + rdbT + " = '" + ReceiptType.RETURN_RECEIPT.getType() + "') THEN " + tnrI + "." + aaabItem.Column.aaaa_SUM.getName()+ "  ELSE 0 END) AS " + CustomReportColumn.SUM_KRED.getName()
					+ " ,  SUM(" + tnrI + "." + aaabItem.Column.aaaa_QUANTITY.getName() + " ) AS " + aaabItem.Column.aaaa_QUANTITY.getName()
					+ " ,  SUM(CASE WHEN (" + tnr + "." + rdbT + " = '" + ReceiptType.SALE_RECEIPT.getType() + "') THEN " + tnrI + "." + aaabItem.Column.aaaa_QUANTITY.getName() + " ELSE 0 END) AS " + CustomReportColumn.QUAN_DEB.getName()
					+ " ,  SUM(CASE WHEN (" + tnr + "." + rdbT + " = '" + ReceiptType.RETURN_RECEIPT.getType() + "') THEN " + tnrI + "." + aaabItem.Column.aaaa_QUANTITY.getName() + " ELSE 0 END) AS " + CustomReportColumn.QUAN_KRED.getName()
					+ " , " + tnr + "." + rdbT
					+ " FROM " + tnrI + " LEFT OUTER JOIN " + tnr + " ON " + tnrI + "." + aaabItem.Column.PARENT_ID.getName() + " = " + tnr + "." + aaab.Column._ID.getName()
					+ " WHERE " + tnrI + "." + aaabItem.Column.PARENT_ID.getName() + " IN (" + sqlSubQuery + ") " + whereArticleFrom + whereArticleTo
					+ " GROUP BY " + tnrI + "." + aaabItem.Column.aaaa_ARTICLE.getName()
					+ orderBy;				
				Cursor cur = db.rawQuery(sqlQuery, null);				
				cur.moveToFirst();
				while (!cur.isAfterLast()) {
					ContentValues values = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, values);
					if (!eater.eat(values)) break;
					cur.moveToNext();
				}
				cur.close();
				DatabaseHelper.tryCloseDatabase();
			}
		}
	}
	
	public static List<Integer> getAAAAIdsList(CharSequence name_article_mask, int prcListMode, Integer categoryCode) {		
	
		synchronized (Table.AAAA)
		{
			synchronized (Table.FAVORITES) 
			{
				SQLiteDatabase db = DatabaseHelper.openDatabase();
				List<Integer> result = new ArrayList<Integer>();
				StringBuilder request = new StringBuilder("");
				
				switch(prcListMode) 
				{
					case ActSellFrgPrclist.PRCLIST_MODE_FAVORITES:
						request.append("( "+Column.aaaa_CODE.getName()+ " IN ( SELECT "+Column.aaaa_CODE.getName()+ " FROM "+Table.FAVORITES.getName()+") )");
					break;
					case ActSellFrgPrclist.PRCLIST_MODE_NO_BC:
						request.append("( " +Column.aaaa_BARCODE.getName() + " IS NULL OR "+Column.aaaa_BARCODE.getName()+ "='0' OR "+ Column.aaaa_BARCODE.getName() + "='' )");
					break;
				}
				
				if (name_article_mask != null && name_article_mask.length() > 0)
				{
					if (request.length()>0) request.append(" AND ");
					request.append(" ( "+
							  		  Column.aaaa_NAME_LOWER.getName()+ " like '%" +name_article_mask+ "%' OR " 
							  		+ Column.aaaa_ARTICLE.getName()+ " like '%" +name_article_mask+ "%' OR "
							  		+ Column.aaaa_BARCODE.getName()+ " like '%" +name_article_mask+ "%' )");
				}
				
				if(categoryCode!=null){
					if (request.length()>0) request.append(" AND ");
					request.append("("+Column.CATEGORY_CODE.getName()+"='"+categoryCode+"' )");
				}
				
				if (request.length()>0) request.insert(0," WHERE ");
				
				request.insert(0, "SELECT "+Column._ID.getName()+" FROM " +Table.AAAA.getName());
				
				SortMethod sorting = SortMethod.find(PreferenceKeys.KEY_SORTING_AAAA.getString());
				if(sorting!=SortMethod.NONE){
					request.append(" ORDER BY "+sorting.sortColumn.fullname());
				}
				
				Cursor cur = db.rawQuery(request.toString(), null);
				cur.moveToFirst();
				while (!cur.isAfterLast())
				{
					ContentValues values = new ContentValues();
					DatabaseUtils.cursorRowToContentValues(cur, values);
					result.add(values.getAsInteger(Column._ID.getName()));
					cur.moveToNext();
				}
				cur.close();
				DatabaseHelper.tryCloseDatabase();
					
				return result;
			}
		}
	}
	
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final AtomicInteger openDbConnectionCounter = new AtomicInteger();
		private static SQLiteDatabase workDB;
		static final String DBNAME = "front2";
		static final int DBVERSION = 9;

		private DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			updateTables(db, true);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Utils.DBG("Updating database...");
			updateTables(db, false);
			if(oldVersion<3){
				appendColumnIfNotExists(db, Table.AAAA,aaaa.Column.CATEGORY_CODE);
				appendColumnIfNotExists(db, Table.AAAA,aaaa.Column.IMAGE_URL);
			}
			if(oldVersion<4) {
				appendColumnIfNotExists(db, Table.AAAB,aaab.Column.H);
			}
			if(oldVersion<5){
				appendColumnIfNotExists(db, Table.AAAA,aaaa.Column.PREFERRED_PRINTER);
				appendColumnIfNotExists(db, Table.AAAA,aaaa.Column.HAS_SUBITEMS);
			}
			if(oldVersion<6){
				appendColumnIfNotExists(db, Table.AAABITEM, aaabItem.Column.SUBITEM_CODE);
				appendColumnIfNotExists(db, Table.AAABITEM, aaabItem.Column.SUBITEM_COMMENT);
			}
			if(oldVersion<7){
				appendColumnIfNotExists(db, Table.aaaa_SUBITEMS, aaaaSubitem.Column.aaaa_CODE);
			}
			if(oldVersion<8){
				appendColumnIfNotExists(db, Table.AAAB, aaab.Column.COMMENT);
			}
			if(oldVersion<9){
				appendColumnIfNotExists(db, Table.AAAA, aaaa.Column.KITCHEN_PRINTER);
			}
		}

		private void updateTables(SQLiteDatabase db, Boolean delTables) {
			for (Table tTable : Table.values()) {
				if (delTables) {
					db.execSQL("drop table if exists " + tTable.getName());
				}
				db.execSQL(getSQLQueryCreateTable(tTable));
			}
		}

		private String getSQLQueryCreateTable(Table tTable) {
			StringBuilder tQuery = new StringBuilder("create table if not exists " + tTable.getName() + " (_id integer primary key autoincrement");
			for (IColumn tColumn : tTable.getColumns())
			{
				if (isDatabasePrimaryKey(tColumn.getName())) {continue;}
				tQuery.append(", " + tColumn.getName() + " " + tColumn.getColumnType());
			}
			tQuery.append(");");
			return tQuery.toString();
		}
		
		private void appendColumnIfNotExists(SQLiteDatabase db, Table table, IColumn column){
			try{
				db.execSQL("select "+column.getName()+" from "+table.getName());
			}catch(SQLException ex){
				//if column not found, creating it
				db.execSQL("alter table "+table.getName()+" add column "+column.getName()+" "+column.getColumnType());
			}
		}
		
		
		public static boolean isDatabasePrimaryKey(String key) {
			if (key.equals("_id")) {
				return true;
			}
			return false;

		}

		
		public static boolean isDatabaseKey(String key) {			
			return isDatabasePrimaryKey(key) || isDatabaseSubKey(key);
		}

		public static synchronized SQLiteDatabase openDatabase() {
			if (openDbConnectionCounter.incrementAndGet() == 1) {
				workDB = new DatabaseHelper(App.getContext(), DBNAME, null, DBVERSION).getWritableDatabase();
			}
			return workDB;
		}

		public static synchronized void tryCloseDatabase() {
			if (openDbConnectionCounter.decrementAndGet() == 0)
			{
				workDB.close();
				workDB = null;
			}
		}
	}
	
	public static void delOldReceiptsReports()
	{
		synchronized (Table.AAAB) {
			synchronized (Table.AAABITEM) {
				synchronized (Table.AAAC) {
					SQLiteDatabase db = DatabaseHelper.openDatabase();
					long dateTo = UtilsDate.getTimestampEndDay(System.currentTimeMillis() - PreferenceKeys.KEY_PERIOD.getInt() * Periodic.DAY);
					String whereTo = "";
					whereTo = aaab.Column.CR_DATE.getName() + " <= '" + Long.valueOf(dateTo) + "' ";
					String sqlSubQuery = "SELECT " + aaab.Column._ID.getName() + " FROM " + Table.AAAB.getName() + " WHERE " + whereTo;
					String sqlQuery = "DELETE FROM " + Table.AAABITEM.getName() + " WHERE " + aaabItem.Column.PARENT_ID.getName() + " IN (" + sqlSubQuery + ")";

					db.beginTransaction();
					db.execSQL(sqlQuery);
					sqlQuery = "DELETE FROM " + Table.AAAB.getName() + " WHERE " + whereTo;
					db.execSQL(sqlQuery);

					whereTo = AAAC.Column.CR_DATE.getName() + " <= '" + Long.valueOf(dateTo) + "' ";
					sqlQuery = "DELETE FROM " + Table.AAAC.getName() + " WHERE " + whereTo;
					db.execSQL(sqlQuery);

					db.setTransactionSuccessful();
					db.endTransaction();
					DatabaseHelper.tryCloseDatabase();					
				}
			}
		}
		vacuumDB();
	}	

	public static void findReceiptsWhereDiMoreThan(long last_di, ContentValuesEater eaterItems){
		synchronized (Table.AAAB) {
		synchronized (Table.AAABITEM) {
			
			String query = "SELECT "+aaabItem.Column.aaaa_CODE.fullname()+","+aaabItem.Column.SUBITEM_CODE.fullname()+",SUM("+aaabItem.Column.aaaa_QUANTITY.fullname()+") "+ 
					"FROM "+Table.AAABITEM.getName()+","+Table.AAAB.getName()+" "+
					"WHERE "+aaab.Column._ID.fullname()+"="+aaabItem.Column.PARENT_ID.fullname()+" "+
					"AND "+aaab.Column.T.fullname()+"="+aaab.ReceiptType.SALE_RECEIPT.getType()+" "+
					"AND "+aaab.Column.FINISHED+"=1 "+
					"AND CAST("+aaab.Column.DI.fullname()+" AS INTEGER) >"+last_di+" "+
					"GROUP BY "+aaabItem.Column.aaaa_CODE.fullname()+","+aaabItem.Column.SUBITEM_CODE.fullname();
			Databases.execQuery(eaterItems, query, Table.AAAB);
			
		}
		}
	}

	public static void execQuery(ContentValuesEater eater, String sqlQuery, Table syncTable)
	{
		synchronized(syncTable)
		{
			SQLiteDatabase db = DatabaseHelper.openDatabase();			
			Cursor cur = db.rawQuery(sqlQuery, null);
			cur.moveToFirst();
			while (!cur.isAfterLast())
			{
				ContentValues values = new ContentValues();
				DatabaseUtils.cursorRowToContentValues(cur, values);
				if (!eater.eat(values)) break;
				cur.moveToNext();
			}
			cur.close();
			DatabaseHelper.tryCloseDatabase();
				
		}
	}	
}
