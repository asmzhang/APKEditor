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
package com.reandroid.apkeditor.info;

import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 信息查询命令选项类
 * 用于定义APK信息查询功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "info",
        description = "info_description",
        examples = {
                "info_example_1",
                "info_example_2",
                "info_example_3"
        })
public class InfoOptions extends OptionsWithFramework {

    /**
     * 输出类型
     * 可选值：text, json, xml
     */
    @ChoiceArg(name = "-t", description = "info_print_types", values = {TYPE_TEXT, TYPE_JSON, TYPE_XML})
    public String type = TYPE_TEXT;

    /**
     * 详细模式标志
     * 当设置为true时，显示详细信息
     */
    @OptionArg(name = "-v", description = "info_verbose_mode", flag = true)
    public boolean verbose = false;

    /**
     * 包名标志
     * 当设置为true时，显示包名
     */
    @OptionArg(name = "-package", description = "info_package_name", flag = true)
    public boolean packageName = false;

    /**
     * 版本号标志
     * 当设置为true时，显示应用版本号
     */
    @OptionArg(name = "-version-code", description = "info_app_version_code", flag = true)
    public boolean versionCode = false;

    /**
     * 版本名称标志
     * 当设置为true时，显示应用版本名称
     */
    @OptionArg(name = "-version-name", description = "info_app_version_name", flag = true)
    public boolean versionName = false;

    /**
     * 最小SDK版本标志
     * 当设置为true时，显示最小SDK版本
     */
    @OptionArg(name = "-min-sdk-version", description = "info_min_sdk_version", flag = true)
    public boolean minSdkVersion = false;

    /**
     * 目标SDK版本标志
     * 当设置为true时，显示目标SDK版本
     */
    @OptionArg(name = "-target-sdk-version", description = "info_target_sdk_version", flag = true)
    public boolean targetSdkVersion = false;

    /**
     * 应用名称标志
     * 当设置为true时，显示应用名称
     */
    @OptionArg(name = "-app-name", description = "info_app_name", flag = true)
    public boolean appName = false;

    /**
     * 应用图标标志
     * 当设置为true时，显示应用图标
     */
    @OptionArg(name = "-app-icon", description = "info_app_icon", flag = true)
    public boolean appIcon = false;

    /**
     * 应用圆形图标标志
     * 当设置为true时，显示应用圆形图标
     */
    @OptionArg(name = "-app-round-icon", description = "info_app_icon_round", flag = true)
    public boolean appRoundIcon = false;

    /**
     * 权限标志
     * 当设置为true时，显示应用权限
     */
    @OptionArg(name = "-permissions", description = "info_permissions", flag = true)
    public boolean permissions = false;

    /**
     * 应用类名标志
     * 当设置为true时，显示应用类名
     */
    @OptionArg(name = "-app-class", description = "info_app_class_name", flag = true)
    public boolean appClass = false;

    /**
     * 活动标志
     * 当设置为true时，显示应用活动
     */
    @OptionArg(name = "-activities", description = "info_activities", flag = true)
    public boolean activities = false;

    /**
     * 资源列表
     * 指定要查询的资源列表
     */
    @OptionArg(name = "-res", description = "info_res")
    public final List<String> resList = new ArrayList<>();

    /**
     * 所有资源标志
     * 当设置为true时，显示所有资源
     */
    @OptionArg(name = "-resources", description = "info_resources", flag = true)
    public boolean resources = false;

    /**
     * 类型过滤器列表
     * 指定要过滤的资源类型
     */
    @OptionArg(name = "-filter-type", description = "info_filter_type")
    public final List<String> typeFilterList = new ArrayList<>();

    /**
     * DEX文件标志
     * 当设置为true时，显示DEX文件信息
     */
    @OptionArg(name = "-dex", description = "info_dex", flag = true)
    public boolean dex = false;

    /**
     * 签名标志
     * 当设置为true时，显示签名信息
     */
    @OptionArg(name = "-signatures", description = "info_signatures", flag = true)
    public boolean signatures = false;

