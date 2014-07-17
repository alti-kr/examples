package *.designer;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import *.ActivityCommonKeyboard;
import *.Colors;
import *.FrgProgressDialog;
import *.R;
import *.Utils;
import *.UtilsDate.DateFormat;
import *.db.Category;
import *.db.Databases;
import *.db.Databases.IColumn;
import *.db.Databases.Table;
import *.db.Metric;
import *.db.Product;
import *.db.ProductSubitem;
import *.db.ProductSubmetric;
import *.db.ReceiptDB;
import *.db.ReceiptDBItem;
import *.designer.ReportDesignerField.ReportFields;

public class ReportDesignerHelper {
	static final String tnr = Table.RDB.getName();
	static final String tnrI = Table.RDBITEM.getName();
	private static final String tnprice = Table.PLIST.getName();
	private static final String tncategories = Table.CAT.getName();
	private static final String tnpsubitems = Table.PRODUCT_SUBITEMS.getName();
	static String rdbT = ReceiptDB.Column.T.getName();
	private static final String without_category = Utils.getString(R.string.rd_frg_result_without_category);
	private static final String TOTAL = "TOTAL";
	public static DateFormat showDateFormat = DateFormat.ddMMyyyy_HHmm;
	private static ReportValues reportValues;
	private static final String bottomTotalString = Utils.getString(R.string.rd_frg_result_bottom_total);

	private static Context context;
	private static ReportDesigner reportDesigner;
	private static TableLayout tableViewHeader;
	private static TableLayout tableView;
	private ActReportDesignerResult aRDR;
	private static final TableLayout.LayoutParams tableRowLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
	
	public enum TypeReportDesigner {
			RECEIPT,
			PRODUCT
	}

    public enum Condition {
			EQUALLY("=", "="), // ==
			UNEQUALLY("<>", "!="), // !=
			LESS("<", "<"), // <
			LESS_EQUALLY("<=", "<="), // <=
			MORE(">", ">"), // >
			MORE_EQUALLY(">=", ">="), // >=
			LIKE(Utils.getString(R.string.rd_rb_helper_condition_like), "LIKE");

		final String representView;
        final String representSql;

		Condition(String rView, String rSql)
		{
			this.representView = rView;
			this.representSql = rSql;
		}

		public static String[] getRepresent()
		{
			Condition[] v = Condition.values();
			String[] represents = new String[v.length];
			for (int i = 0; i < Condition.values().length; i++)
			{
				represents[i] = v[i].representView;
			}
			return represents;
		}
	}

	private static void processReportFieldsList(AtomicBoolean useRECEIPTSDB, AtomicBoolean useRECEIPTSDBITEM, AtomicBoolean usePRICE, List<ReportDesignerField> sourceList)
	{
		checkInclude(sourceList, tnr, useRECEIPTSDB);
		checkInclude(sourceList, tnrI, useRECEIPTSDBITEM);
		checkInclude(sourceList, tnprice, usePRICE);
	}

	private static void checkUsageCategories(ReportDesigner rd, AtomicBoolean usageCat)
	{
		checkInclude(rd.selectedColumns, tncategories, usageCat);
		if (usageCat.get()) {
			return;
		}
		checkInclude(rd.selectedLines, tncategories, usageCat);
		if (usageCat.get()) {
			return;
		}
		checkInclude(rd.selectedConditions, tncategories, usageCat);
		if (usageCat.get()) {
			return;
		}
		checkInclude(rd.selectedSort, tncategories, usageCat);
	}
	
	private static void checkInclude(List<ReportDesignerField> sourceList, String nameClassTable, AtomicBoolean param)
	{
		for (ReportDesignerField rf : sourceList)
		{
			if (rf.isBelongsTable(nameClassTable))
			{
				param.set(true);
				return;
			}
		}
	}

	private static void checkUsageQuantity(ReportDesigner rd, AtomicBoolean usageQuan)
	{
		checkIncludeColumn(rd.selectedColumns, Product.Column.PRODUCT_QUANTITY.fullname(), usageQuan);
		if (usageQuan.get()) {return;}
		checkIncludeColumn(rd.selectedConditions, Product.Column.PRODUCT_QUANTITY.fullname(), usageQuan);
		if (usageQuan.get()) {return;}
		checkIncludeColumn(rd.selectedSort, Product.Column.PRODUCT_QUANTITY.fullname(), usageQuan);		
	}

	private static void checkUsageMetric(ReportDesigner rd, AtomicBoolean usageMetric)
	{
		checkIncludeColumn(rd.selectedColumns, ProductSubitem.Column.SUBITEM_CODE.fullname(), usageMetric);
		if (usageMetric.get()) {return;}
		checkIncludeColumn(rd.selectedConditions, ProductSubitem.Column.SUBITEM_CODE.fullname(), usageMetric);
		if (usageMetric.get()) {return;}
		checkIncludeColumn(rd.selectedSort, ProductSubitem.Column.SUBITEM_CODE.fullname(), usageMetric);		
	}
	
	
	private static void checkIncludeColumn(List<ReportDesignerField> sourceList, String fullColumnName, AtomicBoolean param)
	{
		for (ReportDesignerField rf : sourceList)
		{
			if (rf.getColumnSource().fullname().equals(fullColumnName))
			{
				param.set(true);
				return;
			}
		}
	}	
	
