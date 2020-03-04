package connection;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import utils.Coordinate;
import utils.Map;
import utils.MapTuple;
import utils.Orientation;
import utils.RobotCommand;

public class AlgoClient{ 
    private static AlgoClient instance;
    private static final String MOVE = "0"; 
    private static final String FASTEST_PATH = "1"; 
    private static final String EXPLORATION_START = "3";
    private static final String SET_WAYPOINT = "4";
    private static final String SENSOR = "5";
    private static final String CALIBRATION = "6"; 

    TCPSocket sock; 
    private AlgoClient(TCPSocket sock) { 
        this.sock = sock; 
    }

    public static AlgoClient GetInstance(){
        if(instance == null){
            instance = new AlgoClient(new TCPSocket("192.168.9.9", 9999));
        }
        return instance;
    }

    public void sendFastestPath(List<RobotCommand> ls) { 
        StringBuilder builder = new StringBuilder();
        builder.append(FASTEST_PATH);
        int i = 0; 
        for (RobotCommand command : ls) { 
            if (command == RobotCommand.MOVE_FORWARD) { 
                i++; 
                continue; 
            } else {
                if (i != 0) { 
                    if (i > 16) { 
                        builder.append('F');
                        i -= 16;
                    }
                    builder.append(Integer.toHexString(i)); 
                    i = 0; 
                } 
                builder.append(command.getLetter());
            }
        }
        if (i != 0) { 
            if (i > 16) { 
                builder.append('F');
                i -= 16;
            }
            builder.append(Integer.toHexString(i)); 
            i = 0; 
        } 
        sock.Send(builder.toString());
    }

    public void SendMove(RobotCommand command, MapTuple map, Orientation o, Coordinate c) { 
        StringBuilder builder = new StringBuilder();
        builder.append(MOVE);
        builder.append(command.getLetter());
        builder.append(Integer.toHexString(c.getX()));
        String y = Integer.toHexString(c.getY());
        if (y.length() == 1) { 
            builder.append("0");
        }
        builder.append(Integer.toHexString(c.getY()));
        builder.append(o.ordinal());
        builder.append("|");
        builder.append(map.GetP1());
        builder.append("|");
        builder.append(map.GetP2());
        sock.Send(builder.toString());
    }

    public void SendCalibrate(List<RobotCommand> ls) { 
        StringBuilder builder = new StringBuilder();
        builder.append(CALIBRATION);
        for (RobotCommand command : ls) { 
            builder.append(command.getLetter()); 
        }
        sock.Send(builder.toString());
    }
    
    public void HandleIncoming() throws IOException { 
        String message = sock.Receive(); 
        switch (Character.toString(message.charAt(0))) {   
            case EXPLORATION_START: 
                SyncObject.SignalExplorationStarted();
                break;
            case SET_WAYPOINT:
                String x = Character.toString(message.charAt(1));
                String y = message.substring(2);
                SyncObject.DefineWaypoint(new Coordinate(Integer.parseInt(x, 16), Integer.parseInt(y, 16)));
                break;
            case SENSOR:
                List<Integer> sensorData = message.substring(1).chars()
                .mapToObj(dat -> (dat == 'x') ? -1 : Integer.parseInt(Character.toString(dat)))
                .collect(Collectors.toList());
                SyncObject.AddSensorData(sensorData);
                break;
        }
    }
}