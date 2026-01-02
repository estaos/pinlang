package org.estaos.pin.core.parser.ast;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTypeTest {

    @Test
    void testBasicTypeConstants() {
        Assertions.assertEquals("int8", BasicType.INT8);
        assertEquals("int16", BasicType.INT16);
        assertEquals("int32", BasicType.INT32);
        assertEquals("int64", BasicType.INT64);
        assertEquals("char", BasicType.CHAR);
        assertEquals("boolean", BasicType.BOOLEAN);
        assertEquals("float", BasicType.FLOAT);
        assertEquals("double", BasicType.DOUBLE);
    }

    @Test
    void testSelfCasting() {
        BasicType int8 = new BasicType(null, BasicType.INT8, "");
        assertTrue(int8.canBeCastedTo(int8));
        assertTrue(int8.canBeAssigned(int8));
    }

    @Test
    void testCharToInt8Casting() {
        BasicType charType = new BasicType(null, BasicType.CHAR, "");
        BasicType int8Type = new BasicType(null, BasicType.INT8, "");

        assertTrue(charType.canBeCastedTo(int8Type));
        assertTrue(int8Type.canBeCastedTo(charType));
        assertTrue(charType.canBeAssigned(int8Type));
        assertTrue(int8Type.canBeAssigned(charType));
    }

    @Test
    void testIntegerTypeCasting() {
        BasicType int8 = new BasicType(null, BasicType.INT8, "");
        BasicType int16 = new BasicType(null, BasicType.INT16, "");
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        BasicType int64 = new BasicType(null, BasicType.INT64, "");

        assertTrue(int8.canBeCastedTo(int16));
        assertTrue(int16.canBeCastedTo(int32));
        assertTrue(int32.canBeCastedTo(int64));
        assertTrue(int64.canBeCastedTo(int8));
    }

    @Test
    void testCharToIntegerTypeCasting() {
        BasicType charType = new BasicType(null, BasicType.CHAR, "");
        BasicType int16 = new BasicType(null, BasicType.INT16, "");
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        BasicType int64 = new BasicType(null, BasicType.INT64, "");

        assertTrue(charType.canBeCastedTo(int16));
        assertTrue(charType.canBeCastedTo(int32));
        assertTrue(charType.canBeCastedTo(int64));
        assertTrue(int16.canBeCastedTo(charType));
        assertTrue(int32.canBeCastedTo(charType));
        assertTrue(int64.canBeCastedTo(charType));
    }

    @Test
    void testFloatIntegerCasting() {
        BasicType floatType = new BasicType(null, BasicType.FLOAT, "");
        BasicType doubleType = new BasicType(null, BasicType.DOUBLE, "");
        BasicType int32 = new BasicType(null, BasicType.INT32, "");

        assertTrue(floatType.canBeCastedTo(int32));
        assertTrue(int32.canBeCastedTo(floatType));
        assertTrue(doubleType.canBeCastedTo(int32));
        assertTrue(int32.canBeCastedTo(doubleType));
    }

    @Test
    void testFloatDoubleCasting() {
        BasicType floatType = new BasicType(null, BasicType.FLOAT, "");
        BasicType doubleType = new BasicType(null, BasicType.DOUBLE, "");

        assertTrue(floatType.canBeCastedTo(doubleType));
        assertTrue(doubleType.canBeCastedTo(floatType));
    }

    @Test
    void testInvalidCasting() {
        BasicType booleanType = new BasicType(null, BasicType.BOOLEAN, "");
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        BasicType pointerType = new Pointer(null, "", 0,
                TypeReference.ofType(new BasicType(null, BasicType.INT32, "")));

        assertFalse(booleanType.canBeCastedTo(int32));
        assertFalse(int32.canBeCastedTo(booleanType));
        assertFalse(int32.canBeCastedTo(pointerType));
    }

    @Test
    void testPointerToIntCasting() {
        BasicType int64 = new BasicType(null, BasicType.INT64, "");
        Pointer pointerType = new Pointer(null, "", 0,
                TypeReference.ofType(new BasicType(null, BasicType.CHAR, "")));

        assertTrue(pointerType.canBeCastedTo(int64));
        assertFalse(int64.canBeCastedTo(pointerType));
    }

    @Test
    void testNonBasicTypeCasting() {
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        Type nonBasicType = new Type(null, "CustomType", null, "", null) {
            @Override
            public boolean canBeCastedTo(Type other) {
                return false;
            }

            @Override
            public boolean canBeAssigned(Type other) {
                return false;
            }
        };

        assertFalse(int32.canBeCastedTo(nonBasicType));
        assertFalse(int32.canBeAssigned(nonBasicType));
    }

    @Test
    void testAssignmentEquivalentToCasting() {
        BasicType charType = new BasicType(null, BasicType.CHAR, "");
        BasicType int8Type = new BasicType(null, BasicType.INT8, "");
        BasicType floatType = new BasicType(null, BasicType.FLOAT, "");
        BasicType doubleType = new BasicType(null, BasicType.DOUBLE, "");

        assertEquals(charType.canBeCastedTo(int8Type), charType.canBeAssigned(int8Type));
        assertEquals(floatType.canBeCastedTo(doubleType), floatType.canBeAssigned(doubleType));
        assertEquals(doubleType.canBeCastedTo(floatType), doubleType.canBeAssigned(floatType));
    }

    @Test
    void testPointerToPointerCasting() {
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        BasicType int64 = new BasicType(null, BasicType.INT64, "");
        
        Pointer int32Ptr = new Pointer(null, "", 0, TypeReference.ofType(int32));
        Pointer int64Ptr = new Pointer(null, "", 0, TypeReference.ofType(int64));
        Pointer voidPtr = new Pointer(null, "", 0, null);
        
        // Same type pointers can cast to each other
        assertTrue(int32Ptr.canBeCastedTo(int32Ptr));
        
        // Compatible pointed-to types can cast
        assertTrue(int32Ptr.canBeCastedTo(int64Ptr));
        assertTrue(int64Ptr.canBeCastedTo(int32Ptr));
        
        // Void pointers can cast to any pointer
        assertTrue(voidPtr.canBeCastedTo(int32Ptr));
        assertTrue(int32Ptr.canBeCastedTo(voidPtr));
    }

    @Test
    void testPointerDimensionCasting() {
        BasicType int32 = new BasicType(null, BasicType.INT32, "");
        
        Pointer singlePtr = new Pointer(null, "", 0, TypeReference.ofType(int32));
        Pointer doublePtr = new Pointer(null, "", 1, TypeReference.ofType(int32));
        
        // Different dimensions cannot cast
        assertFalse(singlePtr.canBeCastedTo(doublePtr));
        assertFalse(doublePtr.canBeCastedTo(singlePtr));
    }
}