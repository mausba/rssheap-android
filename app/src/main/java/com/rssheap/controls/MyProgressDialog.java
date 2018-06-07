package com.rssheap.controls;

import com.rssheap.BaseActivity;
import com.rssheap.R;

import android.app.Activity;
import android.app.Dialog;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class MyProgressDialog extends Dialog {
	
    public MyProgressDialog(Activity context) {
        super(context, R.style.NewDialog);
    }
	
	 public static MyProgressDialog show(BaseActivity activity) {
	        return show(activity, "", "", false, false, null);
	    }
	
	public static MyProgressDialog show(BaseActivity context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, false);
    }

    public static MyProgressDialog show(BaseActivity context, CharSequence title,
            CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static MyProgressDialog show(BaseActivity context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static MyProgressDialog show(BaseActivity context, CharSequence title,
            CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
        MyProgressDialog dialog = (MyProgressDialog) ((BaseActivity) context).dialog;
        if(dialog == null) dialog = new MyProgressDialog(context);
       
        if(!dialog.isShowing()) {	
	        dialog.setTitle(title);
	        dialog.setCancelable(cancelable);
	        dialog.setOnCancelListener(cancelListener);
	        /* The next line will add the ProgressBar to the dialog. */
	        dialog.setContentView(new ProgressBar(context), new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	        dialog.show();
        }

        return dialog;
    }
}
