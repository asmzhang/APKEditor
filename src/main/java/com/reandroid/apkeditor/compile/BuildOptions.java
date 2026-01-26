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
package com.reandroid.apkeditor.compile;

import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.app.AndroidManifest;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.jcommand.exceptions.CommandException;

import java.io.File;


/**
 * 构建命令选项类
 * 用于定义APK构建功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "b",
        alternates = {"build"},
        description = "build_description",
        examples = {
                "build_example_1",
                "build_example_2",
                "build_example_3",
                "build_example_4"
        })
public class BuildOptions extends OptionsWithFramework {

    /**
     * 构建类型
     * 可选值：xml, json, raw, sig
     */
    @ChoiceArg(
            name = "-t",
            values = {
                    TYPE_XML,
                    TYPE_JSON,
                    TYPE_RAW, TYPE_SIG
            },
            description = "build_types")
    public String type;

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
    @OptionArg(name = "-vrd", description = "validate_resources_dir", flag = true)
    public boolean validateResDir;

    /**
     * 资源目录名称
     * 指定资源目录的名称
     */
    @OptionArg(name = "-res-dir", description = "res_dir_name")
    public String resDirName;

    /**
     * 禁用缓存标志
     * 当设置为true时，不使用缓存进行构建
     */
    @OptionArg(name = "-no-cache", description = "build_no_cache", flag = true)
    public boolean noCache;

    /**
     * DEX库类型
     * 可选值：internal（内置）, jf（JF库）
     */
    @ChoiceArg(name = "-dex-lib",
            values = {
                    DEX_LIB_INTERNAL,
                    DEX_LIB_JF
            },
            description = "dex_lib"
    )
    public String dexLib = DEX_LIB_INTERNAL;

    /**
     * 签名目录
     * 指定签名文件的存储目录
     */
    @OptionArg(name = "-sig", description = "signatures_path")
    public File signaturesDirectory;

    /**
     * DEX配置文件标志
     * 当设置为true时，处理DEX配置文件
     */
    @OptionArg(name = "-dex-profile", flag = true, description = "encode_dex_profile")
    public boolean dexProfile;

    /**
     * 构造函数
     */
    public BuildOptions() {
        super();
    }

    /**
     * 创建构建命令执行器
     * @return 构建器实例
     */
    @Override
    public Builder newCommandExecutor() {
        return new Builder(this);
    }

    /**
     * 验证输入文件
     * @param isFile 是否必须是文件
     * @param isDirectory 是否必须是目录
     */
    @Override
    public void validateInput(boolean isFile, boolean isDirectory) {
        isFile = TYPE_SIG.equals(type);
        super.validateInput(isFile, !isFile);
        evaluateInputDirectoryType();
        validateSignaturesDirectory();
    }

    /**
     * 评估输入目录类型
     * 根据输入目录的内容自动确定构建类型
     */
    private void evaluateInputDirectoryType() {
        String type = this.type;
        if (type != null) {
            return;
        }
        File file = inputFile;
        if(isRawInputDirectory(file)) {
            // 如果是原始格式输入目录
            type = TYPE_RAW;
        } else if(isJsonInputDirectory(file)) {
            // 如果是JSON格式输入目录
            type = TYPE_JSON;
            this.inputFile = getJsonInDir(this.inputFile);
        } else if (isXmlInputDirectory(file)) {
            // 如果是XML格式输入目录
            type = TYPE_XML;
        } else if(signaturesDirectory != null){
            // 如果指定了签名目录
            type = TYPE_SIG;
        } else {
            throw new CommandException("unknown_build_directory", file);
        }
        this.type = type;
    }

    /**
     * 验证输出文件
     * @param isFile 是否必须是文件
     */
    @Override
    public void validateOutput(boolean isFile) {
        super.validateOutput(true);
    }

    /**
     * 验证签名目录
     */
    private void validateSignaturesDirectory() {
        if (TYPE_SIG.equals(type)) {
            // 如果是签名类型，必须指定签名目录
            File file = this.signaturesDirectory;
            if(file == null) {
                throw new CommandException("missing_sig_directory");
            }
            validateInputFile(file, false, true);
        } else if(this.signaturesDirectory != null) {
            // 如果不是签名类型但指定了签名目录，抛出异常
            throw new CommandException("invalid_sig_parameter_combination");
        }
    }
    
    /**
     * 根据输入文件生成输出文件路径
     * @param file 输入文件
     * @return 输出文件路径
     */
    @Override
    public File generateOutputFromInput(File file) {
        return generateOutputFromInput(file, "_out.apk");
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
    
    /**
     * 检查是否为原始格式输入目录
     * @param dir 要检查的目录
     * @return 如果是原始格式输入目录则返回true
     */
    private static boolean isRawInputDirectory(File dir){
        File file=new File(dir, AndroidManifest.FILE_NAME_BIN);
        if(!file.isFile()) {
            file = new File(dir, TableBlock.FILE_NAME);
        }
        return file.isFile();
    }
    
    /**
     * 检查是否为XML格式输入目录
     * @param dir 要检查的目录
     * @return 如果是XML格式输入目录则返回true
     */
    private static boolean isXmlInputDirectory(File dir) {
        File manifest = new File(dir, AndroidManifest.FILE_NAME);
        return manifest.isFile();
    }
    
    /**
     * 检查是否为JSON格式输入目录
     * @param dir 要检查的目录
     * @return 如果是JSON格式输入目录则返回true
     */
    private static boolean isJsonInputDirectory(File dir) {
        if (isModuleDir(dir)) {
            return true;
        }
        File[] files = dir.listFiles();
        if(files == null) {
            return false;
        }
        for(File file:files) {
            if(isModuleDir(file)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否为模块目录
     * @param dir 要检查的目录
     * @return 如果是模块目录则返回true
     */
    private static boolean isModuleDir(File dir){
        if(!dir.isDirectory()){
            return false;
        }
        File file = new File(dir, AndroidManifest.FILE_NAME_JSON);
        return file.isFile();
    }
    
    /**
     * 获取JSON输入目录
     * @param dir 要搜索的目录
     * @return JSON输入目录
     */
    private static File getJsonInDir(File dir) {
        if(isModuleDir(dir)){
            return dir;
        }
        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            throw new CommandException("Empty directory: %s", dir);
        }
        for(File file:files){
            if(isModuleDir(file)){
                return file;
            }
        }
        throw new CommandException("Invalid directory: '%s', missing file \"uncompressed-files.json\"", dir);
    }
}
