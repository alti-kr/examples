package *.designer;

import java.io.Serializable;

import android.view.Gravity;

import *.R;
import *.Utils;
import *.UtilsDate;
import *.db.Databases.ColumnType;
import *.db.Databases.IColumn;
import *.db.Category;
import *.db.Product;
import *.db.ProductSubitem;
import *.db.ReceiptDB;
import *.db.ReceiptDB.ReceiptType;
import *.db.ReceiptDBItem;
import *.designer.ReportDesignerHelper.Condition;

public class ReportDesignerField implements Serializable {

	private static final long serialVersionUID = -8570218683665852222L;
	private final ReportFields reportFields;
	private boolean countResult; // т.е. по этой колонки будут считаться промежуточные и общие итоги
	private boolean sortDirection; // true - возрастание ( ASC ) или false - убывание ( DESC )
	private Condition condition; // условие "== , <= " и т.д. = enum Condition
	private String conditionValue;
	private Boolean startDay; // for date
	public static final String  columnProductSubitemName = "fullProdSubitemName";
	private ReportDesignerField(ReportFields reportFields)
	{

		this.reportFields = reportFields;
		this.countResult = false;
		this.sortDirection = true;
		this.condition = Condition.EQUALLY;
		this.conditionValue = "";
		this.startDay = null;
	}

	public Boolean isStartDay() {
		return startDay;
	}

	public void setStartDay(Boolean startDay) {
		this.startDay = startDay;
	}

	public static ReportDesignerField getInstance(ReportFields reportFields)
	{
		return new ReportDesignerField(reportFields);
	}

	public static ReportDesignerField getInstance(ReportFields reportFields, Boolean startDay)
	{
		ReportDesignerField rdf = new ReportDesignerField(reportFields);
		rdf.setStartDay(startDay);
		return rdf;
	}

	public void clearValues() {
		this.countResult = false;
		this.sortDirection = true;
		this.condition = Condition.EQUALLY;
		this.conditionValue = "";
	}

	public String getNameHeader()
	{
		if (this.isDate() && this.isStartDay() != null) {
			return this.reportFields.getNameHeader() + " " + Utils.getString(this.isStartDay() ? R.string.act_archive_from : R.string.act_archive_to).toLowerCase();
		}
		return this.reportFields.getNameHeader();
	}

	public void setCountResult(boolean countResult)
	{
		if (this.reportFields.getColumnSource().getColumnType() == ColumnType.INTEGER){this.countResult = countResult;}
		else if (this.reportFields.getColumnSource() == Product.Column.PRODUCT_QUANTITY){this.countResult = countResult;}
	}

	public boolean getCountResult()
	{
		return this.countResult;
	}

	public void setSortDirection(boolean direction)
	{
		this.sortDirection = direction;
	}

	public boolean getSortDirection()
	{
		return this.sortDirection;
	}

	public void setCondition(Condition condition)
	{
		this.condition = condition;
	}

	public Condition getCondition()
	{
		return this.condition;
	}

	public void setConditionValue(String conditionValue)
	{
		this.conditionValue = conditionValue;
	}

	public String getConditionValue()
	{
		return this.conditionValue;
	}

	public String getPartSqlQuery()
	{
		return this.reportFields.getPartSqlQuery();
	}

	public IColumn getColumnSource()
	{
		return this.reportFields.columnSource;
	}

	public String getSqlCondition()
	{
		return this.reportFields.sqlCondition;
	}

	public String getFormated(String asString, boolean isGroup) // 0-String 1- num, 2- Date, 3-sum ,4 _quantity
	{
		if (isGroup && !this.countResult) {
			return "";
		}
		switch (this.reportFields.getformatType()) {
		case 1: {
			return asString;
		}
		case 2: {
			return asString;
		}
		case 3: {
			return Utils.formatAmount(asString);
		}
		case 4: {
			if (asString == null) return "null";
			return asString;
		}
		default:
			return asString;
		}
	}

