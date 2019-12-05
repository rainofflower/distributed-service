import com.yanghui.distributed.rpc.common.util.StringUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author YangHui
 */
public class CommonTest {

    @Test
    public void test1(){
        nextLoop:
        for(int i=0; i<2;i++){
            System.out.println("外层循环："+i);
            for(int j=0; j<2;j++){
                System.out.println("内层循环,i="+i+" ,j="+j);
                if(j == 1){
                    continue nextLoop;
                }
            }
            System.out.println("内层循环退出后继续执行了外层循环");
        }
    }

    @Test
    public void test2(){
        for(int i=0; i<2;i++){
            System.out.println("外层循环："+i);
            for(int j=0; j<2;j++){
                System.out.println("内层循环,i="+i+" ,j="+j);
                if(j == 1){
                    break;
                }
            }
            System.out.println("内层循环退出后继续执行了外层循环");
        }
    }


}
