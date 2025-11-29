package org.rezrov;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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

    static JFrame _window;
    static JTextArea _morseText;
    static JTextArea _latinText;

    static int _textAreaMargin = 10;

    private static JTextArea createTextArea() {
        JTextArea ret = new JTextArea();
        ret.setPreferredSize(new Dimension(500, 150));

        // 3. Combine them: Bevel on the outside, Padding on the inside
        ret.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(_textAreaMargin, _textAreaMargin, _textAreaMargin, _textAreaMargin)));
        return ret;

    }

    private static class MorseTextAreaListener
            implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            // delegate.
            insertUpdate(e);
        }

        public void insertUpdate(DocumentEvent e) {
            try {
                _latinText.setText((String) decodeString.invoke(null, decodeASCII(_morseText.getText())));
            } catch (Exception ex) {
                // If all tests passed, this shouldn't happen...
            }
        }

    }

    private static float _fontSize = 18;

    private static void createAndShowGUI() {
        // Create and set up the window.
        _window = new JFrame("Morse Translator Toy");
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = _window.getContentPane();

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.BOTH;
        JLabel l = new JLabel("Morse Code", SwingConstants.RIGHT);
        l.setFont(l.getFont().deriveFont(_fontSize));

        // l.setHorizontalTextPosition(SwingConstants.LEFT);
        pane.add(l, c);
        c.weightx = 1;
        c.gridx = 1;
        _latinText = createTextArea();
        _latinText.setMargin(new Insets(5, 5, 5, 5));
        _latinText.setEditable(false);
        _latinText.setFont(_latinText.getFont().deriveFont(_fontSize));

        _morseText = createTextArea();
        _morseText.setMargin(new Insets(5, 5, 5, 5));
        _morseText.setEditable(true);
        _morseText.getDocument().addDocumentListener(new MorseTextAreaListener());
        _morseText.setFont(_morseText.getFont().deriveFont(_fontSize));

        String startText = ".... . .-.. .-.. --- / .-- --- .-. .-.. -.."; // hello world
        _morseText.setText(startText);
        _morseText.setCaretPosition(startText.length());
        pane.add(_morseText, c);
        c.gridy = 1;

        pane.add(_latinText, c);
        c.gridx = 0;
        c.weightx = 0;
        l = new JLabel("Text", SwingConstants.RIGHT);
        l.setFont(l.getFont().deriveFont(_fontSize));
        pane.add(l, c);
        _window.pack();
        _window.setMinimumSize(_window.getSize());

        _window.setVisible(true);
    }

    static int[] decodeASCII(String s) {
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("\\s*/\\s*", "/");
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

    static String encodeASCII(int[] code) {
        StringBuilder s = new StringBuilder();
        for (int c : code) {
            switch (c) {

            }
        }
        return s.toString();
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
        int[] code = decodeASCII(p.code);
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

    public static boolean testDecodeString() {
        System.out.print("Testing decodeString()...");
        if (decodeString == null) {
            System.out.println("not yet implemented");
            return false;
        }

        for (DecodeStringTestPair p : codeStringTestData) {
            if (!testSingleDecodeString(p)) {
                return false;
            }
        }
        System.out.println("all tests pass!");
        return true;
    }

    private static boolean testSingleDecodeOne(DecodeOneTestPair p) {

        int[] code = decodeASCII(p.code);
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

    public static boolean testDecodeOne() {
        System.out.print("Testing decodeOne()...");
        if (decodeOne == null) {
            System.out.println("not yet implemented");
            return false;
        }
        for (DecodeOneTestPair t : codeOneTestData) {
            if (!testSingleDecodeOne(t)) {
                return false;
            }
        }
        System.out.println("all tests pass!");
        return true;
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

    public static boolean testGetCharacterLength() {
        if (getCharacterLength == null) {
            System.out.println("getCharacterLength() not yet implemented");
            return false;
        }
        System.out.print("Testing getCharacterLength()...");

        for (int i = 0; i < 100; i++) {
            if (!testRandomGetCharacterLength()) {
                return false;
            }
        }

        System.out.println("all tests passed!");
        return true;
    }

    public static void testLab4() {
        boolean allTestsPassed = true;
        allTestsPassed = testGetCharacterLength() && allTestsPassed;
        allTestsPassed = testDecodeOne() && allTestsPassed;
        allTestsPassed = testDecodeString() && allTestsPassed;
        if (allTestsPassed) {
            System.out.println("Starting toy");
            createAndShowGUI();
        } else {
            System.out.println("Not all tests are passing.");
        }
    }

}
