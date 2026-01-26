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
package com.reandroid.apkeditor.merge;

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;


/**
 * 合并命令选项类
 * 用于定义APK合并功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "m",
        alternates = {"merge"},
        description = "merge_description",
        examples = {
                "merge_example_1"
        })
public class MergerOptions extends Options {

    /**
     * 提取native库选项
     * 可选值：manifest, none, false, true
     */
    @ChoiceArg(
            name = "-extractNativeLibs",
            values = {
                    "manifest",
                    "none",
                    "false",
                    "true"
            },
            description = "extract_native_libs")
    public String extractNativeLibs;

    /**
     * 验证资源目录标志
     * 当设置为true时，验证资源目录的有效性
     */
    @OptionArg(name = "-vrd", flag = true, description = "validate_resources_dir")
    public boolean validateResDir;

    /**
     * 清理元数据标志
     * 当设置为true时，清理APK的元数据
     */
    @OptionArg(name = "-clean-meta", flag = true, description = "clean_meta")
    public boolean cleanMeta;

    /**
     * 验证模块标志
     * 当设置为true时，验证APK模块的有效性
     */
    @OptionArg(name = "-validate-modules", flag = true, description = "validate_modules")
    public boolean validateModules;

    /**
     * 资源目录名称
     * 指定资源目录的名称
     */
    @OptionArg(name = "-res-dir", description = "res_dir_name")
    public String resDirName;

    /**
     * 构造函数
     */
    public MergerOptions(){
        super();
    }

    /**
     * 创建合并命令执行器
     * @return 合并器实例
     */
    @Override
    public Merger newCommandExecutor() {
        return new Merger(this);
    }

    /**
     * 验证输入文件
     * @param isFile 是否必须是文件
     * @param isDirectory 是否必须是目录
     */
    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        super.validateInput(true, true);
    }

    /**
     * 根据输入文件生成输出文件路径
     * @param input 输入文件
     * @return 输出文件路径
     */
    @Override
    public File generateOutputFromInput(File input) {
        return generateOutputFromInput(input, "_merged.apk");
    }

    /**
     * 获取extractNativeLibs属性值
     * @return extractNativeLibs属性值
     */
    public String getExtractNativeLibs() {
        String extractNativeLibs = this.extractNativeLibs;
        if (extractNativeLibs == null) {
            extractNativeLibs = "manifest";
        }
        return extractNativeLibs;
    }
}