	static String getBelongsTable(IColumn columnSource)
	{
		if (columnSource.getClass().equals(ReceiptDB.Column.class))
		{
			return tnr;
		} else if (columnSource.getClass().equals(ReceiptDBItem.Column.class))
		{
			return tnrI;
		}
		else if (columnSource.getClass().equals(Product.Column.class))
		{
			return tnprice;
		}
		else if (columnSource.getClass().equals(Category.Column.class))
		{
			return tncategories;
		}
		else if (columnSource.getClass().equals(ProductSubitem.Column.class))
		{
			return tnpsubitems;
		}
		return "unknown";
	}


	private static String getInjection(ReportDesigner rDesigner, ReportDesignerField rdf) {		
		AtomicBoolean tBool = new AtomicBoolean();
		checkIncludeColumn(rDesigner.selectedLines, rdf.getColumnSource().fullname(),tBool);		
		if (tBool.get()) return "";
		checkIncludeColumn(rDesigner.selectedColumns, rdf.getColumnSource().fullname(),tBool);
		if (tBool.get()) return "";
		
		String source = rdf.getPartSqlQuery();
		if (source.length() == 0)
		{
			source = rdf.getColumnSource().fullname();
		}
		source += " AS " + rdf.getSqlCondition();		
		return source;
	}
	
	public static ReportDesignerHelper getInstance()
	{
		return new ReportDesignerHelper();
	}
	
	public  void generateReport(Context tContext, ReportDesigner tReportDesigner, ActReportDesignerResult actReportDesignerResult)
	{
		context = tContext;
		reportDesigner = tReportDesigner;
		tableViewHeader = new TableLayout(context);
		tableView = new TableLayout(context);
		aRDR = actReportDesignerResult;
		new GeneratorReportDesigner(true).execute();
		
	}
	private static void generateReport(){
		TableRow tableRow;
		TextView textView;
		tableViewHeader.addView(makeTableHeader(context, reportDesigner, LayoutParams.WRAP_CONTENT));
		tableView.addView(makeTableHeader(context, reportDesigner, 1));

		String query = getQuery(reportDesigner);
		if (query == null){	return;}
		// Подготовим объект для хранения пром результатов
		reportValues = new ReportValues(reportDesigner);
		Databases.ContentValuesEater eater = new ReportDesignerContentValuesEater(reportDesigner);
		Databases.execQuery(eater, query, Table.RECEIPTSDB);
		String sqlConditionGroup, sqlCondition;
		// выводим последний итог и общий итог
		if (reportValues.wasResult) {
			for (int i = reportDesigner.selectedLines.size() - 2; i > -1; i--)
			{
				sqlConditionGroup = reportDesigner.selectedLines.get(i).getSqlCondition();
				// выводим последние итоги
				tableRow = createTableRow(context);
				tableRow.setBackgroundColor(getBackgroundFooter(i));
				textView = createTextView(context);
				textView.setText(getFormatStringTotal(reportDesigner.selectedLines.get(i).getFormated(reportValues.currentValueGroup.get(sqlConditionGroup), false)));
				setPadding(textView, i);
				setTypefaceTotal(textView, i);
				tableRow.addView(textView);
				for (ReportDesignerField rf : reportDesigner.selectedColumns)
				{
					sqlCondition = rf.getSqlCondition();
					textView = createTextView(context);
					textView.setText(rf.getFormated(reportValues.getGroupResult(sqlConditionGroup, sqlCondition), true));
					textView.setGravity(rf.getGravity());
					tableRow.addView(textView);
				}
				tableView.addView(tableRow);
			}
			// выводим общий итог
			sqlConditionGroup = TOTAL;
			tableRow = createTableRow(context);
			tableRow.setBackgroundColor(getBackgroundFooter(-1));
			textView = createTextView(context);
			textView.setText(bottomTotalString + ":");
			textView.setTextColor(Colors.COLOR_BACKGROUND);
			setPadding(textView, 0);
			setTypefaceTotal(textView, 0);
			tableRow.addView(textView);
			for (ReportDesignerField rf : reportDesigner.selectedColumns)
			{
				sqlCondition = rf.getSqlCondition();
				textView = createTextView(context);
				textView.setText(rf.getFormated(reportValues.getGroupResult(sqlConditionGroup, sqlCondition), true));
				textView.setTextColor(Colors.COLOR_BACKGROUND);
				setTypefaceTotal(textView, 0);
				textView.setGravity(rf.getGravity());
				tableRow.addView(textView);
			}
			tableView.addView(tableRow);
		}
	}

	private static class ReportValues {
		boolean wasResult;
		boolean backgroundSwitcher = false;
		HashMap<String, String> currentValueGroup = new HashMap<>();
		final HashMap<String, HashMap<String, Long>> internalRes = new HashMap<>();// intermediate result
		final HashMap<String, Long> groupResult = new HashMap<>();
		final Set<String> keysInternalRes;

		public ReportValues(ReportDesigner reportDesigner) {
			wasResult = false;
			currentValueGroup = new HashMap<>();
			for (ReportDesignerField rf : reportDesigner.selectedColumns)
			{
				if (rf.getCountResult()) {
					groupResult.put(rf.getSqlCondition(), 0L);
				}
			}
			// add TOTAL
			currentValueGroup.put(TOTAL, null);
			HashMap<String, Long> tHM = new HashMap<>();
			tHM.putAll(groupResult);
			internalRes.put(TOTAL, tHM);
			for (int i = 0; i < reportDesigner.selectedLines.size() - 1; i++)
			{
				currentValueGroup.put(reportDesigner.selectedLines.get(i).getSqlCondition(), null);
				tHM = new HashMap<>();
				tHM.putAll(groupResult);
				internalRes.put(reportDesigner.selectedLines.get(i).getSqlCondition(), tHM);
			}
			keysInternalRes = internalRes.keySet();
		}

