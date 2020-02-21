package Simulator;

import utils.Coordinate;
import utils.MapCell;
import utils.Orientation;
import utils.RobotCommand;

import java.util.List;


// TODO: refactor all methods that have primitive parameters to take wrapper objects...
// TODO: fix relationship between robot and map	
public interface IRobot {
	public List<Integer> getSensorValues(); // this
	public void doCommand(RobotCommand cmd);
	public Orientation getOrientation(); 
	public Coordinate getPosition();
	public void setPosition(int x, int y); // this
	public void setOrientation(Orientation o);
}
