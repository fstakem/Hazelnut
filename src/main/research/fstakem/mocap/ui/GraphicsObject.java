package main.research.fstakem.mocap.ui;

import java.util.ArrayList;
import java.util.List;

public class GraphicsObject 
{
	private List<GraphicsObjectState> states;
	
	public GraphicsObject()
	{
		this.setStates(new ArrayList<GraphicsObjectState>());
	}
	
	public List<GraphicsObjectState> getStates()
	{
		return this.states;
	}
	
	public void setStates(List<GraphicsObjectState> states)
	{
		if(states != null)
			this.states = states;
		else
			throw new IllegalArgumentException("The states cannot be null.");
	}
}
