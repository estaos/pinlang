package com.oreal.escript.extern;

import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.Type;

import java.util.List;

record NativeSymbols(List<Type> types, List<? extends Symbol> variables) {}
