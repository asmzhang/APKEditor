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
package com.reandroid.apkeditor.refactor;

import com.reandroid.apkeditor.Options;
import com.reandroid.jcommand.annotations.CommandOptions;
import com.reandroid.jcommand.annotations.OptionArg;

import java.io.File;

/**
 * 重构命令选项类
 * 用于定义APK重构功能的各种命令行参数和选项
 */
@CommandOptions(
        name = "x",
        alternates = {"refactor"},
        description = "refactor_description",
        examples = {
                "refactor_example_1"
        })
public class RefactorOptions extends Options {

    /**
     * public.xml文件路径
     * 指定public.xml文件的路径
     */
    @OptionArg(name = "-public-xml", description = "refactor_public_xml")
    public File publicXml;

    /**
     * 修复类型名称标志
     * 当设置为true时，修复类型名称
     */
    @OptionArg(name = "-fix-types", flag = true, description = "refactor_fix_types")
    public boolean fixTypeNames;

    /**
     * 清理元数据标志
     * 当设置为true时，清理APK的元数据
     */
    @OptionArg(name = "-clean-meta", flag = true, description = "clean_meta")
    public boolean cleanMeta;

    /**
     * 构造函数
     */
    public RefactorOptions(){
        super();
    }

    /**
     * 创建重构命令执行器
     * @return 重构器实例
     */
    @Override
    public Refactor newCommandExecutor() {
        return new Refactor(this);
    }

    /**
     * 验证选项值
     */
    @Override
    public void validateValues() {
        super.validateValues();
        validatePublicXml();
    }

    /**
     * 验证public.xml文件
     */
    private void validatePublicXml() {
        File file = this.publicXml;
        if(file != null) {
            validateInputFile(file, true, false);
        }
    }

    /**
     * 根据输入文件生成输出文件路径
     * @param file 输入文件
     * @return 输出文件路径
     */
    @Override
    public File generateOutputFromInput(File file) {
        return generateOutputFromInput(file, "_refactored.apk");
    }
}
