/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.fatptr;

import org.renjin.gcc.codegen.MethodGenerator;
import org.renjin.gcc.codegen.expr.GExpr;
import org.renjin.gcc.codegen.expr.JExpr;
import org.renjin.gcc.codegen.type.voidt.VoidPtr;
import org.renjin.repackaged.asm.Label;
import org.renjin.repackaged.asm.Type;

public class DereferencedWrappedFatPtr implements FatPtr {

  private ValueFunction valueFunction;
  private WrappedFatPtrExpr address;

  public DereferencedWrappedFatPtr(ValueFunction valueFunction, WrappedFatPtrExpr address) {
    this.valueFunction = valueFunction;
    this.address = address;
  }

  @Override
  public void store(MethodGenerator mv, GExpr rhs) {
    if(rhs instanceof VoidPtr) {
      invokeSet(mv, ((VoidPtr) rhs).unwrap());
      
    } else if(rhs instanceof FatPtr) {
      invokeSet(mv, ((FatPtr) rhs).wrap());
      
    } else {
      throw new UnsupportedOperationException("TODO: rhs = " + rhs.getClass().getName());
    }
  }
  
  private void invokeSet(MethodGenerator mv, JExpr rhs) {
    // Invoke the set() method on the ObjectPtr
    JExpr wrapperInstance = address.wrap();

    wrapperInstance.load(mv);
    rhs.load(mv);

    mv.invokevirtual(wrapperInstance.getType(), "set",
        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
  }

  @Override
  public GExpr addressOf() {
    return address;
  }

  @Override
  public Type getValueType() {
    return valueFunction.getValueType();
  }

  @Override
  public boolean isAddressable() {
    return true;
  }

  @Override
  public JExpr wrap() {
    return address.valueExpr();
  }

  @Override
  public FatPtrPair toPair(MethodGenerator mv) {
    return Wrappers.toPair(mv, valueFunction, address.valueExpr());
  }

  @Override
  public void jumpIfNull(MethodGenerator mv, Label label) {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public GExpr valueOf() {
    return valueFunction.dereference(this.address);
  }
}
