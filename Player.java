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

  public Player() {

  }

  public static void main(String args[]) {

    initBoard();

    try {
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
      image = piCamera.takeBufferedStill();
      referenceImage = piCamera.takeBufferedStill();
    }
    catch(IOException | InterruptedException e) {
      e.printStackTrace();
    }

    image = image.getSubimage(558, 334, 651, 700);
    referenceImage = referenceImage.getSubimage(558, 334, 651, 700);

    // Uncomment for visual of grid:
    // -----------------------------
    drawGridOnImage();
    writeImageFile();
    // -----------------------------

    // speak("Ok. Let's play");

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
                if(temp[y2][x2] == 'o') board[y2][x2] = 'x';
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
          if(board[y + modY[t]][x + modX[t]] == 'x') {
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
        board[y][x] = 'x';
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
      p.destroy();
    }
    catch(Exception e) {
      e.printStackTrace();
      System.out.println("Failed to play sound");
    }
  }

  public static void drawGridOnImage() {
    for(float y = 20; y < 690; y += 36.6) {
      for(int x = 18; x < 640; x += 34 ) {
        image.setRGB(x, (int)y, Color.green.getRGB());
        image.setRGB(x - 4, (int)y - 4, Color.red.getRGB());
        image.setRGB(x + 4, (int)y - 4, Color.red.getRGB());
        image.setRGB(x - 4, (int)y + 4, Color.red.getRGB());
        image.setRGB(x + 4, (int)y + 4, Color.red.getRGB());
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
