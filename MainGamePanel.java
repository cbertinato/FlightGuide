/**
 * 
 */
package icepod.FlightGuide;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

// TODO: Make positioning of graphical elements adaptable to device.
// TODO: Rename file to something more appropriate.

/**
 * This is the main surface that handles the ontouch events and draws
 * the image to the screen.
 */
public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback {

	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private MainThread thread;
	//private ServerThread server;
	private ClientThread client;
	private Matrix transform;
	private double angle;
	private double lastAngle = 0;
	private int CenterX;
	private int CenterY;
	private String currentAngle;
	//private String pos;
	private String latOffset;
	private DecimalFormat df = new DecimalFormat("000.0");
	private DecimalFormat df_dev = new DecimalFormat("0");
	private float x;
	private float y;
	private boolean vg_on = true;
	
	private static float TEXTSIZE = 35;
	
	// MAKE SETTABLE
	private static int MAPSCALE = 100;	// 100m per pixel
	
	private waypointDB waypoints;
	private double[] mapCenter;
	private int mapCenterX;
	private int mapCenterY;
	private Float currentPos;
	private Float lastPos;
	private ArrayList<Float> lats;
	private ArrayList<Float> lons;
	private ArrayList<Double> xWP;
	private ArrayList<Double> yWP;
	private ArrayList<Integer> wp_type;
	private ArrayList<String> wp_labels;
	private ArrayList<String> wp_numbers;
	
	private Globals g;
	
	@SuppressLint("NewApi")
	public MainGamePanel(Context context) {
		super(context);
		// adding the callback (this) to the surface holder to intercept events
		getHolder().addCallback(this);

		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		CenterX = size.x / 2;
		CenterY = size.y / 2;
		
		//transform = new Matrix();
		
		//x = CenterX;
		//y = CenterY;
		x = size.x / 2;
		y = size.y / 2;
		
		waypoints = new waypointDB();
		
		lats = waypoints.getLats();
		lons = waypoints.getLons();
		xWP = waypoints.getX();
		yWP = waypoints.getY();
		wp_type = waypoints.getType();
		wp_labels = waypoints.getLabel();
		wp_numbers = waypoints.getNumber();
		
		latOffset = 0 + " ft";
		//pos = "X: " + df.format(x) + ", Y: " + df.format(y);
		angle = 0;
		currentAngle = "HDG: " + df.format(angle) + "˚";
		
		// create the game loop thread
		thread = new MainThread(getHolder(), this);
		
		// create and start server thread
		//server = new ServerThread(6000);
		//server.start();
		
		// create and start client thread
		
		// wired
		//client = new ClientThread("192.168.1.4");
		
		// wireless home
		client = new ClientThread("192.168.1.4");
		
		// wireless lab
		//client = new ClientThread("129.236.40.255");
		 
		client.start();
		
		// FOR TESTING
		lastPos = Float.valueOf(0);
		//lastPos = Float.parseFloat(client.getOutput());
		
		g = Globals.getInstance();
		
		// Make the GamePanel focusable so it can handle events
		setFocusable(true);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// at this point the surface is created and
		// we can safely start the game loop
		
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "Surface is being destroyed");
		// tell the thread to shut down and wait for it to finish
		// this is a clean shutdown
		boolean retry = true;
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// try again shutting down the thread
			}
		}
		Log.d(TAG, "Thread was shut down cleanly");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x = event.getX();
			y = event.getY();
			angle = Math.atan2(x - CenterX, CenterY - y);
		} 
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			x = event.getX();
			y = event.getY();
			
			if (x >= CenterX)
				angle = Math.atan2(x - CenterX, CenterY - y);
			else
				angle = 2*Math.PI - Math.atan2(CenterX - x, CenterY - y);
			
			currentAngle = "HDG: " + df.format(angle*180/Math.PI) + "˚";
			
			// DEBUG
			//Log.d(TAG,"X: " + df.format(x) + ", Y: " + df.format(y));
			
			// TESTING
			// Send heading to server
			
			latOffset = df_dev.format(Math.abs(x - CenterX)*150/100) + " ft";
		} 
		
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (canvas != null) {
			String[] coords = new String[2];
			
			// fills the canvas with black
			canvas.drawColor(Color.BLACK);
			
			// Make screen center coordinates of the current position
			if (client.getStatus()) {
				coords = client.getOutput();
				mapCenter = waypoints.PS(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
				mapCenterX = (int)mapCenter[0];
				mapCenterY = (int)mapCenter[1];
			} else {
				mapCenterX = 0;
				mapCenterY = 0;
			}
			
			// FOR TESTING
			//mapCenter = waypoints.PS(69.9891,-50.0601);
			//mapCenterX = (int)mapCenter[0];
			//mapCenterY = (int)mapCenter[1];
			
			//Log.d(TAG,"mapCenterX=" + mapCenterX + ", mapCenterY=" + mapCenterY);
			
			displayIndicators(canvas);
			displayDevBubbles(canvas);
			displayLatOffset(canvas,latOffset);
			displayCardinals(canvas);
			displayHeading(canvas);
			displayGPSstatus(canvas);
			//displayPos(canvas);
			plotWaypoints(canvas);
			displayDistanceToGo(canvas);
		}
	} // onDraw
	
	public void toggleVG(boolean b) {
		if (b && !g.getVGmode())
			g.setVGmode(true);
		else if (!b && g.getVGmode())
			g.setVGmode(false);	
	} // toggleVG
	
	private void displayIndicators(Canvas canvas) {
		if (canvas != null) {
			Paint paint = new Paint();
			paint.setStrokeWidth(3);
			paint.setARGB(255, 255, 255, 255);
			
			// horizontal guidance
			canvas.drawLine(x, CenterY-100, x, CenterY+100, paint);
			
			// vertical guidance
			if (g.getVGmode())
				canvas.drawLine(CenterX-150, y, CenterX+150, y, paint);
			
			canvas.drawLine(CenterX, CenterY-100, CenterX, CenterY-200, paint);
			canvas.drawLine(CenterX, CenterY+100, CenterX, CenterY+200, paint);
		}
	} // displayIndicators
	
	private void displayDevBubbles(Canvas canvas) {
		if (canvas != null) {
			Paint paintInner = new Paint();
			Paint paintOuter = new Paint();
			Paint paint = new Paint();
			
			int circleRad = 17;
			
			paintInner.setStrokeWidth(3);
			paintOuter.setStrokeWidth(3);
			paintInner.setStyle(Paint.Style.STROKE);
			paintOuter.setStyle(Paint.Style.STROKE);
			
		    if ((x >= (CenterX + 200 - circleRad) && (x > CenterX)) || (x <= (CenterX - 200 + circleRad) && (x < CenterX))) {
		    	paintInner.setARGB(255, 255, 0, 0);
			    paintOuter.setARGB(255, 255, 0, 0);	
		    } else if (x >= (CenterX + 100 - circleRad) || x <= (CenterX - 100 + circleRad)) {
		    	paintInner.setARGB(255, 255, 0, 0);
		    	paintOuter.setARGB(255, 255, 255, 255);
		    } else if ((x < (CenterX + 100 - circleRad) && (x > CenterX)) || (x > (CenterX - 100 + circleRad) && (x < CenterX))) {
		    	paintInner.setARGB(255, 255, 255, 255);
		    	paintOuter.setARGB(255, 255, 255, 255);
			}
		    
		    paint.setStyle(Paint.Style.FILL);
		    paint.setARGB(255, 255, 255, 255);
			
			canvas.drawCircle(CenterX+100, CenterY, circleRad, paintInner);
			canvas.drawCircle(CenterX+200, CenterY, circleRad, paintOuter);
			canvas.drawCircle(CenterX-100, CenterY, circleRad, paintInner);
			canvas.drawCircle(CenterX-200, CenterY, circleRad, paintOuter);
			
			canvas.drawCircle(CenterX+50,CenterY,5,paint);
			canvas.drawCircle(CenterX+150,CenterY,5,paint);
			canvas.drawCircle(CenterX-50,CenterY,5,paint);
			canvas.drawCircle(CenterX-150,CenterY,5,paint);
		}
	} // displayDevBubbles
	
	private void displayLatOffset(Canvas canvas, String latOffset) {
		if (canvas != null) {
			Paint paint = new Paint();
			paint.setARGB(255, 255, 255, 255);
			paint.setTextSize(TEXTSIZE);
			canvas.drawText(latOffset, CenterX - 250, CenterY - 30, paint);
		}
	} // displayLatOffset
	
	private void displayCardinals(Canvas canvas) {
		if (canvas != null) {
			Paint paint = new Paint();
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			paint.setARGB(255, 255, 255, 255);
			
			canvas.drawCircle(CenterX,CenterY,350,paint);
			
			canvas.drawLine(CenterX-350, CenterY, CenterX-320, CenterY, paint);
			canvas.drawLine(CenterX+350, CenterY, CenterX+320, CenterY, paint);
			canvas.drawLine(CenterX, CenterY+350, CenterX, CenterY+320, paint);
			canvas.drawLine(CenterX, CenterY-350, CenterX, CenterY-320, paint);
			canvas.drawLine(CenterX + (float)Math.sqrt(2)/2*350, CenterY + (float)Math.sqrt(2)/2*350, CenterX + (float)Math.sqrt(2)/2*330, CenterY + (float)Math.sqrt(2)/2*330, paint);
			canvas.drawLine(CenterX + (float)Math.sqrt(2)/2*350, CenterY - (float)Math.sqrt(2)/2*350, CenterX + (float)Math.sqrt(2)/2*330, CenterY - (float)Math.sqrt(2)/2*330, paint);
			canvas.drawLine(CenterX - (float)Math.sqrt(2)/2*350, CenterY + (float)Math.sqrt(2)/2*350, CenterX - (float)Math.sqrt(2)/2*330, CenterY + (float)Math.sqrt(2)/2*330, paint);
			canvas.drawLine(CenterX - (float)Math.sqrt(2)/2*350, CenterY - (float)Math.sqrt(2)/2*350, CenterX - (float)Math.sqrt(2)/2*330, CenterY - (float)Math.sqrt(2)/2*330, paint);
		}
	} // displayCardinals
	
	private void displayHeading(Canvas canvas) {
		if (canvas != null) {
			Paint paint1 = new Paint();
			paint1.setStrokeWidth(3);
			paint1.setStyle(Paint.Style.STROKE);
			paint1.setARGB(255, 255, 255, 255);
			
			double heading = computeBearing();
			//double heading = angle;
			//Log.d(TAG,"heading=" + heading*180/Math.PI);
			
			double adj = 300;
			double hyp = Math.sqrt(adj*adj+625);
			double ang = Math.atan(25/adj);
			
			canvas.drawLine(CenterX+(float)(hyp*Math.sin(heading-ang)),CenterY-(float)(hyp*Math.cos(heading-ang)),CenterX+(float)(hyp*Math.sin(heading+ang)),CenterY-(float)(hyp*Math.cos(heading+ang)),paint1);
			canvas.drawLine(CenterX+(float)(hyp*Math.sin(heading-ang)),CenterY-(float)(hyp*Math.cos(heading-ang)),CenterX+(float)(350*Math.sin(heading)),CenterY-(float)(350*Math.cos(heading)),paint1);
			canvas.drawLine(CenterX+(float)(hyp*Math.sin(heading+ang)),CenterY-(float)(hyp*Math.cos(heading+ang)),CenterX+(float)(350*Math.sin(heading)),CenterY-(float)(350*Math.cos(heading)),paint1);
			
			Paint paint2 = new Paint();
			paint2.setARGB(255, 255, 255, 255);
			paint2.setTextSize(TEXTSIZE);
			
			String output = "BRG: " + df.format(heading*180/Math.PI) + "˚";
			
			canvas.drawText(output, CenterX - 600, CenterY-250, paint2);
		}
	} // displayHeading
	
	private void displayGPSstatus(Canvas canvas) {
		if (canvas != null) {
			
			Paint paint = new Paint();
			paint.setTextSize(TEXTSIZE);
			
			if (client.getStatus())
				paint.setARGB(255, 0, 255, 0);
			else
				paint.setARGB(255, 255, 0, 0);
			
			canvas.drawText("GPS", CenterX - 600, CenterY - 300, paint);
		}
	} // displayGPSstatus
	
	private void displayPos(Canvas canvas) {
		if (canvas != null) {
			String output;
			String[] posInfo;
			
			Paint paint = new Paint();
			
			paint.setTextSize(TEXTSIZE);
			paint.setARGB(255, 255, 255,255);
			
			if (client.getStatus()) {
				posInfo = client.getOutput();
				output = "LAT: " + posInfo[0] + ", LON: " + posInfo[1];
			} else {
				output = "LAT: --" + ", LON: --";
			}
			
			//canvas.drawText(output, CenterX - 600, CenterY - 200, paint);
			
		}
	} // displayPos
	
	private void displayDistanceToGo(Canvas canvas) {
		if (canvas != null) {
			String output;
			String[] currentPos;
			int currentWpIndex;
			double distanceToGo;
			float wpLat;
			float wpLon;
			
			Paint paint = new Paint();
			
			paint.setTextSize(TEXTSIZE);
			paint.setARGB(255, 255, 255,255);
			
			if (client.getStatus() && waypoints.getArraySize() > 0) {
                currentWpIndex = g.getCurrentWP();
                wpLat = waypoints.lat(currentWpIndex);
                wpLon = waypoints.lon(currentWpIndex);
				currentPos = client.getOutput();
				distanceToGo = waypoints.distanceTo(Float.parseFloat(currentPos[0]), Float.parseFloat(currentPos[1]), wpLat, wpLon);
				output = "TO GO: " + String.format("%.1f", distanceToGo);
			} else {
				output = "TO GO: --";
			}
			
			canvas.drawText(output, CenterX - 600, CenterY - 200, paint);
			
		}
	} // displayDistanceToGo
	
	private void plotWaypoints(Canvas canvas) {
		if (canvas != null) {
			float[] point = new float[2];
			float[] prev_point = new float[2];
			int prev_type = 0;
			
			transform = new Matrix();
			
			// DRAW LINES FIRST, WAYPOINTS LAST
			
			// MOVE ALL OF THESE TO INITIALIZER
			Paint paint1 = new Paint();
			paint1.setStyle(Paint.Style.STROKE);
		    paint1.setARGB(255, 0, 255, 0);
		    paint1.setStrokeWidth(2);
		    
		    Paint paint2 = new Paint();
			paint2.setStyle(Paint.Style.STROKE);
		    paint2.setARGB(255, 255, 255, 255);
		    paint2.setStrokeWidth(2);
		    
		    Paint paint3 = new Paint();
			paint3.setStyle(Paint.Style.STROKE);
		    paint3.setARGB(255, 255, 255, 255);
		    paint3.setStrokeWidth(2);
		    paint3.setPathEffect(new DashPathEffect(new float[] {5,10}, 0));
		    
		    Paint paint4 = new Paint();
			paint4.setTextSize(25);
			paint4.setARGB(255, 255, 255,255);
		    
			// NEED TO SET currentPos SOMEWHERE
			// If no input from GPS, set current location to last known location.
		    if (!client.getStatus()) {
		    	currentPos = lastPos;
		    }

	    	if (angle != lastAngle) {
	    		// if heading has changed, rotate
	    		// NEED TO TRANSLATE HEADING CHANGE INTO SCREEN ROTATION ANGLE?
	    		
	    		// what's going on here?
	    		transform.setRotate((float)((angle-lastAngle)*180/Math.PI),CenterX*MAPSCALE,CenterY*MAPSCALE);

	    		//Log.d(TAG,"angle=" + (float)(angle-lastAngle)*180/Math.PI);
	    		//Log.d(TAG,"CenterX=" + CenterX + " CenterY=" + CenterY);
			} else {
	    		transform.setRotate(0,CenterX*MAPSCALE,CenterY*MAPSCALE);
			}
		    
	    	// ??NEED TO FIX??
			//transform.postTranslate(0,currentPos.floatValue()-lastPos.floatValue());
			lastPos = currentPos;
			
			lastAngle = angle;
		    
			for (int i=0; i<waypoints.getArraySize(); i++) {
				point[0] = CenterX + ((xWP.get(i)).floatValue() - mapCenterX);
				point[1] = CenterY + ((yWP.get(i)).floatValue() - mapCenterY);
				transform.mapPoints(point);
				
				//Log.d(TAG,"i=" + i + ", point[0]=" + point[0]/MAPSCALE + ", point[1]=" + point[1]/MAPSCALE);
				//Log.d(TAG,"i=" + i + ", point[0]=" + point[0] + ", point[1]=" + point[1]);

				
				int type = wp_type.get(i);
				
				if (type == 0) // fly through
					drawTriangle(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
					
				else if (type == 1) // fly to
					drawSquare(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
				
				else if (type == 2) // turn inside
					canvas.drawCircle(point[0]/MAPSCALE, point[1]/MAPSCALE,8,paint1);
				
				else if (type == 3) // fly through, re-occupy
					drawDoubTriangle(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
				
				else if (type == 4) // fly to, re-occupy
					drawDoubSquare(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
				
				else if (type == 5) { // start lead-in
					paint1.setARGB(255, 255, 165, 0);
					drawEx(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
					paint1.setARGB(255, 0, 255, 0);
					
				} else if (type == 6) { // end survey line
					paint1.setARGB(255, 255, 0, 0);
					drawTriangle(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE, 8, paint1);
					paint1.setARGB(255, 0, 255, 0);
				}
				
				// Plot numbers
				if (type != 5)
					canvas.drawText(wp_numbers.get(i), point[0]/MAPSCALE-30, point[1]/MAPSCALE, paint4);
				
				// Plot cursor
				if (i == g.getCurrentWP())
					markCurrentWP(canvas, point[0]/MAPSCALE, point[1]/MAPSCALE);
				
				// Draw lines
				if (i != 0) {
					
					// this point is a begin lead-in -> no line
					// last point was a begin lead-in -> dotted line
					// neither -> solid line
					
					if (type == 5) // lead-in
						canvas.drawLine(prev_point[0]/MAPSCALE, prev_point[1]/MAPSCALE, point[0]/MAPSCALE, point[1]/MAPSCALE, paint3);
					
					else if (prev_type != 6) {
						canvas.drawLine(prev_point[0]/MAPSCALE, prev_point[1]/MAPSCALE, point[0]/MAPSCALE, point[1]/MAPSCALE, paint2);
						prev_point[0] = point[0];
						prev_point[1] = point[1];
					}
				} else {
					prev_point[0] = point[0];
					prev_point[1] = point[1];
				}
				
				prev_type = type;
				
				xWP.set(i, Double.valueOf(point[0] + mapCenterX - CenterX));
				yWP.set(i, Double.valueOf(point[1] + mapCenterY - CenterY));
				
			}
		}
	} // plotWaypoints
	
	private double computeBearing() {
		// ADD CHECK FOR POPULATED WP ARRAYS, OTHERWISE INDEX OUT OF BOUND AND CRASH
        if (waypoints.getArraySize() == 0) {
            return 0;
        }
		
		float[] point = new float[2];
		double bearing;
		
		if (g.getNavMode() == "FST") {
			point[0] = (xWP.get(0)).floatValue();
			point[1] = (yWP.get(0)).floatValue();
		} else if (g.getNavMode() == "NXT" || g.getNavMode() == "LST") {
			if (g.getCurrentWP() != g.getNumWP()) {
				point[0] = (xWP.get(g.getCurrentWP())).floatValue();
				point[1] = (yWP.get(g.getCurrentWP())).floatValue();
			} else {
				point[0] = (xWP.get(g.getNumWP())).floatValue();
				point[1] = (yWP.get(g.getNumWP())).floatValue();
			}
		}
		
		point[0] = CenterX + point[0] - mapCenterX;
		point[1] = CenterY + point[1] - mapCenterY;
		
		//Log.d(TAG, "CurrentWP=" + g.getCurrentWP());
		
		if (point[0]/MAPSCALE >= CenterX)
			// Right half-plane
			bearing = Math.atan2(point[0]/MAPSCALE - CenterX, CenterY - point[1]/MAPSCALE);
		else
			// Left half-plane
			bearing = 2*Math.PI - Math.atan2(CenterX - point[0]/MAPSCALE, CenterY - point[1]/MAPSCALE);
		
		return bearing;
	} // computeBearing
	
	private void drawTriangle(Canvas canvas, float x, float y, float r, Paint paint) {
		
		float a = (float)(3/Math.sqrt(3)*r);
		
		if (canvas != null) {
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x, (float)(y + Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x, (float)(y + Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
		}
	}
	
	private void drawDoubTriangle(Canvas canvas, float x, float y, float r, Paint paint) {
		
		if (canvas != null) {
			
			float a = (float)(3/Math.sqrt(3)*r);
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x, (float)(y + Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x, (float)(y + Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
			
			a = (float)(3/Math.sqrt(3)*(r+10));
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x, (float)(y + Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x, (float)(y + Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
			canvas.drawLine(x - a/2, (float)(y - Math.sqrt(3)/4*a), x + a/2, (float)(y - Math.sqrt(3)/4*a), paint);
		}
	}
	
	private void drawSquare(Canvas canvas, float x, float y, float r, Paint paint) {
		if (canvas != null) {
			float a = (float)(Math.sqrt(2)*r);
			canvas.drawRect(x-a/2, y+a/2, x+a/2, y-a/2, paint);
		}
	}
	
	private void drawDoubSquare(Canvas canvas, float x, float y, float r, Paint paint) {
		if (canvas != null) {
			float a = (float)(Math.sqrt(2)*r);
			canvas.drawRect(x-a/2, y+a/2, x+a/2, y-a/2, paint);
			
			a = (float)(Math.sqrt(2)*(r+10));
			canvas.drawRect(x-a/2, y+a/2, x+a/2, y-a/2, paint);
		}
	}

	private void drawEx(Canvas canvas, float x, float y, float r, Paint paint) {
		if (canvas != null) {
			float f = (float)(Math.sqrt(2)/2);
			canvas.drawLine(x-f*r, y-f*r, x+f*r, y+f*r, paint);
			canvas.drawLine(x+f*r, y-f*r, x-f*r, y+f*r, paint);
		}
	}
	
	private void markCurrentWP(Canvas canvas, float x, float y) {
		if (canvas != null) {
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
		    paint.setARGB(255, 255, 255, 0);
		    paint.setStrokeWidth(2);
		    
		    canvas.drawCircle(x, y, 20, paint);
		}
	}

} // class
