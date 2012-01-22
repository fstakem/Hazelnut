package main.research.fstakem.mocap.ui;

import com.threed.jpct.Object3D;

public class JpctGraphicsObject extends GraphicsObject
{
	private Object3D graphics_object = null;
	
	public JpctGraphicsObject()
	{
		
	}
	
	public JpctGraphicsObject(Object3D object)
	{
		this.setGraphicsObject(object);
	}
	
	public Object3D getGraphicsObject()
	{
		return this.graphics_object;
	}
	
	public void setGraphicsObject(Object3D object)
	{
		if(object != null)
			this.graphics_object = object;
		else
			throw new IllegalArgumentException("The JPCT graphics object cannot be null.");
	}
	
	@Override
	public String toString()
	{
		if(this.graphics_object == null)
			return "";
		
		return this.graphics_object.getName();
	}
}
