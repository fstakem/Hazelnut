package main.research.fstakem.mocap.ui;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CameraController 
{
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(CameraController.class);
		
	// Static variables
	private static final float CAMERA_MIN_DISTANCE = 30.0f;
	private static final float CAMERA_MAX_DISTANCE = 500.0f;
	private static final float CAMERA_INITIAL_DISTANCE = 150.0f;
	private static final float CAMERA_INITIAL_HEIGHT_OFFSET = 0.0f;
	
	// Variables
	private Camera camera;
	private Object3D object_to_look_at;
	private float distance_from_target;
	
	// Interaction variables
	private boolean is_zoom_locked;
	private boolean is_x_screen_rotation_locked;
	private boolean is_y_screen_rotation_locked;
	
	public CameraController(Camera camera, Object3D object_to_look_at)
	{
		logger.debug("CameraController(): Entering method.");
		
		this.setCamera(camera);
		this.setObjectToLookAt(object_to_look_at);
		this.is_zoom_locked = false;
		this.is_x_screen_rotation_locked = false;
		this.is_y_screen_rotation_locked = false;
		this.resetCamera();
		
		logger.debug("CameraController(): Entering method.");
	}
	
	public void setCamera(Camera camera)
	{
		logger.debug("CameraController.setCamera(): Entering method.");
		
		if(camera == null)
			throw new IllegalArgumentException("The camera object cannot be null.");
		
		this.camera = camera;
		
		logger.debug("CameraController.setCamera(): Exiting method.");
	}
	
	public Object3D getObjectToLookAt()
	{
		return this.object_to_look_at;
	}
	
	public void setObjectToLookAt(Object3D object)
	{
		logger.debug("CameraController.setObjectToLookAt(): Entering method.");
		
		if(object == null)
			throw new IllegalArgumentException("The target object to follow cannot be null.");
		
		this.object_to_look_at = object;
		
		logger.debug("CameraController.setObjectToLookAt(): Exiting method.");
	}
	
	public boolean isZoomLocked()
	{
		return this.is_zoom_locked;
	}
	
	public void setZoomLock(boolean zoom_lock)
	{
		this.is_zoom_locked = zoom_lock;
	}
	
	public boolean isXScreenRotationLocked()
	{
		return this.is_x_screen_rotation_locked;
	}
	
	public void setXScreenRotationLock(boolean lock_x_screen_rotation)
	{
		this.is_x_screen_rotation_locked = lock_x_screen_rotation;
	}
	
	public boolean isYScreenRotationLocked()
	{
		return this.is_y_screen_rotation_locked;
	}
	
	public void setYScreenRotationLock(boolean lock_y_screen_rotation)
	{
		this.is_y_screen_rotation_locked = lock_y_screen_rotation;
	}
	
	public void resetCamera()
	{
		logger.debug("CameraController.resetCamera(): Entering method.");
		
		this.distance_from_target = CameraController.CAMERA_INITIAL_DISTANCE;
		SimpleVector vector = new SimpleVector(0, CameraController.CAMERA_INITIAL_HEIGHT_OFFSET, this.distance_from_target);
		vector.add(this.object_to_look_at.getTransformedCenter());
		this.camera.setPosition(vector);
		this.camera.setOrientation(new SimpleVector(0.0f, 0.0f, -1.0f), new SimpleVector(0.0f, -1.0f, 0.0f));
		
		logger.debug("CameraController.resetCamera(): Exiting method.");
	}
	
	public void zoomCamera(float ratio)
	{
		logger.debug("CameraController.zoomCamera(): Entering method.");
		
		if(!this.is_zoom_locked)
		{
			float new_distance = this.distance_from_target * 1 / ratio;
			
			if(new_distance > CameraController.CAMERA_MAX_DISTANCE)
				new_distance = CameraController.CAMERA_MAX_DISTANCE;
			else if(new_distance < CameraController.CAMERA_MIN_DISTANCE)
				new_distance = CameraController.CAMERA_MIN_DISTANCE;
			
			float movement_distance = this.distance_from_target - new_distance;
			String values = "Initial distance " + String.valueOf(this.distance_from_target) + 
							", new distance " + String.valueOf(new_distance) + 
							", movement " + String.valueOf(movement_distance) + ".";
			logger.info("CameraController.zoomCamera(): " + values);
			this.distance_from_target = new_distance;
			this.camera.moveCamera(Camera.CAMERA_MOVEIN, movement_distance);
		}
		
		logger.debug("CameraController.zoomCamera(): Exiting method.");
	}
	
	public void rotateCamera(float screen_x, float screen_y)
	{
		logger.debug("CameraController.rotateCamera(): Entering method.");
		
		this.camera.setPosition(0.0f, 0.0f, 0.0f);
		if(!this.is_x_screen_rotation_locked)
			this.camera.rotateCameraY(screen_x);
		if(!this.is_y_screen_rotation_locked)
			this.camera.rotateCameraX(screen_y);
		this.camera.setPosition(this.object_to_look_at.getTransformedCenter());
		this.camera.moveCamera(Camera.CAMERA_MOVEOUT, this.distance_from_target);
		
		logger.debug("CameraController.rotateCamera(): Exiting method.");
	}
}
