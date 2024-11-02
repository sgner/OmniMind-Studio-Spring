package com.ai.chat.a.api.textCompliance;

import com.ai.chat.a.api.textCompliance.utils.MyUtil;
import com.alibaba.fastjson.JSONObject;


/**
 * 1、文本合规白名单接口
 */
public class CreateWhiteList {
    private static final String url = "https://audit.iflyaisol.com/audit_res/";
    private static final String APPID = Constants.APPID;
    private static final String APIKey = Constants.APIKey;
    private static final String APISecret = Constants.APISecret;

    public static void main(String[] args) throws Exception {
        /**
         1、创建白名单库，注意：不用每次都执行，除非需要创建另一个库！！！
         --- lib_id 可以根据已有库id直接写固定值。
         */
        String res = MyUtil.createWhite("白名单库1", url, APPID, APIKey, APISecret);
        String mid = JSONObject.parseObject(res).getString("data");
        String lib_id = JSONObject.parseObject(mid).getString("lib_id");

        // 2、添加放行词
        String[] word_list = new String[]{"科大讯飞股份有限公司", "讯飞开放平台"};
        MyUtil.addKeyWord(lib_id, word_list, url, APPID, APIKey, APISecret);

        // 3、查询词库信息
        MyUtil.selectLibrary(lib_id, url, APPID, APIKey, APISecret);

        // 4、查询词库里面的关键词信息
        MyUtil.selectLibraryDetail(lib_id, url, APPID, APIKey, APISecret);

        // 5、删除关键词
        /*  MyUtil.deleteKeyWord(lib_id, word_list, url, APPID, APIKey, APISecret);*/

        // 6、删除词库
        /*MyUtil.deleteLibrary(lib_id, url, APPID, APIKey, APISecret);*/

        // 7、查询appid下面所有的词库列表
        /* MyUtil.selectLibraryList(url, APPID, APIKey, APISecret);*/
    }
}
