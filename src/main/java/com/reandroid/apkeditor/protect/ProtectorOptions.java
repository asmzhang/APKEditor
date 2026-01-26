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
package com.reandroid.apkeditor.protect;

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.utils.io.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 保护命令选项类
 * 用于定义APK保护功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "p",
        alternates = {"protect"},
        description = "protect_description",
        examples = {
                "protect_example_1"
        })
public class ProtectorOptions extends Options {

    /**
     * 跳过清单文件标志
     * 当设置为true时，跳过AndroidManifest.xml的处理
     */
    @OptionArg(name = "-skip-manifest", flag = true, description = "protect_skip_manifest")
    public boolean skipManifest;

    /**
     * 混淆ZIP文件标志
     * 当设置为true时，混淆ZIP文件结构
     */
    @OptionArg(name = "-confuse-zip", flag = true, description = "protect_confuse_zip")
    public boolean confuse_zip;

    /**
     * 要保留的类型集合
     * 指定在混淆过程中要保留的资源类型
     */
    @OptionArg(name = "-keep-type", description = "protect_keep_type")
    public final Set<String> keepTypes = new HashSet<>();

    /**
     * 目录名称字典文件
     * 指定用于混淆目录名称的字典文件
     */
    @OptionArg(name = "-dic-dir-names", description = "protect_dic_dir_name")
    public File dic_dir_name;

    /**
     * 文件名称字典文件
     * 指定用于混淆文件名称的字典文件
     */
    @OptionArg(name = "-dic-file-names", description = "protect_dic_file_name")
    public File dic_file_name;

    /**
     * DEX保护级别
     * 指定DEX文件的保护级别
     */
    @OptionArg(name = "-dex-level", description = "dex_protect_level")
    public int dexLevel;

    /**
     * 构造函数
     */
    public ProtectorOptions() {
        super();
    }

    /**
     * 验证选项值
     */
    @Override
    public void validateValues() {
        super.validateValues();
        addDefaultKeepTypes();
    }
    
    /**
     * 添加默认保留类型
     * 如果没有指定保留类型，默认添加"font"类型
     */
    private void addDefaultKeepTypes() {
        Set<String> keepTypes = this.keepTypes;
        if (!keepTypes.isEmpty()) {
            return;
        }
        keepTypes.add("font");
    }

    /**
     * 创建保护命令执行器
     * @return 保护器实例
     */
    @Override
    public Protector newCommandExecutor() {
        return new Protector(this);
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
    }

    /**
     * 根据输入文件生成输出文件路径
     * @param input 输入文件
     * @return 输出文件路径
     */
    @Override
    public File generateOutputFromInput(File input) {
        return generateOutputFromInput(input, "_protected.apk");
    }

    /**
     * 加载目录名称字典
     * @return 目录名称数组
     */
    public String[] loadDirectoryNameDictionary() {
        return loadDictionary(dic_dir_name, "/protect_dic_dir_name.txt");
    }
    
    /**
     * 加载文件名称字典
     * @return 文件名称数组
     */
    public String[] loadFileNameDictionary() {
        return loadDictionary(dic_file_name, "/protect_dic_file_name.txt");
    }

    /**
     * 加载字典文件
     * @param file 字典文件路径
     * @param resource 默认资源路径
     * @return 字典条目数组
     */
    private String[] loadDictionary(File file, String resource) {
        InputStream inputStream;
        if (file != null) {
            try {
                inputStream = FileUtil.inputStream(file);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        } else {
            inputStream = ProtectorOptions.class.getResourceAsStream(resource);
        }
        String full;
        try {
            full = IOUtil.readUtf8(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        ArrayCollection<String> results = new ArrayCollection<>(
                StringsUtil.split(full, '\n', true));
        results.removeIf(StringsUtil::isEmpty);
        return results.toArray(new String[results.size()]);
    }
    
    /**
     * 检查是否保留指定类型
     * @param type 要检查的类型
     * @return 如果保留则返回true
     */
    public boolean isKeepType(String type) {
        Set<String> keepTypes = this.keepTypes;
        return keepTypes.contains(type) ||
                keepTypes.contains(KEEP_ALL_TYPES);
    }
    
    /**
     * 检查是否保留所有类型
     * @return 如果保留所有类型则返回true
     */
    public boolean isKeepAllTypes() {
        return keepTypes.contains(KEEP_ALL_TYPES);
    }

    /** 保留所有类型的常量 */
    private static final String KEEP_ALL_TYPES = "all-types";
}
