package org.bk.ass.cvis;

import static java.lang.Integer.max;
import static java.util.Collections.emptyList;

import bwapi.Color;
import bwapi.Unit;
import bwapi.UnitType;
import com.github.luben.zstd.ZstdOutputStream;
import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bk.ass.cvis.CVisOutput.Attachment;

public class CVisOutput implements Closeable {

  private int frame;
  private JsonStream out;
  private boolean firstDrawCommand = true;
  private boolean firstDrawCommandFrame = true;
  private Map<String, List<FirstSeen>> firstSeen = new HashMap<>();
  private List<LogEntry> logs = new ArrayList<>();
  private Map<String, List<LogEntry>> units_logs = new HashMap<>();

  private CVisOutput(OutputStream out) {
    Objects.requireNonNull(out);
    this.out = new JsonStream(out, 4096);

    try {
      writePreamble();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static CVisOutput jsonOutput(OutputStream out) {
    return new CVisOutput(out);
  }

  public static CVisOutput compressedOutput(OutputStream out) {
    try {
      return new CVisOutput(new ZstdOutputStream(out));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writePreamble() throws IOException {
    out.writeObjectStart();
    out.writeObjectField("_version");
    out.writeVal(0);
    out.writeMore();
    out.writeObjectField("types_names");
    Any.wrap(Arrays.stream(UnitType.values()).collect(
        Collectors.toMap(it -> Integer.toString(it.ordinal()),
            it -> it.name().substring(it.name().indexOf('_') + 1))))
        .writeTo(out);
    out.writeMore();

    // Write objects that are not yet supported
    for (String name : new String[]{
        "board_updates", "units_updates",
        "tensors_summaries", "game_values"}) {
      out.writeObjectField(name);
      out.writeEmptyObject();
      out.writeMore();
    }

    // Write arrays that are not yet supported
    for (String name : new String[]{"tasks", "trees", "heatmaps"}) {
      out.writeObjectField(name);
      out.writeEmptyArray();
      out.writeMore();
    }

    // Stream draw commands
    out.writeObjectField("draw_commands");
    out.writeObjectStart();
  }

  public void setFrame(int frame) {
    this.frame = frame;

    try {
      // End of current frame draw commands
      if (!firstDrawCommand) {
        out.writeArrayEnd();
      }
      firstDrawCommand = true;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void addDrawCommand(DrawCommand drawCommand) {
    try {
      if (firstDrawCommand) {
        if (firstDrawCommandFrame) {
          firstDrawCommandFrame = false;
        } else {
          out.writeMore();
        }
        out.writeObjectField(Integer.toString(frame));
        out.writeArrayStart();
        firstDrawCommand = false;
      } else {
        out.writeMore();
      }
      Any.wrap(drawCommand).writeTo(out);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public void drawText(int x, int y, String text) {
    addDrawCommand(new DrawCommand(DrawType.DrawText, Arrays.asList(x, y), text));
  }

  public void drawLine(int ax, int ay, int bx, int by, Color color) {
    drawLine(ax, ay, bx, by, color.id);
  }

  public void drawLine(int ax, int ay, int bx, int by, int color) {
    addDrawCommand(new DrawCommand(DrawType.DrawLine, Arrays.asList(ax, ay, bx, by, color)));
  }

  public void drawUnitPosLine(Unit unit, int ax, int ay, int color) {
    addDrawCommand(
        new DrawCommand(DrawType.DrawUnitPosLine, Arrays.asList(unit.getID(), ax, ay, color), "",
            Collections.singletonList(0)));
  }

  public void drawUnitPosLine(Unit unit, int ax, int ay, Color color) {
    addDrawCommand(
        new DrawCommand(DrawType.DrawUnitPosLine, Arrays.asList(unit.getID(), ax, ay, color.id), "",
            Collections.singletonList(0)));
  }

  public void onFirstSeen(Unit unit) {
    firstSeen.computeIfAbsent(Integer.toString(max(frame, 1)), unused -> new ArrayList<>())
        .add(new FirstSeen(unit.getID(), unit.getType().ordinal(), unit.getX(), unit.getY()));
  }

  public Logger getLogger() {
    return new GlobalLogger();
  }

  public Logger getUnitLogger(Unit unit) {
    return new UnitLogger(
        units_logs.computeIfAbsent(Integer.toString(unit.getID()), unused -> new ArrayList<>()));
  }

  @Override
  public void close() {
    try {
      // End of current frame draw commands
      if (!firstDrawCommand) {
        out.writeArrayEnd();
      }
      // End of draw commands
      out.writeObjectEnd();

      out.writeMore();
      out.writeObjectField("units_first_seen");
      Any.wrap(firstSeen).writeTo(out);

      out.writeMore();
      out.writeObjectField("logs");
      Any.wrap(logs).writeTo(out);

      out.writeMore();
      out.writeObjectField("units_logs");
      Any.wrap(units_logs).writeTo(out);

      // End of output
      out.writeObjectEnd();
      out.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static abstract class Logger {

    Logger() {

    }

    public void info(String message) {
      log(message, 0, emptyList());
    }

    public void log(int level, String message, List<Attachment> attachments) {
      log(message, level, attachments);
    }

    public void warn(String message) {
      log(message, 1, emptyList());
    }

    public void error(String message) {
      log(message, 2, emptyList());
    }

    public void fatal(String message) {
      log(message, 3, emptyList());
    }

    protected abstract void log(String message, int level, List<Attachment> attachments);
  }

  private class GlobalLogger extends Logger {

    @Override
    protected void log(String message, int level, List<Attachment> attachments) {
      StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
      logs.add(
          new LogEntry(frame, message, traceElement.getFileName(), traceElement.getLineNumber(),
              level, attachments));
    }
  }

  private class UnitLogger extends Logger {

    private final List<LogEntry> unitLogs;

    private UnitLogger(List<LogEntry> unitLogs) {
      this.unitLogs = unitLogs;
    }

    @Override
    protected void log(String message, int level, List<Attachment> attachments) {
      StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
      unitLogs.add(
          new LogEntry(frame, message, traceElement.getFileName(), traceElement.getLineNumber(),
              level, attachments));
    }
  }

  public static class Attachment {

    @JsonIgnore
    private final AttachmentType keyType;
    @JsonIgnore
    private final AttachmentType valueType;
    private final List<Object[]> map;

    public Attachment(
        AttachmentType keyType,
        AttachmentType valueType,
        List<Object[]> map) {
      this.keyType = keyType;
      this.valueType = valueType;
      this.map = map;
    }

    public Object getKey_type() {
      return keyType.type;
    }

    public Object getValue_type() {
      return valueType.type;
    }
  }

  public static class UnitValue {

    public final String type = "unit";
    public final int id;

    public UnitValue(int id) {
      this.id = id;
    }
  }

  public static class PositionValue {

    public final String type = "position";
    public final int x;
    public final int y;

    public PositionValue(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  public enum AttachmentType {
    UNIT("Unit*"),
    POSITION("Position"),
    DUMPABLE("Dumpable"),
    STRING("std::string"),
    FLOAT("float"),
    INT("int");

    final String type;

    AttachmentType(String value) {
      this.type = value;
    }
  }

}

class CVisData {

  final int _version = 0;
}

enum DrawType {
  DrawLine(20), //  x1, y1, x2, y2, color index
  DrawUnitLine(21), // uid1, uid2, color index
  DrawUnitPosLine(22), // uid, x2, y2, color index
  DrawCircle(23), //  x, y, radius, color index
  DrawUnitCircle(24), // uid, radius, color index
  DrawText(25), // x, y plus text arg
  DrawTextScreen(26),
  ; // x, y plus text arg

  final int code;

  DrawType(int code) {
    this.code = code;
  }
}

class DrawCommand {

  final int code;
  final List<Integer> args;
  final String str;
  final List<Integer> cherrypi_ids_args_indices;

  DrawCommand(
      DrawType drawType,
      List<Integer> args,
      String str,
      List<Integer> cherrypi_ids_args_indices) {
    this.code = drawType.code;
    this.args = args;
    this.str = Objects.requireNonNull(str);
    this.cherrypi_ids_args_indices = Objects.requireNonNull(cherrypi_ids_args_indices);
  }

  public DrawCommand(
      DrawType type,
      List<Integer> args,
      String str) {
    this(type, args, str, emptyList());
  }

  public DrawCommand(DrawType drawType, List<Integer> args) {
    this(drawType, args, "", emptyList());
  }
}


class FirstSeen {

  final int id;
  final int type;
  final int x;
  final int y;

  FirstSeen(int id, int type, int x, int y) {
    this.id = id;
    this.type = type;
    this.x = x;
    this.y = y;
  }
}

class LogEntry {

  final int frame;
  final String message;
  final String file;
  final int line;
  final int sev;
  final List<Attachment> attachments;

  LogEntry(int frame, String message, String file, int line, int sev,
      List<Attachment> attachments) {
    this.frame = frame;
    this.message = message;
    this.file = file;
    this.line = line;
    this.sev = sev;
    this.attachments = attachments;
  }
}


