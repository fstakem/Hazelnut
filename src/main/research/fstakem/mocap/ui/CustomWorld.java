package main.research.fstakem.mocap.ui;


import java.util.ArrayList;

import javax.vecmath.Vector3f;

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

import main.research.fstakem.mocap.scene.Bone;
import main.research.fstakem.mocap.scene.Character;
import main.research.fstakem.mocap.scene.CharacterElement;
import main.research.fstakem.mocap.scene.GraphicsObject;
import main.research.fstakem.mocap.scene.RootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomWorld 
{
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(CustomWorld.class);
	
	// Enums
	public enum BoneObjectType { CYLINDER };
		
	// Static variables
	private static final float SPOTLIGHT_RED_VALUE = 500.0f;
	private static final float SPOTLIGHT_GREEN_VALUE = 500.0f;
	private static final float SPOTLIGHT_BLUE_VALUE = 500.0f;
	
	private static final float SPOTLIGHT_X_POSITION = 100.0f;
	private static final float SPOTLIGHT_Y_POSITION = -100.0f;
	private static final float SPOTLIGHT_Z_POSITION = 100.0f;
	
	private static final int AMBIENT_LIGHT_RED_VALUE = 75;
	private static final int AMBIENT_LIGHT_GREEN_VALUE = 75;
	private static final int AMBIENT_LIGHT_BLUE_VALUE = 75;
	
	private static final String BOX_TEXTURE_NAME = "box_texture";
	private static final String GROUND_TEXTURE_NAME = "ground_texture";
	private static final String FPS_TEXTURE_NAME = "fps_texture";
	private static final String BOX_OBJECT_NAME = "box_object";
	private static final String GROUND_OBJECT_NAME = "ground_object";
	
	private static final BoneObjectType BONE_OBJECT_TYPE = CustomWorld.BoneObjectType.CYLINDER;
	
	
	// Android resources
	private Resources resources = null;
	
	// World
	private World graphics_world = null;
	
	// Graphics world objects
	private Object3D ground_plane_object = null;
	private Object3D box_object = null;
	private Character character = null;
	private Light light = null;
	private Texture font = null;
	
	// Camera object
	private CameraController camera_controller;
			
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
		
		// Graphic components
		this.initializeWorld();
		this.createTextures();
		
		// Scene
		Object3D object_at_center = null;
		if(character == null)
			object_at_center = this.createInitialScene();
		else
			object_at_center = this.createAnimationScene(character);
		
		// Lights
		this.createLights();
		
		// Build world
		this.graphics_world.buildAllObjects();
		
		// Camera
		this.camera_controller = new CameraController(this.graphics_world.getCamera(), object_at_center);
		
		logger.debug("CustomWorld.createWorld(): Exiting method.");
	}
	
	private Object3D createInitialScene()
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
		
		logger.debug("CustomWorld.createInitialScene(): Exiting method.");
		return this.box_object;
	}
	
	private Object3D createAnimationScene(Character character)
	{
		logger.debug("CustomWorld.createAnimationScene(): Entering method.");
		
		this.box_object = null;
		this.character = character;
		
		// Ground plane
		this.ground_plane_object = Primitives.getPlane(20, 10.0f);
		this.ground_plane_object.setName(CustomWorld.GROUND_OBJECT_NAME);
		this.ground_plane_object.rotateX((float) Math.PI / 2f);
		this.ground_plane_object.setTexture(CustomWorld.GROUND_TEXTURE_NAME);
		this.graphics_world.addObject(this.ground_plane_object);
		
		ArrayList<CharacterElement> character_elements = character.getAllCharacterElements();
		for(CharacterElement character_element : character_elements)
		{
			if(character_element.getName() != RootElement.ROOT)
			{
				Bone bone = (Bone) character_element;
				GraphicsObject bone_graphics_object = this.createBone(bone);
			}
		}
		
		logger.debug("CustomWorld.createAnimationScene(): Exiting method.");
		return this.ground_plane_object;
	}
	
	private JpctGraphicsObject createBone(Bone bone) 
	{
		logger.debug("CustomWorld.createBone(): Entering method.");
		
		JpctGraphicsObject bone_graphics_object = new JpctGraphicsObject();
		
		Object3D object = Primitives.getCylinder(90, 1.0f, bone.getLength());
		object.setName(bone.getName());
		Vector3f start_position = bone.getStartPosition();
		object.translate(start_position.x, start_position.y, start_position.z);
		Vector3f orientation = bone.getOrientation();
		object.rotateX(orientation.x);
		object.rotateY(orientation.y);
		object.rotateZ(orientation.z);
		
		StringBuilder bone_info = new StringBuilder();
		bone_info.append("Creating bone graphics object => ");
		bone_info.append(bone.toString());
		logger.debug("CustomWorld.createBone(): {}.", bone_info);
		
		bone_graphics_object.setGraphicsObject(object);
		this.graphics_world.addObject(object);
		
		logger.debug("CustomWorld.createBone(): Exiting method.");
		return bone_graphics_object;
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
		this.light.setIntensity(CustomWorld.SPOTLIGHT_RED_VALUE,
								CustomWorld.SPOTLIGHT_GREEN_VALUE, 
								CustomWorld.SPOTLIGHT_BLUE_VALUE);
		
		SimpleVector sv = new SimpleVector(CustomWorld.SPOTLIGHT_X_POSITION, 
										   CustomWorld.SPOTLIGHT_Y_POSITION, 
										   CustomWorld.SPOTLIGHT_Z_POSITION);
		this.light.setPosition(sv);
		this.light.enable();
		this.graphics_world.setAmbientLight(CustomWorld.AMBIENT_LIGHT_RED_VALUE,
											CustomWorld.AMBIENT_LIGHT_GREEN_VALUE,
											CustomWorld.AMBIENT_LIGHT_BLUE_VALUE);
		
		logger.debug("CustomWorld.createLights(): Exiting method.");
	}
	
	public void drawWorld(FrameBuffer frameBuffer, int fps)
	{
		try
		{
			frameBuffer.clear();
			this.graphics_world.renderScene(frameBuffer);
			this.graphics_world.draw(frameBuffer);
		}
		catch (Exception e) 
		{
			logger.error("CustomWorld.drawWorld(): Error rendering world=> {}", e.getMessage());
		}
	}
	
	public void resetCamera()
	{
		this.camera_controller.resetCamera();
	}
	
	public void zoomCamera(float ratio)
	{
		this.camera_controller.zoomCamera(ratio);
	}
	
	public void rotateCamera(float x, float y)
	{
		this.camera_controller.rotateCamera(x, y);
	}
}
