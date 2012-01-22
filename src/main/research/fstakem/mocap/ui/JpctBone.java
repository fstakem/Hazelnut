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
		
		Vector3f orientation = this.transformToJpctCoordinates(bone.getOrientation());
		AcclaimData.Axis[] orientation_order = this.transformToJpctCoordinates(bone.getOrientationOrder());
		Vector3f position = this.transformToJpctCoordinates(bone.getStartPosition());
		this.rotateBone(orientation, orientation_order);	
		this.moveBone(position);
	}
	
	public String stateToString(int state_index)
	{
		String output = super.toString() + " ";
		List<GraphicsObjectState> states = this.getStates();
		
		if(state_index < states.size())
			output += states.get(state_index).toString();
		
		return output;
	}
	
	private Vector3f transformToJpctCoordinates(Vector3f v)
	{
		return new Vector3f(v.x, -v.y, v.z);
	}
	
	private AcclaimData.Axis[] transformToJpctCoordinates(AcclaimData.Axis[] a)
	{
		AcclaimData.Axis[] axis = new AcclaimData.Axis[3];
		axis[0] = a[0];
		axis[1] = a[1];
		axis[2] = a[2];
		
		return axis;
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
				object.rotateY(orientation.y);
			else if(axis.equals(AcclaimData.Axis.Z))
				object.rotateZ(orientation.z);
		}
	}
}
