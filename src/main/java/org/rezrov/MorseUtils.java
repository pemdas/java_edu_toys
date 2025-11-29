package org.rezrov;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

public class MorseUtils {
    static final int DASH = 0;
    static final int DOT = 1;
    static final int LETTERSPACE = 2;
    static final int WORDSPACE = 3;

    private static Method getCharacterLength = null;
    private static Method decodeOne = null;
    private static Method decodeString = null;

    static {
        Class<?> lab4Class = null;
        Class<?> intArrayClass = (new int[1]).getClass(); // There must be a better way to do this.
        try {
            lab4Class = MorseUtils.class.getClassLoader().loadClass("Lab4");

        } catch (ClassNotFoundException e) {
            System.err.print("No class named Lab4 found.  Did you name your class something else?\n");
            System.exit(-1);
        }
        try {
            getCharacterLength = lab4Class.getDeclaredMethod("getCharacterLength", intArrayClass, int.class);
        } catch (NoSuchMethodException e) {
            // Just leave it as null.
        }

        try {
            decodeOne = lab4Class.getDeclaredMethod("decodeOne", intArrayClass, int.class, int.class);
        } catch (NoSuchMethodException e) {
            // Just leave it as null.
        }

        try {
            decodeString = lab4Class.getDeclaredMethod("decodeString", intArrayClass);
        } catch (NoSuchMethodException e) {
            // Just leave it as null.
        }

    }

