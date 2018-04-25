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
  public static char[][] board = new char[19][19];
  public static char[][] temp = new char[19][19];

  public Player() {

  }

  public static void main(String args[]) {

    initBoard();
    printBoardToConsole();
    Random rand = new Random();
    char colToPlay = 'B';
    int x = rand.nextInt(19);
    int y = rand.nextInt(19);
    for(int t = 0; t < 300; t++) {
      while(board[y][x] != 'x') {
        x = rand.nextInt(19);
        y = rand.nextInt(19);
      }

      board[y][x] = colToPlay;
      findAndRemoveCaptures(x, y, colToPlay);
      colToPlay = oppositeColor(colToPlay);

    }

    printBoardToConsole();

    /*
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

    speak("Ok. Let's play");
    */

  }

  public static void findAndRemoveCaptures(int x, int y, char col) {
    initTemp();
    if(x - 1 >= 0 && board[y][x-1] == oppositeColor(col) && !findLiberty(x - 1, y, col)) {
      if(x + 1 <= 18 && board[y][x+1] == oppositeColor(col) && !findLiberty(x + 1, y, col)) {
        if(y - 1 >= 0 && board[y-1][x] == oppositeColor(col) && !findLiberty(x, y - 1, col)) {
          if(y + 1 <= 18 && board[y+1][x] == oppositeColor(col) && !findLiberty(x, y + 1, col)) {
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
    if(x - 1 >= 0) {
      if(temp[y][x-1] !='o') {
        if(board[y][x-1] == 'x') {
          return true;
        }
        if(board[y][x-1] == col) {
          temp[y][x-1] = 'o';
          if(findLiberty(x - 1, y, col)) {
            return true;
          }
        }
      }
    }
    if(x + 1 <= 18) {
      if(temp[y][x+1] !='o') {
        if(board[y][x+1] == 'x') {
          return true;
        }
        if(board[y][x+1] == col) {
          temp[y][x+1] = 'o';
          if(findLiberty(x + 1, y, col)) {
            return true;
          }
        }
      }
    }
    if(y - 1 >= 0) {
      if(temp[y-1][x] !='o') {
        if(board[y-1][x] == 'x') {
          return true;
        }
        if(board[y-1][x] == col) {
          temp[y-1][x] = 'o';
          if(findLiberty(x, y - 1, col)) {
            return true;
          }
        }
      }
    }
    if(y + 1 <= 18) {
      if(temp[y+1][x] !='o') {
        if(board[y+1][x] == 'x') {
          return true;
        }
        if(board[y+1][x] == col) {
          temp[y+1][x] = 'o';
          if(findLiberty(x, y + 1, col)) {
            return true;
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
