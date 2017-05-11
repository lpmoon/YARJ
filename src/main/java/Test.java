/**
 * Created by zblacker on 2017/5/10.
 */
public class Test {
    public void print() {
        System.out.println("11");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
