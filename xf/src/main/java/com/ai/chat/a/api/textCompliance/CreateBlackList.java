package com.ai.chat.a.api.textCompliance;

import com.ai.chat.a.api.textCompliance.utils.MyUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 1、文本合规黑名单接口
 */
public class CreateBlackList {
    private static final String url = "https://audit.iflyaisol.com/audit_res/";
    private static final String APPID = Constants.APPID;
    private static final String APIKey = Constants.APIKey;
    private static final String APISecret = Constants.APISecret;

    public static void main(String[] args) throws Exception {
        /**
         1、创建黑名单库，注意：不用每次都执行，除非需要创建另一个库！！！
         ---库的可选类型：pornDetection 色情；violentTerrorism暴恐；political 涉政；lowQualityIrrigation低质量灌水；
         contraband违禁；advertisement广告；uncivilizedLanguage不文明用语
         --- lib_id 可以根据已有库id直接写固定值。
         */
        String res = MyUtil.createBlack("黑名单库1", "contraband", url, APPID, APIKey, APISecret);
        String mid = JSONObject.parseObject(res).getString("data");
        String lib_id = JSONObject.parseObject(mid).getString("lib_id");

        // 2、添加拦截词
        String[] word_list = new String[]{"傻缺", "蠢材"};
        MyUtil.addKeyWord(lib_id, word_list, url, APPID, APIKey, APISecret);

        // 3、查询词库信息
        MyUtil.selectLibrary(lib_id, url, APPID, APIKey, APISecret); // 记录创建时生成的库id

        // 4、查询词库里面的关键词信息
        MyUtil.selectLibraryDetail(lib_id, url, APPID, APIKey, APISecret);

        // 5、删除关键词
        /*MyUtil.deleteKeyWord(lib_id, word_list, url, APPID, APIKey, APISecret);*/

        // 6、删除词库
        /*  MyUtil.deleteLibrary(lib_id, url, APPID, APIKey, APISecret);*/

        // 7、查询appid下面所有的词库列表
        /* MyUtil.selectLibraryList(url, APPID, APIKey, APISecret);*/
    }
}
