package io.jpom.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import io.jpom.JpomApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 命令行工具
 *
 * @author jiangzeyin
 * @date 2019/4/15
 */
public class CommandUtil {
    /**
     * 系统命令
     */
    private static final List<String> COMMAND = new ArrayList<>();
    /**
     * 文件后缀
     */
    public static final String SUFFIX;

    static {
        if (SystemUtil.getOsInfo().isLinux()) {
            //执行linux系统命令
            COMMAND.add("/bin/sh");
            COMMAND.add("-c");
        } else if (SystemUtil.getOsInfo().isMac()) {
            COMMAND.add("/bin/sh");
            COMMAND.add("-c");
        } else {
            COMMAND.add("cmd");
            COMMAND.add("/c");
        }

        if (SystemUtil.getOsInfo().isWindows()) {
            SUFFIX = "bat";
        } else {
            SUFFIX = "sh";
        }
    }

    public static List<String> getCommand() {
        return ObjectUtil.clone(COMMAND);
    }

    public static String execCommand(String command) {
        String newCommand = StrUtil.replace(command, StrUtil.CRLF, StrUtil.SPACE);
        newCommand = StrUtil.replace(newCommand, StrUtil.LF, StrUtil.SPACE);
        String result = "error";
        try {
            result = exec(new String[]{newCommand}, null);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error("执行命令异常", e);
            result += e.getMessage();
        }
        return result;
    }

    public static String execSystemCommand(String command) {
        return execSystemCommand(command, null);
    }

    /**
     * 在指定文件夹下执行命令
     *
     * @param command 命令
     * @param file    文件夹
     * @return msg
     */
    public static String execSystemCommand(String command, File file) {
        String newCommand = StrUtil.replace(command, StrUtil.CRLF, StrUtil.SPACE);
        newCommand = StrUtil.replace(newCommand, StrUtil.LF, StrUtil.SPACE);
        String result = "error";
        try {
            List<String> commands = getCommand();
            commands.add(newCommand);
            String[] cmd = commands.toArray(new String[]{});
            result = exec(cmd, file);
        } catch (Exception e) {
            DefaultSystemLog.ERROR().error("执行命令异常", e);
            result += e.getMessage();
        }
        return result;
    }

    /**
     * 执行命令
     *
     * @param cmd 命令行
     * @return 结果
     * @throws IOException          IO
     * @throws InterruptedException 等待超时
     */
    private static String exec(String[] cmd, File file) throws IOException, InterruptedException {
        DefaultSystemLog.LOG().info(Arrays.toString(cmd));
        String result;
        Process process;
        if (cmd.length == 1) {
            process = Runtime.getRuntime().exec(cmd[0]);
        } else {
            process = Runtime.getRuntime().exec(cmd, null, file);
        }
        InputStream is;
        int wait = process.waitFor();
        if (wait == 0) {
            is = process.getInputStream();
        } else {
            is = process.getErrorStream();
        }
        result = IoUtil.read(is, JpomApplication.getCharset());
        is.close();
        process.destroy();
        if (StrUtil.isEmpty(result) && wait != 0) {
            result = "没有返回任何执行信息:" + wait;
        }
        return result;
    }

    /**
     * 异步执行命令
     *
     * @param file    文件夹
     * @param command 命令
     * @throws IOException 异常
     */
    public static void asyncExeLocalCommand(File file, String command) throws Exception {
        if (SystemUtil.getOsInfo().isWindows()) {
            execSystemCommand(command, file);
            return;
        }
        List<String> commands = getCommand();
        commands.add(command);
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(file);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.start();
    }
}
