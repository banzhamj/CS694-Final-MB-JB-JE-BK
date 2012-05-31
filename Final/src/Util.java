import javax.swing.JTextArea;

class Util {
    JTextArea log;

    public Util(JTextArea log) {
        this.log = log;
    }

    public void Print(DbgSub subsys, Object object) {
        if ( log != null ) {
            log.append(subsys.toString() + ": " + object);
            log.append("\n");
        } else {
            System.out.println(subsys + ": " + object);
        }
    }
}