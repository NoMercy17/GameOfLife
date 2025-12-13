package com.gui;

import javax.swing.*;
import java.io.PrintStream;


public class GuiPrintStream extends PrintStream {
    private final PrintStream original;
    private final JTextArea logArea;

    public GuiPrintStream(PrintStream original, JTextArea logArea) {
        super(original);
        this.original = original;
        this.logArea = logArea;
    }

    @Override
    public void print(String s) {
        original.print(s);
        appendToLog(s);
    }

    @Override
    public void println(String s) {
        original.println(s);
        appendToLog(s + "\n");
    }

    @Override
    public void print(Object obj) {
        original.print(obj);
        appendToLog(String.valueOf(obj));
    }

    @Override
    public void println(Object obj) {
        original.println(obj);
        appendToLog(String.valueOf(obj) + "\n");
    }

    private void appendToLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}