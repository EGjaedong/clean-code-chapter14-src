package com.company.utilities.getopts;

import java.text.ParseException;
import java.util.*;

public class Args {
    private String schema;
    private String[] args;
    private boolean valid;
    private Set<Character> unexpectedArguments = new TreeSet<>();
    private Map<Character, ArgumentMarshaler> marshalers = new HashMap<>();
    private Set<Character> argsFount = new HashSet<>();
    private int currentArgument;
    private char errorArgument = '\0';
    private char errorArgumentId = '\0';
    private String errorParameter = "";

    public boolean isValid() {
        return valid;
    }

    enum ErrorCode {
        OK, MISSING_STRING, INVALID_INTEGER, MISSING_INTEGER
    }

    private ErrorCode errorCode = ErrorCode.OK;

    public Args(String schema, String[] args) throws ParseException, ArgsException {
        this.schema = schema;
        this.args = args;
        valid = parse();
    }

    private boolean parse() throws ParseException, ArgsException {
        if (schema.length() == 0 && args.length == 0)
            return true;
        parseSchema();
        parseArguments();
        return unexpectedArguments.size() == 0;
    }

    private boolean parseSchema() throws ParseException {
        for (String element : schema.split(",")) {
            if (element.length() > 0) {
                String trimmedElement = element.trim();
                parseSchemaElement(trimmedElement);
            }
        }
        return true;
    }

    private void parseSchemaElement(String element) throws ParseException {
        char elementId = element.charAt(0);
        String elementTail = element.substring(1);
        validateSchemaElementId(elementId);
        if (isBooleanSchemaElement(elementTail))
            marshalers.put(elementId, new BooleanArgumentMarshaler());
        else if (isStringSchemaElement(elementTail))
            marshalers.put(elementId, new StringArgumentMarshaler());
        else if (isIntSchemaElement(elementTail))
            marshalers.put(elementId, new IntegerArgumentMarshaler());
    }

    private void validateSchemaElementId(char elementId) throws ParseException {
        if (!Character.isLetter(elementId))
            throw new ParseException("Bad character:" + elementId + "in Args format: " + schema, 0);
    }

    private boolean isBooleanSchemaElement(String elementTail) {
        return elementTail.length() == 0;
    }

    private boolean isStringSchemaElement(String elementTail) {
        return elementTail.equals("*");
    }

    private boolean isIntSchemaElement(String elementTail) {
        return elementTail.equals("#");
    }

    private boolean parseArguments() throws ArgsException {
        for (currentArgument = 0; currentArgument < args.length; currentArgument++) {
            String arg = args[currentArgument];
            parseArgument(arg);
        }
        return true;
    }

    private void parseArgument(String arg) throws ArgsException {
        if (arg.startsWith("-"))
            parseElements(arg);
    }

    private void parseElements(String arg) throws ArgsException {
        for (int i = 1; i < arg.length(); i++)
            parseElement(arg.charAt(i));
    }

    private void parseElement(char argChar) throws ArgsException {
        if (setArgument(argChar))
            argsFount.add(argChar);
        else {
            unexpectedArguments.add(argChar);
            valid = false;
        }
    }

    private boolean setArgument(char argChar) throws ArgsException {
        ArgumentMarshaler m = marshalers.get(argChar);
        try {
            if (m instanceof BooleanArgumentMarshaler)
                setBooleanArg(m);
            else if (m instanceof StringArgumentMarshaler)
                setStringArg(m);
            else if (m instanceof IntegerArgumentMarshaler)
                setIntArg(m);
            else
                return false;
        } catch (ArgsException e) {
            valid = false;
            errorArgumentId = argChar;
            throw e;
        }

        return true;
    }

    private void setStringArg(ArgumentMarshaler m) throws ArgsException {
        currentArgument++;
        try {
            m.set(args[currentArgument]);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArgsException();
        }
    }

    private boolean isString(ArgumentMarshaler m) {
        return m instanceof StringArgumentMarshaler;
    }

    private void setBooleanArg(ArgumentMarshaler m) {
        try {
            m.set("true");
        } catch (ArgsException e) {
        }
    }

    private boolean isBoolean(ArgumentMarshaler m) {
        return m instanceof BooleanArgumentMarshaler;
    }

    private void setIntArg(ArgumentMarshaler m) throws ArgsException {
        currentArgument++;
        String parameter = null;
        try {
            parameter = args[currentArgument];
            m.set(parameter);
        } catch (ArrayIndexOutOfBoundsException e) {
            errorCode = ErrorCode.MISSING_INTEGER;
            throw new ArgsException();
        } catch (ArgsException e) {
            errorParameter = parameter;
            errorCode = ErrorCode.INVALID_INTEGER;
            throw e;
        }
    }

    private boolean isInt(ArgumentMarshaler m) {
        return m instanceof IntegerArgumentMarshaler;
    }

    public int cardinality() {
        return argsFount.size();
    }

    public String usage() {
        if (schema.length() > 0)
            return "-[" + schema + "]";
        else
            return "";
    }

    public String errorMessage() throws Exception {
        if (unexpectedArguments.size() > 0) {
            return unexpectedArgumentMessage();
        } else {
            switch (errorCode) {
                case MISSING_STRING:
                    return String.format("Could not find string parameter for -%c.", errorArgument);
                case OK:
                    throw new Exception("TILT: Should not get here.");
            }
        }
        return "";
    }

    private String unexpectedArgumentMessage() {
        StringBuffer message = new StringBuffer("Argument(s)  -");
        for (char c : unexpectedArguments) {
            message.append(c);
        }
        message.append(" unexpected");

        return message.toString();
    }

    public boolean getBoolean(char arg) {
        ArgumentMarshaler am = marshalers.get(arg);
        boolean b = false;
        try {
            b = am != null && (Boolean) am.get();
        } catch (ClassCastException e) {
            b = false;
        }
        return b;
    }

    public String getString(char arg) {
        ArgumentMarshaler am = marshalers.get(arg);
        try {
            return am == null ? "" : (String) am.get();
        } catch (ClassCastException e) {
            return "";
        }
    }

    public int getInt(char arg) {
        ArgumentMarshaler am = marshalers.get(arg);
        try {
            return am == null ? 0 : (Integer) am.get();
        } catch (Exception e) {
            return 0;
        }
    }

    private String blankIfNull(String s) {
        return s == null ? "" : s;
    }

    public boolean has(char arg) {
        return argsFount.contains(arg);
    }

    public boolean ifValid() {
        return valid;
    }

    private abstract class ArgumentMarshaler {
        private int integerValue;

        public abstract void set(String s) throws ArgsException;

        public abstract Object get();
    }

    private class BooleanArgumentMarshaler extends ArgumentMarshaler {
        private boolean booleanValue = false;

        public void set(String s) {
            booleanValue = true;
        }

        public Object get() {
            return booleanValue;
        }
    }

    private class StringArgumentMarshaler extends ArgumentMarshaler {
        private String stringValue = "";

        public void set(String s) {
            stringValue = s;
        }

        public Object get() {
            return stringValue;
        }
    }

    private class IntegerArgumentMarshaler extends ArgumentMarshaler {
        private int integerValue = 0;

        public void set(String s) throws ArgsException {
            try {
                integerValue = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new ArgsException();
            }
        }

        public Object get() {
            return integerValue;
        }
    }
}
