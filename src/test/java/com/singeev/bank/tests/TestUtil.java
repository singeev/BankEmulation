package com.singeev.bank.tests;
import java.util.Random;

public class TestUtil {
	public static String createStringWithLength(int length) {
		Random rand = new Random(47);
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rand.nextInt(characters.length()));
		}
		return new String(text);
	}
}
