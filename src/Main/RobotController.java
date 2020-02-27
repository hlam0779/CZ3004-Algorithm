package Main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import Algorithms.MazeExplorer;
import Constants.MapConstants;
import RealRun.RpiRobot;
import Simulator.IRobot;
import Simulator.VirtualRobot;
import utils.Graph;
import utils.GraphNode;
import utils.Map;
import utils.MapCell;
import utils.Orientation;
import utils.RobotCommand;
import utils.ShortestPath;


public class RobotController {
	public static RobotController robotController;
	public static final boolean REAL_RUN = false;
	private static final int EXPLORE_TIME_LIMIT = 360;
	private static final int FASTEST_PATH_TIME_LIMIT = 120;
	private static final int REAL_ROBOT_SPEED = 1;
	private Timer exploringTimer, fastestPathTimer;
	private IRobot robot;
	
	private GUI gui;
	
	public static RobotController getInstance() {
		if (robotController == null) {
			robotController = new RobotController();
		}
		return robotController;
	}
	
	public RobotController() {
		gui = GUI.getInstance();
	}
	
	public void exploreMaze() {	
		if (!REAL_RUN) {
			robot = VirtualRobot.getInstance();
			if (!gui.isIntExploreInput()) {
				gui.setStatus("invalid input for exploration");
				gui.setExploreBtnEnabled(true);
				return;
			}
			gui.refreshExploreInput();
		}
		
		MazeExplorer explorer = MazeExplorer.getInstance();
		explorer.setRobot(robot);

		ExploreTimeClass timeActionListener;
		SwingWorker<Void, Void> exploreMaze = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {

				try {
					if (!REAL_RUN) {
						robot.prepareOrientation(Orientation.UP);
						explorer.exploreMaze(Map.getExploredMapInstance(), GUI.exploreTimeLimit);
					}else explorer.exploreMaze(Map.getExploredMapInstance(), EXPLORE_TIME_LIMIT);
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				return null;
			}
			@Override
			public void done() {
				
			}

		};
		if (REAL_RUN) {
			timeActionListener = new ExploreTimeClass(EXPLORE_TIME_LIMIT, exploreMaze);
			
		} else timeActionListener = new ExploreTimeClass(GUI.exploreTimeLimit, exploreMaze);
		SwingWorker<Void, Void> updateCoverage = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				while (!(exploreMaze.isDone() || exploreMaze.isCancelled())) {
					Float coverageRate = Map.getExploredMapInstance().getNumExplored() *100f / 300f ;
					gui.setCoverageUpdate(coverageRate);
				}
				return null;
			}
		};
		
	
		exploringTimer = new Timer(1000, timeActionListener);
		exploringTimer.start();
		updateCoverage.execute();
		
	}
	
	public void fastestPath() {
		if (!RobotController.REAL_RUN) {
			robot = VirtualRobot.getInstance();
			if (!gui.isIntFFPInput()) {
				gui.setStatus("invalid input for finding fastest path");
				gui.setFfpBtnEnabled(true);
				return;
			}
			gui.refreshFfpInput();
		} else robot = RpiRobot.getInstance();
		
		SwingWorker<Void, Void> fastestPath = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {

				try {
					int wayPointX = gui.getWayPointX();
					int wayPointY = gui.getWayPointY();
					Graph graph = new Graph(Map.getExploredMapInstance(), wayPointX, wayPointY);
					ShortestPath result = graph.GetShortestPath();
					System.out.println(result.getWeight());
					for(GraphNode n: result.getPath()){
					    gui.setMazeGridColor(n.getX(), n.getY(), GUI.FASTEST_PATH_CORLOR);
					}
					
					for(RobotCommand command: result.generateInstructions()){
					    robot.doCommand(command);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				return null;
			}
			@Override
			public void done() {
				
			}

		};
		
		ExploreTimeClass timeActionListener;
		if (REAL_RUN) {
			timeActionListener = new ExploreTimeClass(EXPLORE_TIME_LIMIT, fastestPath);
			
		} else timeActionListener = new ExploreTimeClass(GUI.exploreTimeLimit, fastestPath);
		fastestPathTimer = new Timer(1000, timeActionListener);
		fastestPathTimer.start();
		
		
	}
	
	class ExploreTimeClass implements ActionListener {
		int timeLimit;
		int timeLeft; 
		SwingWorker<Void, Void> task;
		public ExploreTimeClass(int timeLimit, SwingWorker<Void,Void> task) {
			this.timeLimit = timeLimit;
			timeLeft = timeLimit;
			this.task = task;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (timeLeft == timeLimit)
				task.execute();
			timeLeft--;
			GUI.getInstance().setTimer(timeLeft);
			if (timeLeft >= 0) {

				
				if (timeLeft == 0) {
					exploringTimer.stop();
					gui.setTimerMessage("exploration: time out");
					gui.setExploreBtnEnabled(true);
					gui.setFfpBtnEnabled(true);
				} else if (task.isDone()) {
					exploringTimer.stop();
					gui.setTimerMessage("finish exploration within time limit");
					gui.setExploreBtnEnabled(true);
					gui.setFfpBtnEnabled(true);
				}
				
//				getThreshold.execute();
				
			} 
			
		}
		
	}
	
	public static void main(String[] args) {
		GUI myGui = GUI.getInstance();
		myGui.setVisible(true);
		myGui.refreshExploreInput();

//		String key = null;
//		IRobot myRobot = VirtualRobot.getInstance();
//        Map realMap = Map.getRealMapInstance();
////        Map map = Map.getExploredMapInstance();
//        for (int i=0; i<MapConstants.MAP_WIDTH; i++) {
//        	for (int j=0; j<MapConstants.MAP_HEIGHT; j++) {
//        		MapCell cell = realMap.getCell(i, j);
////        		cell.setExploredStatus(true);
//        		cell.setSeen(true);
//        		if (!cell.isObstacle()) {
//        			//TODO: Refactor to MAP, this sets the boundary of arena to virtual walls
//        			if (i==0 || i==MapConstants.MAP_WIDTH-1 || j==0 || j==MapConstants.MAP_HEIGHT-1) {
//        				cell.setVirtualWall(true);
//        			} else {
//        				for (int p=i-1; p<=i+1; p++) {
//        					for (int q=j-1; q<=j+1; q++)
//        						if (realMap.getCell(p, q).isObstacle())
//        							cell.setVirtualWall(true);
//        				}
//        			}
//        		}
//        	}
//        }
//        myRobot.setOrientation(Orientation.UP);
//        myRobot.setPosition(1, 1);
//        Graph graph = new Graph(realMap, 4, 1);
//        ShortestPath result = graph.GetShortestPath();
//        System.out.println(result.getWeight());
//        for(GraphNode n: result.getPath()){
//            System.out.println("Coordinate: (" + n.getX() + ", " + n.getY() + "), Orientation: " + (n.isHorizontal()? "horizontal":"vertical"));
//        }
//        System.out.println("Starting Orientation");
//        System.out.println(result.isStartingOrientationHorizontal()?"Facing Left":"Facing Up");
//        System.out.println("Instructions:");
//        myRobot.prepareOrientation(Orientation.RIGHT);
//        for(RobotCommand command: result.generateInstructions()){
//            myRobot.doCommand(command);
//        }
//        ((VirtualRobot) myRobot).setSpeed(20);
//		MazeExplorer e = MazeExplorer.getInstance();
//        e.setRobot(myRobot);
//        e.exploreMaze(map, 1000000000);
	}
}