    /**
     * Base64签名标志
     * 当设置为true时，以Base64格式显示签名
     */
    @OptionArg(name = "-signatures-base64", description = "info_signatures_base64", flag = true)
    public boolean signatures_base64 = false;

    /**
     * XML字符串列表
     * 指定要查询的XML字符串
     */
    @OptionArg(name = "-xmlstrings", description = "info_xml_strings")
    public List<String> xmlStrings = new ArrayList<>();

    /**
     * 字符串标志
     * 当设置为true时，显示字符串资源
     */
    @OptionArg(name = "-strings", description = "info_strings", flag = true)
    public boolean strings = false;

    /**
     * XML树列表
     * 指定要查询的XML树结构
     */
    @OptionArg(name = "-xmltree", description = "info_xml_tree")
    public final List<String> xmlTree = new ArrayList<>();

    /**
     * 列出文件标志
     * 当设置为true时，列出APK中的所有文件
     */
    @OptionArg(name = "-list-files", description = "info_list_files", flag = true)
    public boolean listFiles = false;

    /**
     * 列出XML文件标志
     * 当设置为true时，列出APK中的所有XML文件
     */
    @OptionArg(name = "-list-xml-files", description = "info_list_xml_files", flag = true)
    public boolean listXmlFiles = false;

    /**
     * 配置标志
     * 当设置为true时，显示资源配置
     */
    @OptionArg(name = "-configurations", description = "info_configurations", flag = true)
    public boolean configurations = false;

    /**
     * 语言标志
     * 当设置为true时，显示支持的语言
     */
    @OptionArg(name = "-languages", description = "info_languages", flag = true)
    public boolean languages = false;

    /**
     * 区域标志
     * 当设置为true时，显示支持的区域
     */
    @OptionArg(name = "-locales", description = "info_locales", flag = true)
    public boolean locales = false;

    /**
     * 构造函数
     */
    public InfoOptions(){
        super();
    }

    /**
     * 创建信息查询命令执行器
     * @return 信息查询器实例
     */
    @Override
    public Info newCommandExecutor() {
        return new Info(this);
    }

    /**
     * 验证选项值
     */
    @Override
    public void validateValues() {
        super.validateValues();
        initializeDefaults();
    }

    /**
     * 验证输入文件
     * @param isFile 是否必须是文件
     * @param isDirectory 是否必须是目录
     */
    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        super.validateInput(true, false);
    }

    /**
     * 验证输出文件
     * @param isFile 是否必须是文件
     */
    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(true);
        validateOutputExtension();
    }

    /**
     * 验证输出文件扩展名
     */
    private void validateOutputExtension() {
        File file = this.outputFile;
        if(file == null){
            return;
        }
        String name = file.getName().toLowerCase();
        String ext;
        if(TYPE_TEXT.equals(type)){
            if(name.endsWith(".text")){
                ext = ".text";
            }else {
                ext = ".txt";
            }
        } else {
            ext = "." + type.toLowerCase();
        }
        if(!name.endsWith(ext)){
            throw new CommandException("info_invalid_output_extension", ext, file);
        }
    }

    /**
     * 初始化默认值
     * 如果没有指定任何选项，则使用默认选项
     */
    private void initializeDefaults(){
        if(!isDefault()) {
            return;
        }
        appName = true;
        appIcon = true;
        activities = true;
        appClass = true;
        packageName = true;
        versionCode = true;
        versionName = true;
        if (verbose) {
            permissions = true;
        }
    }
    
    /**
     * 检查是否为默认配置
     * @return 如果为默认配置则返回true
     */
    private boolean isDefault() {
        boolean flagsChanged = activities || appClass || appIcon || appName || appRoundIcon ||
                dex || minSdkVersion || packageName || permissions || targetSdkVersion ||
                resources || signatures || signatures_base64 || versionCode || versionName ||
                listFiles || listXmlFiles || configurations || languages || locales || strings;

        return !flagsChanged && resList.isEmpty() && typeFilterList.isEmpty() &&
                xmlTree.isEmpty() && xmlStrings.isEmpty();
    }
}