		public void updateValue(String sqlCondition, String value)
		{
			if (!groupResult.containsKey(sqlCondition)) {
				return;
			}
			
			Long promLong = 0L;
			for (String key : keysInternalRes)
			{
				promLong = internalRes.get(key).get(sqlCondition);
				internalRes.get(key).put(sqlCondition, Utils.getStringToLong(value) + promLong);
			}
		}

		public void clearGroupResult(String sqlCondition)
		{
			Set<String> keys = internalRes.get(sqlCondition).keySet();
			for (String key : keys)
			{
				internalRes.get(sqlCondition).put(key, 0L);
			}
		}

		public String getGroupResult(String sqlConditionGroup, String sqlCondition)
		{
			Long res = this.internalRes.get(sqlConditionGroup).get(sqlCondition);
			return res == null ? "" : String.valueOf(res);

		}
	}

	public static class ReportDesignerContentValuesEater implements Databases.ContentValuesEater
	{
		TableRow tableRow;
		TextView textView;
		final ReportDesigner reportDesigner;
		String sqlConditionGroup = "", sqlCondition = "", sqlConditionGroupJ = "";
		final ReportDesignerField selectedLinesLowerLevel;
		final String sqlConditionSelectedLinesLowerLevel;
		Boolean isFirst = true;
		final int selectedLinesSize_1;
		final int selectedLinesSize_2;
		public ReportDesignerContentValuesEater(ReportDesigner rDesigner)
		{
			this.reportDesigner = rDesigner;
			selectedLinesLowerLevel = reportDesigner.selectedLines.get(reportDesigner.selectedLines.size() - 1);
			sqlConditionSelectedLinesLowerLevel = reportDesigner.selectedLines.get(reportDesigner.selectedLines.size() - 1).getSqlCondition();
			selectedLinesSize_1 = reportDesigner.selectedLines.size() - 1;
			selectedLinesSize_2 = reportDesigner.selectedLines.size() - 2;
		}

		@Override
		public boolean eat(ContentValues values) {
			reportValues.wasResult = true;
			if (isFirst)
			{
				showGroupsHeader(values, 0);
				isFirst = false;
			}

			for (int i= 0 ; i < selectedLinesSize_1; i++)
			{
				sqlConditionGroup = reportDesigner.selectedLines.get(i).getSqlCondition();
				if (reportValues.currentValueGroup.get(sqlConditionGroup) == null)
				{
					reportValues.currentValueGroup.put(sqlConditionGroup, values.getAsString(sqlConditionGroup) == null ? without_category : values.getAsString(sqlConditionGroup));
				}
				if (!reportValues.currentValueGroup.get(sqlConditionGroup).equals(values.getAsString(sqlConditionGroup) == null ? without_category : values.getAsString(sqlConditionGroup)))
				{
					for (int j = selectedLinesSize_2; j > i; j--)
					{
						sqlConditionGroupJ = reportDesigner.selectedLines.get(j).getSqlCondition();
						tableRow = createTableRow(context);
						tableRow.setBackgroundColor(getBackgroundFooter(j));
						textView = createTextView(context);
						textView.setText(getFormatStringTotal(reportDesigner.selectedLines.get(j).getFormated(reportValues.currentValueGroup.get(sqlConditionGroupJ), false)));
						setPadding(textView, j);
						setTypefaceTotal(textView, j);
						tableRow.addView(textView);

						for (ReportDesignerField rf : reportDesigner.selectedColumns)
						{
							sqlCondition = rf.getSqlCondition();
							textView = createTextView(context);
							textView.setText(rf.getFormated(reportValues.getGroupResult(sqlConditionGroupJ, sqlCondition), true));
							textView.setGravity(rf.getGravity());
							tableRow.addView(textView);
						}

						tableView.addView(tableRow);
						reportValues.clearGroupResult(sqlConditionGroupJ);
						reportValues.currentValueGroup.put(sqlConditionGroupJ, values.getAsString(sqlConditionGroupJ));
					}
					tableRow = createTableRow(context);
					tableRow.setBackgroundColor(getBackgroundFooter(i));
					textView = createTextView(context);
					textView.setText(getFormatStringTotal(reportDesigner.selectedLines.get(i).getFormated(reportValues.currentValueGroup.get(sqlConditionGroup), false)));					
					setPadding(textView, i);
					setTypefaceTotal(textView, i);
					tableRow.addView(textView);
					for (ReportDesignerField rf : reportDesigner.selectedColumns)
					{
						sqlCondition = rf.getSqlCondition();
						textView = createTextView(context);
						textView.setText(rf.getFormated(reportValues.getGroupResult(sqlConditionGroup, sqlCondition), true));
						textView.setGravity(rf.getGravity());
						tableRow.addView(textView);
					}

					tableView.addView(tableRow);
					reportValues.clearGroupResult(sqlConditionGroup);
					reportValues.currentValueGroup.put(sqlConditionGroup, values.getAsString(sqlConditionGroup));
					showGroupsHeader(values, i);
				}
			}


			tableRow = createTableRow(context);
			tableRow.setBackgroundColor(getBackgroundRow());
			textView = createTextView(context);

			textView.setText(selectedLinesLowerLevel.getFormated(values.getAsString(sqlConditionSelectedLinesLowerLevel), false));			
			setPadding(textView, -1);
			tableRow.addView(textView);
			for (ReportDesignerField rf : reportDesigner.selectedColumns)
			{
				sqlCondition = rf.getSqlCondition();
				textView = createTextView(context);
				textView.setText(rf.getFormated(values.getAsString(sqlCondition), false));
				textView.setGravity(rf.getGravity());
				tableRow.addView(textView);

				reportValues.updateValue(sqlCondition, values.getAsString(sqlCondition));
			}
			tableView.addView(tableRow);
			reportValues.backgroundSwitcher = !reportValues.backgroundSwitcher;
			return true;
		}

