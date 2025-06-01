package gleam.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Util {

	private MD5Util() {
	}

	public static String toMD5String(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (md != null && str != null) {
				byte[] byteData = md.digest(str.getBytes(StandardCharsets.UTF_8));
				StringBuilder sb = new StringBuilder();
				byte[] var4 = byteData;
				int var5 = byteData.length;

				for (int var6 = 0; var6 < var5; ++var6) {
					byte aByteData = var4[var6];
					sb.append(Integer.toString((aByteData & 255) + 256, 16).substring(1));
				}

				return sb.toString();
			} else {
				return "NULL";
			}
		} catch (NoSuchAlgorithmException var8) {
			return null;
		}
	}

}
