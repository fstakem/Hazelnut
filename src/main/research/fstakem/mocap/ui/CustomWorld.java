package main.research.fstakem.mocap.ui;


import research.fstakem.mocap.R;

import android.content.res.Resources;

import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

import main.research.fstakem.mocap.scene.Character;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWorld 
{
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(CustomWorld.class);
		
	// Static variables
	private static final float CAMERA_INITIAL_X_POSITION = 0;
	private static final float CAMERA_INITIAL_Y_POSITION = -65;
	private static final float CAMERA_INITIAL_Z_POSITION = 100;
	private static final double CAMERA_RADIUS = Math.sqrt( Math.sqrt(CustomWorld.CAMERA_INITIAL_X_POSITION * CustomWorld.CAMERA_INITIAL_X_POSITION) + 
			 											   Math.sqrt(CustomWorld.CAMERA_INITIAL_X_POSITION * CustomWorld.CAMERA_INITIAL_X_POSITION) +
			 											   Math.sqrt(CustomWorld.CAMERA_INITIAL_X_POSITION * CustomWorld.CAMERA_INITIAL_X_POSITION) );
	private static final String BOX_TEXTURE_NAME = "box_texture";
	private static final String GROUND_TEXTURE_NAME = "ground_texture";
	private static final String FPS_TEXTURE_NAME = "fps_texture";
	private static final String BOX_OBJECT_NAME = "box_object";
	private static final String GROUND_OBJECT_NAME = "ground_object";
	
	// Android resrouces
	private Resources resources = null;
	
	// World
	private World graphics_world = null;
	
	// Graphics world objects
	private Object3D ground_plane_object = null;
	private Object3D box_object = null;
	private Object3D object_at_center_of_scene = null;
	private Light light = null;
	private Texture font = null;
	
	// Number rendering constants
	private static final int CHAR_WIDTH = 5;
	private static final int CHAR_HEIGHT = 9;
	private static final int CHAR_SRC_X_OFFSET = 5;
	private static final int CHAR_SRC_Y_OFFSET = 0;
	private static final int CHAR_DST_X_OFFSET = 5;
	private static final int CHAR_DST_Y_OFFSET = 5;
		
	public CustomWorld()
	{
		
	}
	
	public void createWorld(Resources resources)
	{
		this.createWorld(resources, null);
	}
	
	public void createWorld(Resources resources, Character character)
	{
		logger.debug("CustomWorld.createWorld(): Entering method.");
		
		this.resources = resources;
		
		// Ordering of these methods is important
		this.initializeWorld();
		this.createTextures();
		
		if(character == null)
			this.createInitialScene();
		else
			this.createAnimationScene();
		
		this.createLights();
		this.resetCamera();
		
		this.graphics_world.buildAllObjects();
		
		logger.debug("CustomWorld.createWorld(): Exiting method.");
	}
	
	private void createInitialScene()
	{
		logger.debug("CustomWorld.createInitialScene(): Entering method.");
		
		// Ground plane
		this.ground_plane_object = Primitives.getPlane(20, 10.0f);
		this.ground_plane_object.setName(CustomWorld.GROUND_OBJECT_NAME);
		this.ground_plane_object.rotateX((float) Math.PI / 2f);
		this.ground_plane_object.setTexture(CustomWorld.GROUND_TEXTURE_NAME);
		this.graphics_world.addObject(this.ground_plane_object);
		
		// Box
		this.box_object = Loader.load3DS(this.resources.openRawResource(R.raw.box), 5.0f)[0];
		this.box_object.setName(CustomWorld.BOX_OBJECT_NAME);
		this.box_object.setTexture(CustomWorld.BOX_TEXTURE_NAME);
		this.box_object.translate(25.0f, -25.0f, 0.0f);
		this.graphics_world.addObject(this.box_object);
		
		this.object_at_center_of_scene = this.box_object;
		
		logger.debug("CustomWorld.createInitialScene(): Exiting method.");
	}
	
	private void createAnimationScene()
	{
		this.ground_plane_object = null;
		this.box_object = null;
		
		// Character
	}
		
	private void initializeWorld()
	{
		logger.debug("CustomWorld.initializeWorld(): Entering method.");
		
		Config.maxPolysVisible = 5000;
		Config.farPlane = 1500;
		if(graphics_world == null)
			this.graphics_world = new World();
		else
			this.graphics_world.removeAll();
		
		logger.debug("CustomWorld.initializeWorld(): Exiting method.");
	}
	
	private void createTextures()
	{
		logger.debug("CustomWorld.createTextures(): Entering method.");
		
		Texture ground_texture = new Texture(this.resources.openRawResource(R.raw.tile_21));
		Texture box_texture = new Texture(this.resources.openRawResource(R.raw.box_colorful_tex));
		Texture fps_texture = new Texture(this.resources.openRawResource(R.raw.fps_numbers));
		TextureManager tm = TextureManager.getInstance();
		tm.flush();
		tm.addTexture(CustomWorld.GROUND_TEXTURE_NAME, ground_texture);
		tm.addTexture(CustomWorld.BOX_TEXTURE_NAME, box_texture);
		tm.addTexture(CustomWorld.FPS_TEXTURE_NAME, fps_texture);
		
		logger.debug("CustomWorld.createTextures(): Exiting method.");
	}
				
	private void createLights()
	{
		logger.debug("CustomWorld.createLights(): Entering method.");
		
		this.light = new Light(this.graphics_world);
		this.light.setIntensity(500, 500, 500);
		
		SimpleVector sv = new SimpleVector();
		sv.set(this.ground_plane_object.getTransformedCenter());
		sv.x += 100.0f;
		sv.y -= 100.0f;
		sv.z += 100.0f;
		this.light.setPosition(sv);
		this.light.enable();
		this.graphics_world.setAmbientLight(75, 75, 75);
		
		logger.debug("CustomWorld.createLights(): Exiting method.");
	}
	
	public void drawWorld(FrameBuffer frameBuffer, int fps)
	{
		try
		{
			frameBuffer.clear();
			this.graphics_world.renderScene(frameBuffer);
			this.graphics_world.draw(frameBuffer);
			this.blitNumber(frameBuffer, fps);
		}
		catch (Exception e) 
		{
			logger.error("CustomWorld.createLights(): Error rendering world=> {}", e.getMessage());
		}
	}
	
	public void resetCamera()
	{
		logger.debug("CustomWorld.resetCamera(): Entering method.");
		
		Camera camera = this.graphics_world.getCamera();
		SimpleVector initial_position = new SimpleVector(CustomWorld.CAMERA_INITIAL_X_POSITION,
													 	 CustomWorld.CAMERA_INITIAL_Y_POSITION,
													 	 CustomWorld.CAMERA_INITIAL_Z_POSITION);
		camera.setPosition(initial_position);
		camera.lookAt(this.ground_plane_object.getTransformedCenter());
		camera.setFOV(1.5f);
		
		logger.debug("CustomWorld.resetCamera(): Exiting method.");
	}
	
	public void moveCamera(float x, float y)
	{
		logger.debug("CustomWorld.moveCamera(): Entering method.");
		
		Camera camera = this.graphics_world.getCamera();
		SimpleVector current_position = camera.getPosition();
		current_position.x += x;
		current_position.y += y;
		
		// TODO
		// Rotate around the center
	
		camera.setPosition(current_position);
		camera.lookAt(this.ground_plane_object.getTransformedCenter());
		
		logger.debug("CustomWorld.moveCamera(): Exiting method.");
	}
	
	private void blitNumber(FrameBuffer frameBuffer, int number) 
	{
		if (this.font != null) 
		{
			String number_string = Integer.toString(number);
			int xDstOffset = CustomWorld.CHAR_DST_X_OFFSET;
			for (int i = 0; i < number_string.length(); i++) 
			{
				char number_character = number_string.charAt(i);
				int number_integer = number_character - 48;
				frameBuffer.blit(font, 
								 number_integer * CustomWorld.CHAR_SRC_X_OFFSET, 
								 CustomWorld.CHAR_SRC_Y_OFFSET, 
								 xDstOffset, 
								 CustomWorld.CHAR_DST_Y_OFFSET, 
								 CustomWorld.CHAR_WIDTH, 
								 CustomWorld.CHAR_HEIGHT, 
								 FrameBuffer.TRANSPARENT_BLITTING);
				xDstOffset += 5;
			}
		}
	}
}
