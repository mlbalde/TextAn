/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package cz.cuni.mff.ufal.nametag;

public class TokenRange {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected TokenRange(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(TokenRange obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        nametag_javaJNI.delete_TokenRange(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setStart(long value) {
    nametag_javaJNI.TokenRange_start_set(swigCPtr, this, value);
  }

  public long getStart() {
    return nametag_javaJNI.TokenRange_start_get(swigCPtr, this);
  }

  public void setLength(long value) {
    nametag_javaJNI.TokenRange_length_set(swigCPtr, this, value);
  }

  public long getLength() {
    return nametag_javaJNI.TokenRange_length_get(swigCPtr, this);
  }

  public TokenRange() {
    this(nametag_javaJNI.new_TokenRange(), true);
  }

}