package org.bk.ass.manage;

import java.util.function.IntFunction;

/**
 * {@link Reservation} implementation for {@link GMS}. Can be used for tracking the remaining
 * available resources. (Ie. start each frame by {@link #setGms(GMS)} using the current amount of
 * resources, then use {@link GMS}-{@link Lock}s to track reservations.)
 */
public class GMSReservation implements Reservation<GMS> {

  private GMS gms = GMS.ZERO;
  private final IntFunction<GMS> gmsPrediction;

  public GMSReservation(
      IntFunction<GMS> gmsPrediction) {
    this.gmsPrediction = gmsPrediction;
  }

  public GMSReservation() {
    this(unused -> GMS.ZERO);
  }

  public void setGms(GMS gms) {
    this.gms = gms;
  }

  public GMS getGms() {
    return gms;
  }

  @Override
  public boolean reserve(Object source, GMS gmsToReserve) {
    boolean success = gms.canAfford(gmsToReserve);
    gms = gms.subtract(gmsToReserve);
    return success;
  }

  @Override
  public boolean itemReservableInFuture(Object source, GMS futureItem, int futureFrames) {
    GMS futureGMS = gmsPrediction.apply(futureFrames)
        .add(gms)
        // Already reserved
        .add(futureItem);
    return futureGMS.canAfford(futureItem);
  }

  @Override
  public void release(Object source, GMS gmsToRelease) {
    gms = gms.add(gmsToRelease);
  }
}
