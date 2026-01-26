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
package com.reandroid.apkeditor.decompile;

import com.reandroid.apkeditor.OptionsWithFramework;
import com.reandroid.jcommand.annotations.ChoiceArg;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;
import com.reandroid.utils.StringsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 反编译命令选项类
 * 用于定义APK反编译功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "d",
        alternates = {"decode"},
        description = "decode_description",
        usage = "decode_usage",
        examples = {
                "decode_example_1",
                "decode_example_2",
                "decode_example_3",
                "decode_example_4",
                "decode_example_5"
        },
        notes = {
                "decode_note_1",
                "decode_note_2"
        })
public class DecompileOptions extends OptionsWithFramework {

    /**
     * 反编译类型
     * 可选值：xml, json, raw, sig
     */
    @ChoiceArg(name = "-t",
            values = {
                    TYPE_XML,
                    TYPE_JSON,
                    TYPE_RAW,
                    TYPE_SIG
            },
            description = "decode_types"
    )
    public String type = TYPE_XML;

    /**
     * 分割JSON文件标志
     * 当设置为true时，将JSON文件分割成多个小文件
     */
    @OptionArg(name = "-split-json", flag = true, description = "split_json")
    public boolean splitJson;

    /**
     * 验证资源目录标志
     * 当设置为true时，验证资源目录的有效性
     */
    @OptionArg(name = "-vrd", flag = true, description = "validate_resources_dir")
    public boolean validateResDir;

    /**
     * 资源目录名称
     * 指定反编译后的资源目录名称
     */
    @OptionArg(name = "-res-dir", description = "res_dir_name")
    public String resDirName;

    /**
     * 保持原始资源路径标志
     * 当设置为true时，保持资源的原始路径结构
     */
    @OptionArg(name = "-keep-res-path", flag = true, description = "keep_original_res")
    public boolean keepResPath;

    /**
     * 原始DEX文件标志
     * 当设置为true时，保持DEX文件的原始格式
     */
    @OptionArg(name = "-dex", flag = true, description = "raw_dex")
    public boolean dex;

    /**
     * 禁用缓存标志
     * 当设置为true时，不使用缓存进行反编译
     */
    @OptionArg(name = "-no-cache", description = "decode_no_cache", flag = true)
    public boolean noCache;

    /**
     * 禁用DEX调试信息标志
     * 当设置为true时，不生成DEX调试信息
     */
    @OptionArg(name = "-no-dex-debug", flag = true, description = "no_dex_debug")
    public boolean noDexDebug;

    /**
     * 转储DEX标记标志
     * 当设置为true时，转储DEX文件的标记信息
     */
    @OptionArg(name = "-dex-markers", flag = true, description = "dump_dex_markers")
    public boolean dexMarkers;

    /**
     * 加载DEX数量
     * 指定要加载的DEX文件数量，默认为3
     */
    @OptionArg(name = "-load-dex", description = "decode_load_dex")
    public int loadDex = 3;

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
     * Smali寄存器标志
     * 当设置为true时，在Smali代码中显示寄存器信息
     */
    @OptionArg(name = "-smali-registers", flag = true, description = "smali_registers")
    public boolean smaliRegisters;

    /**
     * 注释级别
     * 可选值：off, basic, detail, detail2, full
     */
    @ChoiceArg(name = "-comment-level",
            values = {
                    COMMENT_LEVEL_OFF,
                    COMMENT_LEVEL_BASIC,
                    COMMENT_LEVEL_DETAIL,
                    COMMENT_LEVEL_DETAIL2,
                    COMMENT_LEVEL_FULL
            },
            description = "comment_level"
    )
    public String commentLevel = COMMENT_LEVEL_BASIC;

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
    @OptionArg(name = "-dex-profile", flag = true, description = "decode_dex_profile")
    public boolean dexProfile;

    /**
     * 要移除的注解列表
     * 指定要移除的注解名称
     */
    @OptionArg(name = "-remove-annotation", description = "remove_annotation")
    public final List<String> removeAnnotations = new ArrayList<>();

    /**
     * 构造函数
     */
    public DecompileOptions() {
    }

    /**
     * 创建反编译命令执行器
     * @return 反编译器实例
     */
    @Override
    public Decompiler newCommandExecutor() {
        return new Decompiler(this);
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
        super.validateOutput(false);
    }
    
    /**
     * 检查是否包含指定的注释级别
     * @param level 要检查的注释级别
     * @return 如果包含则返回true，否则返回false
     */
    public boolean containsCommentLevel(String level) {
        String commentLevel = this.commentLevel;
        if (StringsUtil.isEmpty(level)) {
            return COMMENT_LEVEL_OFF.equals(commentLevel);
        }
        if (COMMENT_LEVEL_OFF.equals(level)) {
            return commentLevel.equals(level);
        }
        if (COMMENT_LEVEL_BASIC.equals(level)) {
            return commentLevel.equals(level) ||
                    COMMENT_LEVEL_DETAIL.equals(commentLevel) ||
                    COMMENT_LEVEL_DETAIL2.equals(commentLevel) ||
                    COMMENT_LEVEL_FULL.equals(commentLevel);
        }
        if (COMMENT_LEVEL_DETAIL.equals(level)) {
            return commentLevel.equals(level) ||
                    COMMENT_LEVEL_DETAIL2.equals(commentLevel) ||
                    COMMENT_LEVEL_FULL.equals(commentLevel);
        }
        if (COMMENT_LEVEL_DETAIL2.equals(level)) {
            return commentLevel.equals(level) ||
                    COMMENT_LEVEL_FULL.equals(commentLevel);
        }
        if (COMMENT_LEVEL_FULL.equals(level)) {
            return commentLevel.equals(level);
        }
        return false;
    }

    /**
     * 根据输入文件生成输出文件路径
     * @param input 输入文件
     * @return 输出文件路径
     */
    @Override
    public File generateOutputFromInput(File input) {
        return generateOutputFromInput(input, "_decompile_" + type);
    }

    /** 注释级别：关闭 */
    public static final String COMMENT_LEVEL_OFF = "off";
    /** 注释级别：基础 */
    public static final String COMMENT_LEVEL_BASIC = "basic";
    /** 注释级别：详细 */
    public static final String COMMENT_LEVEL_DETAIL = "detail";
    /** 注释级别：更详细 */
    public static final String COMMENT_LEVEL_DETAIL2 = "detail2";
    /** 注释级别：完整 */
    public static final String COMMENT_LEVEL_FULL = "full";
}
