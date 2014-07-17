package ***.designer;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import *.ActivityCommonKeyboard;
import *.Preferences.PreferenceKeys;
import *.R;
import *.Utils;

public class ActReportDesigner extends ActivityCommonKeyboard {

	private ActReportDesignerMain frgMain;
    private ActReportDesignerResult frgResult;
	ActReportDesignerProperties frgProperties;
	ReportDesignerRepository repDesRep;
	ReportDesigner currentReportDesigner;
    private int actLeftContainer, actRightContainer;
	public static final String POSITION_REPORT_INTENT = "positionReportInList";
	final static String titleRD =  Utils.getString(R.string.preferences_btn_report_designer);
	final static String titleRDNew = Utils.getString(R.string.rd_frg_main_header_create);
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);		
		int position = -1;
		if (getIntent().getExtras() != null) {
			position = getIntent().getExtras().getInt(POSITION_REPORT_INTENT, -1);
		}
		setContentView(position == -1 ? (Utils.isPortrait() ? R.layout.act_designer_p : R.layout.act_designer) : R.layout.act_designer_p);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		actLeftContainer = R.id.frm_designer_left;
		actRightContainer = R.id.frm_designer_right;
		refreshReports();

		if (position < 0)
		{
			frgMain = (ActReportDesignerMain) getSupportFragmentManager().findFragmentByTag("frgMain");
			if (frgMain == null) {
				frgMain = new ActReportDesignerMain();
				ft.add(actLeftContainer, frgMain);
			}
			frgResult = (ActReportDesignerResult) getSupportFragmentManager().findFragmentByTag("frgResult");
			if (frgResult == null) {
				frgResult = new ActReportDesignerResult();
				if (Utils.isLandscape())
				{

					ft.add(actRightContainer, frgResult,"frgResult");
					enableSeekBar(R.id.frm_designer_left_main, R.id.frm_designer_right, PreferenceKeys.KEY_REPORT_DESIGNER_FRAGMENTS_RATIO, 1, 1);
				}
			}			
			frgProperties = (ActReportDesignerProperties) getSupportFragmentManager().findFragmentByTag("frgProperty");
			if (frgProperties == null) {				
				frgProperties = new ActReportDesignerProperties();				
			}			
			ft.commit();
			setTitle(titleRD);
		}
		else {
			setCurrentReportDesigner(getReports().get(position));
			formReportFromOutside();
			setTitle(currentReportDesigner.getName());
		}		
	}

	
	
	
	public void refreshReports()
	{
		repDesRep = ReportDesignerRepository.getInstance();
		repDesRep.refreshReportDesigners();
	}

	public List<ReportDesigner> getReports() {
		refreshReports();
		return repDesRep.getReports();
	}

	public void setCurrentReportDesigner(ReportDesigner reportDesigner)
	{
		this.currentReportDesigner = reportDesigner;
		if (this.currentReportDesigner == null)
		{
			setTitle(titleRD);
			return;
		}
		setTitle(titleRD + ": " + (this.currentReportDesigner.getName().length()>0? this.currentReportDesigner.getName(): titleRDNew));
	}
	
	

	public void refresh()
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(actLeftContainer, frgMain);
		if (!Utils.isPortrait())
		{
			ft.replace(actRightContainer, frgResult);
		}
		ft.commit();
	}

	public ReportDesigner getCurrentReportDesigner()
	{
		return this.currentReportDesigner;
	}

	public void saveReportDesigner()
	{
		boolean isNew = false;
		if (!repDesRep.getReports().contains(currentReportDesigner))
		{
			isNew = true;
			repDesRep.add(currentReportDesigner);
		}

		repDesRep.save();
		if (Utils.isPortrait())
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			frgMain = (ActReportDesignerMain) getSupportFragmentManager().findFragmentByTag("frgMain");
			if (frgMain == null) {
				frgMain = new ActReportDesignerMain();
			}
			ft.replace(actLeftContainer, frgMain);
			ft.commit();			
		}
		else {
			if (isNew) {
				frgMain.setLastReportInList();
			}

			frgMain.refresh();
		}
		setCurrentReportDesigner(repDesRep.get(frgMain.getLastSelectedPosition()));
		Utils.makeToast(this, Utils.getString(R.string.rd_rb_description_button_save_toast), Toast.LENGTH_SHORT).show();
		if (Utils.isPortrait())
		{
			redrawLeftContainer();
		} else {

			frgResult = new ActReportDesignerResult();			
			refresh();
		}

		
	}

	public void disableChangeView()
	{
		setCurrentReportDesigner(null);
		if (Utils.isPortrait())
		{
			redrawLeftContainer();
		} else {

			frgResult = new ActReportDesignerResult();
			frgMain.setLastselectedPosition(-1);
			frgMain.refresh();
			refresh();
		}
	}
	private void redrawLeftContainer()
	{
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		frgMain = (ActReportDesignerMain) getSupportFragmentManager().findFragmentByTag("frgMain");
		if (frgMain == null) {
			frgMain = new ActReportDesignerMain();
		}
		ft.replace(actLeftContainer, frgMain);
		ft.commit();
	}


	public void delReportDesigner()
	{
		if (currentReportDesigner != null)
		{
			repDesRep.remove(currentReportDesigner);
			repDesRep.save();
			frgMain.refresh();
		}
		disableChangeView();
	}

	public void enableChangeView(boolean isNew)
	{
		if ((currentReportDesigner == null) && (!isNew))
		{
			disableChangeView();
		}
		else
		{
			if ((currentReportDesigner == null) || (isNew))
			{
				String rdNewName = frgMain.generateNameNewReport();
				currentReportDesigner = new ReportDesigner(ReportDesignerHelper.TypeReportDesigner.RECEIPT);
				currentReportDesigner.setName(rdNewName);
				setTitle(titleRD + ": " + rdNewName);
			}
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			frgProperties = new ActReportDesignerProperties();
			ft.replace(Utils.isPortrait() ? actLeftContainer : actRightContainer, frgProperties);
			ft.commit();
		}
	}


	public void formReportFromOutside()
	{
		if (currentReportDesigner == null) {
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		frgResult = new ActReportDesignerResult(currentReportDesigner);
		ft.replace(actLeftContainer, frgResult);
		ft.commit();
	}

	public void formReport()
	{
		if (currentReportDesigner == null) {
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		frgResult = new ActReportDesignerResult(currentReportDesigner);
		ft.replace(Utils.isPortrait() ? actLeftContainer : actRightContainer, frgResult);
		if (Utils.isPortrait()) {
			ft.addToBackStack(null);
		}
		ft.commit();
	}

}
