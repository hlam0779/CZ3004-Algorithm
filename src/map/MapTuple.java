package map;

public class MapTuple { 
    private final String p1;
    private final String p2;
    public MapTuple(String p1, String p2) { 
        this.p1 = p1;
        this.p2 = p2;
    }

    public String GetP1() { 
        return this.p1; 
    }

    public String GetP2() {
        return this.p2; 
    }
}