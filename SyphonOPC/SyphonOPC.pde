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
    size(480, 370, P3D);

    // Connect to the local instance of fcserver. You can change this line to connect to another computer's fcserver
    opc = new OPC(this, "127.0.0.1", 7890);
    opc.showLocations(false);

    JSONArray layout = loadJSONArray("layout.json");

    float minX = 1000;
    float minY = 1000;
    float maxX = -1000;
    float maxY = -1000;
    
    for (int i = 0; i < layout.size(); i++)
    {
        JSONArray point = layout.getJSONObject(i).getJSONArray("point");
        float x = point.getFloat(1);
        float y = point.getFloat(0);
          
          minX = min(minX, x);
          maxX = max(maxX, x);
          
         minY = min(minY, y);
         maxY = max(maxY, y);
    }
    
    System.out.println(maxX);
    System.out.println(maxY);

    float xScale = width/(maxX - minX);
    float yScale = (height - 100)/(maxY - minY);
    
    float scale = 0.99 * min(xScale, yScale);
    

    for (int i = 0; i < layout.size(); i++) 
    {
        JSONArray point = layout.getJSONObject(i).getJSONArray("point");
        
        float x = point.getFloat(1);
        float y = point.getFloat(0);
        
        if ((x != -5.0) && (y != -5.0)){
          int xx = (int)(scale * (x - minX));
          int yy = height - 1 - (int)(scale * (y - minY));
          opc.led(i, xx, yy);
        }
        else
        {
          opc.led(i, 0, 0);
        }
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
