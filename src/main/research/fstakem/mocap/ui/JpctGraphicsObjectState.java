package main.research.fstakem.mocap.ui;

import javax.vecmath.Vector3f;

public class JpctGraphicsObjectState 
{
	public Vector3f position = null;
	public Vector3f orientation = null;
	
	public JpctGraphicsObjectState()
	{
		this.position = new Vector3f();
		this.orientation = new Vector3f();
	}
}
