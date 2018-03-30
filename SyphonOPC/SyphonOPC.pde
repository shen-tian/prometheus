import codeanticode.syphon.*;
import java.util.LinkedList;

OPC opc;
PGraphics canvas;
SyphonClient client;

int shiftCoord(int w, float r, float x) 
{
    return (int) (w * ((x + r) / (2 * r)));
}

void setup()
{
    size(480, 480, P3D);

    // Connect to the local instance of fcserver. You can change this line to connect to another computer's fcserver
    opc = new OPC(this, "127.0.0.1", 7890);
    opc.showLocations(false);

    JSONArray layout = loadJSONArray("layout.json");

    for (int i = 0; i < layout.size(); i++) 
    {
        JSONArray point = layout.getJSONObject(i).getJSONArray("point");
        int x = shiftCoord(480, 10, point.getFloat(0));
        int y = shiftCoord(480, 10, point.getFloat(1));
        opc.led(i, x, y);
    }

    // Make the status LED quiet
    opc.setStatusLed(false);

    println("Available Syphon servers:");
    println(SyphonClient.listServers());

    // Create syhpon client to receive frames 
    // from the first available running server: 
    client = new SyphonClient(this);

    background(0);
}

int lastTick = 0;
int lastFrameTick = 0;

LinkedList<Integer> history = new LinkedList<Integer>();
int queueLength = 10; 

void drawHistory(LinkedList<Integer> history)
{
    fill(255);
    for (int i = 0; i < history.size(); i++)
    {
        rect(0, 5 * i, history.get(i) * 3, 5);
    }
    fill(255, 0, 0);
    rect(0, 50, 2, 5);
    rect(90, 50, 2, 5);
    rect(180, 50, 2, 5);
    
    
}

void draw() {
    int tick = millis();

    if (client.newFrame()) {
        background(0);

        canvas = client.getGraphics(canvas);
        image(canvas, 0, 100, width, height-100);
        fill(255);
        textSize(32);
        
        int drawTime = tick-lastTick;
        int frameTime = 1000/ (tick-lastFrameTick);
        
        history.add(frameTime);
        if (history.size() > 10)
            history.remove();
            
        drawHistory(history);
        
        //rect(0, 0, 1000/(tick-lastTick), 32);
        //rect(0, 32, 1000/(tick-lastFrameTick), 32);
        
        lastFrameTick = tick;
    }
    lastTick = tick;
}

void keyPressed() {
    if (key == ' ') {
        client.stop();
    } else if (key == 'd') {
        println(client.getServerName());
    }
}