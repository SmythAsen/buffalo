import com.asen.buffalo.http.HttpClient;
import org.junit.Test;

/**
 * @description:
 * @author: Asen
 * @since: 2020-05-19 15:57:46
 */
public class TestCase {


    @Test
    public void test() {
        String result = HttpClient.create()
                .url("https://www.baidu.com")
                .get()
                .result();
        System.out.println(result);
    }
}
