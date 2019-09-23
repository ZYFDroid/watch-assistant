package com.tomoon.extensions.bluessh;

import android.app.*;
import android.widget.*;
import java.util.*;
import android.view.*;
import android.text.*;
import android.graphics.*;

public class ChartByUi
{
	public int jdss=2;
	public int padding=32;
	public int lineBold=2;
	int fgColor=0;
	Activity ctx;
	AbsoluteLayout viewHolder;
	ArrayList<View> createdView =new ArrayList<View>();
	//Thread mainThread;
	View[] lines;
	TextView[] tags;
	View[] points;
	int rulerMount;
	float[] datas;
	int pannelw;
	int pannelh;
	float cMax;
	TextView minPos;
	float cMin;
	int createdData=0;
	int maxDataAllowwance;
	String[] btmText;
	TextView[] btmLabel;
	TextView[] leftLavel;
	public ChartByUi(Activity mctx,AbsoluteLayout mviewholder,int maxDatas,float minRange,float maxRange,int rulerCount,int fgcolor,String xLabel,String yLabel){
		this.ctx=mctx;
		viewHolder=mviewholder;
		maxDataAllowwance=maxDatas;
		lines=new View[maxDatas-1];
		points=new View[maxDatas];
		tags=new TextView[maxDatas];
		btmLabel=new TextView[maxDatas];
		
		
		datas=new float[maxDatas];
		btmText=new String[maxDatas];
		
		fgColor=fgcolor;
		pannelw=mviewholder.getWidth();
		pannelh=mviewholder.getHeight();
		
		
		leftLavel=new TextView[rulerCount];
		for(int i=0;i<=maxDatas-1;i++){
			if(i<=maxDatas-2){
			lines[i]=newView();}
			points[i]=newView();
			tags[i]=newLabel();
			btmLabel[i]=newLabel();
			datas[i]=0f;
			btmText[i]="";
		}
		createBaseView(xLabel,yLabel,maxDatas,minRange,maxRange,rulerCount);
	}
	
	void createBaseView(String xlab,String ylab,int maxDatas,float minRange,float maxRange,int rulerCount){
		
		cMax=maxRange;
		cMin=minRange;
		rulerMount=rulerCount;
		createShape(padding,pannelh-padding,pannelw-padding*2);
		setPan(createShape(padding,padding,lineBold),padding,padding,lineBold,pannelh-padding*2);
		for(int i=0;i<rulerCount;i++){
			String rulbl=String.valueOf(jds((maxRange-(maxRange-minRange)/rulerCount*i)));
			createShape(padding-8,padding+i*((pannelh-padding*2)/rulerCount),8);
			leftLavel[i]=createLabel(rulbl,
			0,//padding-8-getTextWidth(rulbl),
			padding+i*((pannelh-padding*2)/rulerCount));
		}
		minPos=createLabel(String.valueOf(jds( minRange)),0,pannelh-padding);
		createLabel(ylab,0,0);
		createLabel(xlab,pannelw-padding,pannelh-padding);
		
		
		
		
	}
	
	void addValue(String label,float value){
		if(createdData<(maxDataAllowwance)){
			createdData++;
			
		}else{
			for(int i=1;i<=maxDataAllowwance-1;i++){
				datas[i-1]=datas[i];
				btmText[i-1]=btmText[i];
			}
		}
		datas[createdData-1]=value;
		btmText[createdData-1]=label;
		frame();
	}
	
	void cleanAll(){
		createdData=0;
		frame();
	}
	
	
	
	
	
	void frame(){
		//扫描最大最小值
		float max=cMax;
		float min=cMin;
		for(int i=0;i<createdData;i++)
		{
			if(datas[i]>max){max=datas[i];}

			if(datas[i]<min){min=datas[i];}
			}
		
		
		//应用最大最小值
		for(int i=0;i<rulerMount;i++){
			String rulbl=String.valueOf(jds((max-(max-min)/rulerMount*i)));
			leftLavel[i].setText(rulbl);
		}
		minPos.setText(String.valueOf(jds(min)));
		//绘制线段和点还有标签以及底部
		//先隐藏
		for(int i=0;i<=maxDataAllowwance-1;i++){
			hide(btmLabel[i]);
			hide(tags[i]);
			hide(points[i]);
			if(i<maxDataAllowwance-1){hide(lines[i]);}
		}
		for(int i=0;i<=createdData-1;i++){
			show(btmLabel[i]);
			setPos(btmLabel[i],padding*2+(pannelw-padding*2)/maxDataAllowwance*i,pannelh-padding);
			btmLabel[i].setText(btmText[i]);
			point(points[i],(pannelw-padding*2)/maxDataAllowwance*i,(int)((datas[i]-min)/(max-min)*((float)(pannelh-padding*2))));
			show(tags[i]);
			setrPos(tags[i],(pannelw-padding*2)/maxDataAllowwance*i,padding+(int)((datas[i]-min)/(max-min)*((float)(pannelh-padding*2))));
			tags[i].setText(String.valueOf(jds(datas[i])));
		}
		if(createdData!=0)
		for(int i=1;i<=createdData-1;i++){
			line(lines[i-1],toAnaX(getX(points[i]))+lineBold,toAnaY(getY(points[i]))+lineBold,toAnaX(getX(points[i-1]))+lineBold,toAnaY(getY(points[i-1]))+lineBold);
		}
	}
	
	
	
	
	
	
	public void release(){
		viewHolder.removeAllViews();
	}
	
