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
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MorseUtils {
    static final int DASH = 0;
    static final int DOT = 1;
    static final int LETTERSPACE = 2;
    static final int WORDSPACE = 3;

    private static Method getCharacterLength = null;
    private static Method decodeCharacter = null;
    private static Method decodeString = null;
    private static Method encodeString = null;

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
            decodeCharacter = lab4Class.getDeclaredMethod("decodeCharacter", intArrayClass, int.class, int.class);
        } catch (NoSuchMethodException e) {
            // Just leave it as null.
        }

        try {
            decodeString = lab4Class.getDeclaredMethod("decodeString", intArrayClass);
        } catch (NoSuchMethodException e) {
            // Just leave it as null.
        }
        try {
            encodeString = lab4Class.getDeclaredMethod("encodeString", String.class);

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
        ret.setPreferredSize(new Dimension(600, 250));

        // 3. Combine them: Bevel on the outside, Padding on the inside
        ret.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                BorderFactory.createEmptyBorder(_textAreaMargin, _textAreaMargin, _textAreaMargin, _textAreaMargin)));
        ret.setLineWrap(true);
        ret.setWrapStyleWord(true);

        return ret;

    }

    private static class MorseDocumentFilter extends DocumentFilter {
        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            super.replace(fb, offset, length, text.replaceAll("[^.∙/ -]", "").replaceAll("\\.","∙"), attrs);
        }
    }

    private static class LatinDocumentFilter extends DocumentFilter {
        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            super.replace(fb, offset, length, text.toUpperCase().replaceAll("[^A-Z0-9 ]", ""), attrs);
        }
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
                String sanitized = _morseText.getText().strip().replaceAll("\\s+", " ");
                _latinText.setText((String) decodeString.invoke(null, decodeASCII(sanitized)));
            } catch (Exception ex) {
                // If all tests passed, this shouldn't happen...
            }
        }

    }

    private static class LatinTextAreaListener
            implements DocumentListener {

        public void changedUpdate(DocumentEvent e) {
        }

        public void removeUpdate(DocumentEvent e) {
            // delegate.
            insertUpdate(e);
        }

        public void insertUpdate(DocumentEvent e) {
            try {
                String sanitized = _latinText.getText().stripLeading().replaceAll("\\s+", " ");
                String morse = encodeASCII((int[]) encodeString.invoke(null, sanitized));
                _morseText.setText(morse);
            } catch (Exception ex) {
                // If all tests passed, this shouldn't happen...
            }
        }

    }

    private static float _fontSize = 18;

    private static void createAndShowGUI(boolean extraCreditDone) {
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
        _latinText.setEditable(extraCreditDone);
        _latinText.getDocument().addDocumentListener(new LatinTextAreaListener());
        _latinText.setFont(_latinText.getFont().deriveFont(_fontSize));
        ((AbstractDocument) _latinText.getDocument()).setDocumentFilter(new LatinDocumentFilter());
        _morseText = createTextArea();
        _morseText.setMargin(new Insets(5, 5, 5, 5));
        _morseText.setEditable(true);
        _morseText.getDocument().addDocumentListener(new MorseTextAreaListener());
        _morseText.setFont(_morseText.getFont().deriveFont(1.5f * _fontSize));
        ((AbstractDocument) _morseText.getDocument()).setDocumentFilter(new MorseDocumentFilter());

        String startText = ".... . .-.. .-.. --- / .-- --- .-. .-.. -.."; // hello world
        _morseText.setText(startText);
        _morseText.setCaretPosition(startText.length());
        // JSCrollPane isn't working like this, not sure what's wrong.  SCroll bars appear,
        // but don't do anything even when the text exceeds the displayable area.
//        pane.add(new JScrollPane(_morseText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
//                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), c);
        pane.add(_morseText, c);
        c.gridy = 1;
        pane.add(_latinText, c);

 //       pane.add(new JScrollPane(_latinText, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
   //             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), c);
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
                    case '∙':
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
                case DOT:
                    s.append('∙');
                    break;
                case DASH:
                    s.append('-');
                    break;
                case LETTERSPACE:
                    s.append(' ');
                    break;
                case WORDSPACE:
                    s.append(" / ");
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

    private static boolean testSingleEncodeString(DecodeStringTestPair p) {
        int[] actual;
        try {
            actual = (int[]) encodeString.invoke(null, p.message);
        } catch (IllegalAccessException ie) {
            // Shouldn't happen since we preflight stuff.
            System.out.println("method is not public, can't test.\n");
            return false;
        } catch (InvocationTargetException ie) {
            System.out.println("encodeString() crashed:\n");
            ie.getCause().printStackTrace();
            return false;
        }
        int[] expected = decodeASCII(p.code);
        if (Arrays.equals(expected, actual)) {
            return true;
        }
        System.out.printf("\nencodeString(\"%s\") failure:\n\tExpected: {%s}\n\t  Actual: {%s}\n",
                p.message, stringifyInputs(expected), stringifyInputs(actual));
        return false;
    }

    private static boolean testEncodeString() {
        // Since this is extra credit, we just don't say anything if it's not
        // implemented.
        if (encodeString == null) {
            return false;
        }
        System.out.print("Found extra credit encodeString()...");
        for (DecodeStringTestPair p : codeStringTestData) {
            if (!testSingleEncodeString(p)) {
                return false;
            }
        }
        System.out.println("all tests pass!");
        return true;
    }

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
            System.out.printf("\ndecodeString({%s}) should return \"%s\" but actually returned \"%s\"\n",
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

    private static boolean testSingleDecodeCharacter(DecodeCharacterTestPair p) {

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
            actual = (Character) decodeCharacter.invoke(null, testArray, offset, code.length);
        } catch (IllegalAccessException ie) {
            // Shouldn't happen since we preflight stuff.
            System.out.println("method is not public, can't test.\n");
            return false;
        } catch (InvocationTargetException ie) {
            System.out.println("decodeCharacter() crashed:\n");
            ie.getCause().printStackTrace();
            return false;
        }
        if (actual != p.letter) {
            System.out.printf("\ndecodeCharacter({%s}, %d, %d) should return '%c' but actually returned '%c'\n",
                    stringifyInputs(testArray), offset, code.length, p.letter, actual);
            return false;
        }
        return true;
    }

    private static Random rng = new Random(314);

    private static class DecodeCharacterTestPair {
        DecodeCharacterTestPair(String codeInit, char letterInit) {
            code = codeInit;
            letter = letterInit;
        }

        String code;
        char letter;
    }

    static DecodeCharacterTestPair[] codeOneTestData = {
            new DecodeCharacterTestPair(".", 'E'),
            new DecodeCharacterTestPair("-", 'T'),
            new DecodeCharacterTestPair("..", 'I'),
            new DecodeCharacterTestPair(".-", 'A'),
            new DecodeCharacterTestPair("-.", 'N'),
            new DecodeCharacterTestPair("--", 'M'),
            new DecodeCharacterTestPair("...", 'S'),
            new DecodeCharacterTestPair("..-", 'U'),
            new DecodeCharacterTestPair(".-.", 'R'),
            new DecodeCharacterTestPair(".--", 'W'),
            new DecodeCharacterTestPair("-..", 'D'),
            new DecodeCharacterTestPair("-.-", 'K'),
            new DecodeCharacterTestPair("--.", 'G'),
            new DecodeCharacterTestPair("---", 'O'),
            new DecodeCharacterTestPair("....", 'H'),
            new DecodeCharacterTestPair("...-", 'V'),
            new DecodeCharacterTestPair("..-.", 'F'),
            new DecodeCharacterTestPair(".-..", 'L'),
            new DecodeCharacterTestPair(".--.", 'P'),
            new DecodeCharacterTestPair(".---", 'J'),
            new DecodeCharacterTestPair("-...", 'B'),
            new DecodeCharacterTestPair("-..-", 'X'),
            new DecodeCharacterTestPair("-.-.", 'C'),
            new DecodeCharacterTestPair("-.--", 'Y'),
            new DecodeCharacterTestPair("--..", 'Z'),
            new DecodeCharacterTestPair("--.-", 'Q'),
            new DecodeCharacterTestPair("-----", '0'),
            new DecodeCharacterTestPair(".----", '1'),
            new DecodeCharacterTestPair("..---", '2'),
            new DecodeCharacterTestPair("...--", '3'),
            new DecodeCharacterTestPair("....-", '4'),
            new DecodeCharacterTestPair(".....", '5'),
            new DecodeCharacterTestPair("-....", '6'),
            new DecodeCharacterTestPair("--...", '7'),
            new DecodeCharacterTestPair("---..", '8'),
            new DecodeCharacterTestPair("----.", '9'),
            new DecodeCharacterTestPair(".-.-.", '?')
    };

    public static boolean testDecodeCharacter() {
        System.out.print("Testing decodeCharacter()...");
        if (decodeCharacter == null) {
            System.out.println("not yet implemented");
            return false;
        }
        for (DecodeCharacterTestPair t : codeOneTestData) {
            if (!testSingleDecodeCharacter(t)) {
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
        boolean allBaseTestsPassed = true;
        allBaseTestsPassed = testGetCharacterLength() && allBaseTestsPassed;
        allBaseTestsPassed = testDecodeCharacter() && allBaseTestsPassed;
        allBaseTestsPassed = testDecodeString() && allBaseTestsPassed;
        boolean extraCreditPassed = testEncodeString();
        if (allBaseTestsPassed) {
            System.out.println("Starting toy");
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(extraCreditPassed);
                }
            });

        } else {
            System.out.println("Not all tests are passing.");
        }
    }

}
