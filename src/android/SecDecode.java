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


public class SecDecode extends CordovaPlugin{
    private Context mContext;
    private static MSD msd;
    /* 测试文件存放目录 */
//    private String rootPath = getSDPath() + File.separator + "dxoa" + File.separator;
//    /* 明文加密后的密文存放位置 */
//    private String cipherTextFolder = rootPath + "cipherFolder" + File.separator;
//    /* 密文解密后的明文存放位置 */
//    private String plaintextFolder = rootPath + "plainFolder" + File.separator;
//    /* 文件打开时临时文件存放目录 */
//    private String plainTemp = rootPath + "plainTemp" + File.separator;

    private String configDomain = "219.146.4.153";
    private String configPort = "8088";
    private String isHttps = "0";
    private String authorities = "cordova.plugins.secdoc.custom.fileprovider";
    private String loginName = "";

    private static final String CONFIG_PARAM_ERROR = "param incorrect";

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        mContext = this.cordova.getActivity().getApplicationContext();
        msd = MSD.getInstance(mContext);
//        initDirs();
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("login")) {
            return login(args, callbackContext);
        }
        if (action.equals("isCipher")) {
            return isCipher(args, callbackContext);
        }
        if (action.equals("encrypt")) {
            return encrypt(args, callbackContext);
        }
        if (action.equals("decode")) {
            return decode(args, callbackContext);
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
            configInfo = data.has("configInfo") ? data.getJSONObject("configInfo") : new JSONObject();
            if(configInfo.has("serverIP")){
                this.configDomain = configInfo.getString("serverIP");
            }
            if(configInfo.has("port")){
                this.configPort = configInfo.getString("port");
            }
            if(configInfo.has("isHttps")){
                this.isHttps = configInfo.getString("isHttps");
            }
            if(configInfo.has("authorities")){
                this.authorities = configInfo.getString("authorities");
            }

            loginInfo = data.has("loginInfo") ? data.getJSONObject("loginInfo") : new JSONObject();
            this.loginName = loginInfo.has("userName") ? loginInfo.getString("userName") : "";
            String password = loginInfo.has("passWord") ? loginInfo.getString("passWord") : "";
            String loginType = loginInfo.has("loginType") ? loginInfo.getString("loginType") : "pwd";
            String version = loginInfo.has("version") ? loginInfo.getString("version") : "3.0";
            String loginFrom = loginInfo.has("loginFrom") ? loginInfo.getString("loginFrom") : "1";

            //Log.d("SecDocPlugin",loginName+password+loginType+version+loginFrom);

            Map<String, String> body = new HashMap<String, String>();
            body.put("loginType", loginType);
            body.put("userName", this.loginName);
            body.put("passWord", password);
            body.put("version", version);
            body.put("loginFrom", loginFrom);

            if (this.configHaveNet()) {
                //Log.d("SecDocPlugin","configHaveNet success");
                getTicket(body, callbackContext);
            }
        } catch (JSONException e) {
            Log.d("SecDocPlugin","login fail"+e.toString());
            callbackContext.error(CONFIG_PARAM_ERROR);
            return true;
        }
        return true;
    }

    private boolean configHaveNet() {
        Log.d("SecDecode", loginName + configDomain + configPort + isHttps);
        return msd.sds_config(loginName, configDomain, configPort, authorities, "1".equals(isHttps));
    }

    @SuppressLint("StaticFieldLeak")
    private void getTicket(final Map<String, String> loginBody, final CallbackContext callbackContext) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                final String[] post = {null};
                HttpUtil.getInstance(mContext, configDomain,configPort).login(loginBody, new HttpUtil.CallBackData() {
                    @Override
                    public void CallBackFinish(Map<String, Object> stringObjectMap) {
                        String result = (String) stringObjectMap.get(HttpFields.NET_RESULT);
                        if (result != null && result.equals(HttpFields.NET_REQUEST_SUCESS)) {
                            String ticket = (String) stringObjectMap.get("ticket");
                            post[0] = ticket;
                            Log.d("SecDocPlugin","getTicket success :"+ticket);
                        } else if (result != null && result.equals(HttpFields.NET_REQUEST_FAIL)) {
                            post[0] = (String) stringObjectMap.get("error");
                            callbackContext.error( post[0]);
                        }
                    }

                    @Override
                    public void CallBackError(String error) {
                        post[0] = error;
                        callbackContext.error(error);
                        Log.d("SecDocPlugin","getTicket fail :"+error);
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
        msd.sds_authenticate(postTicket, loginName,"3.3.1.1", new MSD.AuthenticateCallBack() {
            @Override
            public void CallBackFinish() {
                showToast("身份认证成功");
                callbackContext.success(1);
            }

            @Override
            public void CallBackError(int errorCode, String errorInfo) {
                showToast("身份认证失败"+ errorInfo);
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
            msd.sds_isCiphertext(filePath, new MSD.IsCiphertextCallBack() {
                @Override
                public void CallBackFinish(boolean b) {
                    callbackContext.success(1);
                    showToast(b ? "是密文" : "不是密文");
                }

                @Override
                public void CallBackError(int i, String s) {
                    callbackContext.error(s);
                    showToast(fileName + "失败：" + s);
                }
            });
        } catch (JSONException e) {
            callbackContext.error(CONFIG_PARAM_ERROR);
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
            if (checkOpenFileType(fileName)) {
                msd.sds_openFile(filePath, new MSD.OpenFileCallBack() {
                    @Override
                    public void CallBackFinish() {
                        callbackContext.success(1);
                    }

                    @Override
                    public void CallBackError(int errorCode, String errorInfo) {
                        callbackContext.error(errorInfo);
                    }
                });
            } else {
                showToast("不支持此类型文件打开");
                callbackContext.error("不支持此类型文件打开");
            }
        } catch (JSONException e) {
            callbackContext.error(CONFIG_PARAM_ERROR);
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

    /*
     * 明文加密
     */
    private boolean encrypt(CordovaArgs args, final CallbackContext callbackContext) {
//        String filePath = "";
//        try {
//            filePath = args.getString(0);
//            final String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
//            final String cipherPath = cipherTextFolder + fileName;
//            List<UserBean> userBeans = new ArrayList<>();
//            UserBean u = new UserBean(this.loginName, "1,2,3,4,5,6,7,8,9,10");
//            userBeans.add(u);
//            msd.sds_encrypt(filePath, cipherPath, 1, "1",
//                    userBeans, null, new MSD.EncryptCallBack() {
//                        @Override
//                        public void CallBackFinish() {
//                            callbackContext.success(1);
//                            //showToast(fileName + "加密成功，加密至：\n" + cipherPath);
//                        }
//
//                        @Override
//                        public void CallBackError(int i, String s) {
//                            callbackContext.success(0);
//                            //showToast(fileName + "加密失败， 失败原因：" + s);
//                        }
//                    });
//        } catch (JSONException e) {
//            callbackContext.error(CONFIG_PARAM_ERROR);
//            return true;
//        }
        callbackContext.success(1);
        return true;
    }


    /*
     * 密文解密
     */
    private boolean decode(CordovaArgs args, final CallbackContext callbackContext) {
//        String filePath = "";
//        try {
//            filePath = args.getString(0);
//            final String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
//            final String plainPath = plaintextFolder + fileName;
//            msd.sds_openFile(filePath, new MSD.OpenFileCallBack() {
//                @Override
//                public void CallBackFinish() {
//                    callbackContext.success(1);
//                    showToast(fileName + "解密成功，解密至：\n" + plainPath);
//                }
//
//                @Override
//                public void CallBackError(int i, String s) {
//                    callbackContext.success(0);
//                    showToast(fileName + "解密失败， 失败原因：" + s);
//                }
//            });
//        } catch (JSONException e) {
//            callbackContext.error(CONFIG_PARAM_ERROR);
//            return true;
//        }
        callbackContext.success(1);
        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

//    private void initDirs() {
//        File cipherTextFolder = new File(this.cipherTextFolder);
//        if (!cipherTextFolder.exists() || !cipherTextFolder.isDirectory()) {
//            cipherTextFolder.mkdirs();
//        }
//        File plaintextFolder = new File(this.plaintextFolder);
//        if (!plaintextFolder.exists() || !plaintextFolder.isDirectory()) {
//            plaintextFolder.mkdirs();
//        }
//        File plainTemp = new File(this.plainTemp);
//        if (!plainTemp.exists() || !plainTemp.isDirectory()) {
//            plainTemp.mkdirs();
//        }
//    }

//    private String getSDPath() {
//        File sdDir = null;
//        boolean sdCardExist = Environment.getExternalStorageState()
//                .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
//        if (sdCardExist) {
//            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
//        }
//        return sdDir.toString();
//
//    }
}
