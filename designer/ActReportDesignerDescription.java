package *.designer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import *.FrgInput.OnInputFinishedListener;
import *.R;
import *.Utils;
import *.designer.ReportDesignerHelper.TypeReportDesigner;
import *.view.EditText;

public class ActReportDesignerDescription extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View mainView = inflater.inflate(R.layout.act_designer_frg_description, container, false);
		final EditText name = (EditText) mainView.findViewById(R.id.act_designer_frg_desc_header);
		final EditText descr = (EditText) mainView.findViewById(R.id.act_designer_frg_desc);

		name.setText(((ActReportDesigner) getActivity()).currentReportDesigner.getName());
		descr.setText(((ActReportDesigner) getActivity()).currentReportDesigner.getCaption());

		RadioGroup rgProperties = (RadioGroup) mainView.findViewById(R.id.act_designer_rb_basis);		

		((RadioButton) mainView.findViewById(R.id.act_designer_rb_basis_receipts)).setChecked(false);
		((RadioButton) mainView.findViewById(R.id.act_designer_rb_basis_remains)).setChecked(true);
		if (((ActReportDesigner) getActivity()).currentReportDesigner.getTypeReport() == TypeReportDesigner.RECEIPT)
		{
			((RadioButton) mainView.findViewById(R.id.act_designer_rb_basis_receipts)).setChecked(true);
			((RadioButton) mainView.findViewById(R.id.act_designer_rb_basis_remains)).setChecked(false);
		}
		rgProperties.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId)
			{
				switchTypeReport(checkedId);
			}
		});
		
		
		
		name.setOnInputFinishedListener(new OnInputFinishedListener() {
			@Override
			public boolean onInputFinished() {				
				((ActReportDesigner) getActivity()).currentReportDesigner.setName(name.getEditableText().toString());
				((ActReportDesigner) getActivity()).tryCloseKeyboard();
				return true;
			}
		});
		name.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(name.getEditableText().toString().isEmpty()) {showWarning(name);}
				((ActReportDesigner) getActivity()).currentReportDesigner.setName(name.getEditableText().toString());
			}
		});
		
		descr.setOnInputFinishedListener(new OnInputFinishedListener() {
			@Override
			public boolean onInputFinished() {				
				((ActReportDesigner) getActivity()).currentReportDesigner.setCaption(descr.getEditableText().toString());
				((ActReportDesigner) getActivity()).tryCloseKeyboard();
				return true;
			}
		});
		descr.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(name.getEditableText().toString().isEmpty()) {showWarning(name);return;}
				((ActReportDesigner) getActivity()).currentReportDesigner.setCaption(descr.getEditableText().toString());
			}
		});
		return mainView;
	}

	private void switchTypeReport(int checkedId)
	{
		switch (checkedId)
		{
		case R.id.act_designer_rb_basis_receipts: {
			((ActReportDesigner) getActivity()).currentReportDesigner.setTypeReport(TypeReportDesigner.RECEIPT);
			break;
		}
		case R.id.act_designer_rb_basis_remains: {
			((ActReportDesigner) getActivity()).currentReportDesigner.setTypeReport(TypeReportDesigner.PRODUCT);
			break;
		}
		}

	}

	private void showWarning(EditText et)
	{
		et.requestFocus();
		Utils.makeToast(getActivity(),Utils.getString(R.string.required_fields_are_empty), Toast.LENGTH_SHORT).show();
	}
}
