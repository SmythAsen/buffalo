import com.alibaba.fastjson.JSONObject;
import com.asen.buffalo.http.OkHttpClients;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestCase {


    @Test
    public void test() throws IOException {

    }

    @Test
    public void test2() throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("pageNum", 1);
        body.put("pageSize", 10);
        JSONObject result = OkHttpClients.create()
                .url("http://10.69.180.45:8080/open/at/article/listAll")
                .requestBody(body)
                .post()
                .toJSONObject();
        System.out.println(result);
    }
}