	float jds(float f){
		return (float)(Math.floor(f*Math.pow(10d,(double)jdss))/Math.pow(10d,(double)jdss));
	}
	View newView(){
		View w=createShape(0,0,1);
		w.setVisibility(View.GONE);
		return w;
	}
	TextView newLabel(){
		TextView t=createLabel("",0,0);
		t.setVisibility(View.GONE);
		return t;
	}
	TextView createLabel(String defval,int x,int y){
		TextView v=new TextView(ctx);
		v.setTextSize(7);
		v.setText(defval);
		viewHolder.addView(v);
		AbsoluteLayout.LayoutParams lp=(AbsoluteLayout.LayoutParams)v.getLayoutParams();
		lp.x=x;
		lp.y=y;
		createdView.add(v);
		v.setTextColor(fgColor);
		//v.setVisibility(View.GONE);
		return v;
	}
	View createShape(int x,int y,int l){
		View v=new View(ctx);
		viewHolder.addView(v);
v.setBackgroundColor(fgColor);
		AbsoluteLayout.LayoutParams lp=(AbsoluteLayout.LayoutParams)v.getLayoutParams();
		lp.x=x;
		lp.y=y;
		lp.width=l;
		lp.height=lineBold;
		createdView.add(v);
		
		//v.setVisibility(View.GONE);
		return v;
	}
	void setPan(View v,int x,int y,int w,int h){
		AbsoluteLayout.LayoutParams lp=(AbsoluteLayout.LayoutParams) v.getLayoutParams();
		lp.width=w;
		lp.height=h;
		lp.x=x;
		lp.y=y;
		v.setLayoutParams(lp);
	}
	void setrPos(View v,int x,int y){
		setPos(v,toViewX(x),toViewY(y));
	}
	void setPos(View v,int x,int y){
		AbsoluteLayout.LayoutParams lp=(AbsoluteLayout.LayoutParams) v.getLayoutParams();
		//lp.width=w;
		//lp.height=h;
		lp.x=x;
		lp.y=y;
		v.setLayoutParams(lp);
	}
	int getTextWidth(String t){
		TextPaint m=new TextPaint();
		m.setTextSize(7);
		m.setTypeface(Typeface.create("DroidSans",Typeface.NORMAL));
		return (int)m.measureText(t);
	}
	void line(View v,int x1,int y1,int x2,int y2){
		v.setVisibility(View.VISIBLE);
		double wx1=(double)x1;
		double wy1=(double)y1;
		double wx2=(double)x2;
		double wy2=(double)y2;
		AbsoluteLayout.LayoutParams lp=(AbsoluteLayout.LayoutParams) v.getLayoutParams();
		lp.width=(int)Math.sqrt(Math.pow(wx2-wx1,2)+Math.pow(wy2-wy1,2));
		lp.height=lineBold;
		lp.x=toViewX(ave(wx1,wx2))-lp.width/2;
		lp.y=toViewY(ave(wy1,wy2))-lp.height/2;
		v.setLayoutParams(lp);
		v.setRotation((float)((-Math.atan2(wy2-wy1,wx2-wx1))/Math.PI*180d));
	}
	void point(View w,int x,int y){
		w.setVisibility(View.VISIBLE);
		setPan(w,toViewX(x)-lineBold,toViewY(y)-lineBold,lineBold*2,lineBold*2);
	}
	int toViewX(int x){
		return x+padding*2;
	}
	int toViewY(int y){
		return -y+pannelh-padding;
	}
	int toAnaX(int x){
		return x-padding*2;
	}
	int toAnaY(int y){
		return -(y+padding-pannelh);
	}
	int getX(View v){
		return ((AbsoluteLayout.LayoutParams)(v.getLayoutParams())).x;
	}
	int getY(View v){
		return ((AbsoluteLayout.LayoutParams)(v.getLayoutParams())).y;
	}
	void hide(View p1){
		p1.setVisibility(View.INVISIBLE);
	}
	void show(View p1){
		p1.setVisibility(View.VISIBLE);
	}
	int ave(double p1,double p2){
		return (int)(p1+(p2-p1)/2d);
	}
}