		private void showGroupsHeader(ContentValues values, int position) {
			for (int i = position; i < reportDesigner.selectedLines.size() - 1; i++)
			{
				sqlConditionGroup = reportDesigner.selectedLines.get(i).getSqlCondition();

				tableRow = createTableRow(context);
				tableRow.setBackgroundColor(getBackgroundHeader(i));
				textView = createTextView(context);
				textView.setText(reportDesigner.selectedLines.get(i).getFormated(values.getAsString(sqlConditionGroup) == null ? without_category : values.getAsString(sqlConditionGroup), false));				
				textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
				setPadding(textView, i);
				tableRow.addView(textView);
				for (ReportDesignerField rf : reportDesigner.selectedColumns)
				{
					sqlCondition = rf.getSqlCondition();
					textView = createTextView(context);
					textView.setText("");
					textView.setGravity(rf.getGravity());
					tableRow.addView(textView);
				}
				tableView.addView(tableRow);
			}
		}

	}

	private static TableRow makeTableHeader(Context context, ReportDesigner reportDesigner, int height)
	{
		TableRow tableRow = createTableRow(context);
		TextView textView = createTextView(context);
		String textHeader = "";
		for (ReportDesignerField rf : reportDesigner.selectedLines)
		{
			textHeader += textHeader.length() == 0 ? rf.getNameHeader() : " / " + rf.getNameHeader();
		}
		textView.setText(textHeader);
		textView.setTextColor(Colors.COLOR_BACKGROUND);
		textView.setGravity(Gravity.CENTER);
		tableRow.addView(textView, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, height));
		for (ReportDesignerField rf : reportDesigner.selectedColumns)
		{
			textView = createTextView(context);
			textView.setText(rf.getNameHeader());
			textView.setTextColor(Colors.COLOR_BACKGROUND);
			textView.setGravity(Gravity.CENTER);
			tableRow.addView(textView, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, height));
		}
		TableLayout.LayoutParams tableRowParams =
			new TableLayout.LayoutParams
			(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
		tableRowParams.setMargins(0, 0, 0, 0);
		tableRow.setLayoutParams(tableRowParams);
		return tableRow;
	}

	private static int getBackgroundRow() {
		return reportValues.backgroundSwitcher ? Colors.COLOR_TABLE_GREY : Colors.COLOR_BACKGROUND;
	}

	private static int getBackgroundHeader(int position)
	{
		switch (position) {
		case 0: {
			reportValues.backgroundSwitcher = false;
			return Colors.COLOR_TABLE_GREY;
		}
		default:
			reportValues.backgroundSwitcher = true;
			return Colors.COLOR_BACKGROUND;
		}
	}

	private static int getBackgroundFooter(int position)
	{
		switch (position) {
		case -1: {
			return Colors.COLOR_TABLE_DARK_GREY;
		}
		case 0: {
			return Colors.COLOR_TABLE_BLUE;
		}
		case 1: {
			return Colors.COLOR_TABLE_YELLOW;
		}
		default:
			return Colors.COLOR_BACKGROUND;
		}
	}

	private static CharSequence getFormatStringTotal(String formattedString) {

		return bottomTotalString + " (" + formattedString + "):";
	}

	private static void setPadding(TextView textViewTemp, int position) {

		switch (position) {
		case 0: {
			textViewTemp.setPadding(10, 8, 5, 8);
			break;
		}
		case 1: {
			textViewTemp.setPadding(20, 8, 5, 8);
			break;
		}
		default:
			textViewTemp.setPadding(30, 8, 5, 8);
		}

	}

	private static void setTypefaceTotal(TextView textViewTemp, int position)
	{
		switch (position) {
		case 0: {
			textViewTemp.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
			break;
		}
		}
	}

	private static String getQuery(ReportDesigner rDesigner)
	{
		if (rDesigner.selectedLines.size() == 0)
		{
			return null;
		}
		if (rDesigner.typeReport == TypeReportDesigner.RECEIPT)
		{
			return getQueryReceipt(rDesigner);
		}
		else if (rDesigner.typeReport == TypeReportDesigner.PRODUCT)
		{
			return getQueryPricelist(rDesigner);
		}
		return null;
	}

	private static String getQueryReceipt(ReportDesigner rDesigner)
	{
		AtomicBoolean useRECEIPTSDB = new AtomicBoolean(false);
		AtomicBoolean useRECEIPTSDBITEM = new AtomicBoolean(false);
		AtomicBoolean usePRICE = new AtomicBoolean(false);
		processReportFieldsList(useRECEIPTSDB, useRECEIPTSDBITEM, usePRICE, rDesigner.selectedColumns);
		if (!useRECEIPTSDB.get() || !useRECEIPTSDBITEM.get() || !usePRICE.get()) {
			processReportFieldsList(useRECEIPTSDB, useRECEIPTSDBITEM, usePRICE, rDesigner.selectedLines);
		}
		if (!useRECEIPTSDB.get() || !useRECEIPTSDBITEM.get() || !usePRICE.get()) {
			processReportFieldsList(useRECEIPTSDB, useRECEIPTSDBITEM, usePRICE, rDesigner.selectedConditions);
		}
		if (!useRECEIPTSDB.get() || !useRECEIPTSDBITEM.get() || !usePRICE.get()) {
			processReportFieldsList(useRECEIPTSDB, useRECEIPTSDBITEM, usePRICE, rDesigner.selectedSort);
		}
		
		String query = null;
		StringBuilder queryColumns = new StringBuilder();
		String queryGroup = "";
		String queryWhereClause = "";
		String querySort = "";
		String queryJoin = "";
		String outerQueryWhereClause = "";
		
		for (ReportDesignerField rf : rDesigner.selectedLines)
		{
			queryColumnBuilder(queryColumns, rf, tnr);
			if (useRECEIPTSDBITEM.get())
			{
				queryColumnBuilder(queryColumns, rf, tnrI);
			}
		}
		for (ReportDesignerField rf : rDesigner.selectedColumns)
		{
			queryColumnBuilder(queryColumns, rf, tnr);
			if (useRECEIPTSDBITEM.get())
			{
				queryColumnBuilder(queryColumns, rf, tnrI);
			}
		}
		// necessary add columns from condition in main query

		for (ReportDesignerField rf : rDesigner.selectedConditions)
		{
			if (rDesigner.selectedLines.contains(rf)) {
				continue;
			}
			if (rDesigner.selectedColumns.contains(rf)) {
				continue;
			}
			queryColumnBuilder(queryColumns, rf, tnr);
			if (useRECEIPTSDBITEM.get())
			{
				queryColumnBuilder(queryColumns, rf, tnrI);
			}
		}
		
		querySort = getQuerySort(rDesigner.selectedSort);
		// 5 - собриаем группировку
		queryGroup = getQueryGroup(rDesigner.selectedLines);

		if (!useRECEIPTSDBITEM.get() && useRECEIPTSDB.get()) // only receipt
		{
			queryWhereClause = getQueryConditions(rDesigner.selectedConditions, tnr);
			// 4 - собираем сортировку
			querySort = getQuerySort(rDesigner.selectedSort);
			// 5 - собриаем группировку
			queryGroup = getQueryGroup(rDesigner.selectedLines);
			query = "SELECT " + queryColumns.toString() + " FROM " + tnr + " " + queryWhereClause + " " + queryGroup + "  " + querySort;
			outerQueryWhereClause = getQueryExceptionColumnCondition(rDesigner.selectedConditions);
			query = wrapCondition(query, outerQueryWhereClause.length() > 0, outerQueryWhereClause);
		}
		else // all		{

			queryJoin = " LEFT OUTER JOIN " + tnr + " ON " + tnrI + "." + ReceiptDBItem.Column.PARENT_ID.getName() + " = " + tnr + "." + ReceiptDB.Column._ID.getName();
			queryWhereClause = getQueryConditions(rDesigner.selectedConditions, tnrI);
			query = "SELECT " + queryColumns.toString() + " FROM " + tnrI + queryJoin + " " + queryWhereClause + queryGroup + querySort;

			outerQueryWhereClause = getQueryExceptionColumnCondition(rDesigner.selectedConditions);

            queryWhereClause = getQueryConditions(rDesigner.selectedConditions, tnr);
            query = wrapCondition(query, queryWhereClause.length() > 0, queryWhereClause);
			query = wrapCondition(query, outerQueryWhereClause.length() > 0, outerQueryWhereClause);

		}
		// Utils.DBG("My_log", "query = " + query);
		return query;
	}

	private static String getQueryPricelist(ReportDesigner rDesigner)
	{		
		StringBuilder queryColumns = new StringBuilder();
		String queryGroup = "";
		String queryWhereClause = "";
		String querySort = "";	
		String query = null;
		
		AtomicBoolean usageCategories = new AtomicBoolean(false);
		AtomicBoolean usageCategoriesInConditions = new AtomicBoolean(false);
		checkUsageCategories(rDesigner, usageCategories);
		checkInclude(rDesigner.selectedConditions, tncategories, usageCategoriesInConditions);
		String queryJoinCategory = "";		
		String subQueryWhereClauseCategories = "";
		String  injectionCategory = "";
		if (usageCategoriesInConditions.get())
		{		
			injectionCategory = getInjection(rDesigner,ReportDesignerField.getInstance(ReportFields.PRODUCT_CATEGORY));
			injectionCategory = injectionCategory.length()>0? (" ,"+injectionCategory):injectionCategory;
		}
		
		AtomicBoolean usageQuantity = new AtomicBoolean(false);
		AtomicBoolean usageQuantityInConditions = new AtomicBoolean(false);
		checkUsageQuantity(rDesigner, usageQuantity);
		checkIncludeColumn(rDesigner.selectedConditions, Product.Column.PRODUCT_QUANTITY.fullname(), usageQuantityInConditions);		
		String queryJoinQuantity = "";		
		String  injectionQuantity = "";
		String subQueryWhereClauseQuantity = "";
		if (usageQuantityInConditions.get())
		{				
			injectionQuantity = getInjection(rDesigner,ReportDesignerField.getInstance(ReportFields.PRODUCT_QUANTITY));			
			injectionQuantity = injectionQuantity.length()>0? (" ,"+injectionQuantity):injectionQuantity;
			subQueryWhereClauseQuantity = getQueryExceptionColumnCondition(rDesigner.selectedConditions);
		}	
		
		
		
		AtomicBoolean usageMetrics = new AtomicBoolean(false);
		AtomicBoolean usageMetricsInConditions = new AtomicBoolean(false);
		checkUsageMetric(rDesigner, usageMetrics);
		checkIncludeColumn(rDesigner.selectedConditions, ProductSubitem.Column.SUBITEM_CODE.fullname(), usageMetricsInConditions);
		String queryJoinMetrics = "";
		String queryColumnMetric = "";

		String querySubMetric = "(Select FID , GROUP_CONCAT(fullName, ', ') AS "+ ReportDesignerField.columnProductSubitemName+" FROM ( Select  ("+Metric.Column.METRIC_NAME.fullname()+ "||"+ " ': ' "+"||"+ ProductSubmetric.Column.METRIC_VALUE.fullname()+ ") AS fullName , "+ ProductSubmetric.Column.PARENT_ID.fullname() + " AS FID FROM "+	Table.PRODUCT_SUBMETRICS.getName() + " , "+ Table.METRICS.getName() + " WHERE " + ProductSubmetric.Column.METRIC_CODE.fullname() + " = "+ Metric.Column.METRIC_CODE.fullname()+" ) GROUP BY FID) AS tMetriks "+
						" ON " + ProductSubitem.Column._ID.fullname() +" = " + " tMetriks.FID";
		for (ReportDesignerField rf : rDesigner.selectedLines)
		{
			queryColumnBuilder(queryColumns, rf, tnprice);
			queryColumnBuilder(queryColumns, rf, tncategories);
		}
		for (ReportDesignerField rf : rDesigner.selectedColumns)
		{
			queryColumnBuilder(queryColumns, rf, tnprice);
			queryColumnBuilder(queryColumns, rf, tncategories);
		}

		queryWhereClause = getQueryConditions(rDesigner.selectedConditions, tnprice);
		if (subQueryWhereClauseCategories.length() > 0)
		{
			queryWhereClause = queryWhereClause + (queryWhereClause.length() > 0 ? "," + subQueryWhereClauseCategories : " WHERE " + subQueryWhereClauseCategories);
		}		

		
		querySort = getQuerySort(rDesigner.selectedSort);
		queryGroup = getQueryGroup(rDesigner.selectedLines);	
		
		// #1
		if (usageCategories.get())			
		{
			queryJoinCategory = " LEFT JOIN " + tncategories + " ON " + Product.Column.CATEGORY_CODE.fullname() + " = " + Category.Column.CODE.fullname();
			
			if (usageCategoriesInConditions.get())
			{			
				subQueryWhereClauseCategories =  getQueryConditions(rDesigner.selectedConditions, tncategories) ;				
			}
			
		}
		// # 3
		if (usageQuantity.get() && !usageMetrics.get())
		{
			queryJoinQuantity = " LEFT OUTER JOIN " + tnpsubitems+ " ON " + Product.Column._ID.fullname()+ " = " + ProductSubitem.Column.PARENT_ID.fullname();						
			query = "SELECT " + queryColumns.toString() + injectionCategory + injectionQuantity+ " FROM " + tnprice + " " + queryJoinCategory +  queryJoinQuantity + queryWhereClause +  queryGroup + querySort;			
			query = wrapCondition(query,usageQuantityInConditions.get(),subQueryWhereClauseQuantity);
			query = wrapCondition(query,usageCategoriesInConditions.get(),subQueryWhereClauseCategories);
			
		}
		// #5
		else if(!usageQuantity.get() && usageMetrics.get() )
		{//# 6
			if (usageMetricsInConditions.get())
			{
				query = "SELECT "+ReportDesignerField.columnProductSubitemName + " , " + queryColumns.toString()+  injectionCategory+ " , "  +ProductSubitem.Column.PARENT_ID.fullname()+  " AS parIDSubItem  FROM " 
					+ Table.PRODUCT_SUBITEMS.getName()+ " LEFT OUTER JOIN " +querySubMetric + "  LEFT JOIN "+ Table.PRICELIST.getName() +" ON "+ Product.Column._ID.fullname()+ " = parIDSubItem "+  queryJoinCategory + getQueryConditionsMetric(rDesigner.selectedConditions) +  queryGroup + querySort ; //  WHERE "+ ReportDesignerField.columnProductSubitemName+" LIKE '%азмер%'" ;				
			}
			else
			{
				queryJoinMetrics = " LEFT JOIN " +"(SELECT "+ ProductSubitem.Column._ID.fullname()+ " , "+ProductSubitem.Column.PARENT_ID.fullname()+  " AS parIDSubItem, FID , " +ReportDesignerField.columnProductSubitemName+"   FROM " 
					+ Table.PRODUCT_SUBITEMS.getName()+ " LEFT OUTER JOIN " +querySubMetric + " ) AS tSubItems ON  " + Product.Column._ID.fullname()+ " = " + " tSubItems.parIDSubItem";
				 queryColumnMetric = (queryColumns.toString().trim().length()>0? ", " : " " )+ ReportDesignerField.columnProductSubitemName;
				 query = "SELECT " + queryColumns.toString() + queryColumnMetric + injectionCategory + " FROM " + tnprice + " " + queryJoinCategory+ queryJoinQuantity + queryJoinMetrics + queryWhereClause +  queryGroup + querySort;
			}			
			query = wrapCondition(query,usageCategoriesInConditions.get(),subQueryWhereClauseCategories);
		}
		//#7
		else if (usageQuantity.get() && usageMetrics.get())
		{
			//# 8
			if (usageMetricsInConditions.get())
			{

				query = ("SELECT "+ReportDesignerField.columnProductSubitemName + " , " + queryColumns.toString()+  injectionCategory + injectionQuantity+ " ,"  +ProductSubitem.Column.PARENT_ID.fullname()+  " AS parIDSubItem  FROM " 
					+ Table.PRODUCT_SUBITEMS.getName()+ " LEFT OUTER JOIN "+querySubMetric+"  LEFT JOIN "+ Table.PRICELIST.getName() +" ON "+ Product.Column._ID.fullname()+ " = parIDSubItem " +  queryJoinCategory + getQueryConditionsMetric(rDesigner.selectedConditions) + queryGroup + querySort).replace("SUM","") ;
			}
			else
			{
				queryJoinMetrics = " LEFT JOIN " +"(SELECT "+ ProductSubitem.Column.QUANTITY.getName()+" ,"+ ProductSubitem.Column._ID.fullname()+ " , "+ProductSubitem.Column.PARENT_ID.fullname()+  " AS parIDSubItem, FID , " +ReportDesignerField.columnProductSubitemName+"   FROM " 
					+ Table.PRODUCT_SUBITEMS.getName()+ " LEFT OUTER JOIN "+ querySubMetric +") AS tSubItems ON  " + Product.Column._ID.fullname()+ " = " + " tSubItems.parIDSubItem";
				 queryColumnMetric = (queryColumns.toString().trim().length()>0? ", " : " " )+ ReportDesignerField.columnProductSubitemName;			
				 query = "SELECT " + queryColumns.toString() + queryColumnMetric + injectionCategory + injectionQuantity + " FROM " + tnprice + " " + queryJoinCategory+ queryJoinQuantity + queryJoinMetrics + queryWhereClause +  queryGroup + querySort;
			}
			query = wrapCondition(query,usageQuantityInConditions.get(),subQueryWhereClauseQuantity);
			query = wrapCondition(query,usageCategoriesInConditions.get(),subQueryWhereClauseCategories);			
		}
		//#0 просто запрос, если есть категории то приджойним, если есть условие по категории то оберенам запросом с условием
		else
		{
			query = "SELECT " + queryColumns.toString() + injectionCategory + " FROM " + tnprice + " " + queryJoinCategory +  queryWhereClause +  queryGroup + querySort ;			
			query = wrapCondition(query,usageCategoriesInConditions.get(),subQueryWhereClauseCategories);
		}		

		return query;
	}


	private static String wrapCondition(String query, Boolean wrap, String condition)
	{
		 if (wrap)
			{
				query = "SELECT * FROM ("+query+") " + condition;
			}
		 return query;
	}

	private static String getQueryGroup(List<ReportDesignerField> selectedList)
	{
		StringBuilder queryGroup = new StringBuilder();
		for (int i = 0; i < selectedList.size(); i++)
		{
			queryGroupBuilder(queryGroup, selectedList.get(i));
		}
		return queryGroup.toString();
	}

	private static String getQueryExceptionColumnCondition(List<ReportDesignerField> selectedList)
	{
		StringBuilder queryWhereClause = new StringBuilder();
		
		for (ReportDesignerField rf : selectedList)
		{	
			if (isExceptionColumnCondition(rf.getColumnSource()))
			{
				queryWhereBuilder(queryWhereClause, rf, "AND", tnprice);
				queryWhereBuilder(queryWhereClause, rf, "AND", tnr);
				queryWhereBuilder(queryWhereClause, rf, "AND", tnrI);
			}			
		}
		
		if (queryWhereClause.length() > 0)
		{
			queryWhereClause.insert(0, " WHERE ");
		}
		return queryWhereClause.toString();
	}
	
	private static String getQueryConditionsMetric(List<ReportDesignerField> selectedList)
	{
		StringBuilder queryWhereClause = new StringBuilder();
		
		for (ReportDesignerField rf : selectedList)
		{
			if (rf.getColumnSource().fullname().equals(ProductSubitem.Column.SUBITEM_CODE.fullname()))
			{
				queryWhereBuilder(queryWhereClause, rf, "AND", tnpsubitems);
			}			
		}
		
		if (queryWhereClause.length() > 0)
		{
			queryWhereClause.insert(0, " WHERE ");
		}
		return queryWhereClause.toString();
	}
	
	private static String getQueryConditions(List<ReportDesignerField> selectedList, String tableBelongs) {
		StringBuilder queryWhereClause = new StringBuilder();
		if (selectedList.size() > 0)
		{
			for (ReportDesignerField rf : selectedList)
			{
				if (!isExceptionColumnCondition(rf.getColumnSource()))
				{
					queryWhereBuilder(queryWhereClause, rf, "AND", tableBelongs);
				}
			}
		}
		if (queryWhereClause.length() > 0)
		{
			queryWhereClause.insert(0, " WHERE ");
		}
		return queryWhereClause.toString();
	}
	
	private static boolean isExceptionColumnCondition(IColumn tColumn)
	{
		if (tColumn.fullname().equals(Product.Column.PRODUCT_QUANTITY.fullname())) {return true;}
		if (tColumn.fullname().equals(ReceiptDB.Column.AMOUNT_TOTAL.fullname())) {return true;}
		if (tColumn.fullname().equals(ReceiptDB.Column.AMOUNT_CASH_REAL.fullname())) {return true;}
		if (tColumn.fullname().equals(ReceiptDB.Column.AMOUNT_CARD.fullname())) {return true;}
		if (tColumn.fullname().equals(ReceiptDBItem.Column.PRODUCT_QUANTITY.fullname())) {return true;}
		if (tColumn.fullname().equals(ReceiptDBItem.Column.PRODUCT_SUM_BEFORE_DISCOUNT.fullname())) {return true;}
        return tColumn.fullname().equals(ReceiptDBItem.Column.PRODUCT_SUM.fullname());
    }
	
	private static void queryWhereBuilder(StringBuilder whereClause, ReportDesignerField rf, String andOr, String tableBelongs)
	{
		if (!rf.isBelongsTable(tableBelongs)) {
			return;
		}
		String param = "";
		String quoteStart = " '";
		String quoteEnd = "' ";
		if (rf.getCondition() == Condition.LIKE)
		{
			quoteStart = " '%";
			quoteEnd = "%' ";
		}

		param = rf.getSqlCondition() + "  " + rf.getCondition().representSql + quoteStart + rf.getConditionValue() + quoteEnd;
		// }

		whereClause.append(whereClause.length() == 0 ? param : " " + andOr + " " + param);
	}
	private static String getQuerySort(List<ReportDesignerField> selectedList)
	{
		StringBuilder querySort = new StringBuilder();
		if (selectedList.size() > 0)
		{
			for (ReportDesignerField rf : selectedList)
			{
				querySortBuilder(querySort, rf);
			}

			querySort.insert(0, " ORDER BY ");
		}
		return querySort.toString();
	}

	private static void queryColumnBuilder(StringBuilder queryColumns, ReportDesignerField rf, String tableBelongs)
	{
		if (!rf.isBelongsTable(tableBelongs)) {
			return;
		}
		String source = rf.getPartSqlQuery();
		if (source.length() == 0)
		{
			source = rf.getColumnSource().fullname();
		}
		source += " AS " + rf.getSqlCondition();
		queryColumns.append(queryColumns.length() == 0 ? source : " , " + source);
	}

	private static void queryGroupBuilder(StringBuilder queryGroup, ReportDesignerField rf)
	{
		String source = rf.getSqlCondition();
		if (rf.isDate())
		{
			source = "date((" + rf.getColumnSource().fullname() + " /1000) ,'unixepoch')  ";
		}
		queryGroup.append(queryGroup.length() == 0 ? " GROUP BY " + source : " , " + source);
	}

	

	private static void querySortBuilder(StringBuilder whereClause, ReportDesignerField rf)
	{
		String param = "";
		if (rf.isDate())
		{
			param = "date((" + rf.getColumnSource().fullname() + " /1000) ,'unixepoch')  " + (rf.getSortDirection() ? " ASC " : " DESC ");
		}
		else
		{	
			param = rf.getSqlCondition() + "  " + (rf.getSortDirection() ? " ASC " : " DESC ");
		}
		whereClause.append(whereClause.length() == 0 ? param : " , " + param);
	}	

	private static TableRow createTableRow(Context context)
	{
		TableRow tableRow = new TableRow(context);
		tableRowLayoutParams.setMargins(0, 0, 0, 1);
		tableRow.setLayoutParams(tableRowLayoutParams);
		return tableRow;
	}

	private static TextView createTextView(Context context)
	{
		TextView textView = new TextView(context);
		textView.setTextColor(Colors.COLOR_TEXT_NORMAL);
		textView.setPadding(15, 8, 15, 8);
		return textView;
	}

	private class GeneratorReportDesigner extends AsyncTask<Void, Void, Void> 
	{
		private final boolean showProgressDialog;
		private static final String PD_TAG = "progress_dialog";

		public GeneratorReportDesigner(boolean showProgressDialog) {			
			this.showProgressDialog = showProgressDialog;
		}

		@Override
		protected void onPreExecute() {
			if (showProgressDialog) {
				new FrgProgressDialog().show(((ActivityCommonKeyboard) context).getSupportFragmentManager() , PD_TAG);
			}
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			generateReport();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if (showProgressDialog) {
				DialogFragment frg = (DialogFragment) ((ActivityCommonKeyboard) context).getSupportFragmentManager().findFragmentByTag(PD_TAG);
				if (frg != null) frg.dismiss();
			}	
			if (aRDR.isVisible())
			{aRDR.refreshView(tableViewHeader, tableView);}
		}
	}	
	
}




