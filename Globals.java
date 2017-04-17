package icepod.FlightGuide;

public class Globals {
	private static Globals instance;
	
	private boolean vg_on = false;
	private boolean vg_mode_agl_on = false;
	private boolean waypoint_accept = false;
	private String nav_mode = "FST";
	private int current_wp = 0;
	private int num_wp = 1;
	
	public boolean getVGmode() {
		return this.vg_on;
	}
	
	public void setVGmode(boolean b) {
		vg_on = b;
	}
	
	public boolean getVGaglMode() {
		return this.vg_mode_agl_on;
	}
	
	public void setVGaglMode(boolean b) {
		vg_mode_agl_on = b;
	}
	
	public boolean getUploadMode() {
		return this.waypoint_accept;
	}
	
	public void setUploadMode(boolean b) {
		waypoint_accept = b;
	}
	
	public String getNavMode() {
		return this.nav_mode;
	}
	
	public void setNavMode(String s) {
		nav_mode = s;
	}
	
	public int getCurrentWP() {
		return current_wp;
	}
	
	public void setCurrentWP(int i) {
		current_wp = i;
	}
	
	public int getNumWP() {
		return num_wp;
	}
	
	public void setNumWP(int i) {
		num_wp = i;
	}
	
	public static synchronized Globals getInstance() {
		if (instance == null) {
			instance = new Globals();
		}
		return instance;
	}
}