package dk.nindroid.rss.parser;

public class Crypto {
	public static String toHex(byte[] data){
		StringBuilder sb = new StringBuilder();
		for (int i=0; i < data.length; i++) {
		    sb.append(Integer.toString( ( data[i] & 0xff ) + 0x100, 16).substring( 1 ));
		}
		return sb.toString();
	}
}
