/**
 * Created by zblacker on 2017/5/10.
 */
public class Test2 {
    public void print() {
        System.out.println("11");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Test2 t = new Test2();
        Test t1 = new Test();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            t.print();
            t1.print();
        }
    }
}
