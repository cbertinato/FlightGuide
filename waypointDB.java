package icepod.FlightGuide;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class waypointDB {
	
	// Waypoint types:
	// ---------------
	// 	fly through (green triangle) 					= 0
	// 	fly to (green square) 							= 1
	// 	turn inside (green circle)						= 2
	// 	fly through, reoccupy (double green triangle) 	= 3
	// 	fly to, reoccupy (double green square)			= 4
	// 	start lead-in (orange x)						= 5
	
	private static final String TAG = waypointDB.class.getSimpleName();
	
	// Constants
	private static double a = 6378137.0;		// Semi-major axis (m) (WGS84)
	private static double e = 0.08181919;		// Eccentricity (WGS84)
	private static double invf = 298.257223563;	// Inverse flattening
	private static double f = 1/invf;			// Flattening
	
	// For Lambert Conformal
	private static float earthRadius = 6371; // km
	
	private static double km2nmi = 0.539957;
	
	// variables
	private double n;
	private double F;
	private double rho0;
	
	private double radius;
	
	// false = km and m, true = nautical miles and ft
	private boolean distanceUnit;
	
	private float currentLat;
	private float currentLon;
	
	private String waypointDBfilename = "waypoints.db";
	private ArrayList<Float> lat;
	private ArrayList<Float> lon;
	private ArrayList<Integer> type;
	private ArrayList<String> label;
	private int arraySize;
	private ArrayList<Double> x;
	private ArrayList<Double> y;
	private ArrayList<String> number;
	
	public waypointDB () {
		
		// Initialize lists
		lat = new ArrayList<Float>();
		lon = new ArrayList<Float>();
		type = new ArrayList<Integer>();
		label = new ArrayList<String>();
		number = new ArrayList<String>();
		
		x = new ArrayList<Double>();
		y = new ArrayList<Double>();
		
		try {
			
			// Check state of external storage
			String storageState = Environment.getExternalStorageState();
			File externStorage = Environment.getExternalStorageDirectory();
			File externDirectory = new File (externStorage.getAbsolutePath());
			
			if (storageState.equals(Environment.MEDIA_MOUNTED)) {
		        File file = new File(externDirectory, waypointDBfilename);
				if (file.exists()) {
					readDBfile(file);
					Log.d(TAG,"Loaded waypoint database.");

                    //radius = earthRadius;
                    setDistanceUnitMI();

                    Log.d(TAG,"Projecting coordinates...");

                    projectLatLon();
                    addLeadIns();
				} else {
                    Log.d(TAG,"No waypoing database found.");
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // constructor
	
	private void projectLatLon() {
		double[] point = new double[2];
		//double[] test = new double[2];
		
		for (int i=0; i < arraySize; i++) {
			point = PS(lat.get(i),lon.get(i));
			
			x.add(point[0]);
			y.add(point[1]);
			
			// DEBUG
			//Log.d(TAG,"i=" + i + ", x=" + point[0] + ", y=" + point[1]);
		}
	} // projectLatLon
	
	private void addLeadIns() {
		
		// TO DO: MAKE SETTABLE
		float len = 5000; // m
		
		float x1, x2, x3;
		float y1, y2, y3;
		float theta;
		
		for (int i=0; i < arraySize; i++) {
			if (type.get(i) == 0) { // add lead-in
				type.add(i+1,5);
				
				// Compute coordinates
				x1 = x.get(i).floatValue();
			    x2 = x.get(i+1).floatValue();
			    y1 = y.get(i).floatValue();
			    y2 = y.get(i+1).floatValue();
			    
			    theta = Math.abs((float)Math.atan((x2-x1)/(y2-y1)));
			    
			    if (y2 <= y1)
			    	y3 = y1 + (float)(len*Math.cos(theta));
			    else
			    	y3 = y1 - (float)(len*Math.cos(theta));
			    
			    if (x2 <= x1)
			    	x3 = x1 + (float)(len*Math.sin(theta));
			    else
			    	x3 = x1 - (float)(len*Math.sin(theta));
			    
			    // DEBUG
			    //Log.d(TAG,"theta=" + theta);
			    //Log.d(TAG,"x1=" + x1 + ", x2=" + x2 + ", x3=" + x3);
			    //Log.d(TAG,"y1=" + y1 + ", y2=" + y2 + ", y3=" + y3);
			    
			    x.add(i+1,Double.valueOf((double)x3));
			    y.add(i+1,Double.valueOf((double)y3));
			    label.add("lead-in");
			    number.add(i+1,"L"+String.valueOf(i));
			    
			    arraySize++;
			}
			
			//Log.d(TAG,"type(" + i + ")=" + type.get(i));
		}
	} // addLeadIns
	
	public float[] getPoint(int index) {
		
		float[] record = new float[2];
		//float[] record = new float[3];
		
		record[0] = lat.get(index);
		record[1] = lon.get(index);
		//record[2] = type.get(index);
		
		return record;
	} // getPoint
	
	public float lat(int index) {
		return lat.get(index);
	}
	
	public float lon(int index) {
		return lon.get(index);
	}
	
	public ArrayList<Float> getLats() {
		return lat;
	}
	
	public ArrayList<Float> getLons() {
		return lon;
	}
	
	public ArrayList<Double> getX() {
		return x;
	}
	
	public ArrayList<Double> getY() {
		return y;
	}
	
	public ArrayList<Integer> getType() {
		return type;
	}
	
	public ArrayList<String> getLabel() {
		return label;
	}
	
	public ArrayList<String> getNumber() {
		return number;
	}
	
	private void readDBfile(File file) {
		String[] parts;
		
		Globals g = Globals.getInstance();

		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    arraySize = 0;

		    while ((line = br.readLine()) != null) {
		        parts = line.split(",");
		        lat.add(Float.parseFloat(parts[0]));
		        lon.add(Float.parseFloat(parts[1]));
		        type.add(Integer.parseInt(parts[2]));
		        label.add(parts[3]);
		        //Log.d(TAG,"number="+String.valueOf(arraySize+1));
		        number.add(String.valueOf(arraySize+1));
		        arraySize++;
		    }
		    
		    br.close();
		    
		    g.setNumWP(arraySize);
		}
		catch (IOException e) {
		    e.printStackTrace();
		}
	} // readDBfile
	
	public int getArraySize() {
		return arraySize;
	} // getArraySize
	
	public void updateCurrentPos(float lat, float lon) {
		currentLat = lat;
		currentLon = lon;
	} // updateCurrentPos
	
	public void setDistanceUnitKM() {
		radius = earthRadius;
	} // setDistanceUnitKM
	
	public void setDistanceUnitMI() {
		radius = earthRadius*km2nmi;
	} // setDistanceUnitMI
	
	// distance between two sets of coordinates along a great circle (km)
	public double distanceTo(float lat1, float lat2, float lon1, float lon2) {
		double phi1 = lat1*Math.PI/180;
		double phi2 = lat2*Math.PI/180;
			
		double dPhi = (lat2 - lat1)*Math.PI/180;
		double dLambda = (lat2 - lat1)*Math.PI/180;
			
		double a = Math.sin(dPhi/2)*Math.sin(dPhi/2) + Math.cos(phi1)*Math.cos(phi2)*Math.sin(dLambda/2)*Math.sin(dLambda/2);
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
			
		return radius*c;
			
	} // distanceTo
		
	public double bearingTo(float lat1, float lat2, float lon1, float lon2) {
		double phi1 = lat1*Math.PI/180;
		double phi2 = lat2*Math.PI/180;
			
		double dLambda = (lon2 - lon1)*Math.PI/180;
			
		double y = Math.sin(dLambda)*Math.cos(phi2);
		double x = Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(dLambda);
			
		return (Math.atan2(y,x))*Math.PI/180;
	} // bearingTo
		
	// Cross-track distance from great circle between two way points to current pos, in km.
	public double crossTrack(float lat1, float lat2, float lon1, float lon2) {
		double d13 = distanceTo(lat1, currentLat, lon1, currentLon);
		double theta13 = bearingTo(lat1, currentLat, lon1, currentLon);
		double theta12 = bearingTo(lat1, lat2, lon1, lon2);
			
		return Math.asin(Math.sin(d13/radius)*Math.sin(theta13-theta12))*radius;
	} // crossTrack
		
	// Along-track distance from first point (lat1, lat2) to the closest point on the path to current pos, in km.
	public double alongTrack(float lat1, float lat2, float lon1, float lon2) {
		double d13 = distanceTo(lat1, currentLat, lon1, currentLon);
		double dxt = crossTrack(lat1, lat2, lon1, lon2);
			
		return Math.acos(Math.cos(d13/radius)/Math.cos(dxt/radius))*radius;
	} // alongTrack
	
	// WGS84 to Lambert Conformal Conic
	public double[] LCC(double phi, double lambda) {
		
		// phi = latitude, lambda = longitude
		double lambda0 = 0;
		double fn = 0;
		double fe = 0;
		
		double den = Math.pow((1-e*Math.sin(phi))/(1+e*Math.sin(phi)), e/2);
		double t = Math.tan(Math.PI/4 - phi/2) / den;
		double rho = a*F*Math.pow(t, n);
		double gamma = n*(lambda - lambda0);
		double N = fn + rho0 - rho*Math.cos(gamma);
		double E = fe + rho*Math.sin(gamma);
		
		double point[] = new double[2];
		point[0] = N;
		point[1] = E;
		
		return point;
	} // LCC
	
	// WGS84 to polar stereographic
	public double[] PS(double phi, double lambda) {
		// Source: 
		
		// phi = latitude, lambda = longitude
		
		double phi_c;		// standard parallel, latitude of true scale
		double lambda0;		// longitude of the origin
		int pm;				// north = 1, south = -1
		double fe = 0;		// false easting
		double fn = 0;		// false northing
		
		if (phi > 0) { 
			// North (EPSG:3413)
			phi_c = deg2rad(70);
			pm = 1;
			lambda0 = deg2rad(-45);
		} else { 
			// South (EPSG:3031)
			phi_c = deg2rad(-71);
			pm = -1;
			lambda0 = deg2rad(0);
		}
		
		phi = deg2rad(phi);
		lambda = deg2rad(lambda);
		
		double den = Math.pow((1-e*Math.sin(phi))/(1+e*Math.sin(phi)), e/2);
		double t = Math.tan(Math.PI/4 - phi/2) / den;
		
		double den_c = Math.pow((1-e*Math.sin(phi_c))/(1+e*Math.sin(phi_c)), e/2);
		double t_c = Math.tan(Math.PI/4 - phi_c/2) / den_c;
		
		double m_c = Math.cos(phi_c)/Math.sqrt(1-Math.pow(e*Math.sin(phi_c),2));
		
		// True scale at lat phi_c
		double rho = a*m_c*t/t_c;
		
		double x = fe+pm*rho*Math.sin(lambda - lambda0);
		double y = fn-pm*rho*Math.cos(lambda - lambda0);
		
		double[] point = new double[2];
		point[0] = x;
		point[1] = y;
		
		return point;
		
	} // PS
	
	private double deg2rad(double deg) {
		return deg*Math.PI/180;
	}
	
} // class