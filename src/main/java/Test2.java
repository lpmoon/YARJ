/**
 * Created by zblacker on 2017/5/10.
 */
public class Test2 {
    public void print(String name) {
        System.out.println(name);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } finally {
            System.out.println(111);
        }
    }

    public static void main(String[] args) {
        Test2 t = new Test2();
        Test t1 = new Test();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            t.print(String.valueOf(i));
            t1.print();
        }
    }
}
