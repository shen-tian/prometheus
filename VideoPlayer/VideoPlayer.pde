import processing.video.*;
import java.util.Properties.*;
import java.lang.Thread.*;


String filename = "/Users/Shen/Desktop/VJ Loops/quicksilver (loop).mp4";

float zoom = 1;

Movie movie;
PGraphics[] pyramid;

OPC opc;

int shiftCoord(int w, float r, float x) 
{
    return (int) (w * ((x + r) / (2 * r)));
}

void setup()
{
    System.out.println("Launching video");
    size(480, 270, P3D);
    
        // Connect to the local instance of fcserver. You can change this line to connect to another computer's fcserver
    opc = new OPC(this, "127.0.0.1", 7890);
    opc.showLocations(true);

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
    float yScale = height/(maxY - minY);
    
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
    
    movie = new Movie(this, filename);
    movie.loop();
    
    delay(1000);
    
    while (!movie.available())
        delay(100);

    pyramid = new PGraphics[4];
    for (int i = 0; i < pyramid.length; i++) {
        pyramid[i] = createGraphics(width / (1 << i), height / (1 << i), P3D);
    }
}

void keyPressed() {
    if (key == ' ') movie.pause();
    if (key == ']') zoom *= 1.1;
    if (key == '[') zoom *= 0.9;
    if (key == 'n') {  
        movie.stop();
        movie = new Movie(this, "/Users/Shen/lsdome/club.mp4");
        movie.loop();
    }
}

void keyReleased() {
    if (key == ' ') movie.play();
}  

void movieEvent(Movie m)
{
    if (m.available()) {
        m.read();
    }
}

void draw()
{
    // Scale to width, center height
    int mWidth = int(pyramid[0].width * zoom);
    int mHeight = mWidth * movie.height / movie.width;

    // Center location
    float x, y;

    if (mousePressed) {
        // Pan horizontally and vertically with the mouse
        x = -mouseX * (mWidth - pyramid[0].width) / width;
        y = -mouseY * (mHeight - pyramid[0].height) / height;
    } else {
        // Centered
        x = -(mWidth - pyramid[0].width) / 2;
        y = -(mHeight - pyramid[0].height) / 2;
    }

    pyramid[0].beginDraw();
    pyramid[0].background(0);
    pyramid[0].image(movie, x, y, mWidth, mHeight);
    pyramid[0].endDraw();

    for (int i = 1; i < pyramid.length; i++) {
        pyramid[i].beginDraw();
        pyramid[i].image(pyramid[i-1], 0, 0, pyramid[i].width, pyramid[i].height);
        pyramid[i].endDraw();
    }

    image(pyramid[pyramid.length - 1], 0, 0, width, height);
}
