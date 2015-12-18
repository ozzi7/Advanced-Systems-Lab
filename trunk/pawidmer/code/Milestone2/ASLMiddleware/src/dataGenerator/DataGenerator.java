package dataGenerator;

import java.util.Random;

public class DataGenerator {

	private Random random = new Random();
	
	public String getRandomString(int length) {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	public int getRandomInt(int fromInclusive, int toInclusive) {
		return fromInclusive + random.nextInt((toInclusive-fromInclusive)+1);
	}
}
