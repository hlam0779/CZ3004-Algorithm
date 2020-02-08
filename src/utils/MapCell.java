package utils;

public class MapCell {
	private Boolean isExplored;
	private Boolean isObstacle;
	private Boolean isVirtualWall;
	
	public MapCell() {
		isExplored = false;
		isObstacle = null;
		isVirtualWall = null;
	}
	
	public boolean isExplored() {
		return isExplored;
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}
	
	public boolean isVirtualWall() {
		return isVirtualWall;
	}
	
	public void setExploredStatus(boolean status) {
		isExplored = status;
	}
	
	public void setObstacleStatus(boolean status) {
		isObstacle = status;
	}
	
	public void setVirtualWall(boolean status) {
		isVirtualWall = status;
	}
}
