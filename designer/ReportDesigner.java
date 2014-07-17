package *.designer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import *.UtilsDate;
import *.designer.ReportDesignerField.ReportFields;
import *.designer.ReportDesignerHelper.TypeReportDesigner;

public class ReportDesigner implements Serializable {	
	private static final long serialVersionUID = -8285017661858124279L;
	
	transient List<ReportDesignerField> availableLines = new ArrayList<>();
	transient List<ReportDesignerField> availableColumns = new ArrayList<>();
	transient List<ReportDesignerField> availableConditions = new ArrayList<>();
	transient List<ReportDesignerField> availableSort = new ArrayList<>();

	 List<ReportDesignerField> selectedLines,selectedColumns ,selectedConditions,selectedSort; 

	private String name_rd;
	private String caption_rd;
    ReportDesignerHelper.TypeReportDesigner typeReport;

	public ReportDesigner(TypeReportDesigner typeReport)
	{
		this.typeReport = typeReport;
		determineStructure();
		clearSelected();
	}

	public void setTypeReport(TypeReportDesigner typeReport)
	{
		this.typeReport = typeReport;
		determineStructure();
		clearSelected();
	}

	public TypeReportDesigner getTypeReport()
	{
		return this.typeReport;
	}
	
	private void determineStructure()
	{
		availableLines = new ArrayList<>();
		availableColumns = new ArrayList<>();
		availableConditions = new ArrayList<>();
		availableSort = new ArrayList<>();
		switch (typeReport) {
		case PRODUCT: {
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_NAME));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_CODE));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_ARTICLE));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_BARCODE));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_CATEGORY));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_METRIC));			
			
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_NAME));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_CODE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_ARTICLE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_BARCODE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_PRICE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_CATEGORY));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.PRODUCT_QUANTITY));
			break;
		}
		case RECEIPT: {
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_DATE));			
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_NUMBER));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_CASHIER_NAME));
			// receipt items			
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_NAME));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_ARTICLE));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_BARCODE));
			availableLines.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_CODE));
			

			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_DATE));			
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_NUMBER));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_CASHIER_NAME));			
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_AMOUNT_TOTAL));			
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_AMOUNT_TOTAL_DEB));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_AMOUNT_TOTAL_KRED));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_AMOUNT_CASH_REAL));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_AMOUNT_CARD));
			// receipt items
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_NAME));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_ARTICLE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_BARCODE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_CODE));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_DIVISIBILITY));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_QUANTITY));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_SUM));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_SUM_BEFORE_DISCOUNT));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_TAX));
			availableColumns.add(ReportDesignerField.getInstance(ReportFields.RECEIPT_ITEM_PRODUCT_COMMENT));
			
			
			break;
		}
		default: {
			break;
		}

		}

		prepareReportFieldsList(availableConditions, availableLines);
		prepareReportFieldsList(availableConditions, availableColumns);
		prepareReportFieldsList(availableSort, availableLines);
		prepareReportFieldsList(availableSort, availableColumns);
		prepareReportFieldsListCondition();		

	}

	private void prepareReportFieldsListCondition() {
		int iMax = availableConditions.size();
		for (int i = 0 ; i < iMax ; i++)
		{
			if (availableConditions.get(i).isDate() && availableConditions.get(i).isStartDay() == null)
			{				
				ReportDesignerField rdf = ReportDesignerField.getInstance(availableConditions.get(i).getReportFields(),true);
				availableConditions.set(i, rdf);				
				rdf = ReportDesignerField.getInstance(availableConditions.get(i).getReportFields(),false);
				availableConditions.add(rdf);
			}
		}
	}



	public void refreshAfterSerializable()
	{
		determineStructure();
		checkList( availableLines,  selectedLines);
		checkList( availableColumns,  selectedColumns);		
		checkList( availableConditions,  selectedConditions);
		checkList( availableSort,  selectedSort);		
	}
	
	private static void checkList(List<ReportDesignerField> dest, List<ReportDesignerField> source)
	{
		for (ReportDesignerField tRF : source)
		{
			if (dest.contains(tRF))
			{
				dest.remove(tRF);
			}
		}
	}
	
	private void prepareReportFieldsList(List<ReportDesignerField> dest, List<ReportDesignerField> source)
	{
		for (ReportDesignerField rf : source)
		{
			if (!dest.contains(rf))
			{
				dest.add(rf);
			}
		}
	}

	public String getName()
	{
		return this.name_rd;
	}

	public void setName(String name)
	{
		this.name_rd = name;
	}

	public String getCaption()
	{
		return this.caption_rd;
	}

	public void setCaption(String caption)
	{
		this.caption_rd = caption;
	}
	
	private void clearSelected()
	{
		selectedLines = new ArrayList<>();
		selectedColumns = new ArrayList<>();
		selectedConditions = new ArrayList<>();
		selectedSort = new ArrayList<>();
	}
	
	public void check()
	{
		checkList(this.selectedColumns,this.selectedLines);
		checkSelectedSort();
		checkConditions();
	}
	
	private void checkSelectedSort()
	{

		int index = 0;
		while (index < this.selectedSort.size())
		{
			if((!this.selectedLines.contains(this.selectedSort.get(index))) && (!this.selectedColumns.contains(this.selectedSort.get(index))))
			{
				this.selectedSort.remove(index);
				continue;
			}
			index++;
		}
		

		
		for (ReportDesignerField tRF : this.selectedLines)
		{
			if (this.selectedSort.contains(tRF))
			{
				 
				for (int ind = 0; ind < this.selectedSort.size(); ind++)
				{
					if (tRF.equals(this.selectedSort.get(ind)))
					{
						tRF.setSortDirection(this.selectedSort.get(ind).getSortDirection());
						this.selectedSort.remove(ind);
						break;
					}
				}				
			}
			else
			{
				tRF.setSortDirection(true);
			}
		}

		int offset = 0;
		if (this.selectedSort.size() > 0)
		{
			offset = 1;
		}
		for (int i = 0; i < (this.selectedLines.size() - offset); i++)
		{
			this.selectedSort.add(i, this.selectedLines.get(i));
		}
	}
	
	private void checkConditions()
	{
		for (ReportDesignerField rf : this.selectedConditions)
		{
			
			if (rf.getConditionValue() == null || rf.getConditionValue().isEmpty())
			{
				String tValue = "";
				if (rf.isNumeric())
				{					
					tValue = "0";
				}
				else if (rf.isDate())
				{
					tValue = UtilsDate.getFormatedDateAsString(ReportDesignerHelper.showDateFormat, rf.isStartDay() ? UtilsDate.getTimestampStartCurrentDay() : UtilsDate.getTimestampEndCurrentDay());
				}
				rf.setConditionValue(tValue);
			}

		}
	}

}
