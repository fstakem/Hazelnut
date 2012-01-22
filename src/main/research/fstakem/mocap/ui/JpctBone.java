package main.research.fstakem.mocap.ui;

import java.util.List;

import javax.vecmath.Vector3f;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;

import main.research.fstakem.mocap.parser.AcclaimData;
import main.research.fstakem.mocap.scene.Bone;

public class JpctBone extends JpctGraphicsObject
{
	private static final int NUMBER_OF_OBJECT_FACES = 90;
	private static final float OBJECT_SCALE = 0.5f; 
	
	public JpctBone(Bone bone)
	{
		Object3D object = Primitives.getCylinder(JpctBone.NUMBER_OF_OBJECT_FACES, JpctBone.OBJECT_SCALE, bone.getLength());
		object.setName(bone.getName());
		this.setGraphicsObject(object);
		this.rotateBone(bone.getOrientation(), bone.getOrientationOrder());	
		this.moveBone(bone.getStartPosition());
	}
	
	public String stateToString(int state_index)
	{
		String output = super.toString() + " ";
		List<GraphicsObjectState> states = this.getStates();
		
		if(state_index < states.size())
			output += states.get(state_index).toString();
		
		return output;
	}
	
	private void moveBone(Vector3f displacement)
	{
		Object3D object = this.getGraphicsObject();
		object.translate(displacement.x, displacement.y, displacement.z);
	}
	
	private void rotateBone(Vector3f orientation, AcclaimData.Axis[] orientation_order)
	{
		Object3D object = this.getGraphicsObject();
		
		for(AcclaimData.Axis axis : orientation_order)
		{
			if(axis.equals(AcclaimData.Axis.X))
				object.rotateX(orientation.x);
			else if(axis.equals(AcclaimData.Axis.Y))
				object.rotateX(orientation.y);
			else if(axis.equals(AcclaimData.Axis.Z))
				object.rotateX(orientation.z);
		}
	}
}
