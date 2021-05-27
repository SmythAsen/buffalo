import com.alibaba.fastjson.JSONObject;
import com.asen.buffalo.http.OkHttpClients;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestCase {


    @Test
    public void test() throws IOException {
        Response response = OkHttpClients.create()
                .url("http://api-sentry.huya.com/")
                .get()
                .response();
        System.out.println(response);
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
