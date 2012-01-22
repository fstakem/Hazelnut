package main.research.fstakem.mocap.ui;

import javax.vecmath.Vector3f;

public class GraphicsObjectState 
{
	public Vector3f position = null;
	public Vector3f orientation = null;
	
	public GraphicsObjectState()
	{
		this.position = new Vector3f();
		this.orientation = new Vector3f();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.position.toString());
		//sb.append("Position (");
		//sb.append(this.position.x);
		
		return sb.toString();
	}
}