    static int[] translate(String s) {
        s = s.replaceAll(" / ", "/");
        ArrayList<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '.':
                    l.add(DOT);
                    break;
                case '-':
                    l.add(DASH);
                    break;
                case '/':
                    l.add(WORDSPACE);
                    break;
                case ' ':
                    l.add(LETTERSPACE);
                    break;
                default:
                    throw new InvalidParameterException("" + s.charAt(i));
            }
        }
        int[] ret = new int[l.size()];
        for (int i = 0; i < l.size(); i++) {
            ret[i] = l.get(i);
        }
        return ret;
    }

    static class LetterTestElement {
        public LetterTestElement(int[] inputsInit, char resultInit) {
            inputs = inputsInit;
            result = resultInit;
        }

        int[] inputs;
        char result;
    }

    static String stringifyInputs(int[] inputs) {
        return stringifyInputs(inputs, 0, inputs.length);
    }

    static String stringifyInputs(int[] inputs, int offset, int size) {
        String[] tokens = new String[size];
        for (int i = offset; i < offset + size; i++) {
            tokens[i - offset] = "" + inputs[i];
        }
        return String.join(",", tokens);
    }

    static private class DecodeStringTestPair {
        DecodeStringTestPair(String codeInit, String messageInit) {
            code = codeInit;
            message = messageInit;
        }

        String code;
        String message;
    }

    private static DecodeStringTestPair[] codeStringTestData = {
            new DecodeStringTestPair(".... . .-.. .-.. --- / .-- --- .-. .-.. -..", "HELLO WORLD"),
            new DecodeStringTestPair(
                    "- .... .. ... / .. ... / .- / - . ... - / ... . -. - . -. -.-. .",
                    "THIS IS A TEST SENTENCE"),
            new DecodeStringTestPair(
                    "- .... . / ..-. .. .-. ... - / -.. .. --. .. - ... / --- ..-. / .--. .. / .- .-. . / ...-- .---- ....- .---- ..... ----. ..--- -.... ..... ...-- ..... ---.. ----. --...",
                    "THE FIRST DIGITS OF PI ARE 31415926535897"),
            new DecodeStringTestPair(
                    "- .... . / --.- ..- .. -.-. -.- / -... .-. --- .-- -. / ..-. --- -..- / .--- ..- -- .--. . -.. / --- ...- . .-. / - .-- --- / .-.. .- --.. -.-- / -.. --- --. ...",
                    "THE QUICK BROWN FOX JUMPED OVER TWO LAZY DOGS"),
            new DecodeStringTestPair(
                    ".---- ----- / ----. / ---.. / --... / -.... / ..... / ....- / ...-- / ..--- / .---- / .-.. .. ..-. - --- ..-. ..-.",
                    "10 9 8 7 6 5 4 3 2 1 LIFTOFF"),
    };

    private static boolean testSingleDecodeString(DecodeStringTestPair p) {
        String actual;
        int[] code = translate(p.code);
        try {
            actual = (String) decodeString.invoke(null, code);
        } catch (IllegalAccessException ie) {
            // Shouldn't happen since we preflight stuff.
            System.out.println("method is not public, can't test.\n");
            return false;
        } catch (InvocationTargetException ie) {
            System.out.println("decodeString() crashed:\n");
            ie.getCause().printStackTrace();
            return false;
        }
        if (!p.message.equals(actual)) {
            System.out.printf("\ndecodeString({%s}) should return '%s' but actually returned '%s'\n",
                    stringifyInputs(code), p.message, actual);
            return false;
        }
        return true;
    }

    public static void testDecodeString() {
        System.out.print("Testing decodeString()...");
        if (decodeString == null) {
            System.out.println("not yet implemented");
            return;
        }

        for (DecodeStringTestPair p : codeStringTestData) {
            if (!testSingleDecodeString(p)) {
                return;
            }
        }
        System.out.println("all tests pass!");
    }

    private static boolean testSingleDecodeOne(DecodeOneTestPair p) {

        int[] code = translate(p.code);
        int size = 1 + code.length + rng.nextInt(3);
        int offset = rng.nextInt(size - code.length);

        // Put the code to be tested somewhere in an array that may be slightly larger
        // than code, so
        // that the offset is not always zero in the tests.
        int[] testArray = new int[size];
        for (int i = 0; i < size; ++i) {
            testArray[i] = rng.nextInt(1);
        }
        for (int i = 0; i < code.length; i++) {
            testArray[offset + i] = code[i];
        }
        char actual = 0;
        try {
            actual = (Character) decodeOne.invoke(null, testArray, offset, code.length);
        } catch (IllegalAccessException ie) {
            // Shouldn't happen since we preflight stuff.
            System.out.println("method is not public, can't test.\n");
            return false;
        } catch (InvocationTargetException ie) {
            System.out.println("decodeOne() crashed:\n");
            ie.getCause().printStackTrace();
            return false;
        }
        if (actual != p.letter) {
            System.out.printf("\ndecodeOne({%s}, %d, %d) should return '%c' but actually returned '%c'\n",
                    stringifyInputs(testArray), offset, code.length, p.letter, actual);
            return false;
        }
        return true;
    }

    private static Random rng = new Random(314);

    private static class DecodeOneTestPair {
        DecodeOneTestPair(String codeInit, char letterInit) {
            code = codeInit;
            letter = letterInit;
        }

        String code;
        char letter;
    }

    static DecodeOneTestPair[] codeOneTestData = {
            new DecodeOneTestPair(".", 'E'),
            new DecodeOneTestPair("-", 'T'),
            new DecodeOneTestPair("..", 'I'),
            new DecodeOneTestPair(".-", 'A'),
            new DecodeOneTestPair("-.", 'N'),
            new DecodeOneTestPair("--", 'M'),
            new DecodeOneTestPair("...", 'S'),
            new DecodeOneTestPair("..-", 'U'),
            new DecodeOneTestPair(".-.", 'R'),
            new DecodeOneTestPair(".--", 'W'),
            new DecodeOneTestPair("-..", 'D'),
            new DecodeOneTestPair("-.-", 'K'),
            new DecodeOneTestPair("--.", 'G'),
            new DecodeOneTestPair("---", 'O'),
            new DecodeOneTestPair("....", 'H'),
            new DecodeOneTestPair("...-", 'V'),
            new DecodeOneTestPair("..-.", 'F'),
            new DecodeOneTestPair(".-..", 'L'),
            new DecodeOneTestPair(".--.", 'P'),
            new DecodeOneTestPair(".---", 'J'),
            new DecodeOneTestPair("-...", 'B'),
            new DecodeOneTestPair("-..-", 'X'),
            new DecodeOneTestPair("-.-.", 'C'),
            new DecodeOneTestPair("-.--", 'Y'),
            new DecodeOneTestPair("--..", 'Z'),
            new DecodeOneTestPair("--.-", 'Q'),
            new DecodeOneTestPair("-----", '0'),
            new DecodeOneTestPair(".----", '1'),
            new DecodeOneTestPair("..---", '2'),
            new DecodeOneTestPair("...--", '3'),
            new DecodeOneTestPair("....-", '4'),
            new DecodeOneTestPair(".....", '5'),
            new DecodeOneTestPair("-....", '6'),
            new DecodeOneTestPair("--...", '7'),
            new DecodeOneTestPair("---..", '8'),
            new DecodeOneTestPair("----.", '9'),
            new DecodeOneTestPair(".-.-.", '?')
    };

    public static void testDecodeOne() {
        System.out.print("Testing decodeOne()...");
        if (decodeOne == null) {
            System.out.println("not yet implemented");
            return;
        }
        for (DecodeOneTestPair t : codeOneTestData) {
            if (!testSingleDecodeOne(t)) {
                return;
            }
        }
        System.out.println("all tests pass!");
    }

    static boolean testRandomGetCharacterLength() {
        int charSize = 1 + rng.nextInt(5);
        int arrSize = charSize + rng.nextInt(3);
        int offset;
        if (arrSize == charSize) {
            offset = 0;
        } else {
            offset = rng.nextInt(arrSize - charSize);
        }
        int[] arr = new int[arrSize];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rng.nextInt(4);
        }
        for (int i = offset; i < offset + charSize; i++) {
            arr[i] = rng.nextInt(2);
        }
        if (offset + charSize <= (arr.length - 1)) {
            arr[offset + charSize] = (rng.nextBoolean() ? LETTERSPACE : WORDSPACE);
        }
        int actual;
        try {
            actual = (Integer) getCharacterLength.invoke(null, arr, offset);
        } catch (IllegalAccessException ie) {
            // Shouldn't happen since we preflight stuff.
            System.out.println("method is not public, can't test.\n");
            return false;
        } catch (InvocationTargetException ie) {
            System.out.println("getCharacterLength() crashed:\n");
            ie.getCause().printStackTrace();
            return false;
        }
        if (actual != charSize) {
            System.out.printf("getCharacterLength({%s}, %d) should return %d but actually returned %d\n",
                    stringifyInputs(arr), offset, charSize, actual);
            return false;
        }
        return true;
    }

    public static void testGetCharacterLength() {
        if (getCharacterLength == null) {
            System.out.println("getCharacterLength() not yet implemented");
            return;
        }
        System.out.print("Testing getCharacterLength()...");

        for (int i = 0; i < 100; i++) {
            if (!testRandomGetCharacterLength()) {
                return;
            }
        }

        System.out.println("all tests passed!");
    }

    public static void testLab4() {
        testGetCharacterLength();
        testDecodeOne();
        testDecodeString();
    }

}
