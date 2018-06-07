package com.rssheap.interfaces;

public interface IDialogCallback {
	void onOk(Object data, String string);
	void onCancel(String caller);
}