	public String getFormatedSQL(String inputString) // 0-String 1- num, 2- Date, 3-sum ,4 _quantity
	{
		switch (this.reportFields.getformatType()) {
		case 1: {
			return inputString;
		}
		case 2: {
			return Utils.getStringFromLong(UtilsDate.getDateStringToLong(ReportDesignerHelper.showDateFormat, inputString + (this.isStartDay() == null ? ":00" : (this.isStartDay() ? ":00" : ":59"))));
		}
		case 3: {
			return Utils.getStringFromLong(Utils.parseAmount(inputString));
		}
		case 4: {
			return inputString;
		}
		default:
			return inputString;
		}
	}

	public String getBelongsTab()
	{
		return this.reportFields.getBelongsTab();
	}

	public boolean isBelongsTable(String tab)
	{
		return this.reportFields.getBelongsTab().equals(tab);
	}

	public boolean isNumeric()
	{
		if (this.reportFields.getColumnSource() == ProductSubitem.Column.SUBITEM_CODE) {return false;}
		if (this.reportFields.getColumnSource() == Product.Column.PRODUCT_QUANTITY) {return true;}
		return this.reportFields.getColumnSource().getColumnType() == ColumnType.INTEGER;
	}

	public boolean isDate() {
		return this.reportFields.getColumnSource() == ReceiptDB.Column.CR_DATE;
	}

	public ReportFields getReportFields()
	{
		return this.reportFields;
	}

	public int getGravity()
	{
		switch (this.reportFields.getformatType()) {
		case 2: {
			return Gravity.CENTER;
		}
		case 3: {
			return Gravity.RIGHT;
		}
		case 4: {
			return Gravity.RIGHT;
		}
		}
		return Gravity.LEFT;
	}

	@Override
	public boolean equals(Object obj)
	{
        // TODO  equals() should check the class of its parameter at line 227
		if (this.isDate() && (this.isStartDay() != null))
		{	
			return this.reportFields.equals(((ReportDesignerField) obj).getReportFields()) && this.isStartDay().equals(((ReportDesignerField) obj).isStartDay());
		}
		return this.reportFields.equals(((ReportDesignerField) obj).getReportFields());
	}

	
	
	
	private static final String receiptPlusMinus = "(CASE WHEN (" + ReportDesignerHelper.tnr + "." + ReportDesignerHelper.rdbT + " = '"
				+ ReceiptType.SALE_RECEIPT.getType() + "') THEN 1 ELSE (-1) END) * ";
	private static final String receiptDeb = "(CASE WHEN (" + ReportDesignerHelper.tnr + "." + ReportDesignerHelper.rdbT + " = '"
		+ ReceiptType.SALE_RECEIPT.getType() + "') THEN 1 ELSE 0 END) * ";
	private static final String receiptKred = "(CASE WHEN (" + ReportDesignerHelper.tnr + "." + ReportDesignerHelper.rdbT + " = '"
			+ ReceiptType.RETURN_RECEIPT.getType() + "') THEN -1 ELSE 0 END) * ";		
	private static final String queryQuantity = " CAST((CASE WHEN " + Product.Column.HAS_SUBITEMS.fullname() + " = '1'  THEN  SUM ("+ProductSubitem.Column.QUANTITY.getName()+")   ELSE "+Product.Column.PRODUCT_QUANTITY.fullname() +" END) AS INTEGER)";
		
	public enum ReportFields {

			PRODUCT_NAME(Product.Column.PRODUCT_NAME, "", "", Utils.getString(R.string.rd_rf_product_name), 0),
			PRODUCT_CODE(Product.Column.PRODUCT_CODE, "_product_code", "", Utils.getString(R.string.rd_rf_product_code), 1),
			PRODUCT_ARTICLE(Product.Column.PRODUCT_ARTICLE,"","",Utils.getString(R.string.rd_rf_product_article),1),
			PRODUCT_BARCODE(Product.Column.PRODUCT_BARCODE,"","",Utils.getString(R.string.rd_rf_product_barcode),1),
			PRODUCT_PRICE(Product.Column.PRODUCT_PRICE,"","",Utils.getString(R.string.rd_rf_product_price),3),
			PRODUCT_CATEGORY(Category.Column.NAME,"_cat","",Utils.getString(R.string.rd_rf_product_category),0),
			PRODUCT_QUANTITY(Product.Column.PRODUCT_QUANTITY,"_calc",queryQuantity,Utils.getString(R.string.rd_rf_product_remains),4),
			PRODUCT_METRIC(ProductSubitem.Column.SUBITEM_CODE,columnProductSubitemName,"",Utils.getString(R.string.rd_rf_product_metric),0),
			
			

