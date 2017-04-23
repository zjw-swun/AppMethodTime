package com.branch.dellog

/**
 * Created by Ryze on 2017-2-5.
 */
public class DelLogUtil {

  public static final String head = 'Log.';
  public static final String tail_end = ');';
  public static final String suffix = '.java';
  public static final String charset = "utf-8";

  public static void delLog(File rootFile) {

    if (rootFile == null) {
      return;
    }

    if (rootFile.isDirectory()) {
      rootFile.eachFile {File file ->
        if (file.isFile()) {
          if (file.canRead() && file.name.endsWith(suffix)) {
            println "file: " + file.getAbsolutePath();
            delFileLog(file);
          }
        } else if (file.isDirectory()) {
          println "dir: " + file.getAbsolutePath();
          delLog(file);
        }
      }
    }
  }


  private static void delFileLog(File file) {
    def endFlag = 0;
    File ftmp = File.createTempFile(file.getAbsolutePath(), ".tmp");
    def printWriter = ftmp.newPrintWriter(charset);
    def reader = file.newReader(charset);
    def tmpline = null;
    String line;
    while ((line = reader.readLine()) != null) {
      if (line != null) {
        tmpline = line.trim();
        if (tmpline.startsWith(head) || endFlag == 1) {

          if (tmpline.endsWith(tail_end)) {
            endFlag = 0;
            printWriter.write(";\n")
            continue
          } else {
            endFlag = 1;
            continue
          }
        } else {
          printWriter.write(line + "\n");
        }
      }
    }

    reader.close();

    printWriter.flush();
    printWriter.close();

    file.delete();
    ftmp.renameTo(file.getAbsolutePath());
  }

}
