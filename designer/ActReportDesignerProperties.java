package *.designer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import *.FrgDialog;
import *.R;

public class ActReportDesignerProperties extends Fragment {

    private ActReportDesignerDescription frgDescription;
	ActReportDesignerStructure frgStructure;
	ActReportDesignerSelect frgSelect;
    private int actPropLeftContainer;
    private int actPropRightContainer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.act_designer_frg_properties, container, false);
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();

		frgDescription = (ActReportDesignerDescription) getChildFragmentManager().findFragmentByTag("frgDescription");
		if (frgDescription == null) {
			frgDescription = new ActReportDesignerDescription();
            int actPropCenterContainer = R.id.frm_designer_properties_ll;
            ft.add(actPropCenterContainer, frgDescription,"frgDescription");
		}

		frgStructure = (ActReportDesignerStructure) getChildFragmentManager().findFragmentByTag("frgStructure");
		if (frgStructure == null) {
            frgStructure = ActReportDesignerStructure.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedLines, 0);
            actPropLeftContainer = R.id.frm_designer_properties_left;
            ft.add(actPropLeftContainer, frgStructure,"frgStructure");
		}

		frgSelect = (ActReportDesignerSelect) getChildFragmentManager().findFragmentByTag("frgSelect");
		if (frgSelect == null) {
            frgSelect = ActReportDesignerSelect.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableLines);
            actPropRightContainer = R.id.frm_designer_properties_right;
            ft.add(actPropRightContainer, frgSelect,"frgSelect");
		}
		frgStructure.setListFields(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedLines);
		frgSelect.setListFields(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableLines);

		
		
		ft.hide(frgStructure);
		ft.hide(frgSelect);		
		ft.commit();

		RadioGroup rgProperties = (RadioGroup) mainView.findViewById(R.id.act_designer_rb_prop);
		rgProperties.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switchViews(checkedId);
			}
		});
		Button buttonSave = (Button) mainView.findViewById(R.id.act_designer_frg_desc_button_save);
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (((ActReportDesigner) getActivity()).currentReportDesigner.getName().isEmpty())
				{
					FrgDialog.fragmentize(
						new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.required_fields_are_empty) + ": " + getString(R.string.name)).setMessage("")
							.setCancelable(true)
							.setNegativeButton(getString(R.string.btn_cancel), null)).show(getActivity().getSupportFragmentManager(), null);
					return;
				}
				((ActReportDesigner) getActivity()).tryCloseKeyboard();
				((ActReportDesigner) getActivity()).saveReportDesigner();
				
			}
		});
		Button buttonCancel = (Button) mainView.findViewById(R.id.act_designer_frg_desc_button_canсel);
		buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((ActReportDesigner) getActivity()).tryCloseKeyboard();
				((ActReportDesigner) getActivity()).disableChangeView();
			}
		});
		return mainView;
	}
	
	private void switchViews(int checkedId)
	{
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		switch (checkedId)
		{
		case R.id.act_des_frg_rb_prop_description: {
			ft.hide(frgSelect);
			ft.hide(frgStructure);
			ft.show(frgDescription);
			break;
		}
		case R.id.act_des_frg_rb_prop_lines: {
            frgStructure = ActReportDesignerStructure.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedLines, 0);
            frgSelect = ActReportDesignerSelect.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableLines);
			ft.hide(frgDescription);
            ft.replace(actPropRightContainer,frgSelect);
			ft.replace(actPropLeftContainer,frgStructure);
			break;
		}
		case R.id.act_des_frg_rb_prop_columns: {
            frgStructure = ActReportDesignerStructure.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedColumns, 1);
            frgSelect = ActReportDesignerSelect.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableColumns);
            ft.replace(actPropLeftContainer,frgStructure);
            ft.replace(actPropRightContainer,frgSelect);
			ft.hide(frgDescription);
			break;
		}
		case R.id.act_des_frg_rb_prop_conditions: {
            frgStructure = ActReportDesignerStructure.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedConditions, 2);
            frgSelect = ActReportDesignerSelect.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableConditions);
            ft.replace(actPropLeftContainer,frgStructure);
            ft.replace(actPropRightContainer,frgSelect);
			ft.hide(frgDescription);
			break;
		}
		case R.id.act_des_frg_rb_prop_sorting: {
            frgStructure = ActReportDesignerStructure.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().selectedSort, 3);
            frgSelect = ActReportDesignerSelect.makeInstance(((ActReportDesigner) getActivity()).getCurrentReportDesigner().availableSort);
            ft.replace(actPropLeftContainer,frgStructure);
            ft.replace(actPropRightContainer,frgSelect);
			ft.hide(frgDescription);
			break;
		}
		}
		ft.commitAllowingStateLoss();
        getFragmentManager().executePendingTransactions();
	}
}
