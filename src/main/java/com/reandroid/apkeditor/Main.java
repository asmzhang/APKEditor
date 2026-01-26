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

import com.reandroid.apk.xmlencoder.EncodeException;
import com.reandroid.apkeditor.compile.BuildOptions;
import com.reandroid.apkeditor.decompile.DecompileOptions;
import com.reandroid.apkeditor.info.InfoOptions;
import com.reandroid.apkeditor.merge.MergerOptions;
import com.reandroid.apkeditor.protect.ProtectorOptions;
import com.reandroid.apkeditor.refactor.RefactorOptions;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.coder.xml.XmlEncodeException;
import com.reandroid.jcommand.CommandHelpBuilder;
import com.reandroid.jcommand.CommandParser;
import com.reandroid.jcommand.annotations.MainCommand;
import com.reandroid.jcommand.annotations.OnOptionSelected;
import com.reandroid.jcommand.annotations.OtherOption;
import com.reandroid.jcommand.exceptions.CommandException;


@SuppressWarnings("unused")
/**
 * APKEditor应用程序主入口类
 * 负责解析命令行参数并执行相应的操作
 */
@MainCommand(
        headers = {"title_app_name_and_version", "title_app_repo", "title_app_description"},
        options = {
                DecompileOptions.class,    // 反编译选项
                BuildOptions.class,        // 构建选项
                MergerOptions.class,       // 合并选项
                RefactorOptions.class,     // 重构选项
                ProtectorOptions.class,    // 保护选项
                InfoOptions.class          // 信息选项
        }
)
public class Main {

    /** 标记是否为空选项 */
    private boolean mEmptyOption;
    /** 当前选中的选项对象 */
    private Options mOptions;
    /** 退出代码 */
    private int mExitCode;

    /**
     * 私有构造函数
     */
    private Main() {

    }
    
    /**
     * 应用程序主入口点
     * @param args 命令行参数数组
     */
    public static void main(String[] args) {
        int result = execute(args);
        System.exit(result);
    }

    /**
     * 执行命令行操作
     * 如果在Java应用程序内部运行，使用此方法可以避免不必要的System.exit()
     * 
     * 返回值说明：
     * 0 - 执行成功
     * 1 - 错误
     * 2 - 非执行命令如帮助、版本信息
     * 
     * @param args 命令行参数数组
     * @return 执行结果代码
     */
    public static int execute(String[] args) {
        Main main = new Main();
        return main.run(args);
    }

    /**
     * 处理主帮助命令
     * 显示应用程序的帮助信息
     */
    @OtherOption(
            names = {"-h", "-help"}, alternates = {"--help"},
            description = "app_help"
    )
    void onMainHelp() {
        mExitCode = 2;
        CommandHelpBuilder builder = new CommandHelpBuilder(
                ResourceStrings.INSTANCE, Main.class);
        builder.setFooters("", "help_main_footer", "<command> -h", "");
        System.err.println(builder.build());
    }
    
    /**
     * 处理版本信息命令
     * 显示应用程序和依赖库的版本信息
     */
    @OtherOption(
            names = {"-v", "-version"}, alternates = {"--version"},
            description = "app_version"
    )
    void onPrintVersion() {
        mExitCode = 2;
        System.err.println(APKEditor.getName() +
                " version " + APKEditor.getVersion() +
                ", " + ARSCLib.getName() +
                " version " + ARSCLib.getVersion());
    }
    
    /**
     * 当选项被选中时调用
     * @param option 选中的选项对象
     * @param emptyArgs 是否为空参数
     */
    @OnOptionSelected
    void onOption(Object option, boolean emptyArgs) {
        this.mOptions = (Options) option;
        this.mEmptyOption = emptyArgs;
    }

    /**
     * 运行命令行解析和执行
     * @param args 命令行参数数组
     * @return 退出代码
     */
    private int run(String[] args) {
        // 初始化状态
        mOptions = null;
        mEmptyOption = false;
        mExitCode = 2;
        
        // 创建命令解析器
        CommandParser parser = new CommandParser(Main.class);
        try {
            // 解析命令行参数
            parser.parse(this, args);
        } catch (CommandException e) {
            System.err.flush();
            System.err.println(e.getMessage(ResourceStrings.INSTANCE));
            return mExitCode;
        }
        
        // 如果没有选项，返回退出代码
        if(mOptions == null) {
            return mExitCode;
        }
        
        // 如果选项为空，显示错误信息
        if(mEmptyOption) {
            System.err.println(ResourceStrings.INSTANCE.getString(
                    "empty_command_option_exception"));
            return mExitCode;
        }
        
        // 验证选项
        try {
            mOptions.validate();
        } catch (CommandException e) {
            System.err.flush();
            System.err.println(e.getMessage(ResourceStrings.INSTANCE));
            return mExitCode;
        }
        
        // 如果请求帮助，显示帮助信息
        if(mOptions.help) {
            System.err.println(mOptions.getHelp());
            return mExitCode;
        }
        
        // 执行命令
        mExitCode = 1;
        try {
            mOptions.runCommand();
            mExitCode = 0;
        }  catch (CommandException ex1) {
            System.err.flush();
            System.err.println(ex1.getMessage(ResourceStrings.INSTANCE));
        } catch (EncodeException | XmlEncodeException ex) {
            System.err.flush();
            System.err.println("\nERROR:\n" + ex.getMessage());
        } catch (Exception exception) {
            System.err.flush();
            System.err.println("\nERROR:");
            exception.printStackTrace(System.err);
        }
        return mExitCode;
    }
}
