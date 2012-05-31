public class GlobalData {
	public static String password;
        public static String cookie;
	
	public static synchronized String GetPassword() {
		return password;
	}
	public static synchronized void SetPassword(String pass) {
		password = pass;
	}

	public static synchronized String GetCookie() {
		return cookie;
	}
	public static synchronized void SetCookie(String pass) {
		cookie = pass;
	}
}