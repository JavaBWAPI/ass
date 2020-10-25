package org.bk.ass.cvis;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class CVisOutputTest {

  @Test
  void test() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CVisOutput sut = CVisOutput.jsonOutput(bos);
    sut.drawText(10, 10, "test");
    sut.close();
    System.err.println(new String(bos.toByteArray()));
  }
}