			RECEIPT_ID(ReceiptDB.Column._ID, "", "", "", 0),
			RECEIPT_DATE(ReceiptDB.Column.CR_DATE, "", "date((" + ReportDesignerHelper.tnr + "." + ReceiptDB.Column.CR_DATE.getName() + " /1000) ,'unixepoch')  ", Utils
				.getString(R.string.rd_rf_receipt_date), 2), // milliseconds
			RECEIPT_NUMBER(ReceiptDB.Column.RECEIPT, "", "", Utils.getString(R.string.rd_rf_receipt_number), 0),
			RECEIPT_CASHIER_NAME(ReceiptDB.Column.CASHIER_NAME, "", "", Utils.getString(R.string.rd_rf_receipt_cashier_name), 0),
			RECEIPT_AMOUNT_TOTAL(ReceiptDB.Column.AMOUNT_TOTAL, "", " CAST(SUM("+receiptPlusMinus+ReportDesignerHelper.tnr + "." + ReceiptDB.Column.AMOUNT_TOTAL.getName() +" ) AS INTEGER) ", Utils.getString(R.string.rd_rf_receipt_amount_tot), 3),
			RECEIPT_AMOUNT_TOTAL_DEB(ReceiptDB.Column.AMOUNT_TOTAL, "_deb", " CAST(SUM("+receiptDeb+ReportDesignerHelper.tnr + "." + ReceiptDB.Column.AMOUNT_TOTAL.getName() +" ) AS INTEGER) ", Utils
				.getString(R.string.rd_rf_receipt_amount_tot_deb), 3),
			RECEIPT_AMOUNT_TOTAL_KRED(ReceiptDB.Column.AMOUNT_TOTAL, "_kred", "CAST(SUM("+receiptKred+ReportDesignerHelper.tnr + "." + ReceiptDB.Column.AMOUNT_TOTAL.getName() +" ) AS INTEGER) ", Utils
				.getString(R.string.rd_rf_receipt_amount_tot_kred), 3),
			RECEIPT_AMOUNT_CASH_REAL(ReceiptDB.Column.AMOUNT_CASH_REAL,"","CAST(SUM("+receiptPlusMinus+ ReportDesignerHelper.tnr + "." + ReceiptDB.Column.AMOUNT_CASH_REAL.getName()+") AS INTEGER) ", Utils.getString(R.string.rd_rf_receipt_amount_cash), 3),
			RECEIPT_AMOUNT_CARD(ReceiptDB.Column.AMOUNT_CARD,"","CAST(SUM("+receiptPlusMinus+ ReportDesignerHelper.tnr + "." + ReceiptDB.Column.AMOUNT_CARD.getName()+") AS INTEGER) ", Utils.getString(R.string.rd_rf_receipt_amount_card), 3),
			
			
			RECEIPT_ITEM_ID(ReceiptDBItem.Column._ID, "", "", "", 0),
			RECEIPT_ITEM_PARENT_ID(ReceiptDBItem.Column.PARENT_ID, "", "", "", 0),
			RECEIPT_ITEM_PRODUCT_CODE(ReceiptDBItem.Column.PRODUCT_CODE, "", "", Utils.getString(R.string.rd_rf_product_code), 1),
			RECEIPT_ITEM_PRODUCT_NAME(ReceiptDBItem.Column.PRODUCT_NAME, "", "", Utils.getString(R.string.rd_rf_product_name), 0),			
			RECEIPT_ITEM_PRODUCT_SUM_BEFORE_DISCOUNT(ReceiptDBItem.Column.PRODUCT_SUM_BEFORE_DISCOUNT, "", "CAST(SUM("+receiptPlusMinus+ ReportDesignerHelper.tnrI + "." + ReceiptDBItem.Column.PRODUCT_SUM_BEFORE_DISCOUNT+") AS INTEGER) ", Utils.getString(R.string.rd_rf_receipt_product_sum_before_dis), 3),
			RECEIPT_ITEM_PRODUCT_SUM(ReceiptDBItem.Column.PRODUCT_SUM, "", "CAST(SUM("+receiptPlusMinus+ ReportDesignerHelper.tnrI + "." + ReceiptDBItem.Column.PRODUCT_SUM+") AS INTEGER) ", Utils.getString(R.string.rd_rf_product_sum), 3),
			RECEIPT_ITEM_PRODUCT_QUANTITY(ReceiptDBItem.Column.PRODUCT_QUANTITY, "", "CAST(SUM("+receiptPlusMinus+ ReportDesignerHelper.tnrI + "." + ReceiptDBItem.Column.PRODUCT_QUANTITY+")  AS INTEGER) ", Utils.getString(R.string.rd_rf_product_quantity), 4),
			RECEIPT_ITEM_PRODUCT_PRICE(ReceiptDBItem.Column.PRODUCT_PRICE, "", "", Utils.getString(R.string.rd_rf_product_price), 3),
			RECEIPT_ITEM_PRODUCT_TAX(ReceiptDBItem.Column.PRODUCT_TAX, "", "", Utils.getString(R.string.rd_rf_product_tax), 0),
			RECEIPT_ITEM_PRODUCT_ARTICLE(ReceiptDBItem.Column.PRODUCT_ARTICLE, "", "", Utils.getString(R.string.rd_rf_product_article), 1),
			RECEIPT_ITEM_PRODUCT_BARCODE(ReceiptDBItem.Column.PRODUCT_BARCODE, "", "", Utils.getString(R.string.rd_rf_product_barcode), 1),
			RECEIPT_ITEM_PRODUCT_DIVISIBILITY(ReceiptDBItem.Column.PRODUCT_DIVISIBILITY, "", "", Utils.getString(R.string.rd_rf_product_divisibility), 1),			
			RECEIPT_ITEM_PRODUCT_COMMENT(ReceiptDBItem.Column.COMMENT, "comment_item", "", Utils.getString(R.string.rd_rf_receipt_product_comment), 0);

