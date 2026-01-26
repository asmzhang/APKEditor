/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apkeditor;

import java.io.File;
import java.io.IOException;

import com.reandroid.arsc.ARSCLib;
import com.reandroid.jcommand.CommandHelpBuilder;
import com.reandroid.jcommand.OptionStringBuilder;
import com.reandroid.jcommand.SubCommandHelpBuilder;
import com.reandroid.jcommand.SubCommandParser;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

/**
 * 命令选项基类
 * 定义了所有命令选项的通用属性和方法
 */
public class Options {

    /** 签名选项 */
    @OptionArg(name = "-s", flag = true, description = "enable signature (default)")
    public boolean signature = true;

    @OptionArg(name = "-sf", description = "signature file path")
    public String signatureFile;

    /** 输入文件路径 */
    @OptionArg(name = "-i", description = "input_path")
    public File inputFile;
    
    /** 输出文件路径 */
    @OptionArg(name = "-o", description = "output_path")
    public File outputFile;
    
    /** 强制删除标志 */
    @OptionArg(name = "-f", flag = true, description = "force_delete")
    public boolean force;
    
    /** 命令类型 */
    public String type;
    
    /** 帮助标志 */
    @OptionArg(name = "-h", alternates = {"-help", "--help"}, description = "help_description", flag = true)
    public boolean help = false;

    /** 是否已验证标志 */
    private boolean mValidated;

    /**
     * 构造函数
     */
    public Options() {
    }

    /**
     * 获取帮助信息
     * @return 格式化的帮助字符串
     */
    public String getHelp() {
        SubCommandHelpBuilder builder = new SubCommandHelpBuilder(ResourceStrings.INSTANCE, this.getClass());
        builder.setMaxWidth(Options.PRINT_WIDTH);
        builder.setColumnSeparator("   ");
        return builder.build();
    }
    
    /**
     * 解析命令行参数
     * @param args 命令行参数数组
     */
    public void parse(String[] args) {
        SubCommandParser.parse(this, args);
        validate();
    }
    
    /**
     * 运行命令
     * 创建命令执行器并执行命令
     * @throws IOException IO异常
     */
    public void runCommand() throws IOException {
        CommandExecutor<?> executor = newCommandExecutor();
        executor.logMessage(this.toString());
        executor.runCommand();
    }
    
    /**
     * 创建命令执行器（抽象方法，由子类实现）
     * @return 命令执行器实例
     */
    public CommandExecutor<?> newCommandExecutor() {
        throw new RuntimeException("Method not implemented");
    }
    
    /**
     * 验证选项
     */
    public void validate() {
        if (!help && !mValidated) {
            mValidated = true;
            validateValues();
        }
    }
    
    /**
     * 验证选项值
     */
    public void validateValues() {
        validateInput(true, false);
        validateOutput(true);
    }
    
    /**
     * 验证输入文件
     * @param isFile 是否必须是文件
     * @param isDirectory 是否必须是目录
     */
    public void validateInput(boolean isFile, boolean isDirectory) {
        File file = this.inputFile;
        if (file == null) {
            throw new CommandException("missing_input_file");
        }
        validateInputFile(file, isFile, isDirectory);
    }
    
    /**
     * 验证输入文件或目录
     * @param file 要验证的文件或目录
     * @param isFile 是否必须是文件
     * @param isDirectory 是否必须是目录
     */
    public void validateInputFile(File file, boolean isFile, boolean isDirectory) {
        if (isFile) {
            if(file.isFile()) {
                return;
            }
            if(!isDirectory) {
                throw new CommandException("no_such_file", file);
            }
        }
        if (isDirectory) {
            if(file.isDirectory()) {
                return;
            }
            throw new CommandException("no_such_directory", file);
        }
        if (!file.exists()) {
            throw new CommandException("no_such_file_or_directory", file);
        }
    }

    /**
     * 验证输出文件
     * @param isFile 是否必须是文件
     */
    public void validateOutput(boolean isFile) {
        File file = this.outputFile;
        if (file == null) {
            file = generateOutputFromInput(this.inputFile);
            this.outputFile = file;
        }
        if (file == null || !file.exists()) {
            return;
        }
        if (isFile != file.isFile()) {
            if (file.isFile()) {
                throw new CommandException("path_is_file_expect_directory", file);
            }
            throw new CommandException("path_is_directory_expect_file", file);
        }
        if(!force) {
            throw new CommandException("path_already_exists", file);
        }
    }
    
    /**
     * 根据输入文件生成输出文件路径（抽象方法，由子类实现）
     * @param input 输入文件
     * @return 输出文件路径
     */
    public File generateOutputFromInput(File input) {
        return null;
    }
    
    /**
     * 根据输入文件和后缀生成输出文件路径
     * @param file 输入文件
     * @param suffix 输出文件后缀
     * @return 输出文件路径
     */
    public File generateOutputFromInput(File file, String suffix) {
        String name = file.getName();
        if (file.isFile()) {
            int i = name.lastIndexOf('.');
            if(i > 0){
                name = name.substring(0, i);
            }
        }
        name = name + suffix;
        File dir = file.getParentFile();
        if (dir == null) {
            return new File(name);
        }
        return new File(dir, name);
    }

    /**
     * 转换为字符串表示
     * @return 格式化的选项字符串
     */
    @Override
    public String toString() {
        OptionStringBuilder builder = new OptionStringBuilder(this);
        builder.setMaxWidth(Options.PRINT_WIDTH);
        builder.setTab2("      ");
        return "Using: " + APKEditor.getName() + " version " + APKEditor.getVersion() +
                ", " + ARSCLib.getName() + " version " + ARSCLib.getVersion() +
                "\n" + builder.buildTable();
    }

    /**
     * 获取指定选项类的帮助信息
     * @param optionClass 选项类
     * @return 格式化的帮助字符串
     */
    public static String getHelp(Class<?> optionClass){
        CommandHelpBuilder builder = new CommandHelpBuilder(ResourceStrings.INSTANCE, optionClass);
        builder.setMaxWidth(Options.PRINT_WIDTH);
        return builder.build();
    }

    /** 打印宽度常量 */
    public static final int PRINT_WIDTH = 80;

    /** 签名类型常量 */
    public static final String TYPE_SIG = "sig";
    /** JSON类型常量 */
    public static final String TYPE_JSON = "json";
    /** 原始类型常量 */
    public static final String TYPE_RAW = "raw";
    /** XML类型常量 */
    public static final String TYPE_XML = "xml";
    /** 文本类型常量 */
    public static final String TYPE_TEXT = "text";

    /** 内置DEX库常量 */
    public static final String DEX_LIB_INTERNAL = "internal";
    /** JF DEX库常量 */
    public static final String DEX_LIB_JF = "jf";
}
