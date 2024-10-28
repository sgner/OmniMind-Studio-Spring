package com.ai.chat.a.utils;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FFMPEGUtil {

    // 提取视频的关键帧并返回 MultipartFile 列表
    public static List<MultipartFile> extractKeyFrames(MultipartFile videoFile) throws IOException, InterruptedException {
        // 生成一个唯一的目录用于存储临时文件
        String tempDirPath = System.getProperty("java.io.tmpdir") + "/" + IdUtil.simpleUUID();
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // 将 MultipartFile 保存为临时文件，放到该临时目录中
        File tempVideoFile = new File(tempDir, videoFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempVideoFile)) {
            fos.write(videoFile.getBytes());
        }

        // 生成 UUID 用于关键帧文件名
        String uuid = IdUtil.simpleUUID();

        // FFmpeg 命令，用于提取关键帧并输出为 JPEG 流
        String command = String.format("ffmpeg -i %s -vf \"select=eq(pict_type\\,I)\" -vsync vfr -f image2pipe -vcodec mjpeg pipe:1",
                tempVideoFile.getAbsolutePath());

        Process process = Runtime.getRuntime().exec(command);
        InputStream inputStream = process.getInputStream();

        // 创建 MultipartFile 列表存储关键帧
        List<MultipartFile> keyFrameFiles = new ArrayList<>();
        int frameIndex = 0;

        // 读取关键帧流并转换为 MultipartFile
        while (inputStream.available() > 0) {
            byte[] frameBytes = inputStream.readAllBytes();
            String fileName = String.format("keyframe_%s_%04d.jpg", uuid, frameIndex++);
            MultipartFile frameMultipartFile = new MockMultipartFile(fileName, fileName, "image/jpeg", frameBytes);
            keyFrameFiles.add(frameMultipartFile);
        }

        // 删除临时文件和目录
        tempVideoFile.delete();
        tempDir.delete();

        return keyFrameFiles; // 返回关键帧的 MultipartFile 列表
    }


    // 从音频中提取可识别的文本并返回
    public static String extractAudioToText(MultipartFile audioFile) throws IOException {
        // 将 MultipartFile 保存为临时文件
        File tempAudioFile = convertMultipartFileToFile(audioFile);

        // 提取音频文件到 wav 格式（方便后续处理）
        String wavFilePath = tempAudioFile.getAbsolutePath().replaceAll("\\.[a-zA-Z0-9]+$", ".wav");
        String command = String.format("ffmpeg -i %s -ar 16000 -ac 1 -f wav pipe:1", tempAudioFile.getAbsolutePath());
        runCommand(command);

        // 模拟调用语音识别服务
        String transcript = convertSpeechToText(wavFilePath);

        // 删除临时音频文件
        tempAudioFile.delete();

        return transcript; // 返回提取的文本
    }

    // 将 MultipartFile 转换为临时文件
    private static File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/"+IdUtil.simpleUUID() + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    // 执行FFmpeg命令
    private static void runCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
        }
    }

    // 模拟的自动语音识别方法
    private static String convertSpeechToText(String wavFilePath) {
        // 假设我们调用了某个 ASR 模型，返回处理后的文本
        // 这里可以使用 Google Speech-to-Text 或其他服务
        return "这是从音频文件中提取的示例文本。";
    }

    // 提取视频的第一帧并返回为 MultipartFile
    public static MultipartFile extractFirstFrameAsMultipartFile(MultipartFile videoFile) throws IOException {
        // 将 MultipartFile 保存为临时文件
        File tempVideoFile = convertMultipartFileToFile(videoFile);
        String name = IdUtil.simpleUUID() + ".jpg";

        // FFmpeg 命令提取第一帧
        String command = String.format("ffmpeg -i %s -vf \"select=eq(n\\,0)\" -q:v 3 -f image2pipe -vcodec mjpeg pipe:1",
                tempVideoFile.getAbsolutePath());

        // 执行命令并获取输出流（第一帧作为 JPEG 图片流返回）
        Process process = Runtime.getRuntime().exec(command);
        InputStream inputStream = process.getInputStream();
        byte[] imageBytes = inputStream.readAllBytes();

        // 将生成的第一帧图片转换为 MultipartFile
        MultipartFile imageMultipartFile = new MockMultipartFile(name, name, "image/jpeg", imageBytes);

        // 删除临时视频文件
        tempVideoFile.delete();

        return imageMultipartFile;
    }
}
