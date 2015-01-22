package com.codeex.kchat;

import javax.swing.text.*;

public class TextFieldLimit extends PlainDocument {
	private static final long serialVersionUID = 1L;
	
	private int limit;
	
	public TextFieldLimit(int limit) {
		super();
		this.limit = limit;
	}
	
	public void insertString (int offset, String str, AttributeSet as) throws BadLocationException {
		if (str == null)
			return;
		
		if (getLength()+str.length() <= limit) {
			super.insertString(offset, str, as);
		}
	}
	
	
}
