import exceptions.FailedToRunRaspistillException;
import java.io.IOException;
import java.lang.InterruptedException;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Color;

public class Player {

  public static RPiCamera piCamera;
  public static BufferedImage image;

  public Player() {

  }

  public static void main(String args[]) {

    try {
      piCamera = new RPiCamera("/home/pi/Player");
    }
    catch(FailedToRunRaspistillException e) {
      e.printStackTrace();
      System.out.println("Failed to initiate piCamera");
    }

    piCamera.setWidth(1640);
    piCamera.setHeight(1232);
    piCamera.setTimeout(5);
    piCamera.turnOffPreview();
    piCamera.setVerticalFlipOn();
    piCamera.setHorizontalFlipOn();

    try {
      image = piCamera.takeBufferedStill();
    }
    catch(IOException | InterruptedException e) {
      e.printStackTrace();
    }
    image = image.getSubimage(558, 333, 651, 700);
    drawGridOnImage();
    writeImageFile();

    System.out.println("Grejer");
  }

  public static void drawGridOnImage() {
    for(float y = 20; y < 690; y += 36.6) {
      for(int x = 18; x < 640; x += 34 ) {
        image.setRGB(x, (int)y, Color.green.getRGB());
      }
    }
  }

  public static void writeImageFile() {
    try {
      File outputFile = new File("test.png");
      ImageIO.write(image, "png", outputFile);
    }
    catch(IOException e) {
      e.printStackTrace();
      System.out.println("Faild to write image file");
    }
  }

}
