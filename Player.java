import exceptions.FailedToRunRaspistillException;
import java.io.IOException;
import java.lang.Exception;
import java.lang.InterruptedException;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.lang.Process;
import java.lang.Runtime;

import java.util.Random;
import java.lang.Thread;

public class Player {

  public static RPiCamera piCamera;
  public static BufferedImage image;
  public static BufferedImage referenceImage;
  public static char[][] board = new char[19][19];
  public static char[][] temp = new char[19][19];

  public static int[] modX = {-1, +1, 0, 0};
  public static int[] modY = {0, 0, -1, +1};

  public static char[] letters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T'};

  public Player() {

  }

  public static void main(String args[]) {
    
    initBoard();

    try {
      System.out.println("Initializing piCamera");
      piCamera = new RPiCamera("/home/pi/Player");
    }
    catch(FailedToRunRaspistillException e) {
      e.printStackTrace();
      System.out.println("Failed to initiate piCamera");
    }

    piCamera.setWidth(1640);
    piCamera.setHeight(1232);
    piCamera.setTimeout(4000);
    piCamera.turnOffPreview();
    piCamera.setVerticalFlipOn();
    piCamera.setHorizontalFlipOn();

    try {
      System.out.println("Capturing image");
      image = piCamera.takeBufferedStill();
      System.out.println("Capturing reference image");
      referenceImage = piCamera.takeBufferedStill();
    }
    catch(IOException | InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("Cropping images");
    image = image.getSubimage(563, 329, 651, 700);
    referenceImage = referenceImage.getSubimage(563, 329, 651, 700);

    // Uncomment for visual of grid:
    // -----------------------------
    // drawGridOnImage();
    // writeImageFile();
    // writeReferenceFile();
    // -----------------------------
    
    piCamera.setTimeout(1200);

    int moveCounter = 0;
    String move = "";
    String[] moveInfo;
    System.out.println("Ready to play");

    while(moveCounter < 20) {
      sleep(500);
      updateImage();
      move = findNewMove();
      if(move != "") {
	moveInfo = move.split(":");
	System.out.println("" + moveInfo[0] + ": " + moveInfo[1] + "-" + moveInfo[2]);
	board[Integer.parseInt(moveInfo[4])][Integer.parseInt(moveInfo[3])] = moveInfo[0].charAt(0);
	findAndRemoveCaptures(Integer.parseInt(moveInfo[4]), Integer.parseInt(moveInfo[3]), moveInfo[0].charAt(0));
	printBoardToConsole();
	moveCounter++;
      }
    }

    drawGridOnImage();
    writeImageFile();

  }


 
public static void updateImage() {
  try {
    System.out.println("Updating image");
    image = piCamera.takeBufferedStill();
    image = image.getSubimage(563, 329, 651, 700);
  }
  catch(IOException | InterruptedException e) {
    e.printStackTrace();
  }
}

  public static void findAndRemoveCaptures(int x, int y, char col) {
    for(int t = 0; t < 4; t++) {
      initTemp();
      if(x + modX[t] >= 0 && x + modX[t] <= 18 && y + modY[t] >= 0 && y + modY[t] <= 18) {
        if(board[y + modY[t]][x + modX[t]] == oppositeColor(col)) {
	  temp[y + modY[t]][x + modX[t]] = 'o';
	  if(!findLiberty(x + modX[t], y + modY[t], oppositeColor(col))) {
            printBoardToConsole();
            for(int y2 = 0; y2 < 19; y2++) {
              for(int x2 = 0 ; x2 < 19; x2++) {
                if(temp[y2][x2] == 'o') board[y2][x2] = '.';
              }
            }
            printBoardToConsole();
          }
	}
      }
    }
  }

  public static boolean findLiberty(int x, int y, char col) {
    for(int t = 0; t < 4; t++) {
      if(x + modX[t] >= 0 && x + modX[t] <= 18 && y + modY[t] >= 0 && y + modY[t] <= 18) {
        if(temp[y + modY[t]][x + modX[t]] !='o') {
          if(board[y + modY[t]][x + modX[t]] == '.') {
            return true;
          }
          if(board[y + modY[t]][x + modX[t]] == col) {
            temp[y + modY[t]][x + modX[t]] = 'o';
            if(findLiberty(x + modX[t], y + modY[t], col)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public static char oppositeColor(char col) {
    if(col == 'B') {
      return 'W';
    } 
    else if (col == 'W') {
      return 'B';
    }
    else {
      return 'X';
    }
  }

  public static void initBoard() {
    for(int y = 0; y < 19; y++) {
      for(int x = 0 ; x < 19; x++) {
        board[y][x] = '.';
      }
    }
  }

  public static void initTemp() {
    for(int y = 0; y < 19; y++) {
      for(int x = 0 ; x < 19; x++) {
        temp[y][x] = '.';
      }
    }
  }

  public static void printBoardToConsole() {
    System.out.println("===================");
    for(int y = 0; y < 19; y++) {
      for(int x = 0 ; x < 19; x++) {
        System.out.print(board[y][x]);
      }
      System.out.println("");
    }
  }

  public static void speak(String whatToSay) {
    try {
      String[] args = {"flite", "-voice", "slt", "-t", "\"" + whatToSay + "\""};
      Process p = Runtime.getRuntime().exec(args);
      p.waitFor();
      p.destroyForcibly();
    }
    catch(Exception e) {
      e.printStackTrace();
      System.out.println("Failed to play sound");
    }
  }

  public static String findNewMove() {
    int xCoordinate = 0;
    int yCoordinate = 0;
    char mark = '.';
    for(float y = 20; y < 690; y += 36.6) {
      xCoordinate = 0;
      for(float x = 18; x < 640; x += 34 ) {
        if(board[yCoordinate][xCoordinate] == '.') {
	  if(xCoordinate == 16 && yCoordinate == 4) {
	    mark = comparePixels(x, y, true);
	  }
	  else {
	  mark = comparePixels(x, y, false);
	  }
          if(mark == 'W') {
            sleep(1000);
	    updateImage();
	    if(comparePixels(x, y, false) == 'W') {
              return "W:" + letters[xCoordinate] + ":" + Integer.toString(yCoordinate + 1) + ":" + Integer.toString(xCoordinate) + ":" + Integer.toString(yCoordinate);
	    }
	  }  
	  if(mark == 'B') {
	    sleep(1000);
	    updateImage();
	    if(comparePixels(x, y, false) == 'B') {
              return "B:" + letters[xCoordinate] + ":" + Integer.toString(yCoordinate + 1) + ":" + Integer.toString(xCoordinate) + ":" + Integer.toString(yCoordinate);
	    }
	  }
	}
	xCoordinate++;
      }
      yCoordinate++;
    }
    return "";
  }

  public static char comparePixels(float x, float y, boolean output) {
    int[] xx = {-4, 4, -4, 4};
    int[] yy = {-4, -4, 4, 4};
    int black = 0;
    int white = 0;
    int pixelColor;
    int red;
    int green;
    int blue;
    int referenceColor;
    int refRed;
    int refGreen;
    int refBlue;
    for(int t = 0; t < 4; t++) {
      pixelColor = image.getRGB((int)(x + xx[t]), (int)(y + yy[t]));
      blue = pixelColor & 0xff;
      green = (pixelColor & 0xff00) >> 8;
      red = (pixelColor & 0xff0000) >> 16;
      referenceColor = referenceImage.getRGB((int)(x + xx[t]), (int)(y + yy[t]));
      refBlue = referenceColor & 0xff;
      refGreen = (referenceColor & 0xff00) >> 8;
      refRed = (referenceColor & 0xff0000) >> 16;
      if(output) {
	System.out.println(red + ", " + green + ", " + blue + " -- " + refRed + ", " + refGreen + ", " + refBlue);
      }
      if(blue >= refBlue - 5 && blue <= refBlue + 5) {
        if(red >= refRed - 5 && red <= refRed + 5) {
          if(green >= refGreen - 5 && green <= refGreen + 5) {
            referenceImage.setRGB((int)(x + xx[t]), (int)(y + yy[t]), image.getRGB((int)(x + xx[t]), (int)(y + yy[t])));
          }
        }
      }
      if(red > refRed + 5 && green > refGreen + 15 && blue > refBlue + 20) {
        white++;
      }
      if(red < refRed - 30 && green < refGreen - 5) {
        black++;
      }
    }
    if(white >= 3) return 'W';
    if(black >= 3) return 'B';
    return '.';
  }

  public static void drawGridOnImage() {
    for(float y = 20; y < 690; y += 36.6) {
      for(float x = 18; x < 640; x += 34 ) {
        image.setRGB((int)x, (int)y, Color.green.getRGB());
        image.setRGB((int)(x - 4), (int)(y - 4), Color.red.getRGB());
        image.setRGB((int)(x + 4), (int)(y - 4), Color.red.getRGB());
        image.setRGB((int)(x - 4), (int)(y + 4), Color.red.getRGB());
        image.setRGB((int)(x + 4), (int)(y + 4), Color.red.getRGB());
      }
    }

  }

  public static void sleep(int millis) {
      try {
	Thread.sleep(millis);
      }
      catch(InterruptedException e) {
	e.printStackTrace();
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

  public static void writeReferenceFile() {
    try {
      File outputFile = new File("referemce.png");
      ImageIO.write(referenceImage, "png", outputFile);
    }
    catch(IOException e) {
      e.printStackTrace();
      System.out.println("Faild to write image file");
    }
  }

}