		private final IColumn columnSource;
		private final String nameHeader; // имя поля показываемое пользователю
		private String sqlCondition; // имя колонки в SQL запросе = .ICcolumn..getName()+"subName"
		private final String partSqlQuery; // готовая часть sql query
		private boolean countResult;
		private boolean sortDirection;
		private Condition condition;
		private String conditionValue;
		private final String belongsTatble;
		private final int formatType;

		/**
		 * 
		 * @param columnSource
		 * @param subName
		 * @param sqlQuery
		 * @param nameHeader
		 * @param formatType
		 *            форматировать при выводе как 0-String 1- num, 2- Date, 3-sum ,4 _quantity
		 */

		private ReportFields(IColumn columnSource, String subName, String sqlQuery, String nameHeader, int formatType) {
			this.columnSource = columnSource;
			this.sqlCondition = columnSource.getName();
			if (subName.trim().length() > 0)
			{
				this.sqlCondition = subName.trim();
			}
			this.partSqlQuery = sqlQuery;
			this.nameHeader = nameHeader;
			this.belongsTatble = ReportDesignerHelper.getBelongsTable(this.columnSource);
			this.formatType = formatType;
		}

		public String getNameHeader()
		{
			return this.nameHeader;
		}

		public boolean getCountResult()
		{
			return this.countResult;
		}

		public boolean getSortDirection()
		{
			return this.sortDirection;
		}

		public Condition getCondition()
		{
			return this.condition;
		}

		public String getConditionValue()
		{
			return this.conditionValue;
		}

		public String getPartSqlQuery()
		{
			return this.partSqlQuery;
		}

		public IColumn getColumnSource()
		{
			return this.columnSource;
		}

		public int getformatType() // 0-String 1- num, 2- Date, 3-sum ,4 _quantity
		{
			return this.formatType;
		}

		public String getBelongsTab()
		{
			return this.belongsTatble;
		}
	}
}
