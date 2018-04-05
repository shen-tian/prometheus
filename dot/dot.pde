OPC opc;
PImage dot;

int shiftCoord(int w, float r, float x) 
{
    return (int) (w * ((x + r) / (2 * r))); 
}

void setup()
{
  size(400, 400);

  // Load a sample image
  dot = loadImage("color-dot.png");

  opc = new OPC(this, "127.0.0.1", 7890);
  //opc.showLocations(false);

  JSONArray layout = loadJSONArray("layout.json");
  
  for (int i = 0; i < layout.size(); i++) 
  {
    JSONArray point = layout.getJSONObject(i).getJSONArray("point");
    int x = shiftCoord(width, 5, point.getFloat(0));
    int y = shiftCoord(height, 5, point.getFloat(1));
    opc.led(i, x, y);
  }
}

void draw()
{
  background(0);

  // Draw the image, centered at the mouse location
  float dotSize = width * 0.2;
  image(dot, mouseX - dotSize/2, mouseY - dotSize/2, dotSize, dotSize);
}

