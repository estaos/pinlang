package org.estaos.pin.core.extern;

import org.estaos.pin.core.parser.ast.Symbol;
import org.estaos.pin.core.parser.ast.Type;

import java.util.List;

record NativeSymbols(List<Type> types, List<? extends Symbol> variables) {}
