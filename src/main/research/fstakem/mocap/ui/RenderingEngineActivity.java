package main.research.fstakem.mocap.ui;

import research.fstakem.mocap.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.ContextMenu;  
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.os.AsyncTask;
 
import android.view.ContextMenu.ContextMenuInfo;  

import com.threed.jpct.FrameBuffer;

import main.research.fstakem.mocap.parser.AcclaimImporter;
import main.research.fstakem.mocap.parser.AmcData;
import main.research.fstakem.mocap.parser.AsfData;
import main.research.fstakem.mocap.scene.AcclaimCharacterGenerator;
import main.research.fstakem.mocap.scene.Character;
import main.research.fstakem.mocap.util.Utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderingEngineActivity extends Activity
{
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(RenderingEngineActivity.class);
	
	// App settings
	ApplicationSettings app_settings;
	
	// Used to handle pause and resume...
	private static RenderingEngineActivity master = null;
	
	// Rendering variables
	private GLSurfaceView gl_view;
	private CustomRenderer renderer = null;
	private FrameBuffer frame_buffer = null;
	
	// Animation variables
	private boolean was_character_added = false;
	private Character main_character = null;
	
	// Activity variables
	private boolean is_activity_paused = false;
	
	// Interaction variables
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	private enum TouchState { NONE, MOVE, ZOOM };
	private TouchState current_touch_state = TouchState.NONE;
	private boolean recenter_camera = false;
	private PointF finger_position = new PointF();
	private PointF delta_finger_position = new PointF();
	private float last_distance_between_fingers = 0.0f;
	private float zoom_ratio = 0.0f;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		logger.debug("RenderingEngineActivity.onCreate(): Entering method.");
		
		if(RenderingEngineActivity.master != null) 
			this.copyActivityState(RenderingEngineActivity.master);
		
		super.onCreate(savedInstanceState);
		this.app_settings = new ApplicationSettings();
		this.gl_view = new GLSurfaceView(getApplication());
		this.gl_view.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() 
		
		{
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) 
			{
				// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
				// back to Pixelflinger on some device (read: Samsung I7500)
				int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
				EGLConfig[] configs = new EGLConfig[1];
				int[] result = new int[1];
				egl.eglChooseConfig(display, attributes, configs, 1, result);
				return configs[0];
			}
		});
		
		this.gestureDetector = new GestureDetector(new SimpleCustomGestureDetector());
        gestureListener = new View.OnTouchListener() 
        {
            public boolean onTouch(View v, MotionEvent event) 
            {
                if (gestureDetector.onTouchEvent(event)) 
                {
                    return true;
                }
                return false;
            }
        };

		this.renderer = new CustomRenderer();
		this.gl_view.setRenderer(this.renderer);
		setContentView(this.gl_view);
		
		logger.debug("RenderingEngineActivity.onCreate(): Exiting method.");
	}

	@Override
	protected void onPause() 
	{
		logger.debug("RenderingEngineActivity.onPause(): Entering method.");
		
		this.is_activity_paused = true;
		super.onPause();
		this.gl_view.onPause();
		
		logger.debug("RenderingEngineActivity.onPause(): Exiting method.");
	}

	@Override
	protected void onResume() 
	{
		logger.debug("RenderingEngineActivity.onResume(): Entering method.");
		
		this.is_activity_paused = false;
		super.onResume();
		this.gl_view.onResume();
		
		logger.debug("RenderingEngineActivity.onResume(): Exiting method.");
	}

	protected void onStop() 
	{
		logger.debug("RenderingEngineActivity.onStop(): Entering method.");
		
		this.renderer.pauseRendering();
		super.onStop();
		
		logger.debug("RenderingEngineActivity.onStop(): Exiting method.");
	}

	public boolean onTouchEvent(MotionEvent me) 
	{
		logger.debug("RenderingEngineActivity.onTouchEvent(): Entering method.");
		
		if(this.gestureDetector.onTouchEvent(me))
		{
			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
			return true;
		} 
	    else
	    {
	    	switch (me.getAction() & MotionEvent.ACTION_MASK)
	    	{
	    		case MotionEvent.ACTION_DOWN:
	    			logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_DOWN, (X, Y) => ({}, {})", me.getX(), me.getY());
	    			this.current_touch_state = TouchState.MOVE;
	    			this.finger_position = new PointF(me.getX(), me.getY());
	    			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
					return true;
	    		case MotionEvent.ACTION_POINTER_DOWN:
	    			logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_POINTER_DOWN, (X, Y) => ({}, {})", me.getX(), me.getY());
	    			this.current_touch_state = TouchState.ZOOM;
	    			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
					return true;
	    		case MotionEvent.ACTION_UP:
	    			logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_UP, (X, Y) => ({}, {})", me.getX(), me.getY());
	    			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
					return true;
	    		case MotionEvent.ACTION_POINTER_UP:
	    			logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_POINTER_UP, (X, Y) => ({}, {})", me.getX(), me.getY());
	    			this.current_touch_state = TouchState.NONE;
	    			this.finger_position = new PointF();
	    			this.delta_finger_position = new PointF();
	    			this.last_distance_between_fingers = 0.0f;
	    			this.zoom_ratio = 0.0f;
	    			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
					return true;
	    		case MotionEvent.ACTION_MOVE:
	    			if(this.current_touch_state == TouchState.MOVE)
	    			{
	    				logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_MOVE State => MOVE, (X, Y) => ({}, {})", me.getX(), me.getY());
	    				float delta_x = me.getX() - this.finger_position.x;
	    				float delta_y = me.getY() - this.finger_position.y;
	    				this.delta_finger_position = new PointF(delta_x, delta_y);
	    				this.finger_position = new PointF(me.getX(), me.getY());		
	    			}
	    			else if(this.current_touch_state == TouchState.ZOOM)
	    			{
	    				logger.info("RenderingEngineActivity.onTouchEvent(): ACTION_MOVE State => ZOOM, (X, Y) => ({}, {})", me.getX(), me.getY());
	    				float distance_between_fingers = this.calculateDistanceBetweenFingers(me);
	    				if(this.last_distance_between_fingers > 0.0f)
	    					this.zoom_ratio = distance_between_fingers / this.last_distance_between_fingers;
    					this.last_distance_between_fingers = distance_between_fingers;
	    			}
	    			logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
					return true;
	    	}
	    	
			try 
			{
				Thread.sleep(15);
			} 
			catch (Exception e) 
			{
				// Doesn't matter here...
			}
	    }
		
		logger.debug("RenderingEngineActivity.onTouchEvent(): Exiting method.");
		return super.onTouchEvent(me);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		logger.debug("RenderingEngineActivity.onCreateOptionsMenu(): Entering method.");
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(research.fstakem.mocap.R.menu.main_menu, menu);
	    
	    logger.debug("RenderingEngineActivity.onCreateOptionsMenu(): Exiting method.");
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		logger.debug("RenderingEngineActivity.onCreateContextMenu(): Entering method.");
		
		logger.debug("RenderingEngineActivity.onCreateContextMenu(): Exiting method.");
	}
		
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		logger.debug("RenderingEngineActivity.onOptionsItemSelected(): Entering method.");
		logger.info("RenderingEngineActivity.onOptionsItemSelected(): Selected => \'{}\'.", item.getTitle());
		
		switch (item.getItemId()) 
		{
		   case research.fstakem.mocap.R.id.load_local_character:
		   {
			   Resources resources = getResources();
			   InputStream asf_input = resources.openRawResource(R.raw.asf);
			   InputStream amc_input = resources.openRawResource(R.raw.amc);
			   CreateCharacterFromFileTask create_char_from_file = new CreateCharacterFromFileTask();
			   create_char_from_file.execute("George", asf_input, amc_input);
			   break;
		   }
		   case research.fstakem.mocap.R.id.find_remote_characters:
		   {
			   Toast.makeText(getApplicationContext(), "Remote characters are not currently supported.", Toast.LENGTH_LONG).show();
			   break;
		   }
		   case research.fstakem.mocap.R.id.settings:
		   {
			   Toast.makeText(getApplicationContext(), "Changing the settings is not currently supported.", Toast.LENGTH_LONG).show();
			   break;
		   }
		   case research.fstakem.mocap.R.id.about:
		   {
			   Toast.makeText(getApplicationContext(), "The about dialog has not been created.", Toast.LENGTH_LONG).show();
			   break;
		   }   
		}
		
		logger.debug("RenderingEngineActivity.onOptionsItemSelected(): Exiting method.");
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{
		logger.debug("RenderingEngineActivity.onContextItemSelected(): Entering method.");
		
		logger.debug("RenderingEngineActivity.onContextItemSelected(): Exiting method.");
		return true;
	}
	
	private void copyActivityState(Object src) 
	{
		logger.debug("RenderingEngineActivity.copyActivityState(): Entering method.");
		
		try 
		{
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) 
			{
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} 
		catch(Exception e) 
		{
			throw new RuntimeException(e);
		}
		
		logger.debug("RenderingEngineActivity.copyActivityState(): Exiting method.");
	}
		
	protected boolean isFullscreenOpaque() 
	{
		logger.debug("RenderingEngineActivity.isFullscreenOpaque(): Entering method.");
		
		logger.debug("RenderingEngineActivity.isFullscreenOpaque(): Exiting method.");
		return true;
	}
	
	private float calculateDistanceBetweenFingers(MotionEvent event) 
	{
		float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
	    return FloatMath.sqrt(x * x + y * y);
	 }
	
	// Inner Class
	// ***********
    private class SimpleCustomGestureDetector extends SimpleOnGestureListener 
    {
    	@Override
    	public boolean onDoubleTap(MotionEvent e)
    	{
    		logger.debug("SimpleCustomGestureDetector.onDoubleTap(): Entering method.");
    		
    		recenter_camera = true;
    		
    		logger.debug("SimpleCustomGestureDetector.onDoubleTap(): Exiting method.");
			return false;
    	}
    }
    
    // Inner Class
    // ***********
    private class CreateCharacterFromFileTask extends AsyncTask<Object, Void, Character> 
    {
    	private ProgressDialog progress_dialog;
    	private String character_name = "";
    	private InputStream asf_input_stream = null;
    	private InputStream amc_input_stream = null;
    	private long start_time = 0;
    	
    	@Override
    	protected void onPreExecute()
    	{ 
    	   super.onPreExecute();
    	   logger.debug("CreateCharacterFromFile.onPreExecute(): Entering method.");
    	   
    	   this.progress_dialog = new ProgressDialog(RenderingEngineActivity.this);
    	   this.progress_dialog.setMessage("Loading animation...");
    	   this.progress_dialog.show();    
    	   
    	   logger.debug("CreateCharacterFromFile.onPreExecute(): Exiting method.");
    	}

		@Override
		protected Character doInBackground(Object... params) 
		{
			logger.debug("CreateCharacterFromFile.doInBackground(): Entering method.");
			this.start_time = System.currentTimeMillis();
			
			String name_param = (String) params[0];
			InputStream asf_param = (InputStream) params[1];
			InputStream amc_param = (InputStream) params[2];
			
			if(name_param != null)
				this.character_name = name_param;
			
			
			if(asf_param == null || amc_param == null)
			{
				logger.error("CreateCharacterFromFile.doInBackground(): Acclaim InputStream can not be null.");
				throw new IllegalArgumentException("Acclaim InputStream can not be null.");
			}
			
			this.asf_input_stream = asf_param;
			this.amc_input_stream = amc_param;
			Character character = this.parseFiles();
			
			long elapsed_time = System.currentTimeMillis() - this.start_time;
			logger.info("CreateCharacterFromFile.doInBackground(): It took {} ms to parse the acclaim files and create the character.", elapsed_time);
			
			logger.debug("CreateCharacterFromFile.doInBackground(): Exiting method.");
			return character;
		}
		
		@Override
		protected void onPostExecute(Character character) 
		{
			logger.debug("CreateCharacterFromFile.onPostExecute(): Entering method.");
			
			logger.info("Character name: \'{}\'.", character.getName());
			main_character = character;
			was_character_added = true;
			this.progress_dialog.dismiss();
			
			logger.debug("CreateCharacterFromFile.onPostExecute(): Exiting method.");
		}
		
		private Character parseFiles()
		{
			logger.debug("CreateCharacterFromFile.parseFiles(): Entering method.");
			ArrayList<String> lines;
			Character character = null;
			try 
			{
				lines = Utility.readFile(this.asf_input_stream);
				AsfData asf_data = AcclaimImporter.parseAsfData(lines);
				lines = Utility.readFile(this.amc_input_stream);
				AmcData amc_data = AcclaimImporter.parseAmcData(lines);
				character = AcclaimCharacterGenerator.createCharacterFromData(this.character_name, asf_data, amc_data);
			} 
			catch (IOException e) 
			{
				logger.error("CreateCharacterFromFile.parseFiles(): Error importing the acclaim data.");
				Utility.printStackTraceToLog(logger, e);
			} 
			catch (ParseException e) 
			{
				logger.error("CreateCharacterFromFile.parseFiles(): Error importing the acclaim data.");
				Utility.printStackTraceToLog(logger, e);
			} 
			catch (Exception e) 
			{
				logger.error("CreateCharacterFromFile.parseFiles(): Error importing the acclaim data.");
				Utility.printStackTraceToLog(logger, e);
			}
			
			logger.debug("CreateCharacterFromFile.parseFiles(): Exiting method.");
			return character;
		}
    }

    // Inner Class
    // ***********
	private class CustomRenderer implements GLSurfaceView.Renderer 
	{
		private CustomWorld custom_world = null;
		private int fps = 0;
		private int last_fps = 0;
		private long time = System.currentTimeMillis();
		private boolean is_renderer_paused = false;
		private float ind;

		public void pauseRendering() 
		{
			logger.debug("CustomRenderer.pauseRendering(): Entering method.");
			
			this.is_renderer_paused = true;
			if(frame_buffer != null) 
			{
				frame_buffer.dispose();
				frame_buffer = null;
			}
			
			logger.debug("CustomRenderer.pauseRendering(): Exiting method.");
		}

		public void onSurfaceChanged(GL10 gl, int w, int h) 
		{
			logger.debug("CustomRenderer.onSurfaceChanged(): Entering method.");
			
			if(frame_buffer != null) 
			{
				frame_buffer.dispose();
			}
			frame_buffer = new FrameBuffer(gl, w, h);
			
			logger.debug("CustomRenderer.onSurfaceChanged(): Exiting method.");
		}

		public synchronized void onSurfaceCreated(GL10 gl, EGLConfig config) 
		{
			logger.debug("CustomRenderer.onSurfaceCreated(): Entering method.");
			
			Resources resources = getResources();
			this.custom_world = new CustomWorld();
			this.custom_world.createWorld(resources);
			
			logger.debug("CustomRenderer.onSurfaceCreated(): Exiting method.");
		}
		
		public synchronized void loadCharacter(Character character)
		{
			logger.debug("CustomRenderer.loadCharacter(): Entering method.");
			
			Resources resources = getResources();
			if(this.custom_world == null)
				this.custom_world = new CustomWorld();
			this.custom_world.createWorld(resources, character);
			
			logger.debug("CustomRenderer.loadCharacter(): Exiting method.");
		}
		
		public void onDrawFrame(GL10 gl) 
		{
			if(!this.is_renderer_paused) 
			{
				if(is_activity_paused) 
				{
					try 
					{
						Thread.sleep(500);
					} 
					catch(InterruptedException e) 
					{
						e.printStackTrace();
					}
				} 
				else 
				{
					this.renderCustomWorld();
					frame_buffer.display();
					this.calculateFps();	
				}
			} 
			else 
			{
				if(frame_buffer != null) 
				{
					frame_buffer.dispose();
					frame_buffer = null;
				}
			}
		}
		
		private synchronized void renderCustomWorld()
		{
			if(recenter_camera)
			{
				this.custom_world.resetCamera();
				recenter_camera = false;
			}
			else if(was_character_added)
			{
				loadCharacter(main_character);
				was_character_added = false;
			}
			else
			{
				float x_camera_movement = delta_finger_position.x * ApplicationSettings.TOUCH_MOVEMENT_SCALING_FACTOR;
				float y_camera_movement = delta_finger_position.y * ApplicationSettings.TOUCH_MOVEMENT_SCALING_FACTOR;
				if(x_camera_movement > ApplicationSettings.TOUCH_MOVEMENT_THRESHOLD || 
				   y_camera_movement > ApplicationSettings.TOUCH_MOVEMENT_THRESHOLD ||
				   x_camera_movement < -ApplicationSettings.TOUCH_MOVEMENT_THRESHOLD || 
				   y_camera_movement < -ApplicationSettings.TOUCH_MOVEMENT_THRESHOLD)
				{
					logger.info("CustomRenderer.renderCustomWorld(): Rotating camera ({}, {}).", x_camera_movement, y_camera_movement);
					this.custom_world.rotateCamera(x_camera_movement, y_camera_movement);
				}
				
				float camera_zoom_ratio = zoom_ratio * ApplicationSettings.TOUCH_ZOOM_DISTANCE_SCALING_FACTOR;
				if( camera_zoom_ratio > 0.0f )
				{
					logger.info("CustomRenderer.renderCustomWorld(): Zooming camera {}.", camera_zoom_ratio);
					this.custom_world.zoomCamera(camera_zoom_ratio);
				}
				
    			delta_finger_position = new PointF();
    			camera_zoom_ratio = 0.0f;
			}
			
			this.custom_world.drawWorld(frame_buffer, this.last_fps);
		}
	
		private void calculateFps()
		{
			if(System.currentTimeMillis() - this.time >= 1000) 
			{
				this.last_fps = (this.fps + this.last_fps) >> 1;
				this.fps = 0;
				time = System.currentTimeMillis();
			}
			
			this.fps++;
			this.ind += 0.02f;
			
			if(this.ind > 1) 
			{
				this.ind -= 1;
			}
		}
	}
}
