package com.unfbx.chatgpt.v1_1_6;

import com.unfbx.chatgpt.FirstKeyStrategy;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.assistant.Tool;
import com.unfbx.chatgpt.entity.assistant.run.Run;
import com.unfbx.chatgpt.entity.assistant.run.RunResponse;
import com.unfbx.chatgpt.entity.chat.BaseChatCompletion;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import com.unfbx.chatgpt.sse.ConsoleEventSourceListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.internal.sse.RealEventSource;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 描述：
 *
 * @author https://www.unfbx.com
 * @since 2024-05-06
 */
@Slf4j
public class AssistantTest {

    private OpenAiClient client;
    private OpenAiStreamClient streamClient;

    @Before
    public void before() {
        //可以为null
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //！！！！千万别再生产或者测试环境打开BODY级别日志！！！！
        //！！！生产或者测试环境建议设置为这三种级别：NONE,BASIC,HEADERS,！！！
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
//                .proxy(proxy)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiResponseInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        client = OpenAiClient.builder()
                .apiKey(Arrays.asList("sk-****"))
                .keyStrategy(new FirstKeyStrategy())
                .okHttpClient(okHttpClient)
                .apiHost("https://dgr.life/")
                .build();

        streamClient = OpenAiStreamClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(Arrays.asList("sk-***"))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .apiHost("https://dgr.life/")
                .build();
    }


    /**
     * 创建run
     */
    @Test
    public void run() {
        HashMap<String, String> map = new HashMap<>();
        map.put("A", "a");
        Tool tool = Tool.builder().type(Tool.Type.CODE_INTERPRETER.getName()).build();
        Run run = Run
                .builder()
                .assistantId("asst_jxUD2Byvy4mdI3fR354y5Rwd")
                .model(BaseChatCompletion.Model.GPT_3_5_TURBO_1106.getName())
                .instructions("你是一个数学导师。当我问你问题时，编写并运行Java代码来回答问题。")
                .tools(Collections.singletonList(tool))
                .stream(true)
                .metadata(map).build();
        ConsoleEventSourceListener eventSourceListener = new ConsoleEventSourceListener();
        client.runWithStream("thread_ZOGnTQpCkxGJgqplvWTOIJs7", run, eventSourceListener);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
