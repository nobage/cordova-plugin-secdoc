package cordova.plugins.secdoc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.eetrust.securedocsdk.MSD;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cordova.plugins.secdoc.http.HttpFields;
import cordova.plugins.secdoc.http.HttpUtil;

public class SecDecode extends CordovaPlugin {
    private Context mContext;
    private static MSD msd;

    private String configDomain = "219.146.4.153";
    private String configPort = "8088";
    private String isHttps = "0";
    private String authorities = "cordova.plugin.secdoc.fileprovider";
    private String loginName = "";

    private static final String CONFIG_PARAM_ERROR = "param incorrect";

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        mContext = this.cordova.getActivity().getApplicationContext();
        msd = MSD.getInstance(mContext);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            return login(args, callbackContext);
        }
        if (action.equals("isCipher")) {
            return isCipher(args, callbackContext);
        }
        if (action.equals("openFile")) {
            return openFile(args, callbackContext);
        }
        return super.execute(action, args, callbackContext);
    }

    /*
     * 登录，身份认证
     */
    private boolean login(CordovaArgs args, CallbackContext callbackContext) {
        final JSONObject data, configInfo, loginInfo;
        try {
            data = args.getJSONObject(0);
            if (data.has("configInfo")) {
                configInfo = data.getJSONObject("configInfo");
                if (configInfo.has("serverIP")) {
                    this.configDomain = configInfo.getString("serverIP");
                }
                if (configInfo.has("port")) {
                    this.configPort = configInfo.getString("port");
                }
                if (configInfo.has("isHttps")) {
                    this.isHttps = configInfo.getString("isHttps");
                }
                if (configInfo.has("authorities")) {
                    this.authorities = configInfo.getString("authorities");
                }
            }
            String password, loginType, version, loginFrom;
            if (data.has("loginInfo")) {
                loginInfo = data.getJSONObject("loginInfo");
                this.loginName = loginInfo.has("userName") ? loginInfo.getString("userName") : "";
                password = loginInfo.has("passWord") ? loginInfo.getString("passWord") : "";
                loginType = loginInfo.has("loginType") ? loginInfo.getString("loginType") : "pwd";
                version = loginInfo.has("version") ? loginInfo.getString("version") : "3.0";
                loginFrom = loginInfo.has("loginFrom") ? loginInfo.getString("loginFrom") : "1";
            } else {
                callbackContext.error("没有获取到用户信息");
                return true;
            }

            Map<String, String> body = new HashMap<String, String>();
            body.put("loginType", loginType);
            body.put("userName", this.loginName);
            body.put("passWord", password);
            body.put("version", version);
            body.put("loginFrom", loginFrom);

            if (msd.sds_config(loginName, configDomain, configPort, authorities, "1".equals(isHttps))) {
                getTicket(body, callbackContext);
            }else{
                callbackContext.error("插件sds_config配置失败");
            }
        } catch (JSONException e) {
            callbackContext.error(CONFIG_PARAM_ERROR);
            Log.d("SecDecode", "login fail" + e.toString());
            return true;
        }
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    private void getTicket(final Map<String, String> loginBody, final CallbackContext callbackContext) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                final String[] post = {null};
                HttpUtil.getInstance(mContext, configDomain, configPort).login(loginBody, new HttpUtil.CallBackData() {
                    @Override
                    public void CallBackFinish(Map<String, Object> stringObjectMap) {
                        String result = (String) stringObjectMap.get(HttpFields.NET_RESULT);
                        if (result != null && result.equals(HttpFields.NET_REQUEST_SUCESS)) {
                            String ticket = (String) stringObjectMap.get("ticket");
                            post[0] = ticket;
                        } else if (result != null && result.equals(HttpFields.NET_REQUEST_FAIL)) {
                            post[0] = (String) stringObjectMap.get("error");
                            callbackContext.error(post[0]);
                            Log.d("SecDecode", "getTicket error :" + post[0]);
                        }else {
                            callbackContext.error("getTicket error");
                            Log.d("SecDecode", "getTicket error");
                        }
                    }

                    @Override
                    public void CallBackError(String error) {
                        post[0] = error;
                        callbackContext.error(error);
                        Log.d("SecDecode", "getTicket fail :" + error);
                    }
                });
                return post[0];
            }

            @Override
            protected void onPostExecute(String post) {
                if (post != null && post.length() == 10) {
                    configInform(post, callbackContext);
                }
            }
        }.execute();
    }

    /**
     * 身份认证
     */
    private void configInform(String postTicket, final CallbackContext callbackContext) {
        msd.sds_authenticate(postTicket, loginName, "3.3.1.1", new MSD.AuthenticateCallBack() {
            @Override
            public void CallBackFinish() {
                callbackContext.success(1);
            }

            @Override
            public void CallBackError(int errorCode, String errorInfo) {
                callbackContext.error(errorInfo);
            }
        });
    }


    /*
     * 判断是否为密文
     */
    private boolean isCipher(CordovaArgs args, final CallbackContext callbackContext) {
        String filePath = "";
        try {
            filePath = args.getString(0);
            final String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            Log.d("SecDecode", "filePath = " + filePath);

            msd.sds_isCiphertext(filePath, new MSD.IsCiphertextCallBack() {
                @Override
                public void CallBackFinish(boolean b) {
                    callbackContext.success(1);
                }

                @Override
                public void CallBackError(int i, String s) {
                    callbackContext.error(s);
                }
            });
        } catch (JSONException e) {
            callbackContext.error("密文检查出现错误");
            return true;
        }
        return true;
    }


    /*
     * 文件打开, 支持密文、明文
     */
    private boolean openFile(CordovaArgs args, final CallbackContext callbackContext) {
        String filePath = "";
        try {
            filePath = args.getString(0);
            final String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
            Log.d("SecDecode", "filePath = " + filePath);
            if (checkOpenFileType(fileName)) {
                msd.sds_openFile(filePath, new MSD.OpenFileCallBack() {
                    @Override
                    public void CallBackFinish() {
                        Log.d("SecDecode", "打开成功");
                        callbackContext.success(1);
                    }

                    @Override
                    public void CallBackError(int errorCode, String errorInfo) {
                        callbackContext.error(errorInfo);
                    }
                });
            } else {
                Log.d("SecDecode", "checkOpenFileType 不支持此类型文件打开");
                callbackContext.error("不支持此类型文件打开");
            }
        } catch (JSONException e) {
            callbackContext.error("文件打开出现问题");
            return true;
        }
        return true;

    }

    private boolean checkOpenFileType(String ext) {
        // wps,wpt,et,dps,doc,docx,dot,dotx,rtf,txt,PDF,xlsx,xls,xltx,xlt,ppt,pptx,potx,pot
        if (ext.endsWith(".wps") || ext.endsWith(".wpt") || ext.endsWith(".et") || ext.endsWith(".ett") || ext.endsWith(".dps") || ext.endsWith(".doc") ||
                ext.endsWith(".docx") || ext.endsWith(".dot") || ext.endsWith(".dotx") || ext.endsWith(".txt") || ext.endsWith(".pdf") || ext.endsWith(".xlsx") ||
                ext.endsWith(".xls") || ext.endsWith(".dps") || ext.endsWith(".xltx") ||
                ext.endsWith(".xlt") || ext.endsWith(".ppt") || ext.endsWith(".pptx") || ext.endsWith(".pot") || ext.endsWith(".potx") || ext.endsWith(".pptm")) {
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (msd != null) {
            msd = null;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
