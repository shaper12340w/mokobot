package com.jfposton.ytdlp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfposton.ytdlp.DownloadProgressCallback;

public class StreamProcessExtractor extends Thread {
  private static final String GROUP_PERCENT = "percent";
  private static final String GROUP_MINUTES = "minutes";
  private static final String GROUP_SECONDS = "seconds";
  private InputStream stream;
  private StringBuilder buffer;
  private final DownloadProgressCallback callback;

  private Pattern p =
      Pattern.compile(
          "\\[download\\]\\s+(?<percent>\\d+\\.\\d)% .* ETA (?<minutes>\\d+):(?<seconds>\\d+)");

  public StreamProcessExtractor(
      StringBuilder buffer, InputStream stream, DownloadProgressCallback callback) {
    this.stream = stream;
    this.buffer = buffer;
    this.callback = callback;
    this.start();
  }

  @Override
  public void run() {
    try {
      StringBuilder currentLine = new StringBuilder();
      int nextChar;
      while ((nextChar = stream.read()) != -1) {
        buffer.append((char) nextChar);
        if (nextChar == '\r' && callback != null) {
          processOutputLine(currentLine.toString());
          currentLine.setLength(0);
          continue;
        }
        currentLine.append((char) nextChar);
      }
    } catch (IOException ignored) {
      ignored.printStackTrace();
    }
  }

  private void processOutputLine(String line) {
    Matcher m = p.matcher(line);
    if (m.matches()) {
      float progress = Float.parseFloat(m.group(GROUP_PERCENT));
      long eta = convertToSeconds(m.group(GROUP_MINUTES), m.group(GROUP_SECONDS));
      callback.onProgressUpdate(progress, eta);
    }
  }

  private int convertToSeconds(String minutes, String seconds) {
    return Integer.parseInt(minutes) * 60 + Integer.parseInt(seconds);
  }
}
