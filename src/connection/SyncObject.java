package connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gui.GUISettings;
import gui.GUIUpdate;
import map.Map;
import threading.AlgoThread;
import utils.*;

public class SyncObject{
    private static SyncObject instance;
    private final Semaphore isWaypointAvailable = new Semaphore(0);
    private final Semaphore hasExplorationStarted = new Semaphore(0);
    private final Semaphore isSensorDataAvailable = new Semaphore(0);
    private final Semaphore hasGUIUpdate = new Semaphore(0);
    private final Lock lockGUIUpdate = new ReentrantLock();
    private Map prevMap;
    // Only for virtual runs
    private final Semaphore startFastestPath = new Semaphore(0);
    // Only for virtual runs
    private final Semaphore resetRobot = new Semaphore(0);
    // Only for virtual runs
    private final Lock lockExploreStatus = new ReentrantLock();
    private boolean hasExplorationFinished = false;
    // Only for virtual runs
    private final Lock lockFastestPath = new ReentrantLock();
    private List<Coordinate> fastestPathSquares;

    public final GUISettings settings = new GUISettings();

    //Waypoint
    private Coordinate waypoint;

    private SyncObject(){
    }

    public static SyncObject getSyncObject(){
        if(instance == null){
            instance = new SyncObject();
        }
        return instance;
    }

    public void SetWaypoint(Coordinate c){
        waypoint = c;
        isWaypointAvailable.release();
    }

    public Coordinate GetWaypoint() throws InterruptedException{
        isWaypointAvailable.acquire();
        return waypoint;
    }

    //Exploration

    public void SignalExplorationStart(){
        hasExplorationStarted.release();
    }

    public void IsExplorationStarted() throws InterruptedException{
        hasExplorationStarted.acquire();
    }

    //Sensors

    private Queue<List<Integer>> sensorData = new ConcurrentLinkedQueue<>(); 

    public void SetSensorData(List<Integer> data){
        sensorData.add(data);
        isSensorDataAvailable.release();
    }

    public List<Integer> GetSensorData() throws InterruptedException{
        isSensorDataAvailable.acquire();
        return sensorData.poll();
    }

    // GUI Update Queue
    private Queue<GUIUpdate> GUIUpdates = new ConcurrentLinkedQueue<>();

    public void SetGUIUpdate(Map mapToClone, Coordinate c, Orientation o){
        GUIUpdate update;
        if(mapToClone == null){
            update = new GUIUpdate(prevMap, c, o);
        }else {
            Map map = mapToClone.clone();
            prevMap = map;
            update = new GUIUpdate(map, c, o);
        }
        GUIUpdates.add(update);
        hasGUIUpdate.release();
    }

    public GUIUpdate GetGUIUpdate() throws InterruptedException{
        hasGUIUpdate.acquire();
        GUIUpdate update = GUIUpdates.poll();
        return update;
    }

    //Fastest Path

    public void SignalFastestPathStart(){
        startFastestPath.release();
    }

    public void IsFastestPathStart() throws InterruptedException{
        startFastestPath.acquire();
    }

    //Reset Robot

    public void SignalResetRobot(){
        resetRobot.release();
    }

    public void CheckResetRobot(AlgoThread thread) throws InterruptedException{
        resetRobot.acquire();
        System.out.println("Resetting");
        thread.interrupt();
        while(thread.isAlive());
        ResetAll();
    }

    private void ResetAll(){
        isWaypointAvailable.drainPermits();
        waypoint = null;
        hasExplorationStarted.drainPermits();
        isSensorDataAvailable.drainPermits();
        // Only for virtual runs
        startFastestPath.drainPermits();
        // Only for virtual runs
        resetRobot.drainPermits();
        sensorData = new ConcurrentLinkedQueue<>();
        hasGUIUpdate.drainPermits();
        lockGUIUpdate.lock();
        GUIUpdates = new ConcurrentLinkedQueue<>();
        lockGUIUpdate.unlock();
        prevMap = null;
        // Only for virtual runs
        lockExploreStatus.lock();
        hasExplorationFinished = false;
        lockExploreStatus.unlock();
        // Only for virtual runs
        lockFastestPath.lock();
        fastestPathSquares = null;
        lockFastestPath.unlock();
        System.out.println("Resetted");
    }

    //Has Exploration Finished
    public void SignalExplorationFinished(){
        lockExploreStatus.lock();
        hasExplorationFinished = true;
        lockExploreStatus.unlock();
    }

    public boolean IsExplorationFinished(){
        lockExploreStatus.lock();
        boolean temp = hasExplorationFinished;
        lockExploreStatus.unlock();
        return  temp;
    }

    public void SetFastestPath(List<RobotCommand> commands, Coordinate position, Orientation o){
        List<Coordinate> path = new ArrayList<>();
        path.add(position);
        for(RobotCommand command: commands){
            switch(command){
                case TURN_LEFT:
                    o = Orientation.getCounterClockwise(o);
                    break;
                case TURN_RIGHT:
                    o = Orientation.getClockwise(o);
                    break;
                case MOVE_FORWARD:
                    switch(o){
                        case UP:
                            position = new Coordinate(position.getX(), position.getY()+1);
                            break;
                        case RIGHT:
                            position = new Coordinate(position.getX()+1, position.getY());
                            break;
                        case LEFT:
                            position = new Coordinate(position.getX()-1, position.getY());
                            break;
                        case DOWN:
                            position = new Coordinate(position.getX(), position.getY()-1);
                            break;
                    }
                    path.add(position);
                    break;
                default:
                    break;
            }
        }
        lockFastestPath.lock();
        fastestPathSquares = path;
        lockFastestPath.unlock();
    }

    public List<Coordinate> GetFastestPathSquares(){
        lockFastestPath.lock();
        List<Coordinate> squares;
        if(fastestPathSquares != null) squares = new ArrayList<>(fastestPathSquares);
        else squares = null;
        lockFastestPath.unlock();
        return squares;
    }


}