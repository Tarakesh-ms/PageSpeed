/**
 * 
 */
package com.tms.pagespeed.ui;


import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tms.pagespeed.R;
import com.tms.pagespeed.data.Preference;

/**
 * @author Tarak
 * 
 */
public class StrategyDialogFragment extends DialogFragment {
	
	

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Context context = getActivity().getApplicationContext();
		final String[] items  = context.getResources().getStringArray(R.array.entries_list_strategy);
	    final Preference pref = Preference.getInstance(context);
	    int selectedItem = 0;
		String strategy = pref.getStrategy();
		for (int i=0; i<items.length; i++){
			if(items[i].toLowerCase(Locale.ENGLISH).equals(strategy)){
				selectedItem = i;
				break;
			}
		}
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.strategy).setSingleChoiceItems(R.array.entries_list_strategy, selectedItem,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						pref.setStrategy(items[which]);

					}
				});
		return builder.create();
	}
}
