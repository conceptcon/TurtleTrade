package com.stock.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.stock.data.PriceBar;
import com.stock.data.StockData;
import com.stock.index.StockIndex;

public class CandleImage extends StockPhoto {
	
	public CandleImage(Context context) {
		super(context);
	}
	
	public CandleImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private List<PriceBar> bar_list;
	private List<StockIndex> indexes = new ArrayList<StockIndex>();
	
	public void AddIndex(StockIndex index) {
		index.calcIndex(bar_list);
		indexes.add(index);
	}

	public void Save(String file, int width, int height) {
		Bitmap bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		
		if( bar_list.size() > 0 ) {
			Canvas g = new Canvas(bmp);
			this.display(g, new Rect(0, 0, width, height));
			g.save( Canvas.ALL_SAVE_FLAG );//保存
			g.restore();//存储
//			g.dispose();
		}
		
//		try {
//			
//			ImageIO.write(bmp, "jpeg", new File(file));
//		}
//		catch( IOException ex ) {
//			ex.printStackTrace();
//		}
	}
	
	public void display(Canvas canvas, Rect rect) {
		int divid = Math.round(rect.height() * 0.8f);
		float b = divid * 0.5f, s = b * scale;
		Rect topWindow = new Rect(rect.left, rect.top + Math.round(b - s), rect.width(), Math.round(2 * s));
		Rect btmWindow = new Rect(rect.left, rect.top + divid + 10, rect.width(), rect.height() - divid - 10);
		
		Paint p = new Paint();
		p.setColor(background);
		p.setStyle(Paint.Style.FILL);//设置填满 
		
		canvas.clipRect(rect);
		canvas.drawRect(rect, p);

		p.setColor(Color.RED);
		canvas.drawRect(rect.left + 1, rect.top + 1, rect.width() - 2, divid - 2, p);
		canvas.drawRect(btmWindow.left + 1, btmWindow.top, btmWindow.width() - 2, 
				btmWindow.height() - 2, p);

		int width = Math.min(rect.width(), rect.width() + (scoll - trans) * step);
		topWindow.right = topWindow.left + width;
		btmWindow.right = btmWindow.left + width;
		
		int count = Math.min(topWindow.width() / step + 1, bar_list.size()), 
				first = Math.max(0, scoll - trans);
		double high = 0, low = 0, maxVolume = 0;
		
		Iterator<PriceBar> iter = bar_list.listIterator(first);
		PriceBar[] bars = new PriceBar[count];

		if( iter.hasNext() ) {
			bars[0] = iter.next();
			high = bars[0].high;
			low = bars[0].low;
			maxVolume = bars[0].volume;
		}
		
		for( int i = 1; i < count && iter.hasNext(); i ++ ) {
			bars[i] = iter.next();
			high = Math.max(bars[i].get(PriceBar.PRICE_HIGH), high);
			low = Math.min(bars[i].get(PriceBar.PRICE_LOW), low);
			maxVolume = Math.max(bars[i].volume, maxVolume);
		}

		drawVolume(canvas, p, bars, btmWindow, maxVolume);
		drawCandle(canvas, p, bars, topWindow);

	}

	protected void drawIndexInTop(Canvas canvas, StockIndex index, Rect topWindow) {
		int right = topWindow.width() - step / 2;
		double  scale1 = - topWindow.height() / (high - low);
		double  base1 = topWindow.top + topWindow.height() - low * scale1;
		index.drawIndex(canvas, first, count, step, right, scale1, base1);
	}
	
	protected void drawIndexes(Canvas canvas, Rect topWindow, Rect btmWindow) {
		int right = topWindow.width() - step / 2;
		double  scale1 = - topWindow.height() / (high - low), 
				scale2 = - btmWindow.height() * 0.8 / maxVolume;
		double  base1 = topWindow.top + topWindow.height() - low * scale1,
				base2 = btmWindow.top + btmWindow.height();
		
		Iterator<StockIndex> _it = indexes.iterator();
		while( _it.hasNext() ) {
			StockIndex index = _it.next();
			
			if( index.getWindowIndex() == StockIndex.WINDOW_TOP )
				index.drawIndex(canvas, first, count, step, right, scale1, base1);
			else if( index.getWindowIndex() == StockIndex.WINDOW_BOTTOM )
				index.drawIndex(canvas, first, count, step, right, scale2, base2);
		}
	}
	
	private void drawCandle(Canvas g, Paint p, PriceBar[] bars, Rect rect) {
		int delta = step / 8, bar_width = step * 3 / 4;
		int x = rect.width() - step + delta;
		int height = rect.top + rect.height(), middle = rect.width() - step / 2;
		double bhp = rect.height() / (high - low);
		
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.setStroke(new BasicStroke(2));
		
		for( int i = 0; i < bars.length; i ++ ) {
			PriceBar curr = bars[i];
			
			if( curr.get(PriceBar.PRICE_CLOSE) < curr.get(PriceBar.PRICE_OPEN) ) {
				int y = (int) Math.round( (curr.open - low) * bhp );
				int h = (int) Math.round( (curr.close - low) * bhp );

				p.setColor(Color.GREEN);
				p.setStyle(Paint.Style.FILL);
				g.drawRect(x, height - y, bar_width, y - h, p);
			}
			else {
				int y = (int) Math.round( (curr.close - low) * bhp );
				int h = (int) Math.round( (curr.open - low) * bhp );
				
				p.setColor(Color.RED);
				p.setStyle(Paint.Style.STROKE);
				g.drawRect(x, height - y, bar_width, y - h, p);
			}
			
			int y1 = (int) Math.round( (curr.get(PriceBar.PRICE_HIGH) - low) * bhp );
			int y2 = (int) Math.round( (curr.get(PriceBar.PRICE_LOW) - low) * bhp );
			p.setStyle(Paint.Style.STROKE);
			g.drawLine(middle, height - y1, middle, height - y2, p);
			
			x -= step;
			middle -= step;
		}
	}
	
}
