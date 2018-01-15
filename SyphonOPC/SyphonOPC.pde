import codeanticode.syphon.*;

OPC opc;
PGraphics canvas;
SyphonClient client;

int shiftCoord(int w, float r, float x) 
{
    return (int) (w * ((x + r) / (2 * r))); 
}
    

void setup()
{
  int zoom = 8;
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

  // A Syphon server can be specified by the name of the application that it contains it,
  // its name, or both:
  
  // Only application name.
  //client = new SyphonClient(this, "SendFrames");
    
  // Both application and server names
  //client = new SyphonClient(this, "SendFrames", "Processing Syphon");
  
  // Only server name
  //client = new SyphonClient(this, "", "Processing Syphon");
    
  // An application can have several servers:
  //client = new SyphonClient(this, "Quartz Composer", "Raw Image");
  //client = new SyphonClient(this, "Quartz Composer", "Scene");
  
  background(0);
}

void draw() {
  if (client.newFrame()) {
    canvas = client.getGraphics(canvas);
    image(canvas, 0, 0, width, height);    
  }  
}

void keyPressed() {
  if (key == ' ') {
    client.stop();  
  } else if (key == 'd') {
    println(client.getServerName());
  }
}