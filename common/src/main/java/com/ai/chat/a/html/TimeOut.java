package com.ai.chat.a.html;

public class TimeOut {
    public static String TIME_OUT = """
            <!DOCTYPE html>
            <html lang="zh">
            <head>
                <meta charset="UTF-8">
                <title>Title</title>
            </head>
            <body>
            <div>
                <p>上传返回超时了，但文件仍然可能上传成功请点击下面按钮重新获取文件结果</p>
                <button id="retry-button">获取结果</button>
            </div>
            </body>
            </html>
            """;
}
