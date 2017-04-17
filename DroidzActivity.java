package icepod.FlightGuide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;
import android.widget.LinearLayout;

// TODO: Rename file to something more appropriate.

@SuppressLint({ "NewApi", "InlinedApi" }) public class DroidzActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	private static final String TAG = DroidzActivity.class.getSimpleName();
	
	private View decorView;
	private FrameLayout game;
	private LinearLayout gameWidgets;
	private MainGamePanel gamePanel;
	private Button toggle_vg_button;
	private Button vg_mode_agl_button;
	private Button vg_mode_msl_button;
	private Button waypoint_accept_button;
	private Button nav_mode_first_button;
	private Button nav_mode_int_button;
	private Button nav_mode_next_button;
	private Button nav_mode_last_button;
	
	// Flags
	//private boolean vg_on = false;
	//private boolean vg_mode_agl_on = false;
	//private boolean waypoint_accept = false;
	//private String nav_mode = "FST";
	
	private int CenterX;
	private int CenterY;
	
	Globals g;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Requesting to turn the title OFF
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // Set orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        
        // Making it full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN;
		              //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		              //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		              //| View.INVISIBLE
		              //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		
		decorView.setSystemUiVisibility(uiOptions);
		
		game = new FrameLayout(this);
		gameWidgets = new LinearLayout(this);
		Log.d(TAG, "Adding MainPanel.");
		gamePanel = new MainGamePanel(this);
		toggle_vg_button = new Button(this);
		vg_mode_agl_button = new Button(this);
		vg_mode_msl_button = new Button(this);
		waypoint_accept_button = new Button(this);
		nav_mode_first_button = new Button(this);
		nav_mode_int_button = new Button(this);
		nav_mode_next_button = new Button(this);
		nav_mode_last_button = new Button(this);
		
		g = Globals.getInstance();
		
		// Get center coordinates of display
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		CenterX = size.x / 2;
		CenterY = size.y / 2;

		// Setup buttons for waypoint upload button
		waypoint_accept_button.setWidth(100);
		waypoint_accept_button.setHeight(75);
		waypoint_accept_button.setTextColor(Color.BLACK);
		waypoint_accept_button.setX(2*CenterX - 550);
		waypoint_accept_button.setY(2*CenterY - 500);

		// Logic for waypoint upload button flags
		if (!g.getUploadMode()) {
			waypoint_accept_button.setBackgroundColor(Color.GREEN);
			waypoint_accept_button.setText("ACC");
		} else {
			waypoint_accept_button.setBackgroundColor(Color.RED);
			waypoint_accept_button.setText("BLK");
		}
				
		// Set up buttons for altitude tracking
		toggle_vg_button.setWidth(100);
		toggle_vg_button.setHeight(75);
		toggle_vg_button.setText("VG");
		toggle_vg_button.setTextColor(Color.BLACK);
		toggle_vg_button.setX(2*CenterX - 200);
		toggle_vg_button.setY(2*CenterY - 300);
		
		vg_mode_agl_button.setWidth(100);
		vg_mode_agl_button.setHeight(75);
		vg_mode_agl_button.setText("AGL");
		vg_mode_agl_button.setTextColor(Color.BLACK);
		vg_mode_agl_button.setX(2*CenterX - 317);
		vg_mode_agl_button.setY(2*CenterY - 225);
		
		vg_mode_msl_button.setWidth(100);
		vg_mode_msl_button.setHeight(75);
		vg_mode_msl_button.setText("MSL");
		vg_mode_msl_button.setTextColor(Color.BLACK);
		vg_mode_msl_button.setX(2*CenterX - 434);
		vg_mode_msl_button.setY(2*CenterY - 150);
		
		// Logic for altitude tracking button flags
		if (!g.getVGmode()) {
			toggle_vg_button.setBackgroundColor(Color.RED);
			vg_mode_agl_button.setEnabled(false);
			vg_mode_agl_button.setBackgroundColor(Color.GRAY);
			vg_mode_msl_button.setEnabled(false);
			vg_mode_msl_button.setBackgroundColor(Color.GRAY);
			gamePanel.toggleVG(false);
		} else {
			toggle_vg_button.setBackgroundColor(Color.GREEN);
			vg_mode_agl_button.setEnabled(true);
			gamePanel.toggleVG(true);
			
			if (g.getVGaglMode()) {
				vg_mode_agl_button.setBackgroundColor(Color.GREEN);
				vg_mode_msl_button.setBackgroundColor(Color.RED);
			} else {
				vg_mode_agl_button.setBackgroundColor(Color.RED);
				vg_mode_msl_button.setBackgroundColor(Color.GREEN);
			}
			
			vg_mode_msl_button.setEnabled(true);
		}
		
		// Setup buttons for tracking mode
		nav_mode_first_button.setWidth(100);
		nav_mode_first_button.setHeight(75);
		nav_mode_first_button.setText("FIRST");
		nav_mode_first_button.setTextColor(Color.BLACK);
		nav_mode_first_button.setX(CenterX - 1040);
		nav_mode_first_button.setY(2*CenterY - 375);
		
		nav_mode_int_button.setWidth(100);
		nav_mode_int_button.setHeight(75);
		nav_mode_int_button.setText("INT");
		nav_mode_int_button.setTextColor(Color.BLACK);
		nav_mode_int_button.setX(CenterX - 1157);
		nav_mode_int_button.setY(2*CenterY - 300);
		
		nav_mode_next_button.setWidth(100);
		nav_mode_next_button.setHeight(75);
		nav_mode_next_button.setText("NEXT");
		nav_mode_next_button.setTextColor(Color.BLACK);
		nav_mode_next_button.setX(CenterX - 1274);
		nav_mode_next_button.setY(2*CenterY - 225);	
		
		nav_mode_last_button.setWidth(100);
		nav_mode_last_button.setHeight(75);
		nav_mode_last_button.setText("LAST");
		nav_mode_last_button.setTextColor(Color.BLACK);
		nav_mode_last_button.setX(CenterX - 1391);
		nav_mode_last_button.setY(2*CenterY - 150);
		
		// Logic for tracking mode button flags
		if (g.getNavMode() == "FIRST") {
			nav_mode_first_button.setBackgroundColor(Color.GREEN);
			nav_mode_int_button.setBackgroundColor(Color.GRAY);
			nav_mode_next_button.setBackgroundColor(Color.GRAY);
			nav_mode_last_button.setBackgroundColor(Color.GRAY);
		} else if (g.getNavMode() == "INT") {
			nav_mode_first_button.setBackgroundColor(Color.GRAY);
			nav_mode_int_button.setBackgroundColor(Color.GREEN);
			nav_mode_next_button.setBackgroundColor(Color.GRAY);
			nav_mode_last_button.setBackgroundColor(Color.GRAY);
		} else if (g.getNavMode() == "NEXT") {
			nav_mode_first_button.setBackgroundColor(Color.GRAY);
			nav_mode_int_button.setBackgroundColor(Color.GRAY);
			nav_mode_next_button.setBackgroundColor(Color.GREEN);
			nav_mode_last_button.setBackgroundColor(Color.GRAY);
		} else if (g.getNavMode() == "LAST") {
			nav_mode_first_button.setBackgroundColor(Color.GRAY);
			nav_mode_int_button.setBackgroundColor(Color.GRAY);
			nav_mode_next_button.setBackgroundColor(Color.GRAY);
			nav_mode_last_button.setBackgroundColor(Color.GREEN);
		}
		
		gameWidgets.addView(toggle_vg_button);
		gameWidgets.addView(vg_mode_agl_button);
		gameWidgets.addView(vg_mode_msl_button);
		gameWidgets.addView(waypoint_accept_button);
		gameWidgets.addView(nav_mode_first_button);
		gameWidgets.addView(nav_mode_int_button);
		gameWidgets.addView(nav_mode_next_button);
		gameWidgets.addView(nav_mode_last_button);
		
		game.addView(gamePanel);
		game.addView(gameWidgets);
		
		toggle_vg_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getVGmode()) {
					toggle_vg_button.setBackgroundColor(Color.RED);
					vg_mode_agl_button.setEnabled(false);
					vg_mode_agl_button.setBackgroundColor(Color.GRAY);
					vg_mode_msl_button.setEnabled(false);
					vg_mode_msl_button.setBackgroundColor(Color.GRAY);
					gamePanel.toggleVG(false);
					g.setVGmode(false);
				} else {
					toggle_vg_button.setBackgroundColor(Color.GREEN);
					vg_mode_agl_button.setEnabled(true);
					gamePanel.toggleVG(true);
					
					if (g.getVGaglMode()) {
						vg_mode_agl_button.setBackgroundColor(Color.GREEN);
						vg_mode_msl_button.setBackgroundColor(Color.RED);
					} else {
						vg_mode_agl_button.setBackgroundColor(Color.RED);
						vg_mode_msl_button.setBackgroundColor(Color.GREEN);
					}
					
					vg_mode_msl_button.setEnabled(true);
					
					g.setVGmode(true);
				}
			}
		});
		
		vg_mode_agl_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!g.getVGaglMode()) {
					vg_mode_agl_button.setBackgroundColor(Color.GREEN);
					vg_mode_msl_button.setBackgroundColor(Color.RED);
					g.setVGaglMode(true);
				}
			}
		});
		
		vg_mode_msl_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getVGaglMode()) {
					vg_mode_msl_button.setBackgroundColor(Color.GREEN);
					vg_mode_agl_button.setBackgroundColor(Color.RED);
					g.setVGaglMode(false);
				} 
			}
		});
		
		waypoint_accept_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!g.getUploadMode()) {
					waypoint_accept_button.setBackgroundColor(Color.RED);
					waypoint_accept_button.setText("BLK");
					g.setUploadMode(true);
				} else {
					waypoint_accept_button.setBackgroundColor(Color.GREEN);
					waypoint_accept_button.setText("ACC");
					g.setUploadMode(false);
				}
			}
		});
		
		nav_mode_first_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getNavMode() != "FIRST") {
					nav_mode_first_button.setBackgroundColor(Color.GREEN);
					nav_mode_int_button.setBackgroundColor(Color.GRAY);
					nav_mode_next_button.setBackgroundColor(Color.GRAY);
					nav_mode_last_button.setBackgroundColor(Color.GRAY);
					g.setNavMode("FIRST");
				}
				
				g.setCurrentWP(0);
			}
		});
		
		nav_mode_int_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getNavMode() != "INT") {
					nav_mode_first_button.setBackgroundColor(Color.GRAY);
					nav_mode_int_button.setBackgroundColor(Color.GREEN);
					nav_mode_next_button.setBackgroundColor(Color.GRAY);
					nav_mode_last_button.setBackgroundColor(Color.GRAY);
					g.setNavMode("INT");
				}
			}
		});
		
		nav_mode_next_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getNavMode() != "NEXT") {
					nav_mode_first_button.setBackgroundColor(Color.GRAY);
					nav_mode_int_button.setBackgroundColor(Color.GRAY);
					nav_mode_next_button.setBackgroundColor(Color.GREEN);
					nav_mode_last_button.setBackgroundColor(Color.GRAY);
					g.setNavMode("NEXT");
				}
				
				int currentWP = g.getCurrentWP();
				if (currentWP != g.getNumWP())
					g.setCurrentWP(currentWP+1);
			}
		});
		
		nav_mode_last_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (g.getNavMode() != "LAST") {
					nav_mode_first_button.setBackgroundColor(Color.GRAY);
					nav_mode_int_button.setBackgroundColor(Color.GRAY);
					nav_mode_next_button.setBackgroundColor(Color.GRAY);
					nav_mode_last_button.setBackgroundColor(Color.GREEN);
					g.setNavMode("LAST");
				}
				
				int currentWP = g.getCurrentWP();
				if (currentWP != 0)
					g.setCurrentWP(currentWP-1);
			}
		});
        
        // set our MainGamePanel as the View
        setContentView(game);
        Log.d(TAG, "View added");
        
    }

	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		if (hasFocus) {
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN;
		              //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		              //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		              //| View.INVISIBLE
		              //| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		
			decorView.setSystemUiVisibility(uiOptions);
		}
	}
    
    @Override
	protected void onDestroy() {
		Log.d(TAG, "Destroying...");
		super.onDestroy();
	}
    
    @Override
	protected void onPause() {
		Log.d(TAG, "Pausing...");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "Stopping...");
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "Resuming...");
		super.onResume();
		
		View decorView = getWindow().getDecorView();
		// Hide both the navigation bar and the status bar.
		// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
		// a general rule, you should design your app to hide the status bar whenever you
		// hide the navigation bar.
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		              | View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
	}
	
	public int getX() {
		return CenterX;
	}
	
	public int getY() {
		return CenterY;
	}
    
}