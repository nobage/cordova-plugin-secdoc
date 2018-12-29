package cordova.plugins.secdoc.http;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtil implements TrustManager, X509TrustManager, HostnameVerifier {
    private static String baseDomain;
    private static String basePort;
    private static String baseIp;
    private static String getTicket;
    private static String logIn;

    @SuppressLint("StaticFieldLeak")
    private static Context context = null;
    @SuppressLint("StaticFieldLeak")
    private static HttpUtil instance = null;


    public static HttpUtil getInstance(Context context, String baseDomain, String basePort) {
        if (instance == null) {
            synchronized (HttpUtil.class) {
                if (instance == null) {
                    instance = new HttpUtil(context, baseDomain, basePort);
                }
            }
        }
        return instance;
    }

    private HttpUtil(Context context, String baseDomain, String basePort) {
        HttpUtil.context = context;
        HttpUtil.baseDomain = baseDomain;
        HttpUtil.basePort = basePort;
        HttpUtil.baseIp = "http://" + baseDomain + ":" + basePort;
        HttpUtil.getTicket = HttpUtil.baseIp + "/securedoc/clientInterface/clientLogin.do?loginType=sdsc_sso";
        HttpUtil.logIn = HttpUtil.baseIp + "/securedoc/clientInterface/clientLogin.do";
    }

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public boolean isServerTrusted(
            java.security.cert.X509Certificate[] certs) {
        return true;
    }

    public boolean isClientTrusted(
            java.security.cert.X509Certificate[] certs) {
        return true;
    }

    public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
        return;
    }

    public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType)
            throws java.security.cert.CertificateException {
        return;
    }

    @Override
    public boolean verify(String urlHostName, SSLSession session) { //允许所有主机
        return true;
    }

    /**
     * 网络请求的回调接口
     */
    public interface CallBackData {
        //请求结束
        void CallBackFinish(Map<String, Object> stringObjectMap);

        //请求错误
        void CallBackError(String error);
    }

    /**
     * 模拟登录
     *
     * @param callBackData
     */
    public void login(Map<String, String> loginBody, final CallBackData callBackData) {
        if (!ConnectUtil.isConnenct(context)) {
            callBackData.CallBackError("网络连接失败，请重新配置");
            return;
        }

        Log.d("HttpUtil", "login: " + logIn + " loginBody :" + loginBody.toString());

        urlLoginPost(logIn, loginBody, XMLPullUtils.CLIENTLOGIN, new CallBackData() {
            @Override
            public void CallBackFinish(Map<String, Object> response) {
                String result = (String) response.get(HttpFields.NET_RESULT);
                if (result != null && result.equals(HttpFields.NET_REQUEST_SUCESS)) {
                    urlConnectionPost(getTicket, null, XMLPullUtils.CLIENTLOGIN, new CallBackData() {
                        @Override
                        public void CallBackFinish(Map<String, Object> stringObjectMap) {
                            callBackData.CallBackFinish(stringObjectMap);
                        }

                        @Override
                        public void CallBackError(String error) {
                            callBackData.CallBackError(error);
                        }
                    });
                } else {
                    callBackData.CallBackError("登录失败");
                }
            }

            @Override
            public void CallBackError(String error) {
                callBackData.CallBackError(error);
            }
        });
    }


    /**
     * 获取ticket
     *
     * @param urlPost      url地址
     * @param body         参数集合
     * @param callBackData 回调
     */
    private void urlLoginPost(final String urlPost, final Map<String, String> body, int flag, final CallBackData callBackData) {
        if (!ConnectUtil.isConnenct(context)) {
            callBackData.CallBackError("网络连接失败，请重新配置");
            return;
        }
        HttpURLConnection connection = null;
        try {
            //调用URL对象的openConnection方法获取HttpURLConnection的实例
            URL url = new URL(urlPost);
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = getInstance(context, baseDomain, basePort);
            trustAllCerts[0] = tm;
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                    .getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc
                    .getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier((HostnameVerifier) tm);

            connection = (HttpURLConnection) url.openConnection();
            //设置请求方式,GET或POST
            connection.setRequestMethod("POST");
            //如果body中含有参数,则加入输出流,否则不加
            if (body != null) {
                DataOutputStream data = new DataOutputStream(connection.getOutputStream());
                data.writeBytes(getBody(body));
            }
            //设置连接超时,读取超时时间,单位为毫秒(ms)
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            // 判断服务器给我们返回的状态信息。
            // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误

            int code = connection.getResponseCode();
            if (code == 200) {
                //利用链接成功的 connection 得到输入流
                //getInputStream方法获取服务器返回的输入流
                InputStream inputStream = connection.getInputStream();
                //使用BufferedReader对象读取返回的输入流
                //获取当前连接的session
                String session_id = connection.getHeaderField("Set-Cookie");
                //将session保存到SP中
                Log.i("Session", "toLoginServer: " + session_id);
                saveSessionSP(context, session_id);
                //按行读取,存储在StringBuilder对象Response中
                //InputStreamReader(inputStream,"gbk")中要对相应的xml格式进行解析,与xml头一致
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Map<String, Object> stringObjectMap = XMLPullUtils.getInstance().xml2Obj(response.toString(), flag);
                Log.i("HttpUtils", "run: " + stringObjectMap.toString());
                //解析完成,给调用此方法的地方一个成功回调
                callBackData.CallBackFinish(stringObjectMap);
            } else if (code == 408) {
                // 状态码 != 200 , 请求失败
                callBackData.CallBackError("请求超时");
            } else {
                callBackData.CallBackError("请求失败");
            }

        } catch (MalformedURLException e) {
            callBackData.CallBackError(e.toString());
        } catch (IOException e) {
            callBackData.CallBackError(e.toString());
        } catch (IllegalAccessException e) {
            callBackData.CallBackError(e.toString());
        } catch (XmlPullParserException e) {
            callBackData.CallBackError(e.toString());
        } catch (InstantiationException e) {
            callBackData.CallBackError(e.toString());
        } catch (NoSuchFieldException e) {
            callBackData.CallBackError(e.toString());
        } catch (NoSuchAlgorithmException e) {
            callBackData.CallBackError(e.toString());
        } catch (KeyManagementException e) {
            callBackData.CallBackError(e.toString());
        } finally {
            if (connection != null) {
                //结束后,关闭连接
                connection.disconnect();
            }
        }
    }

    /**
     * HttpURLConnection的post请求方法
     *
     * @param urlPost      url地址
     * @param body         参数集合
     * @param callBackData 回调
     */
    private void urlConnectionPost(final String urlPost, final Map<String, String> body, int flag, final CallBackData callBackData) {
        if (!ConnectUtil.isConnenct(context)) {
            callBackData.CallBackError("网络连接失败，请重新配置");
            return;
        }
        HttpURLConnection connection = null;
        try {
            //调用URL对象的openConnection方法获取HttpURLConnection的实例
            URL url = new URL(urlPost);
            TrustManager[] trustAllCerts = new TrustManager[1];
            TrustManager tm = getInstance(context, baseDomain, basePort);
            trustAllCerts[0] = tm;
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                    .getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc
                    .getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier((HostnameVerifier) tm);

            connection = (HttpURLConnection) url.openConnection();
            //设置请求方式,GET或POST
            connection.setRequestMethod("POST");
            //获取SP中的session,添加入connection
            connection.setRequestProperty("Cookie", getSessionSP(context));
            Log.i("HttpUtils", "urlConnectionPost: " + getSessionSP(context));
            //如果body中含有参数,则加入输出流,否则不加
            if (body != null) {
                DataOutputStream data = new DataOutputStream(connection.getOutputStream());
                data.writeBytes(getBody(body));
            }
            //设置连接超时,读取超时时间,单位为毫秒(ms)
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            // 判断服务器给我们返回的状态信息。
            // 200 成功 302 从定向 404资源没找到 5xx 服务器内部错误

            int code = connection.getResponseCode();
            if (code == 200) {
                //利用链接成功的 connection 得到输入流
                //getInputStream方法获取服务器返回的输入流
                InputStream inputStream = connection.getInputStream();
                //使用BufferedReader对象读取返回的输入流
                //按行读取,存储在StringBuilder对象Response中
                //InputStreamReader(inputStream,"gbk")中要对相应的xml格式进行解析,与xml头一致
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                Map<String, Object> stringObjectMap = XMLPullUtils.getInstance().xml2Obj(response.toString(), flag);
                Log.i("HttpUtils", "run: " + stringObjectMap.toString());
                //解析完成,给调用此方法的地方一个成功回调
                callBackData.CallBackFinish(stringObjectMap);
            } else if (code == 408) {
                // 状态码 != 200 , 请求失败
                callBackData.CallBackError("请求超时");
            } else {
                callBackData.CallBackError("请求失败");
            }

        } catch (MalformedURLException e) {
            callBackData.CallBackError(e.toString());
        } catch (IOException e) {
            callBackData.CallBackError(e.toString());
        } catch (IllegalAccessException e) {
            callBackData.CallBackError(e.toString());
        } catch (XmlPullParserException e) {
            callBackData.CallBackError(e.toString());
        } catch (InstantiationException e) {
            callBackData.CallBackError(e.toString());
        } catch (NoSuchFieldException e) {
            callBackData.CallBackError(e.toString());
        } catch (NoSuchAlgorithmException e) {
            callBackData.CallBackError(e.toString());
        } catch (KeyManagementException e) {
            callBackData.CallBackError(e.toString());
        } finally {
            if (connection != null) {
                //结束后,关闭连接
                connection.disconnect();
            }
        }
    }

    /**
     * 将Map集合解析成String
     *
     * @param map 传进来的参数
     * @return 参数的字符串
     */
    private static String getBody(Map<String, String> map) {
        StringBuilder initUrl = new StringBuilder();
        //遍历key解析map
        for (String key : map.keySet()) {
            initUrl.append("&" + key + "=" + map.get(key));
        }
        return initUrl.toString();
    }

    /**
     * 保存session
     *
     * @param context   传进来的上下文
     * @param sessionId 登录成功以后返回的session
     */

    private static void saveSessionSP(Context context, String sessionId) {
        //获取当前context的SP
        SharedPreferences sp = context.getSharedPreferences("login_user", Context.MODE_PRIVATE);
        //获取SP的Editor
        SharedPreferences.Editor edit = sp.edit();
        //key-value写入SP
        edit.putString(HttpFields.SDS_SERVER_SESSION, sessionId);
        //最后要提交
        edit.commit();
    }

    /**
     * 获取当前用户的session
     *
     * @param context 传进来的上下文
     * @return 当前用户session
     */
    private static String getSessionSP(Context context) {
        //获取当前context的SP
        SharedPreferences sp = context.getSharedPreferences("login_user", Context.MODE_PRIVATE);
        //根据key获取value
        return sp.getString(HttpFields.SDS_SERVER_SESSION, "");
    }

    /**
     * 获取SP保存的服务器IP
     *
     * @param context 传进来的上下文
     * @return 服务器地址
     */
    private static String getServerAddressSP(Context context) {
        //获取当前context的SP
        SharedPreferences sp = context.getSharedPreferences("login_user", Context.MODE_PRIVATE);
        //根据key获取value
        return sp.getString(HttpFields.SDS_SERVER_ADRESS, "");
    }
}
