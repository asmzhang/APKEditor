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
package com.reandroid.apkeditor.cloner;

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

/**
 * 应用克隆命令选项类
 * 用于定义应用克隆功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "c",
        alternates = {"clone"},
        description = "Clones application (NOT Implemented)",
        examples = {
                "[Basic]\n  java -jar APKEditor.jar p -i input.apk -o output.apk"
        })
public class ClonerOptions extends Options {

    /**
     * 包名参数
     * 用于指定克隆后应用的包名
     */
    @OptionArg(name = "-package", description = "Package name")
    public String packageName;

    /**
     * 应用名称参数
     * 用于指定克隆后应用的显示名称
     */
    @OptionArg(name = "-app-name", description = "Application name")
    public String appName;

    /**
     * 应用图标参数
     * 用于指定克隆后应用的图标文件路径
     * 可以是单个图标文件路径，也可以是多个图标文件的路径
     */
    @OptionArg(name = "-app-icon", description = "Application icon. File path of app icon(s).")
    public String appIcon;

    /**
     * 保持授权标识参数
     * 当设置为true时，不根据包名重命名授权标识
     * 仅在使用-package选项时生效
     */
    @OptionArg(name = "-keep-auth", description = "Do not rename authorities as per package. \n  *Applies only when option -package used.")
    public boolean keepAuth;

    /**
     * 构造函数
     * 初始化克隆选项对象
     */
    public ClonerOptions(){
        super();
    }

    /**
     * 创建命令执行器
     * @return 返回一个新的Cloner实例，用于执行克隆操作
     */
    @Override
    public Cloner newCommandExecutor() {
        return new Cloner(this);
    }
}
