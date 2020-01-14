package com.company.utilities.getopts;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {
    @Test
    void testBoolean() throws Exception {
        Args args = new Args("l", new String[]{"-l"});
        assertTrue(args.isValid());
        assertTrue(args.getBoolean('l'));
        assertEquals(1, args.cardinality());
    }

    @Test
    void testBoolean2() throws Exception {
        Args args = new Args("l", new String[]{"-l"});
        assertTrue(args.isValid());
        assertFalse(args.getBoolean('d'));
        assertEquals(1, args.cardinality());
    }

    @Test
    void testString() throws Exception {
        Args args = new Args("d*", new String[]{"-d", "qwqr"});
        assertTrue(args.isValid());
        assertEquals("qwqr", args.getString('d'));
        assertEquals(1, args.cardinality());
    }

    @Test
    void testBooleanAndString() throws Exception {
        Args args = new Args("l,d*", new String[]{"-l", "-d", "qwqr"});
        assertTrue(args.isValid());
        assertTrue(args.getBoolean('l'));
        assertEquals("qwqr", args.getString('d'));
        assertEquals(2, args.cardinality());
    }

    @Test
    void testSimpleDouble() throws Exception {
        Args args = new Args("x##", new String[]{"-x", "42.3"});
        assertTrue(args.isValid());
        assertEquals(1, args.cardinality());
        assertTrue(args.has('x'));
        assertEquals(42.3, args.getDouble('x'), .001);
    }

    @Test
    void testInvalidDouble() throws Exception {
        Args args = new Args("x##", new String[]{"-x", "Forty two"});
        assertFalse(args.isValid());
        assertEquals(0, args.cardinality());
        assertFalse(args.has('x'));
        assertEquals(0, args.getInt('x'));
    }

    @Test
    void testMissingDouble() throws Exception {
        Args args = new Args("x##", new String[]{"-x"});
        assertFalse(args.isValid());
        assertEquals(0, args.cardinality());
        assertFalse(args.has('x'));
        assertEquals(0.0, args.getDouble('x'), 0.01);
        assertEquals("Could not find double parameter for -x.", args.errorMessage());
    }
}