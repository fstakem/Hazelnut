package main.research.fstakem.mocap.ui;

import java.util.ArrayList;

import main.research.fstakem.mocap.scene.GraphicsObject;

import com.threed.jpct.Object3D;

public class JpctGraphicsObject extends GraphicsObject
{
	private Object3D graphics_object = null;
	private ArrayList<JpctGraphicsObjectState> states;
	
	public JpctGraphicsObject()
	{
		
	}
	
	public Object3D getGraphicsObject()
	{
		return this.graphics_object;
	}
	
	public void setGraphicsObject(Object3D object)
	{
		this.graphics_object = null;
	}
}
