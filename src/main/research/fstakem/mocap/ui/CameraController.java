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
		this.setCamera(camera);
		this.setObjectToLookAt(object_to_look_at);
		this.is_zoom_locked = false;
		this.is_x_screen_rotation_locked = false;
		this.is_y_screen_rotation_locked = false;
		this.resetCamera();
	}
	
	public void setCamera(Camera camera)
	{
		if(camera == null)
			throw new IllegalArgumentException("The camera object cannot be null.");
		
		this.camera = camera;
	}
	
	public Object3D getObjectToLookAt()
	{
		return this.object_to_look_at;
	}
	
	public void setObjectToLookAt(Object3D object)
	{
		if(object == null)
			throw new IllegalArgumentException("The target object to follow cannot be null.");
		
		this.object_to_look_at = object;
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
		this.distance_from_target = CameraController.CAMERA_INITIAL_DISTANCE;
		SimpleVector vector = new SimpleVector(0, CameraController.CAMERA_INITIAL_HEIGHT_OFFSET, this.distance_from_target);
		vector.add(this.object_to_look_at.getTransformedCenter());
		this.camera.setPosition(vector);
		this.camera.setOrientation(new SimpleVector(0.0f, 0.0f, -1.0f), new SimpleVector(0.0f, -1.0f, 0.0f));
	}
	
	public void zoomCamera(float distance)
	{
		if(!this.is_zoom_locked)
		{
			this.distance_from_target += distance;
			
			if(this.distance_from_target > CameraController.CAMERA_MAX_DISTANCE)
				this.distance_from_target = CameraController.CAMERA_MAX_DISTANCE;
			else if(this.distance_from_target < CameraController.CAMERA_MIN_DISTANCE)
				this.distance_from_target = CameraController.CAMERA_MIN_DISTANCE;
			
			this.camera.moveCamera(Camera.CAMERA_MOVEIN, distance);
		}
	}
	
	public void rotateCamera(float screen_x, float screen_y)
	{
		this.camera.setPosition(0.0f, 0.0f, 0.0f);
		if(!this.is_x_screen_rotation_locked)
			this.camera.rotateCameraY(screen_x);
		if(!this.is_y_screen_rotation_locked)
			this.camera.rotateCameraX(screen_y);
		this.camera.setPosition(this.object_to_look_at.getTransformedCenter());
		this.camera.moveCamera(Camera.CAMERA_MOVEOUT, this.distance_from_target);
	}
	

}
