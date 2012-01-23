package main.research.fstakem.mocap.ui;

public class ApplicationSettings 
{
	// Application state types
	public enum ApplicationState { TESTING, UNRESTRAINED_PLAYBACK };
	public enum CameraInteractionState { NO_CAMERA_MOVEMENT, HORIZONTAL_CAMERA_MOVEMENT,
								   		 VERTICAL_CAMERA_MOVEMENT, ALL_CAMERA_MOVEMENTS };
	
   	// Interaction constants
	public static final float TOUCH_MOVEMENT_SCALING_FACTOR = 0.05f;
	public static final float TOUCH_MOVEMENT_THRESHOLD = 0.06f;
	public static final float TOUCH_ZOOM_DISTANCE_SCALING_FACTOR = 1.0f;
   		
	// State variables
	public static ApplicationState app_state;
	public static CameraInteractionState camera_state;
	public static boolean zoom_enabled;
	public static boolean fps_showing;
	public static boolean allow_device_rotation;
	public static String default_server_address;
	public static int default_server_port;
								   						   
	public ApplicationSettings()
	{
		ApplicationSettings.resetToPlaybackSettings();
	}
	
	public static void resetToTestingSettings()
	{
		
	}
	
	public static void resetToPlaybackSettings()
	{
		
	}
}
