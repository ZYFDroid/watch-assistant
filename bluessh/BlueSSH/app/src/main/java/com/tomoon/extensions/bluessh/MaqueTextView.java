package com.tomoon.extensions.bluessh;
import android.widget.*;
import android.content.*;
import android.util.*;

public class MaqueTextView extends TextView
{
	public MaqueTextView(Context ctx){
		super(ctx);
	}
	public MaqueTextView(Context ctx,AttributeSet attrs){
		super(ctx,attrs);
	}
	public MaqueTextView(Context ctx,AttributeSet attrs,int i){
		super(ctx,attrs,i);
	}

	@Override
	public boolean isFocused()
	{
		// TODO: Implement this method
		return true;
	}
	
}
