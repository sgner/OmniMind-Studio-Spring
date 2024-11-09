package com.ai.chat.a.constant;

import com.ai.chat.a.enums.UserRobotTypeEnum;

import java.util.List;

public class Constants {
    public static final String ZERO_STR = "0";
    public static final String NEW_SESSION = "可以开始对话了";

    public static final Integer ZERO = 0;

    public static final Integer ONE = 1;

    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_11 = 11;
    public static final Integer LENGTH_20 = 20;

    public static final Integer LENGTH_30 = 30;

    public static final String DEFAULT_USER_MESSAGE = "你好啊";
    public static final String SESSION_KEY = "session_key";

    public static final String FILE_FOLDER_FILE = "/file/";

    public static final String FILE_FOLDER_TEMP = "/temp/";

    public static final String FILE_FOLDER_TEMP_2 = "temp";

    public static final String FILE_FOLDER_IMAGE = "images/";

    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";

    public static final String CHECK_CODE_KEY = "check_code_key";

    public static final String IMAGE_SUFFIX = ".png";

    public static final String COVER_IMAGE_SUFFIX = "_cover.png";

    public static final String[] IMAGE_SUFFIX_LIST = new String[]{".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};

    public static final String[] VIDEO_SUFFIX_LIST = new String[]{".mp4", ".avi", ".rmvb", ".mkv", ".mov"};

    public static final Long FILE_SIZE_MB = 1024 * 1024L;

    /**
     * redis key 相关
     */

    /**
     * 过期时间 1分钟
     */
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;


    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    /**
     * 过期时间 1天
     */
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;


    public static final Integer REDIS_KEY_TOKEN_EXPIRES = REDIS_KEY_EXPIRES_DAY * 2;


    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";
    public static final String REDIS_KEY_WS_TOKEN = "easychat:ws:token:";

    public static final String REDIS_KEY_WS_TOKEN_USERID = "easychat:ws:token:userid";

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat";

    public static final String REDIS_KEY_WS_ON_LINE_USER = "easychat:ws:online:";

    //用户联系人列表
    public static final String REDIS_KEY_USER_CONTACT = "easychat:ws:user:contact:";

    //用户参与的会话列表
    public static final String REDIS_KEY_USER_SESSION = "easychat:ws:user:session:";

    public static final Long MILLISECOND_3DAYS_AGO = 3 * 24 * 60 * 60 * 1000L;

    public static final String ROBOT_UID = UserRobotTypeEnum.ROBOT.getPrefix() + "robot";

    //系统设置
    public static final String REDIS_KEY_SYS_SETTING = "easychat:syssetting:";

    public static final String APP_UPDATE_FOLDER = "/app/";

    public static final String APP_NAME = "EasyChatSetup.";
    public static final String APP_EXE_SUFFIX = ".exe";

    //正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    //申请信息模板
    public static final String APPLY_INFO_TEMPLATE = "我是%s";

    //自己退群
    public static final String out_group_TEMPLATE_self = "%s退出了群聊";

    public static final String HANDLE_VIDEO_PROMPT = "下面是视频里提取出的关键帧，根据关键帧内容给出生成该视频内容的关键词，只要关键词，不能有其他描述，关键词以逗号分隔";

    public static final String SUBSCRIBE_SUCCESS = "感谢你的订阅";
    public static final String UPLOAD_FILE_TYPE = "文件格式不支持";

    public static final String FILE_TO_FILE = "你只需要文件进行描述即可而且你要从你是文件的创造者的角度进行描述,如果有多个文件那么你只需要对最后一个文件进行描述并且说说它和之前几个文件之间的联系,如果你无法描述上传的文件，你就给出提示类似于你暂时无法处理让我去使用更加专业的模型";

    public static final String USER_IDEA_PROMPT = """

            现在请推测出上面的这段文字是否是在请求生成图片或视频或音频的其中一个或者都不是.
            注意：生成图片或视频或音频只能选择其中一个，不能同时选择多个即不能出现generateImage:true,generateVideo:true,generateVoice:false这种情况，可以都不是即：generateImage:false,generateVideo:false,generateVoice:false。
            如果不是生成图片或视频或音频那么不需要推测出风格
            如果是生成图片或视频那么请推测出请求的风格，并且风格只能是这些\s
            Base：基础风格
            3D Model：3D模型
            Analog Film：模拟胶片
            Anime：动漫
            Cinematic：电影
            Comic Book：漫画
            Craft Clay：工艺黏土
            Digital Art：数字艺术
            Enhance：增强
            Fantasy Art：幻想艺术
            Isometric：等距风格
            Line Art：线条艺术
            Lowpoly：低多边形
            Neonpunk：霓虹朋克
            Origami：折纸
            Photographic：摄影
            Pixel Art：像素艺术
            Texture：纹理
            如果不是生成图片或视频或音频那么不需要给出提示词
            如果上面的文字的意思是生成类似的图片或视频（即用户上传了参考文件）那么就不需要给出提示词
            如果是生成图片或视频那么给出你的推荐提示词,提示词必须为英文。
            最重要的是，你最后必须给出下面这样的json格式文本不要有其他的回答,再次提示不要有其他回答一定要给出下面的这种json格式： \s
            {
                "generateImage":true或false,
                "generateVideo":true或false,
                "generateVoice":true或false,
                "style":"这里写你推测的风格",
                "prompt":"这里写你给出的提示词"
            }""";
public static final String FILE_PRE_TYPE = "该文件类型为: ";
public static final String FILE_PRE = "该文件内容为: ";
public  static final String FILE_PRE_PROMPT = "这是从当前上传的文件提取出的纯文本内容：";
public static final String SYSTEM_MESSAGE_PROMPT = "以上是对话的历史记录和相关资料，请参考上面的资料回答用户的问题";
public static final String FILE_SUF_PROMPT = "根据这个文件信息结合用户问题进行回答，如果前面还有对话的历史文件和资料那么就再结合资料并且以当前上传的这几个文件为主进行回答。";
    public static final String FILE_DESCRIPTION_PROMPT = """
            
            现在请获取出这个文件的信息
            如果是图片，那么就给出图片的描述(desc)图片的描述需要是英文，并且给出生成这个图片的相关提示词(prompt)，提示词需要是英文.
            如果文件内容无法获取，那么就给出false(fetch)，该文件的type和desc和prompt都填""。
            最后给出json格式字符串不要有其他的文字样例如下： \s
            {
                "type":"这里写该文件的类型，比如图片",
                "fetch":true或false,true表示可以获取文件内容，false表示无法获取文件内容,
                "desc":"这里写文件内容描述",
                "prompt":"这里写你给出的提示词,文本文件和其他文件没有提示词那么这里填"" "
            }
            """;
    public static final String NO_AUTHOR = "你的余额不足";
    public static final String UPLOAD_FILE_PROMPT_PRE = "文件信息：";
    public static final String CAN_NOT_UPLOAD = "非常抱歉我暂时无法处理文件，请换一个机器人";
    public static final String GET_FILE_CONTENT_PROMPT = "现在假定你作为这个文件的创作者，但是请不要直接说作为创作者等类似的话，请介绍这个文件，并判断是否符合这个预期:";
    public static final List<String> N_MODEL = List.of("gpt-3.5-turbo-16k");
    public static final int MAX_UPLOAD_FILES = 4;
//    public static final String SYSTEM_MESSAGE_PROMPT = "下面是从文件里提取出来的该文件的信息，根据该文件的信息结合后面用户的提问回答相应的问题：";
}
