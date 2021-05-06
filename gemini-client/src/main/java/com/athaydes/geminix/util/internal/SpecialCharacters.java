package com.athaydes.geminix.util.internal;

public sealed interface SpecialCharacters permits SpecialCharacters.Nothing {
    final class Nothing implements SpecialCharacters {}

    byte[] CRLF = new byte[]{0x0D, 0x0A};

    int ASCII_0 = 48;
    int ASCII_1 = 49;
    int ASCII_2 = 50;
    int ASCII_3 = 51;
    int ASCII_4 = 52;
    int ASCII_5 = 53;
    int ASCII_6 = 54;
    int ASCII_7 = 55;
    int ASCII_8 = 56;
    int ASCII_9 = 57;

    String URL_ENCODED_AMPERSAND = "%26";
}
