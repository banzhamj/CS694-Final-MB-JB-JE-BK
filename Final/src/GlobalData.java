public class GlobalData {
	public static String password;
	
	public static synchronized String GetPassword() {
		return password;
	}
	public static synchronized void SetPassword(String pass) {
		password = pass;
	}
}